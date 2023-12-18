package ICSI311_Interpreter4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Interpreter {
    private HashMap<String, InterpreterDataType> globalVariables;
    private HashMap<String, FunctionDefinitionNode> functionCalls;
    private LineManager lineManager;
    private ProgramNode program;
    public class LineManager{
        private List<String> inputLines;
        private int NR;
        private int NF;
        public LineManager(List<String> input){
            inputLines = input;
            NF = 0;
            NR = 0;
        }
        public boolean SplitAndAssign(){
            //Checks if there are more lines to read.
            if(NR < inputLines.size()){
                InterpreterDataType valueFS = globalVariables.get("FS");
                String currentLine = inputLines.get(NR);
                //Checks if $0 already exists in the hashmap.
                if(globalVariables.containsKey("$0")){
                    //If so, replace the current value of $0 to the contents of the new line.
                    globalVariables.replace("$0", new InterpreterDataType(currentLine));
                }else{
                    //If not, initialize $0 to be a new spot in the hashmap.
                    globalVariables.put("$0", new InterpreterDataType(currentLine));
                }

                String[] currentFields = currentLine.split(valueFS.getValue());
                int currentNR = 1;
                for(int i = 0; i < currentFields.length;i++){
                    //Sets up each reference to the global variables HashMap.
                    String currentReference = "$" + (currentNR+i+NF);
                    globalVariables.put(currentReference, new InterpreterDataType(currentFields[i]));
                }
                //Updates NF and NR properly.
                NF += currentFields.length;
                NR++;

                //Checks if $NF already exists in the hashmap.
                if(globalVariables.containsKey("$NF")){
                    //If so, replace the value of $NF to the last field of the line.
                    globalVariables.replace("$NF", new InterpreterDataType(currentFields[currentFields.length-1]));
                }else{
                    //If not, initialize $NF to the last field of the line.
                    globalVariables.put("$NF", new InterpreterDataType(currentFields[currentFields.length-1]));
                }
                return true;
            }else{
                //No more lines, return false.
                return false;
            }
        }
    }

    public Interpreter(ProgramNode program, Path filePath) throws IOException {
        this.program = program;
        //Initializing HashMaps
        globalVariables = new HashMap<>();
        functionCalls = new HashMap<>();
        globalVariables.put("FS", new InterpreterDataType(" "));
        globalVariables.put("OFMT", new InterpreterDataType("%.6g"));
        globalVariables.put("ORS", new InterpreterDataType("\n"));

        //Checks if the filePath is a valid path to a file.
        if(filePath.toFile().exists()){

            //If so, create a LineManager with said file and updated the hashmap.
            List<String> documentContent = Files.readAllLines(filePath);
            lineManager = new LineManager(documentContent);
            String fileName = filePath.getFileName().toString();
            globalVariables.put("FILENAME", new InterpreterDataType(fileName));
        }else{
            //If not, create a empty LineManager and update the hashmap.
            List<String> empty = new LinkedList<String>();
            lineManager = new LineManager(empty);
            globalVariables.put("FILENAME", new InterpreterDataType(""));
        }


        functionCalls.put("print", new BuiltInFunctionDefinitionNode((parameters)->{
            String output = "";
            for(int i = 0;i < parameters.size();i++){
                //Go through the parameters' IADT and get each value in order to output
                String current = parameters.get(String.valueOf(i)).getValue();
                output += current;
            }
            System.out.print(output);
            return "";
        }, true));

        functionCalls.put("printf", new BuiltInFunctionDefinitionNode((parameters)->{

            InterpreterArrayDataType mods = (InterpreterArrayDataType)(parameters.get("0"));
            HashMap<String, InterpreterDataType> modMaps = mods.getVariableStorage();
            ArrayList<String> modList = new ArrayList<>();
            for(int i = 0; i < modMaps.size();i++){
                modList.add(modMaps.get(String.valueOf(i)).getValue());
            }

            //Second step is to gather all the strings in the second parameter.
            InterpreterArrayDataType strings = (InterpreterArrayDataType)(parameters.get("1"));
            HashMap<String, InterpreterDataType> stringMap = strings.getVariableStorage();
            ArrayList<String> stringList = new ArrayList<>();
            for(int i = 0; i < stringMap.size();i++){
                stringList.add(stringMap.get(String.valueOf(i)).getValue());
            }

            if(stringMap.size() != modList.size()){
                try {
                    throw new Exception("Incorrect Parameters for printf statement.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            for(int i = 0; i < stringMap.size();i++){
                System.out.printf(modList.get(i), stringList.get(i));
            }
            return "";
        }, true));

        functionCalls.put("getline", new BuiltInFunctionDefinitionNode((parameters)->{
            //SplitAndAssign returns either true or false, so either "1" or "0"
            return (lineManager.SplitAndAssign()) ? "1" : "0";
        }, false));

        functionCalls.put("next", new BuiltInFunctionDefinitionNode((parameters)->{
            //SplitAndAssign returns either true or false, so either "1" or "0"
            return (lineManager.SplitAndAssign()) ? "1" : "0";
        }, false));

        functionCalls.put("gsub", new BuiltInFunctionDefinitionNode((parameters)->{
            InterpreterDataType regExp = parameters.get("0");
            InterpreterDataType replacement = parameters.get("1");
            String regExpString = regExp.getValue();
            String replacementString = replacement.getValue();
            //If there is a third parameter, then there is a target string.
            if(parameters.containsKey("2")){
                //If so, we can use the target string.
                InterpreterDataType target = parameters.get("2");
                String targetString = target.getValue();
                //Returns the result of replaceAll on the target string.
                return targetString.replaceAll(regExpString, replacementString);
            }else{
                //If not, then AWK uses the value of $0, so check if there is a value of $0.
                if(globalVariables.containsKey("$0")){
                    //if so, we use replaceAll on the value of $0 and return the result.
                    String targetString = globalVariables.get("$0").getValue();
                    return targetString.replaceAll(regExpString, replacementString);
                }
                //If there is no $0, there is nothing we can act on.
            }
            return "";
        }, false));

        functionCalls.put("match", new BuiltInFunctionDefinitionNode((parameters)->{
            InterpreterDataType target = parameters.get("0");
            InterpreterDataType regExp = parameters.get("1");
            String regExpString = regExp.getValue();
            String targetString = target.getValue();
            //Uses Pattern and Matcher in order to use the regExp in the targetString.
            Pattern pattern = Pattern.compile(regExpString);
            Matcher matcher = pattern.matcher(targetString);

            //Loop through the targetString's matcher.
            while(matcher.find()){
                //If there is a parameter in the third parameter of the hashmap, then it HAS to be a array.
                if(parameters.containsKey("2")){
                    //Populate the array with the part matcher got from the operation.
                    InterpreterArrayDataType array = (InterpreterArrayDataType)parameters.get("2");
                    array.addVariables("0", new InterpreterDataType(targetString.substring(matcher.start())));

                }
                //Returns the index of the first location of the pattern (+1 because AWK).
                return String.valueOf(matcher.start() + 1);
            }
            //If it finds nothing, returns 0.
            return "0";
        }, false));

        functionCalls.put("sub", new BuiltInFunctionDefinitionNode((parameters)->{
            //Grabs the values from the hashmap parameters and gets their string value.
            InterpreterDataType regExp = parameters.get("0");
            InterpreterDataType replacement = parameters.get("1");
            InterpreterDataType target = parameters.get("2");

            String regExpString = regExp.getValue();
            String replacementString = replacement.getValue();
            String targetString = target.getValue();

            //replaceFirst replaces the first occurance of regExp in the targetString with the replacementString and returns
            //the results.
            return targetString.replaceFirst(regExpString, replacementString);
        }, false));

        functionCalls.put("index", new BuiltInFunctionDefinitionNode((parameters)->{
            //Grabs the value from the parameters' HashMap.
            InterpreterDataType target = parameters.get("0");
            InterpreterDataType find = parameters.get("1");
            String targetString = target.getValue();
            String findString = find.getValue();
            //Returns the operation of IndexOf (+1 because of AWK) and returns it as a string.
            return String.valueOf(targetString.indexOf(findString) + 1);
        }, false));
        
        functionCalls.put("length", new BuiltInFunctionDefinitionNode((parameters)->{
            if(parameters.size() != 1){
                try {
                    throw new Exception("Incorrect parameters for length.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            InterpreterDataType target = parameters.get("0");
            String targetString = target.getValue();
            //Checks if the targetString is empty.
            if(targetString.isEmpty()){
                //If so, then we rely on the globalVariables $0
                //Check if $0 exists in the hashmap.
                if(globalVariables.containsKey("$0")){
                    //If so, use length() on the value of $0.
                    InterpreterDataType firstString = globalVariables.get("$0");
                    String first = firstString.getValue();
                    return String.valueOf(first.length());
                }
                //If there is no targetString and no value for $0, then return 0.
                return "0";
            }
            //use length() on the value of targetString.
            return String.valueOf(targetString.length());
        }, false));

        functionCalls.put("split", new BuiltInFunctionDefinitionNode((parameters)->{
            InterpreterDataType target = parameters.get("0");//Grabs the target from parameters.
            InterpreterArrayDataType splitArray = (InterpreterArrayDataType)parameters.get("1");//Grabs the array from parameters.
            //Checks if there is a third parameter.
            if(parameters.containsKey("2")){
                //If so, then we have a separator value for the split process.
                //Checks if there is a fourth parameter.
                if(parameters.containsKey("3")){
                    //If so, then we have to populate the array of separators.
                    //Grabs the separator value.
                    InterpreterDataType stringSeparator = parameters.get("2");
                    InterpreterArrayDataType sepArray = (InterpreterArrayDataType)parameters.get("3");

                    String[] splitTarget = target.getValue().split(stringSeparator.getValue());
                    //Loop through the splitTarget.
                    for(int i = 0; i < splitTarget.length-1;i++){
                        splitArray.addVariables(String.valueOf(i), new InterpreterDataType(splitTarget[i]));//Adds to the 2nd parameter (array).
                        sepArray.addVariables(String.valueOf(i), new InterpreterDataType(stringSeparator.getValue()));//Adds to the 4th parameter (array).
                    }

                    //Since the loop leaves one value off (due to the 4th parameter), then we want to add the last one to the 2nd parameter.
                    splitArray.addVariables(String.valueOf(splitTarget.length-1), new InterpreterDataType(splitTarget[splitTarget.length-1]));
                    //Returns the amount of split words.
                    return String.valueOf(splitTarget.length);
                }else{
                    //Grabs the separator value.
                    InterpreterDataType stringSeparator = parameters.get("2");
                    String[] splitTarget = target.getValue().split(stringSeparator.getValue());
                    //Loops through the splitTarget
                    for(int i = 0; i < splitTarget.length;i++){
                        //Adds to the 2nd parameter (array)
                        splitArray.addVariables(String.valueOf(i), new InterpreterDataType(splitTarget[i]));
                    }
                    //Returns the amount of split words.
                    return String.valueOf(splitTarget.length);
                }
            }else{
                //If there is no third parameter, there is not specified separator value, so we use the FS in the globalVariables.
                //Checks if there is an FS.
                if(globalVariables.containsKey("FS")){
                    //Grabs the value of FS from the global Variables.
                    String[] splitTarget = target.getValue().split(globalVariables.get("FS").getValue());
                    //Loops through the splitTarget
                    for(int i = 0; i < splitTarget.length;i++){
                        //Adds it to the 2nd parameter (array).
                        splitArray.addVariables(String.valueOf(i), new InterpreterDataType(splitTarget[i]));
                    }
                    //Returns the amount of split words.
                    return String.valueOf(splitTarget.length);
                }
                //If all fails, then no split occurred, returns 1.
                return "1";
            }

        }, false));

        functionCalls.put("substr", new BuiltInFunctionDefinitionNode((parameters)->{
            //Grabs all the values from the parameters.
            InterpreterDataType target = parameters.get("0");
            InterpreterDataType start = parameters.get("1");
            InterpreterDataType length = parameters.get("2");
            String targetString = target.getValue();

            //Gets the start value as a Integer.
            //-1 is a offset since AWK starts counting from 1, not 0.
            int startValue = Integer.parseInt(start.getValue()) - 1;
            //In AWK, if it is a startValue less than 0, it says to treat it as 1.
            if(startValue < 1){
                startValue = 0;
            }

            //Initialized for scope.
            int lengthValue = -1;
            //If length isn't empty.
            if(length != null){
                //Grabs the value of the length as a Integer.
                lengthValue = Integer.parseInt(length.getValue());
                //If the length is greater than the targetString's length,
                //In AWK, it says to just have it reach to the end of the string.
                if(lengthValue > targetString.length()){
                    //So we can return it using this format.
                    return targetString.substring(startValue);
                }
            }
            //If the lengthValue is not -1, that means it got initialized with a proper value.
            if(lengthValue != -1){
                //substring(start, end) in java so in order to get end, it would be startValue+lengthValue.
                return targetString.substring(startValue, startValue+lengthValue);
            }else{
                //If lengthValue didn't get initializied with a proper value, we can return from the start Location to the end of the string.
                return targetString.substring(startValue);
            }
        }, false));

        functionCalls.put("tolower", new BuiltInFunctionDefinitionNode((parameters)->{
            return parameters.get("0").getValue().toLowerCase();//Grabs the value of 0 from the parameter and return the result of toLowerCase();
        }, false));

        functionCalls.put("toupper", new BuiltInFunctionDefinitionNode((parameters)->{
            return parameters.get("0").getValue().toUpperCase();//Grabs the value of 0 from the parameter and return the result of toUpperClase();
        }, false));

        //Loop to add all user-made functions.
        for(FunctionDefinitionNode userMade : program.getFunctionList()){
            String userName = userMade.getFunctionName().getTokenValue();
            functionCalls.put(userName, userMade);
        }
    }

    public InterpreterDataType GetIDT(Node node, HashMap<String, InterpreterDataType> localVariables) throws Exception {
        if(node instanceof AssignmentNode){
            //Grabs the target.
            Node targetNode = (((AssignmentNode) node).getVariable());
            //Evaluate the right side.
            InterpreterDataType rightNode = GetIDT(((AssignmentNode)node).getOperation(), localVariables);
            //Checks if it is a variable.
            if(targetNode instanceof VariableReferenceNode){
                //Grabs the information from the VariableReferenceNode class
                String varName = ((VariableReferenceNode)targetNode).getVariableName().getTokenValue();
                Optional<Node> index = ((VariableReferenceNode)targetNode).getIndex();
                //If index is empty, it means no array.
                if(index.isEmpty()){
                    //Checks if the variable already exists
                    //If so, replace the current value with the new value.
                    if(localVariables.containsKey(varName)){
                        localVariables.replace(varName, rightNode);
                    }else if(globalVariables.containsKey(varName)){
                        globalVariables.replace(varName, rightNode);
                    }else{
                        //If not, put the new variable into the hashmap.
                        localVariables.put(varName, rightNode);
                    }
                    //Returns the new value.
                    return rightNode;
                }else{
                    //If it is a array, search through the array of localVariables.
                    if(localVariables.containsKey(varName)){
                        if(localVariables.get(varName) instanceof InterpreterArrayDataType){
                            //Search through the array and replace if the current value with the new value.
                            InterpreterDataType indexValue = GetIDT(index.get(), localVariables);
                            if(((InterpreterArrayDataType) localVariables.get(varName)).getVariableStorage().containsKey(indexValue.getValue())){
                                ((InterpreterArrayDataType)localVariables.get(varName)).getVariableStorage().replace(indexValue.getValue(), rightNode);
                            }
                        }
                        //Search through the array of globalVariables.
                    }else if(globalVariables.containsKey(varName)){
                        //Search through and replace if the current value with the new value.
                        if(globalVariables.get(varName) instanceof InterpreterArrayDataType){
                            InterpreterDataType indexValue = GetIDT(index.get(), localVariables);
                            if(((InterpreterArrayDataType) globalVariables.get(varName)).getVariableStorage().containsKey(indexValue.getValue())){
                                ((InterpreterArrayDataType)globalVariables.get(varName)).getVariableStorage().replace(indexValue.getValue(), rightNode);
                            }
                        }
                    }else{
                        //If it doesn't exist, add it to the array.
                        localVariables.put(varName, rightNode);
                    }

                    //Returns the new value.
                    return rightNode;
                }
                //Checks if it is a OperationNode AND it is a reference.
            }else if(targetNode instanceof OperationNode){
                if(((OperationNode)targetNode).getExpression() == OperationNode.Operations.DOLLAR){
                    //If so, grabs the value.
                    InterpreterDataType dollarExp = GetIDT(targetNode, localVariables);
                    //Checks if it exists and replace.
                    if(localVariables.containsKey(dollarExp.getValue())){
                        localVariables.replace(dollarExp.getValue(), rightNode);
                    }else if(globalVariables.containsKey(dollarExp.getValue())){
                        globalVariables.replace(dollarExp.getValue(), rightNode);
                    }else{
                        //If it doesn't exist, add it to the localVariables.
                        localVariables.put(dollarExp.getValue(), rightNode);
                    }
                }
            }
        }else if(node instanceof ConstantNode){
            //Returns the value stored in the ConstantNode
            String value = ((ConstantNode)node).getValue().getTokenValue();
            return new InterpreterDataType(value);
        }else if(node instanceof FunctionCallNode){
            //Calls the RunFunctionCall method and returns its output.
            String result = RunFunctionCall((FunctionCallNode)node, localVariables);
            return new InterpreterDataType(result);
        }else if(node instanceof PatternNode){
            //Throws a Exception if there is a PatternNode on its own.
            throw new Exception("Patterns can't be passed to a function or assignment.");
        }else if(node instanceof TernaryNode){
            //Evaulate the boolean Expression of the ternary.
            InterpreterDataType booleanExpression = GetIDT(((TernaryNode)node).getBooleanExpression(), localVariables);
            boolean booleanValue = false;
            try{
                //If the value can be converted to a float and it is NOT a 0.0, that means its true.
                float booleanFloat = Float.parseFloat(booleanExpression.getValue());
                if(booleanFloat != 0.0f){
                    booleanValue = true;
                }
            }catch(NumberFormatException e){
                booleanValue = false;
            }

            if(booleanValue){
                //Evaluate the true case and returns its results
                InterpreterDataType trueCase = GetIDT(((TernaryNode)node).getTrueCase(), localVariables);
                return trueCase;
            }else{
                //Evaluvate the false case and returns its results.
                InterpreterDataType falseCase = GetIDT(((TernaryNode)node).getFalseCase(), localVariables);
                return falseCase;
            }
        }else if(node instanceof VariableReferenceNode){
            //Grabs the information from VariableReferenceNode
            String varName = ((VariableReferenceNode)node).getVariableName().getTokenValue();
            Optional<Node> index = ((VariableReferenceNode)node).getIndex();
            //If index is empty, it is a stand-alone variable.
            if(index.isEmpty()){
                //If the hashmap is NOT null and it exists, return the value.
                if(localVariables != null && localVariables.containsKey(varName)){
                    return localVariables.get(varName);
                    //Checks if it is in the globalVariables's hashMap.
                }else if(globalVariables.containsKey(varName)){
                    return globalVariables.get(varName);
                }else{
                    //If not, we initialize it to the localVariables with a blank value
                    localVariables.put(varName, new InterpreterDataType("0.0f"));
                    return localVariables.get(varName);
                }
            }else{
                //We have a index, meaning this HAS to be a array.
                InterpreterDataType valueIndex = GetIDT(index.get(), localVariables);
                //Checks if the localVariables is NOT null and exists in the localVariables's hashmap.
                if(localVariables != null && localVariables.containsKey(varName)){
                    if(localVariables.get(varName) instanceof InterpreterArrayDataType){
                        //If so, grab the hashmap of the array.
                        HashMap<String, InterpreterDataType> indexes = ((InterpreterArrayDataType)localVariables.get(varName)).getVariableStorage();
                        try{
                            //Since array stores as 0,1,2,3, etc, we need to convert it from float into a integer.
                            float floatIndex = Float.parseFloat(valueIndex.getValue());
                            int integerIndex = (int)(floatIndex);
                            //Checks if the key exists in the indexes's HashMap.
                            if(indexes.containsKey(String.valueOf(integerIndex))){
                                return indexes.get(String.valueOf(integerIndex));
                            }
                        }catch(NumberFormatException e){
                            ;
                        }
                        //If this fails, we try to return it using its string's value.
                        if(indexes.containsKey(valueIndex.getValue())){
                            return indexes.get(valueIndex.getValue());
                        }
                    }else{
                        //Throws a Exception if there isn't a IADT (a array);
                        throw new Exception("Missing array reference.");
                    }
                    //Checks if it is in the globalVariables.
                }else if(globalVariables.containsKey(varName)){
                    if(globalVariables.get(varName) instanceof InterpreterArrayDataType){
                        //Grabs the indexes.
                        HashMap<String, InterpreterDataType> indexes = ((InterpreterArrayDataType)localVariables.get(varName)).getVariableStorage();
                        try{
                            //Same as before, converts to integers and search the indexes.
                            float floatIndex = Float.parseFloat(valueIndex.getValue());
                            int integerIndex = (int)(floatIndex);
                            if(indexes.containsKey(String.valueOf(integerIndex))){
                                return indexes.get(String.valueOf(integerIndex));
                            }
                        }catch(NumberFormatException e){
                            ;
                        }
                        //If it fails, we try to search the array using the string's value.
                        if(indexes.containsKey(valueIndex.getValue())){
                            return indexes.get(valueIndex.getValue());
                        }
                    }else{
                        //Throws a exception if we don't have a array reference.
                        throw new Exception("Missing array reference.");
                    }
                }
            }
        }else if(node instanceof OperationNode){
            //Grabs the information from the OperationNode.
            Node leftNode = ((OperationNode)node).getLeftNode();
            Optional<Node> rightNode = ((OperationNode)node).getRightNode();
            OperationNode.Operations expression = ((OperationNode)node).getExpression();
            //If the right side is empty, we evaluate all expressions that only need the left side.
            if(rightNode.isEmpty()){
                InterpreterDataType leftValue = GetIDT(leftNode, localVariables);
                switch(expression){
                    case DOLLAR:
                        try{
                            //Since references are only $0,$1,$2,etc, we see if we can convert the left side to a integer in order to get a valid reference.
                            float referenceFloat = Float.parseFloat(leftValue.getValue());
                            String result = "$" + (int)referenceFloat;
                            if(localVariables.containsKey(result)){
                                return localVariables.get(result);
                            }else if(globalVariables.containsKey(result)){
                                return globalVariables.get(result);
                            }
                            //This.
                            return new InterpreterDataType("");
                        }catch (NumberFormatException e){
                            //Throws a exception if not possible.
                            throw new Exception("Non-integer number used in reference.");
                        }
                    case PREINC:
                        //PREINC can only work with VariableReferenceNode.
                        if(leftNode instanceof VariableReferenceNode){
                            try{
                                if(leftValue == null){
                                    //If the leftValue is null, we initialize a new variable with a value of 1.0f
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    localVariables.put(varName, new InterpreterDataType(String.valueOf(1.0f)));
                                    return new InterpreterDataType(String.valueOf(1.0f));
                                }else{
                                    //If leftValue exists, we gets its value and increment by one.
                                    float leftResult = Float.parseFloat(leftValue.getValue());
                                    ++leftResult;
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    Optional<Node> varIndex = ((VariableReferenceNode) leftNode).getIndex();
                                    if (varIndex.isEmpty()) {
                                        //Stores the new value in the HashMap of either the localVariables or globalVariables.
                                        if (localVariables.containsKey(varName)) {
                                            localVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        } else if (globalVariables.containsKey(varName)) {
                                            globalVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        }
                                    }
                                    //Returns the new Result.
                                    return new InterpreterDataType(String.valueOf(leftResult));
                                }
                            }catch (NumberFormatException e){
                                //Throws a exception if we have a string
                                throw new Exception("Non-numbers used for Pre Increment");
                            }
                        }else{
                            //Throws a exception if the program tries to operate on a Non-Variable Node.
                            throw new Exception("Incorrect Syntax for Pre Increment");
                        }
                    case POSTINC:
                        if(leftNode instanceof VariableReferenceNode){
                            try{
                                //If leftValue is null, we initialize the value.
                                if(leftValue == null){
                                    //Create the key and give it a value of 1 for increment from 0.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    localVariables.put(varName, new InterpreterDataType(String.valueOf(1.0f)));
                                    return new InterpreterDataType(String.valueOf(1.0f));
                                }else{
                                    //If it exists, we get the value as a float.
                                    float leftResult = Float.parseFloat(leftValue.getValue());
                                    leftResult++;
                                    //Then we save this new value to the same key, replacing it.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    Optional<Node> varIndex = ((VariableReferenceNode) leftNode).getIndex();
                                    if (varIndex.isEmpty()) {
                                        if (localVariables.containsKey(varName)) {
                                            localVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        } else if (globalVariables.containsKey(varName)) {
                                            globalVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        }
                                    }
                                    //Return the new value.
                                    return new InterpreterDataType(String.valueOf(leftResult));
                                }
                            }catch (NumberFormatException e){
                                //Only floats are allowed to be incremented.
                                throw new Exception("Non-numbers used for Pre Increment");
                            }
                        }else{
                            //Only VariableReferenceNode can be incremeted.
                            throw new Exception("Incorrect Syntax for Pre Increment");
                        }
                    case PREDEC:
                        if(leftNode instanceof VariableReferenceNode){
                            try{
                                //If the leftValue is empty,
                                if(leftValue == null){
                                    //We get the key and give it a value of -1 for decrementing from 0.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    localVariables.put(varName, new InterpreterDataType(String.valueOf(-1.0f)));
                                    //Returns this new value.
                                    return new InterpreterDataType(String.valueOf(-1.0f));
                                }else{
                                    //If it exists, grab the value and update it.
                                    float leftResult = Float.parseFloat(leftValue.getValue());
                                    --leftResult;
                                    //Look for the key in either local or global variables and replace the value.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    Optional<Node> varIndex = ((VariableReferenceNode) leftNode).getIndex();
                                    if (varIndex.isEmpty()) {
                                        if (localVariables.containsKey(varName)) {
                                            localVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        } else if (globalVariables.containsKey(varName)) {
                                            globalVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        }
                                    }
                                    //Return new value.
                                    return new InterpreterDataType(String.valueOf(leftResult));
                                }
                            }catch (NumberFormatException e){
                                //Only floats are allowed to be decremented.
                                throw new Exception("Non-numbers used for Pre Increment");
                            }
                        }else{
                            //Only variables are allowed to be decremeted.
                            throw new Exception("Incorrect Syntax for Pre Increment");
                        }
                    case POSTDEC:
                        if(leftNode instanceof VariableReferenceNode){
                            try{
                                //If the leftValue is null, we initialize the value and key.
                                if(leftValue == null){
                                    //Grab the key and give it a value of -1 for decrementing from 0. Save this in one of the hashMaps.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    localVariables.put(varName, new InterpreterDataType(String.valueOf(-1.0f)));
                                    //Return the new value.
                                    return new InterpreterDataType(String.valueOf(-1.0f));
                                }else{
                                    //If it exists, grab the value from the key and update it.
                                    float leftResult = Float.parseFloat(leftValue.getValue());
                                    leftResult--;
                                    //Store it back to the hashmap with the same key but updated value.
                                    String varName = ((VariableReferenceNode) leftNode).getVariableName().getTokenValue();
                                    Optional<Node> varIndex = ((VariableReferenceNode) leftNode).getIndex();
                                    if (varIndex.isEmpty()) {
                                        if (localVariables.containsKey(varName)) {
                                            localVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        } else if (globalVariables.containsKey(varName)) {
                                            globalVariables.replace(varName, new InterpreterDataType(String.valueOf(leftResult)));
                                        }
                                    }
                                    //Returns the new Value.
                                    return new InterpreterDataType(String.valueOf(leftResult));
                                }
                            }catch (NumberFormatException e){
                                //Only floats are allowed to be incrememted.
                                throw new Exception("Non-numbers used for Pre Increment");
                            }
                        }else{
                            //Only Variable reference Node are allowed to be incremeted.
                            throw new Exception("Incorrect Syntax for Pre Increment");
                        }
                    case UNARYPOS:
                        try{
                            //grab the value as a float and send it back (since its just +1 * value);
                            float leftSide = Float.parseFloat(leftValue.getValue());
                            return new InterpreterDataType(String.valueOf(leftSide));
                        }catch (NumberFormatException e){
                            //Only Numbers (floats) are allowed.
                            throw new Exception("Non-numbers used in Unary POS");
                        }
                    case UNARYNEG:
                        try{
                            //grab the value as a float and sent it back with a negative (-1 * value);
                            float leftSide = Float.parseFloat(leftValue.getValue());
                            float newValue = (-1.0f) * (leftSide);
                            return new InterpreterDataType(String.valueOf(newValue));
                        }catch (NumberFormatException e){
                            //Only Numbers (floats) are allowed.
                            throw new Exception("Non-numbers use in Unary NEG");
                        }
                    case NOT:

                        boolean leftSide;
                        try{
                            //If it can be converted as a float AND the value is not 0.0f, then it is true.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            leftSide = (leftFloat != 0.0f);
                        }catch(NumberFormatException e){
                            //If it can't be converted then it is false.
                            leftSide = false;
                        }
                        if(!leftSide){
                            //Returns 1 for true.
                            return new InterpreterDataType("1");
                        }else{
                            //Return 0 for false.
                            return new InterpreterDataType("0");
                        }
                }
            }else{
                //Grabs the left side's value.
                InterpreterDataType leftValue = GetIDT(leftNode, localVariables);
                //If the right side is a patternNode, we can only use it with MATCH and NOTMATCH
                if(rightNode.get() instanceof PatternNode){
                    switch(expression){
                        case MATCH:
                            PatternNode rightPattern;
                            try{
                                //Make sures it is a valid Pattern.
                                rightPattern = (PatternNode)rightNode.get();
                            }catch(ClassCastException e){
                                throw new Exception("No Pattern found in Match.");
                            }
                            //Using Pattern and Matcher, determine if the pattern exists in the string.
                            Pattern pattern = Pattern.compile(rightPattern.getTokenPattern().getTokenValue());
                            Matcher matcher = pattern.matcher(leftValue.getValue());
                            //If so, return 1, if not, return 0.
                            if(matcher.find()){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        case NOTMATCH:
                            try{
                                //Make sures it is a valid Pattern.
                                rightPattern = (PatternNode)rightNode.get();
                            }catch(ClassCastException e){
                                throw new Exception("No Pattern found in Match.");
                            }
                            //Using Pattern and Matcher, determine if the apttern exists in the string.
                            pattern = Pattern.compile(rightPattern.getTokenPattern().getTokenValue());
                            matcher = pattern.matcher(leftValue.getValue());
                            //If not, return 1, if so, return 0.
                            if(!matcher.find()){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                    }
                }

                //All other expresssion uses the right side node (excluding patternNode).
                InterpreterDataType rightValue = GetIDT(rightNode.get(), localVariables);
                switch(expression){
                    case ADD:
                        try{
                            //Convert both to floats in order to do addition.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            float totalFloat = leftFloat + rightFloat;
                            //Return new value.
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case SUBTRACT:
                        try{
                            //Convert both to floats in order to do subtraction
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            float totalFloat = leftFloat - rightFloat;
                            //Return new value.
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case MULTIPLY:
                        try{
                            //Convert both to floats in order to do multi.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            float totalFloat = leftFloat * rightFloat;
                            //return new value.
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case DIVIDE:
                        try{
                            //Convert both to floats in order to do division.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            //If the bottom is 0, that is dividing by 0, not allowed.
                            if(rightFloat == 0.0f){
                                throw new Exception("Can't divide by 0.");
                            }
                            //If not 0, divide and return new value.
                            float totalFloat = leftFloat/rightFloat;
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case MODULO:
                        try{
                            //Convert both to floats in order to do modulo.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            float totalFloat = leftFloat%rightFloat;
                            //Return new value.
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case EXPONENT:
                        try{
                            //Convert both to floats in to do power.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            //Using Math.Pow in order to achieve, return new value.
                            float totalFloat = (float)Math.pow(leftFloat, rightFloat);
                            return new InterpreterDataType(String.valueOf(totalFloat));
                        }catch (NumberFormatException e){
                            throw new Exception("Non-numbers in math operations");
                        }
                    case EQ:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat == rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(leftValue.getValue().equals(rightValue.getValue())){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case NE:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat != rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(!(leftValue.getValue().equals(rightValue.getValue()))){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case GE:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat >= rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(leftValue.getValue().compareTo(rightValue.getValue()) >= 0){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case GT:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat > rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(leftValue.getValue().compareTo(rightValue.getValue()) > 0){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case LE:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat <= rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(leftValue.getValue().compareTo(rightValue.getValue()) <= 0){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case LT:
                        try{
                            //If both are valid floats, compare them as floats.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            if(leftFloat < rightFloat){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }catch(NumberFormatException e){
                            //If one is not a valid float, compare them as strings.
                            if(leftValue.getValue().compareTo(rightValue.getValue()) < 0){
                                return new InterpreterDataType("1");
                            }else{
                                return new InterpreterDataType("0");
                            }
                        }
                    case AND:
                        boolean leftSide;
                        boolean rightSide;
                        try{
                            //If it can be converted to a float AND it is not 0.0f, then it is true.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            leftSide = (leftFloat != 0.0f);
                        }catch(NumberFormatException e){
                            //If it is a string, then it is false.
                            leftSide = false;
                        }

                        try{
                            //If it can be converted to a float AND it is not 0.0f, then it is true.
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            rightSide = (rightFloat != 0.0f);

                        }catch(NumberFormatException e){
                            //If it is a string, then it is false.
                            rightSide = false;
                        }
                        //Returns the boolean value.
                        if(leftSide && rightSide){
                            return new InterpreterDataType("1");
                        }else{
                            return new InterpreterDataType("0");
                        }
                    case OR:
                        try{
                            //If it can be converted to a float AND it is not 0.0f, then it is true.
                            float leftFloat = Float.parseFloat(leftValue.getValue());
                            leftSide = (leftFloat != 0.0f);
                        }catch(NumberFormatException e){
                            //If it is a string, then it is false.
                            leftSide = false;
                        }
                        try{
                            //If it can be converted to a float AND it is not 0.0f, then it is true.
                            float rightFloat = Float.parseFloat(rightValue.getValue());
                            rightSide = (rightFloat != 0.0f);
                        }catch(NumberFormatException e){
                            //If it is a string, then it is false.
                            rightSide = false;
                        }
                        //Returns the boolean value.
                        if(leftSide || rightSide){
                            return new InterpreterDataType("1");
                        }else{
                            return new InterpreterDataType("0");
                        }
                    case CONCATENATION:
                        //Just add both sides as strings.
                        return new InterpreterDataType(leftValue.getValue() + rightValue.getValue());
                    case IN:
                        //Needs to be a VariableReferenceNode AND a Array reference
                        if(rightNode.get() instanceof VariableReferenceNode){
                            //Get the name, key.
                            String varName = ((VariableReferenceNode)rightNode.get()).getVariableName().getTokenValue();
                            //Checks to see if it is in the localVariables.
                            if(localVariables.containsKey(varName)){
                                //Checks if it is a array reference
                                if(localVariables.get(varName) instanceof InterpreterArrayDataType){
                                    //Grabs the array's HashMap and see if the key is in the hashmap. Return true (1) or false (0).
                                    HashMap<String, InterpreterDataType> hashMapOfArray = ((InterpreterArrayDataType) localVariables.get(varName)).getVariableStorage();
                                    if(hashMapOfArray.containsKey(leftValue.getValue())){
                                        return new InterpreterDataType("1");
                                    }else{
                                        return new InterpreterDataType("0");
                                    }
                                }else{
                                    //If no array reference, exception
                                    throw new Exception("No array reference for IN operation.");
                                }
                            }
                        }else{
                            //If no array reference, exception.
                            throw new Exception("No array reference for IN operation");
                        }
                    case ASSIGN:
                        //If the key already exists, replace the current value with the new one.
                        if(localVariables.containsKey(leftValue.getValue())){
                            localVariables.replace(leftValue.getValue(), new InterpreterDataType(rightValue.getValue()));
                            //Checks if it is in the globalVariables, not localVariables.
                        }else if(globalVariables.containsKey(leftValue.getValue())) {
                            globalVariables.replace(leftValue.getValue(), new InterpreterDataType(rightValue.getValue()));
                        }else{
                            //If both fails, initialize the new variable.
                            localVariables.put(leftValue.getValue(), new InterpreterDataType(rightValue.getValue()));
                        }
                        //Return the new value.
                        return new InterpreterDataType(rightValue.getValue());
                }
            }
        }
        //It shouldn't reach down here unless something really bad happened...
        return null;
    }

    public String RunFunctionCall(FunctionCallNode function, HashMap<String, InterpreterDataType> localVariables) throws Exception {
        //Grab the functionName
        String functionName = function.getFunctionName().getTokenValue();
        if(functionName == null){//Since token like PRINT, PRINTF, etc don't store their string value, we add it manually.
            switch(function.getFunctionName().getToken()){
                //If it is one of these token, manually change the functionName.
                case PRINT:
                    functionName = "print";
                    break;
                case PRINTF:
                    functionName = "printf";
                    break;
                case GETLINE:
                    functionName = "getline";
                    break;
                case NEXT:
                    functionName = "next";
                    break;
                case EXIT:
                    functionName = "exit";
                    break;
            }
        }
        //Check to see if the functionName points to a Built-In function.
        if(functionCalls.get(functionName) instanceof BuiltInFunctionDefinitionNode){
            //Grabs the built-in function.
            BuiltInFunctionDefinitionNode builtInFunction = ((BuiltInFunctionDefinitionNode) functionCalls.get(functionName));
            LinkedList<Node> callParameters = function.getParameters();//Grabs the parameters from the functionCallNode.
                if(builtInFunction.getVariadic()){//If it is a variadic, we have a unique set up.
                    if(functionName.equals("print")){
                        //If it is print, we need to set up one IADT.
                        HashMap<String, InterpreterDataType> printMap = new HashMap<>();
                        for(int i = 0; i < callParameters.size();i++){
                            //We map each parameter into a value from 0 to n.
                            printMap.put(String.valueOf(i), GetIDT(callParameters.get(i), localVariables));
                        }
                        //We send it to the lambda expression and return what it returns.
                        return builtInFunction.executeFunction(printMap);
                    }else if(functionName.equals("printf")){
                        //If it is printf, we need to set up Two IADT.
                        //Grabs the string modifiers.
                        String completeModString = GetIDT(callParameters.get(0), localVariables).getValue();
                        HashMap<String, InterpreterDataType> modMap = new HashMap<>();

                        int counter = 0;
                        //This while loop will add each modifiers into the IADT with an unique key.
                        while(completeModString.indexOf("%s") != -1){
                            //Take the substring.
                            String currentMod = completeModString.substring(0, completeModString.indexOf("%s") + 2);
                            //Adds it to the map with a key from 0 to n.
                            modMap.put(String.valueOf(counter), new InterpreterDataType(currentMod));
                            counter++;
                            //Update the curent string and loop over.
                            completeModString = completeModString.substring(completeModString.indexOf("%s") + 2);
                        }
                        //If there is more to the string after the last %s, add it to the last string in the map.
                        if(!completeModString.isBlank()){
                            String currentString = modMap.get(String.valueOf(counter-1)).getValue();
                            modMap.replace(String.valueOf(counter-1), new InterpreterDataType(currentString + completeModString));
                        }

                        //Similar setup to print, but we start at 1.
                        InterpreterArrayDataType stringArray = new InterpreterArrayDataType();
                        for(int i = 1; i < callParameters.size();i++){
                            stringArray.addVariables(String.valueOf(i-1), GetIDT(callParameters.get(i), localVariables));
                        }

                        //We map these two IADT to the main HashMap.
                        InterpreterArrayDataType modArray = new InterpreterArrayDataType();
                        modArray.setVariableStorage(modMap);
                        HashMap<String, InterpreterDataType> printfMap = new HashMap<>();
                        printfMap.put("0", modArray);
                        printfMap.put("1", stringArray);
                        //We send the main HashMap to the lambda function and return its result.
                        return builtInFunction.executeFunction(printfMap);
                    }
                }else{
                    //If it is not a varidic, we just map each parameters to a number from 0 to n.
                    HashMap<String, InterpreterDataType> parameterMap = new HashMap<>();
                    for(int i = 0; i < callParameters.size();i++){
                        parameterMap.put(String.valueOf(i), GetIDT(callParameters.get(i), localVariables));
                    }
                    //Send it to the lambda expression and return its result.
                    return builtInFunction.executeFunction(parameterMap);
                }
        }
        //If it fails to be a Built-In, it must be a user-made function or it is not defined.
        if(functionCalls.containsKey(functionName)){
            //We grab the FunctionDefinitionNode and both parameters's list.
            FunctionDefinitionNode userFunctionDef = functionCalls.get(functionName);
            ArrayList<Token> defParameters = userFunctionDef.getParameters();
            LinkedList<Node> callParameters = function.getParameters();
            HashMap<String, InterpreterDataType> parameterMap = new HashMap<>();
            //We map the parameters from the callNode to the DefinitionNode and store it into a new HashMap.
            for(int i = 0; i < defParameters.size();i++){
                parameterMap.put(defParameters.get(i).getTokenValue(), GetIDT(callParameters.get(i), localVariables));
            }
            //If the size don't match, throw an exception.
            if(defParameters.size() != function.getParameters().size()){
                throw new Exception("Parameters amount don't match.");
            }
            //If they do, we call InterpretListOfStatements with the statements within the function with
            //the local variables being the parameters.
            ReturnType result = InterpretListOfStatements(userFunctionDef.getStatements(), parameterMap);
            if(result.getReturnType() == ReturnType.rType.RETURN || result.getReturnType() == ReturnType.rType.BREAK){
                return result.getReturnString();
            }
        }else{
            throw new Exception("Function: " + functionName + " not defined.");
        }
        return "";
    }

    public ReturnType ProcessStatement(HashMap<String, InterpreterDataType> locals, StatementNode stmt) throws Exception {
        if(stmt instanceof BreakNode){
            //Return a break type to tell the code to break out.
            return new ReturnType(ReturnType.rType.BREAK);
        }else if(stmt instanceof ContinueNode){
            //Return a continue type to tell the code to continue.
            return new ReturnType(ReturnType.rType.CONTINUE);
        }else if(stmt instanceof DeleteNode){
            //Grabs the array reference stored in the DeleteNode's class.
            Optional<Node> arrayRef = ((DeleteNode)stmt).getArrayReference();
            //Checks if it is present.
            if(arrayRef.isPresent()){
                //Grabs the node as a Node, not a optional.
                Node arrayNode = arrayRef.get();
                //Checks if it is a VariableReferenceNode since that is the only way to show arrays/variables.
                if(arrayNode instanceof VariableReferenceNode){
                    //Grabs both the array name and a possible index.
                    String arrayName = ((VariableReferenceNode)arrayNode).getVariableName().getTokenValue();
                    Optional<Node> arrayIndex = ((VariableReferenceNode)arrayNode).getIndex();
                    //If the index is empty, we want to delete the whole array.
                    if(arrayIndex.isEmpty()){
                        //If it is stored as a local variable, remove it.
                        if(locals.containsKey(arrayName)){
                            //Makes sure it is pointing to an array data type.
                            if(locals.get(arrayName) instanceof InterpreterArrayDataType){
                                locals.remove(arrayName);
                            }
                        }
                        //If it is stored as a global variable, remove it.
                        if(globalVariables.containsKey(arrayName)){
                            //Makes sure it is pointing to an array data type.
                            if(globalVariables.get(arrayName) instanceof InterpreterArrayDataType){
                                globalVariables.remove(arrayName);
                            }
                        }
                    }else{
                        //If there is a index, evaluate the index.
                        InterpreterDataType indexValue = GetIDT(arrayIndex.get(), locals);
                        //Checks if the array is stored in locals.
                        if(locals.containsKey(arrayName)){
                            //Makes sure it is pointng to an array data type.
                            if(locals.get(arrayName) instanceof InterpreterArrayDataType){
                                //If so, grab the hashmap and remove it.
                                InterpreterArrayDataType arrayIDT = (InterpreterArrayDataType)(locals.get(arrayName));
                                HashMap<String, InterpreterDataType> indexes = arrayIDT.getVariableStorage();
                                if(indexes.containsKey(indexValue)){
                                    indexes.remove(indexValue);
                                }
                            }
                        }
                        //Check if the array is stored in globals.
                        if(globalVariables.containsKey(arrayName)){
                            //Makes sure it is pointing to an array data type.
                            if(globalVariables.get(arrayName) instanceof InterpreterArrayDataType){
                                //If so, grab its hashmap and remove it.
                                InterpreterArrayDataType arrayIDT = (InterpreterArrayDataType)(globalVariables.get(arrayName));
                                HashMap<String, InterpreterDataType> indexes = arrayIDT.getVariableStorage();
                                if(indexes.containsKey(indexValue)){
                                    indexes.remove(indexValue);
                                }
                            }
                        }
                    }
                }else{
                    //Exception for incorrect syntax with delete.
                    throw new Exception("Incorrect parameter in Delete statement.");
                }
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else if(stmt instanceof DoWhileNode){
            //Grabs the statements and the condition.
            LinkedList<StatementNode> listOfStatements = ((DoWhileNode)stmt).statements.returnStatement();
            Optional<Node> condition = ((DoWhileNode)stmt).getCondition();
            boolean conditionResult = false;
            do{
                //We run the statements for the first iteration.
                ReturnType result = InterpretListOfStatements(listOfStatements, locals);
                //If it is either BREAK or RETURN, return that result.
                if(result.getReturnType() == ReturnType.rType.BREAK){
                    return result;
                }else if(result.getReturnType() == ReturnType.rType.RETURN){
                    return result;
                }
                //If not, evaluate the condition again and update conditionResult as necessary.
                if(condition.isPresent()){
                    //Gets the value from the condition.
                    InterpreterDataType booleanIDT = GetIDT(condition.get(), locals);
                    try{
                        float floatIDT = Float.parseFloat(booleanIDT.getValue());
                        //If it can parsed to a float, check if it is zero.
                        if(floatIDT != 0.0f){
                            conditionResult = true;
                        }else{
                            conditionResult = false;
                        }
                    }catch(NumberFormatException e){
                        //If it can't be parsed, check if the string is empty.
                        if(booleanIDT.getValue().isEmpty()){
                            conditionResult = false;
                        }else{
                            conditionResult = true;
                        }
                    }
                }
            }while(conditionResult);
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else if(stmt instanceof ForNode){
            //Grabs the initilzation, condition, update, and statements.
            Optional<Node> initial = ((ForNode)stmt).getInitialization();
            Optional<Node> condition = ((ForNode)stmt).getCondition();
            Optional<Node> update = ((ForNode)stmt).getUpdate();
            LinkedList<StatementNode> statementsList = ((ForNode)stmt).statements.returnStatement();
            boolean boolCondition = false;
            //If there is a initialization, we don't mind what it returns, we just want to initialize the variables.
            if(initial.isPresent()){
                GetIDT(initial.get(), locals);
            }
            //If there is a condition, evaluate the condition before looping.
            if(condition.isPresent()){
                InterpreterDataType result = GetIDT(condition.get(), locals);
                try{
                    float floatIDT = Float.parseFloat(result.getValue());
                    if(floatIDT != 0.0f){
                        boolCondition = true;
                    }else{
                        boolCondition = false;
                    }
                }catch(NumberFormatException e){
                    if(result.getValue().isEmpty()){
                        boolCondition = false;
                    }else{
                        boolCondition = true;
                    }
                }
            }

            //Using a while loop, we continue based on the boolCondition.
            while(boolCondition){
                //Runs through the list of statements and return a ReturnType.
                ReturnType typeResult = InterpretListOfStatements(statementsList, locals);
                //If that is either BREAK or RETURN, we return that result, and breaks out of the while loop.
                if(typeResult.getReturnType() == ReturnType.rType.BREAK){
                    return typeResult;
                }else if(typeResult.getReturnType() == ReturnType.rType.RETURN){
                    return typeResult;
                }

                //If it is not one of those types, we call GetIDT on the update.
                if(update.isPresent()){
                    GetIDT(update.get(), locals);
                }
                //then we re-evaluate the condition again.
                if(condition.isPresent()){
                    InterpreterDataType result = GetIDT(condition.get(), locals);
                    try{
                        float floatIDT = Float.parseFloat(result.getValue());
                        if(floatIDT != 0.0f){
                            boolCondition = true;
                        }else{
                            boolCondition = false;
                        }
                    }catch(NumberFormatException e){
                        if(result.getValue().isEmpty()){
                            boolCondition = false;
                        }else{
                            boolCondition = true;
                        }
                    }
                }
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else if(stmt instanceof ForEachNode){
            //Grabs the condition and statements from ForEachNode
            Optional<Node> condition = ((ForEachNode) stmt).getCondition();
            LinkedList<StatementNode> statements = ((ForEachNode) stmt).statements.returnStatement();
            //Checks if the condition is present and if it is a OperationNode
            if(condition.isPresent() && condition.get() instanceof OperationNode) {
                //Grabs the information from the OperationNode.
                OperationNode inOp = ((OperationNode) condition.get());
                Node leftSide = inOp.getLeftNode();
                Optional<Node> rightSide = inOp.getRightNode();
                //Checks if the expression is a IN operation.
                if(inOp.getExpression() == OperationNode.Operations.IN){
                    //Checks if the leftSide is a variable
                    if(leftSide instanceof VariableReferenceNode){
                        //Checks if the rightSide is present and it also a variable.
                        if(rightSide.isPresent() && rightSide.get() instanceof VariableReferenceNode){
                            //Gets the names from both the variable nodes.
                            String leftName = ((VariableReferenceNode)leftSide).getVariableName().getTokenValue();
                            String rightName = ((VariableReferenceNode)rightSide.get()).getVariableName().getTokenValue();
                            //Check if the array exists in the code.
                            if(locals.containsKey(rightName)){
                                //Check if the name is a array data type.
                                if(locals.get(rightName) instanceof InterpreterArrayDataType){
                                    //Grabs the indexes from the array's IADT
                                    HashMap<String, InterpreterDataType> indexes = ((InterpreterArrayDataType)locals.get(rightName)).getVariableStorage();
                                    //Iterate through the keyes in the array's IADT
                                    for(String currentKey : indexes.keySet()){
                                        //Put it in the local's HashMap pointing the leftName to the currentKey
                                        locals.put(leftName, new InterpreterDataType(currentKey));
                                        //Calls InterpretListOfStatements
                                        ReturnType result = InterpretListOfStatements(statements, locals);
                                        //If the return type is BREAK or RETURN, return the result and break out.
                                        if(result.getReturnType() == ReturnType.rType.BREAK){
                                            return new ReturnType(ReturnType.rType.BREAK);
                                        }else if(result.getReturnType() == ReturnType.rType.RETURN){
                                            return result;
                                        }
                                        //If not, continue as normal.
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else if(stmt instanceof IfNode){
            IfNode currentIf = ((IfNode)stmt);
            //We want to travel through the linkedlist of IfNode.
            //If the currentIf does equal null, that tells the code we are at the end of the chain.
            while(currentIf != null){
                //Grabs the condition of the current IfNode.
                Optional<Node> ifCondition = currentIf.getCondition();
                //If it is empty, then we have to execute the statement under the current IfNode.
                if(ifCondition.isEmpty()){
                    //Grabs the statement and calls InterpretListOfStatements.
                    LinkedList<StatementNode> statements = currentIf.statements.returnStatement();
                    ReturnType result = InterpretListOfStatements(statements, locals);
                    if(result.getReturnType() == null){
                        //If the ReturnType is valid, return that to break out of this loop.
                        return result;
                    }
                    break;
                }else{
                    //If there is a condition present, we need to evaluate the condition.
                    InterpreterDataType checkCondition = GetIDT(ifCondition.get(), locals);
                    boolean boolCondition = false;
                    try{
                        float floatIDT = Float.parseFloat(checkCondition.getValue());
                        if(floatIDT != 0.0f){
                            boolCondition = true;
                        }else{
                            boolCondition = false;
                        }
                    }catch(NumberFormatException e){
                        if(checkCondition.getValue().isEmpty()){
                            boolCondition = false;
                        }else{
                            boolCondition = true;
                        }
                    }
                    //If the condition is met, we execute the statement under the current IfNode.
                    if(boolCondition){
                        //Grabs the statement and calls InterpretListOfStatements.
                        LinkedList<StatementNode> statements = currentIf.statements.returnStatement();
                        ReturnType result = InterpretListOfStatements(statements, locals);
                        if(result.getReturnType() == ReturnType.rType.BREAK){
                            return result;
                        }else if(result.getReturnType() == ReturnType.rType.RETURN){
                            return result;
                        }
                        if(result.getReturnType() == null){
                            //If the ReturnType is valid, return that to break out of this loop.
                            return result;
                        }
                        break;
                    }else{
                        //If the condition is NOT met, move to the next IfNode.
                        currentIf = currentIf.nextIf;
                    }
                }
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else if(stmt instanceof ReturnNode){
            //Grabs the statement for returnNode.
            Optional<Node> returnStatement = ((ReturnNode)stmt).getStatement();
            if(returnStatement.isPresent()){
                //If there is a statement, call GetIDT to evaluate and return with that value.
                InterpreterDataType returnIDT = GetIDT(returnStatement.get(), locals);
                return new ReturnType(ReturnType.rType.RETURN, returnIDT.getValue());
            }else{
                //If there is not a statement, return with a empty value.
                return new ReturnType(ReturnType.rType.RETURN);
            }
        }else if(stmt instanceof WhileNode){
            //Grabs the condition and statements from WhileNode.
            LinkedList<StatementNode> listOfStatements = ((WhileNode)stmt).statements.returnStatement();
            Optional<Node> condition = ((WhileNode)stmt).getCondition();
            boolean conditionResult = false;
            //If there is a condition, evaluate it.
            if(condition.isPresent()){
                //Calls GetIDT to evaluate the condition.
                InterpreterDataType booleanIDT = GetIDT(condition.get(), locals);
                try{
                    //Sees if it can be parsed to a float.
                    float floatIDT = Float.parseFloat(booleanIDT.getValue());
                    //If so, check if it is 0.0 and change if needed.
                    if(floatIDT != 0.0f){
                        conditionResult = true;
                    }else{
                        conditionResult = false;
                    }
                }catch(NumberFormatException e){
                    //If it can't be parsed to a float, check if it is a empty string.
                    if(booleanIDT.getValue().isEmpty()){
                        conditionResult = false;
                    }else{
                        conditionResult = true;
                    }
                }
            }
            //Runs if the condition is met.
            while(conditionResult){
                //Calls InterpretListOfStatements for its iteration.
                ReturnType result = InterpretListOfStatements(listOfStatements, locals);
                //If it returns either a BREAK or RETURN, return the result and break out of the loop.
                if(result.getReturnType() == ReturnType.rType.BREAK){
                    return result;
                }else if(result.getReturnType() == ReturnType.rType.RETURN){
                    return result;
                }
                //If it is neither BREAK or RETURN, we need to re-evaluate the condition again.
                if(condition.isPresent()){
                    //Calls GetIDT to evaluate the condition.
                    InterpreterDataType booleanIDT = GetIDT(condition.get(), locals);
                    try{
                        //Sees if it can be parsed to a float.
                        float floatIDT = Float.parseFloat(booleanIDT.getValue());
                        //If so, check if it is zero and change if needed.
                        if(floatIDT != 0.0f){
                            conditionResult = true;
                        }else{
                            conditionResult = false;
                        }
                    }catch(NumberFormatException e){
                        //if it can't be parsed to a float, check if it is a empty string
                        if(booleanIDT.getValue().isEmpty()){
                            conditionResult = false;
                        }else{
                            conditionResult = true;
                        }
                    }
                }
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }else{
            //call GetIDT for Assignment and FunctionCallNode.
            InterpreterDataType assignAndFunction = GetIDT(stmt, locals);
            //If GetIDT returns null, that means it is a invalid statement.
            if(assignAndFunction == null){
                throw new Exception("Error, invalid statement.");
            }
            //If all works, return a NORMAL ReturnType.
            return new ReturnType(ReturnType.rType.NORMAL);
        }
    }
    public ReturnType InterpretListOfStatements(LinkedList<StatementNode> statements, HashMap<String, InterpreterDataType> locals) throws Exception {
        int counter = 0;
        //We loop through each of the statements.
        while(counter < statements.size()){
            //We get the current statement of this loop.
            StatementNode currentStatement = statements.get(counter);
            //We call ProcessStatement to get that result.
            ReturnType processResult = ProcessStatement(locals, currentStatement);
            //If it returns a BREAK or RETURN, return that result.
            if(processResult.getReturnType() == ReturnType.rType.BREAK){
                return processResult;
            }else if(processResult.getReturnType() == ReturnType.rType.RETURN){
                return processResult;
            }
            //If not, move on to the next statement in the list.
            counter++;
        }
        //If all is processed, we return a NORMAL returnType, telling the code all the statements were processed naturally.
        return new ReturnType(ReturnType.rType.NORMAL);
    }
    public void InterpretProgram() throws Exception {
        //We first execute the statements in the BEGIN Blocks.
        ArrayList<BlockNode> currentBlockList = program.getBeginBlocks();
        for(BlockNode curr : currentBlockList){
            InterpretBlock(curr);
        }

        //Then we run the OTHER Blocks once and for each successful SplitAndAssign(),
        //We iterate the blocks again.
        do{
            currentBlockList = program.getOtherBlocks();
            for(BlockNode curr : currentBlockList){
                InterpretBlock(curr);
            }
        }while(lineManager.SplitAndAssign());

        //We execute the statements in the END blocks.
        currentBlockList = program.getEndBlocks();
        for(BlockNode curr : currentBlockList){
            InterpretBlock(curr);
        }
    }
    public void InterpretBlock(BlockNode block) throws Exception {
        //We grab the condition and statementes from the blockNode.
        Optional<Node> blockCondition = block.returnCondition();
        LinkedList<StatementNode> blockStatements = block.returnStatement();
        //We initialize a new localVariables map.
        HashMap<String, InterpreterDataType> locals = new HashMap<>();
        //If the condition is empty, we can instantly execute the statements.
        if(blockCondition.isEmpty()){
            for(StatementNode currStatement : blockStatements){
                ReturnType result = ProcessStatement(locals, currStatement);
            }
        }else{
            //if there is a condition, we check and see if the condition is true.
            boolean blockBool = false;
            //Grabs the result from the condition.
            String resultCondition = GetIDT(blockCondition.get(), locals).getValue();
            try{
                //Converts it to a float.
                float resultFloat = Float.parseFloat(resultCondition);
                if(resultFloat != 0.0f){
                    //If it is NOT 0.0f, then it is true.
                    blockBool = true;
                }
            }catch(NumberFormatException e){
                //If it can't convert to a float, we check if the string is empty.
                if(!resultCondition.isEmpty()){
                    //If so, it is false.
                    blockBool = true;
                }
            }

            //If the condition results in a true boolean, we execute the statements.
            if(blockBool){
                for(StatementNode currStatement : blockStatements){
                    ReturnType result = ProcessStatement(locals, currStatement);
                }
            }
        }
    }

    //These methods (getGlobalVariables, functionCallMethod, SplitAndAssignLine) are used for testing only.
    public HashMap<String, InterpreterDataType> getGlobalVariables(){
        return globalVariables;
    }

    public void addVariables(String key, InterpreterDataType value){
        globalVariables.put(key, value);
    }

    //To execute the function in the FunctionCall HashMap.
    public String functionCallMethod(String keyword, HashMap<String, InterpreterDataType> parameters){
        BuiltInFunctionDefinitionNode function = (BuiltInFunctionDefinitionNode)functionCalls.get(keyword);
        return function.executeFunction(parameters);
    }
    //To call the SplitAndAssign method from jUnits.
    public boolean SplitAndAssignLine(){
        return lineManager.SplitAndAssign();
    }

}
