package ICSI311_Interpreter4;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;


public class Parser{
    private TokenManager tokenList;

    public Parser(LinkedList<Token> tL){
        tokenList = new TokenManager(tL);
    }
    public boolean AcceptSeparators(){
        boolean check = false;//If the first token is a SEPERATOR, then it will return true.
        //while loop is used to check if there is more SEPERATOR after the first one.
        while(tokenList.MatchAndRemove(TokenType.SEPERATOR).isPresent()){
            check = true;
        }
        return check;
    }

    public ProgramNode Parse() throws Exception {
        ProgramNode program = new ProgramNode();
        while(tokenList.MoreTokens()){
            //If both ParseFunction and ParseAction fails, throws a Exception.
            if(!ParseFunction(program) && !ParseAction(program)){
                throw new Exception("Unknown Statement");
            }
        }
        return program;
    }
    public boolean ParseFunction(ProgramNode program) throws Exception {
        ArrayList<Token> parameterList = new ArrayList<>();
        Token functionName = null;
        boolean endingCheck = false;//THis is to make sure the syntax is correct.
        if(tokenList.MoreTokens()){
            if(!(tokenList.MatchAndRemove(TokenType.FUNCTION).equals(Optional.empty()))){
                //After FUNCTION, we are expecting a WORD
                if(tokenList.Peek(0).get().getToken() == TokenType.WORD){
                    functionName = tokenList.MatchAndRemove(TokenType.WORD).get();//Save the function Name.
                    //After WORD, we are expecting a '('
                    if(!(tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).equals(Optional.empty()))){
                        //While loop used to save multiple parameters.
                        while(tokenList.MoreTokens()){
                            //After the 'C', it can be either a WORD or the ')'
                            if(tokenList.Peek(0).get().getToken() == TokenType.WORD){
                                //Save the WORD in the list.
                                parameterList.add(tokenList.MatchAndRemove(TokenType.WORD).get());
                                AcceptSeparators();//Remove any SEPERATOR aftwards
                                //We are expecting a COMMA if it is more parameters.
                                if((tokenList.MatchAndRemove(TokenType.COMMA).equals(Optional.empty()))){
                                    //If it IS NOT a COMMA, then it is only one parameter.
                                    //Checks if the ')' exists.
                                    if(tokenList.Peek(0).get().getToken() == TokenType.RIGHT_SOFT_BRACKET) {
                                        //If so, remove the ')'
                                        tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET);
                                        //changes to true, proving it is a valid function call.
                                        endingCheck = true;
                                        break;
                                    }else{//If not, then it is incorrect syntax.
                                        return false;
                                    }
                                }
                                AcceptSeparators();
                                //If it IS a COMMA, then we are expecting another WORD.
                            }else{
                                //If it IS NOT a WORD, then it is the end of the parameter list.
                                //Checksif the ')' exists.
                                if(tokenList.Peek(0).get().getToken() == TokenType.RIGHT_SOFT_BRACKET){
                                    //If so, remove the ')'
                                    tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET);
                                    //changes to true, proving it is a valid function call.
                                    endingCheck = true;
                                    break;
                                }else{
                                    //If not, then it is incorrect syntax.
                                    throw new Exception("Incorrect Syntax");
                                }
                            }

                        }
                    }
                }
            }else{
                return false;
            }
            if(endingCheck){//Checks if it is a valid function call.
                BlockNode functionBlock = ParseBlock();
                //Creates a BlockNode and add it to the list in ProgramNode.
                program.addFunction(new FunctionDefinitionNode(functionName, parameterList, functionBlock.returnStatement()));
                return true;
            }else{
                throw new Exception("Incorrect Syntax");
            }
        }
        return false;
    }
    public boolean ParseAction(ProgramNode program) throws Exception {
        //If it is BEGIN token, create a BlockNode and add it to BEGIN list in ProgramNode.
        if(tokenList.MoreTokens()){
            if(!tokenList.MatchAndRemove(TokenType.BEGIN).equals(Optional.empty())){
                program.addBeginBlocks(ParseBlock());
                return true;
                //If it is END token, create a BlockNode and add it to END list in ProgramNode.
            }
            if(!tokenList.MatchAndRemove(TokenType.END).equals(Optional.empty())){
                program.addEndBlocks(ParseBlock());
                return true;
            }else{
                //If it neither BEGIN or END, add to Other list in ProgramNode.
                Optional<Node> conditional = ParseOperation();//Check for a Possible Condition.
                BlockNode conditionalBlock = ParseBlock();//Get the Block underneath it.
                //In order to have a block with a condition, we grab the statements from ParseBlock() and create a
                //new blockNode with the condition and the statements.
                program.addOtherBlocks(new BlockNode(conditional, conditionalBlock.returnStatement()));
                return true;
            }
        }
        return false;
    }
   public BlockNode ParseBlock() throws Exception{
        AcceptSeparators();
        BlockNode returnBlock = new BlockNode();
        if(tokenList.MatchAndRemove(TokenType.LEFT_CURLY_BRACKET).isPresent()){
            AcceptSeparators();
            StatementNode statement = ParseStatement();
            AcceptSeparators();
            if(statement != null){//If there are no statement, no need to add NULL to the list.
                returnBlock.addStatement(statement);
                statement = ParseStatement();
                AcceptSeparators();
                //Continue to cover every statement after each other til there are no more statements.
                while(statement != null){
                    returnBlock.addStatement(statement);
                    statement = ParseStatement();
                    AcceptSeparators();
                }
            }
            AcceptSeparators();
            //If there isn't a { at the end, then it is missing.
            if(tokenList.MatchAndRemove(TokenType.RIGHT_CURLY_BRACKET).isEmpty()){
                throw new Exception("Missing } on block or Invalid Statement.");
            }
        }else{
            if(tokenList.MoreTokens()){
                StatementNode statement = ParseStatement();
                if(statement != null){//No need to add NULL to the list.
                    returnBlock.addStatement(statement);
                }
                AcceptSeparators();
            }
        }
        return returnBlock;
   }
   public StatementNode ParseStatement() throws Exception{

        //Returns statement if ParseContinueBreak() returns a valid statement.
        StatementNode statement = ParseContinueBreak();
        if(statement != null){
            return statement;
        }

        //Returns statement if ParseIf() returns a valid statement.
        statement = ParseIf();
        if(statement != null){
            return statement;
        }

        //Returns statement if ParseFor() returns a valid statement.
        statement = ParseFor();
        if(statement != null){
            return statement;
        }

        //Returns statement if ParseDelete() returns a valid statement.
       statement = ParseDelete();
       if(statement != null){
           return statement;
       }

        //Returns statement if ParseWhile() returns a valid statement.
       statement = ParseWhile();
       if(statement != null){
           return statement;
       }

       //Returns statement if ParseDoWhile() returns a valid statement.
       statement = ParseDoWhile();
       if(statement != null){
           return statement;
       }

       //Returns statement if ParseReturn() returns a valid statement.
       statement = ParseReturn();
       if(statement != null){
           return statement;
       }

       //Process to check for AssignmentNode, Increment and Decrement, and FunctionCallNodes.
       Optional<Node> possibleStatement = ParseOperation();
       if(possibleStatement.isPresent()){
           AcceptSeparators();
           //If it is a FunctionCallNode, then it is a valid statement.
           if(possibleStatement.get() instanceof FunctionCallNode){
               AcceptSeparators();
               return (StatementNode)(possibleStatement.get());
           }
           //If the possibleStatement is a AssignmentNode, we can return it.
           if(possibleStatement.get() instanceof AssignmentNode){
               AcceptSeparators();
               return (StatementNode)(possibleStatement.get());
           }
       }
       return null;
   }
   public StatementNode ParseContinueBreak() throws Exception{

        //Returns a ContinueNode if it finds CONTINUE.
        if(tokenList.MatchAndRemove(TokenType.CONTINUE).isPresent()){
            return new ContinueNode();
            //Returns a BreakNode if it finds BREAK.
        }else if(tokenList.MatchAndRemove(TokenType.BREAK).isPresent()){
            return new BreakNode();
        }
        return null;
   }
   public StatementNode ParseIf() throws Exception{
        BlockNode IfBlock;
        if(tokenList.MatchAndRemove(TokenType.IF).isPresent()){
            if(tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).isPresent()){
                //Grabs the condition.
                Optional<Node> condition = ParseOperation();
                if(condition.isPresent()){//Checks for that condition.
                    if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                        AcceptSeparators();
                        //Grabs the statements underneath it.
                        IfBlock = ParseBlock();
                        //If there is a ELSE, it can have a if after it.
                        if(tokenList.MatchAndRemove(TokenType.ELSE).isPresent()){
                            IfNode nextIf = (IfNode)(ParseIf());
                            //If nextIf is null, that means it isn't a else if, but just a else.
                            if(nextIf == null){
                                //Creating the else block for the IfNode.
                                BlockNode elseBlock = ParseBlock();
                                return new IfNode(condition, IfBlock, (new IfNode(elseBlock)));
                            }
                            //This returns when it is a if else if
                            return new IfNode(condition, IfBlock, nextIf);
                        }
                        //This returns as a if statement.
                        return new IfNode(condition, IfBlock);
                    }else{
                        throw new Exception("Missing ) on if statement");
                    }
                }else{
                    throw new Exception("Missing Expression within if statement.");
                }
            }else{
                throw new Exception("Missing ( on if statement.");
            }
        }
        return null;
   }
   public StatementNode ParseFor() throws Exception{
        if(tokenList.MatchAndRemove(TokenType.FOR).isPresent()){
            if(tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).isPresent()){
                Optional<Node> inOrInitial = ParseOperation();//Calls for either the initialization, or the in operation.
                if(inOrInitial.isPresent()){
                    //If there is a ), then we can assume it is a IN operations.
                    if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                        try{
                            OperationNode inArray = (OperationNode)(inOrInitial.get());
                            //If the operation is IN, then it is a valid ForEachNode.
                            if(inArray.getExpression() == OperationNode.Operations.IN){
                                return new ForEachNode(inOrInitial, ParseBlock());
                            }else{
                                throw new Exception("Incorrect Syntax in FOR, should be IN expression or for(int; condition; update)");
                            }
                        }catch (ClassCastException e){
                            throw new Exception("Incorrect Syntax in FOR");
                        }
                    }
                    AcceptSeparators();
                    //Grabs the second statement, the condition.
                    Optional<Node> condition = ParseOperation();
                    if(condition.isPresent()){
                        AcceptSeparators();
                        //Grabs the third statement, the update.
                        Optional<Node> update = ParseOperation();
                        if(update.isEmpty()){
                            throw new Exception("Missing third statement in for loop definition");
                        }
                        if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                            //Creates the valid ForNode if there is a ) ending it.
                            return new ForNode(inOrInitial, condition, update, ParseBlock());
                        }else{
                            throw new Exception("Missing ) in for loop definition");
                        }
                    }else{
                        throw new Exception("Missing second statement in for loop definition");
                    }

                }else{
                    throw new Exception("Missing first statement in for loop definition");
                }
            }else{
                throw new Exception("Missing ( on for statement.");
            }
        }
        return null;
   }
   public StatementNode ParseDelete() throws Exception{
        if(tokenList.MatchAndRemove(TokenType.DELETE).isPresent()){
            //We call ParseLValue for either a array name or a array reference.
            Optional<Node> arrayReference = ParseLValue();
            if(arrayReference.isPresent()){
                try{
                    //Since both arrayName and array reference falls into a VariableReferenceNode.
                    //If it can be typecasted, then we can return a valid DeleteNode.
                    VariableReferenceNode arrayR = (VariableReferenceNode)(arrayReference.get());
                    return new DeleteNode(arrayReference);
                }catch (ClassCastException e){
                    throw new Exception("Incorrect Array Reference for DELETE");
                }
            }else{
                throw new Exception("Missing Array Reference for DELETE");
            }
        }
        return null;
   }
   public StatementNode ParseWhile() throws Exception{
        if(tokenList.MatchAndRemove(TokenType.WHILE).isPresent()){
            if(tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).isPresent()){
                Optional<Node> condition = ParseOperation();//Grabs the condition for the While.
                if(condition.isPresent()){
                    if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                        //If there is a ), then it is a valid WHILE loop.
                        return new WhileNode(condition, ParseBlock());
                    }else{
                        throw new Exception("Missing ) for While statement.");
                    }
                }else{
                    throw new Exception("Missing condition for While statement.");
                }
            }else{
                throw new Exception("Missing ( on While statement.");
            }
        }
        return null;
   }
   public StatementNode ParseDoWhile() throws Exception{
        if(tokenList.MatchAndRemove(TokenType.DO).isPresent()){
            BlockNode block = ParseBlock();//Calls for the Block between Do and While.
            if(tokenList.MatchAndRemove(TokenType.WHILE).isPresent()){
                if(tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).isPresent()){
                    Optional<Node> condition = ParseOperation();//Grabs the condition for the While Loop
                    if(condition.isPresent()){
                        if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                            AcceptSeparators();
                            //If there is a ), then it is a valid DoWhileNode.
                            return new DoWhileNode(condition, block);
                        }else{
                            throw new Exception("Missing ( on While For DoWhile statement");
                        }
                    }else{
                        throw new Exception("Missing condition on While for DoWhile statement");
                    }
                }else{
                    throw new Exception("Missing ) on While For DoWhile statement");
                }
            }else{
                throw new Exception("Missing While for DoWhile statement");
            }
        }
        return null;
   }
   public StatementNode ParseReturn() throws Exception{
        if(tokenList.MatchAndRemove(TokenType.RETURN).isPresent()){
            Optional<Node> returnStatement = ParseOperation();//Calls for the value.
            if(returnStatement.isPresent()){
                //If there is a value, then it is a valid ReturnNode.
                AcceptSeparators();
                return new ReturnNode(returnStatement);
            }else{
                throw new Exception("Missing statement on return");
            }
        }
        return null;
   }
   public Optional<Node> ParseFunctionCall() throws Exception{
        //Checks if the list of tokens isn't empty.
        if(!tokenList.MoreTokens()){
            return Optional.empty();
        }

        Token inBuiltFunctionToken = null;
        //We grab the TokenType of the first Token.
        TokenType tempToken = tokenList.Peek(0).get().getToken();
        //If it is any of these, then it is using a built-in AWK function, we save that.
        switch(tempToken){
            case GETLINE:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.GETLINE).get();
                break;
            case PRINT:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.PRINT).get();
                break;
            case PRINTF:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.PRINTF).get();
                break;
            case EXIT:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.EXIT).get();
                break;
            case NEXTFILE:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.NEXTFILE).get();
                break;
            case NEXT:
                inBuiltFunctionToken = tokenList.MatchAndRemove(TokenType.NEXT).get();
                break;
        }
        //If it is using the Built-In function, then we don't need to check for WORD.
        if(inBuiltFunctionToken != null){
            //If there is a (, remove it. If not, continue.
            tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET);
            LinkedList<Node> parameterList = new LinkedList<>();
            Optional<Node> parameter = ParseOperation();//Calls for the first parameter.
            while(parameter.isPresent()){
                //Add it to the list
                parameterList.add(parameter.get());
                //If there is not a COMMA, then we have reached the end of the function call.
                if(tokenList.MatchAndRemove(TokenType.COMMA).isEmpty()){
                    //If there is a ), remove it. If not, continue.
                    tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET);
                    return Optional.of(new FunctionCallNode(inBuiltFunctionToken, parameterList));
                }
                parameter = ParseOperation();//If there is a comma, there is more parameters.
            }
            //Returns when there isn't anything parameters.
            return Optional.of(new FunctionCallNode(inBuiltFunctionToken, parameterList));
        }
        //We peek to see if the current token is a WORD.
        if(tokenList.Peek(0).get().getToken() == TokenType.WORD){
            //This is used to see if there is another token after this.
            Optional<Token> tempValue = tokenList.Peek(1);
            if(tempValue.isEmpty()){
                return Optional.empty();
            }
            //If there is another token after WORD, and it is a (, then we can assume it is a function Call.
            //This is so we aren't removing the WORD token too early.
            if(tokenList.Peek(1).get().getToken() == TokenType.LEFT_SOFT_BRACKET){
                //Remove these two from the list, knowing fully we are in a Function Call.
                Token functionName = tokenList.MatchAndRemove(TokenType.WORD).get();
                tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET);
                //If there is a ), then it has no parameters.
                if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                    return Optional.of(new FunctionCallNode(functionName));
                }else{
                    //List to store all parameters.
                    LinkedList<Node> parameterList = new LinkedList<>();
                    Optional<Node> parameter = ParseOperation();
                    while(parameter.isPresent()){
                        //Adds a valid parameter to the list.
                        parameterList.add(parameter.get());
                        //If there isn't a COMMA after the paramenter, then there must be a ) for correct syntax.
                        if(tokenList.MatchAndRemove(TokenType.COMMA).isEmpty()){
                            if(tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()){
                                //If so, then it is a valid FunctionCallNode.
                                return Optional.of(new FunctionCallNode(functionName, parameterList));
                            }else{
                                throw new Exception("Missing ) on Function Call: " + functionName);
                            }
                        }
                        parameter = ParseOperation();
                    }
                    throw new Exception("Incorrect Syntax for Parameters for Function Call: " + functionName);
                }
            }
        }
        return Optional.empty();
   }

   public Optional<Node> ParseOperation() throws Exception {
        return AssignmentOperations();
    }

    public Optional<Node> ParseBottomLevel() throws Exception {
        //Checks if there are any tokens to look at.
        if(tokenList.MoreTokens()) {
            //StringLiteral ---> ConstantNode(Token STRINGLITERAL)
            if (tokenList.Peek(0).get().getToken() == TokenType.STRINGLITERAL) {
                return Optional.of(new ConstantNode(tokenList.MatchAndRemove(TokenType.STRINGLITERAL).get()));
                //Number ---> ConstantNode(Token NUMBER)
            } else if (tokenList.Peek(0).get().getToken() == TokenType.NUMBER) {
                return Optional.of(new ConstantNode(tokenList.MatchAndRemove(TokenType.NUMBER).get()));
                //Pattern/Literal ---> PatterNode(Token LITERAL)
            } else if (tokenList.Peek(0).get().getToken() == TokenType.LITERAL) {
                return Optional.of(new PatternNode(tokenList.MatchAndRemove(TokenType.LITERAL).get()));
                //Checks for a '('
            } else if (tokenList.MatchAndRemove(TokenType.LEFT_SOFT_BRACKET).isPresent()) {
                //Call conditionOperations to get the expression between the '()'
                Optional<Node> parseTemp = ParseOperation();
                //If ParseOperation returns nothing, then throw an exception.
                if (parseTemp.isEmpty()) {
                    throw new Exception("No expression in '()'");
                } else if (tokenList.MatchAndRemove(TokenType.RIGHT_SOFT_BRACKET).isPresent()) {
                    //Checks if there is a ')' after the expression. And if so, we can return.
                    return parseTemp;
                } else {
                    //Throws an Exception if there isn't a ')'
                    throw new Exception("Incorrect Syntax: Missing ')'");
                }
            } else if (tokenList.MatchAndRemove(TokenType.NOT).isPresent()) {
                //Checks for the NOT TokenType.
                Optional<Node> parseTemp = ParseOperation();//Calls ParseOperation for the expression.
                if (parseTemp.isEmpty()) {
                    //If ParseOperation return nothing, throws an exception.
                    throw new Exception("Missing reference after '!'");
                } else {
                    //If it returns something, return the new OperationNode.
                    return Optional.of(new OperationNode(parseTemp.get(), OperationNode.Operations.NOT));
                }
            } else if (tokenList.MatchAndRemove(TokenType.SUBTRACT).isPresent()) {
                //Checks for the SUBTRACT TokenType.
                Optional<Node> parseTemp = ParseBottomLevel();//Calls ParseBottomLevel for the expression.
                if (parseTemp.isEmpty()) {
                    //If ParseOperation return nothing, throws an exception.
                    throw new Exception("Missing reference after '-'");
                } else {
                    //If it returns something, return the new OperationNode.
                    return Optional.of(new OperationNode(parseTemp.get(), OperationNode.Operations.UNARYNEG));
                }
            } else if (tokenList.MatchAndRemove(TokenType.ADD).isPresent()) {
                //Checks for the ADD TokenType.
                Optional<Node> parseTemp = ParseBottomLevel();//Calls ParseOperation for the expression.
                if (parseTemp.isEmpty()) {
                    //If ParseOperation returns nothing, throws an exception.
                    throw new Exception("Missing reference after '+' ");
                } else {
                    //If it returns something, return the new OperationNode.
                    return Optional.of(new OperationNode(parseTemp.get(), OperationNode.Operations.UNARYPOS));
                }
            }else if(tokenList.MatchAndRemove(TokenType.INCREMENT).isPresent()){
                //Checks for the INCREMENT TokenType.
                Optional<Node> parseTemp = ParseOperation();//Calls ParseOperation for the expression.
                if(parseTemp.isEmpty()){
                    //If ParseOperation returns nothing, throws an exception.
                    throw new Exception("Missing reference after '++'");
                }else{
                    //If it returns something, return the new OperationNode.
                    OperationNode inc = new OperationNode(parseTemp.get(), OperationNode.Operations.PREINC);
                    return Optional.of(new AssignmentNode(parseTemp.get(), inc));
                }
            }else if(tokenList.MatchAndRemove(TokenType.DECREMENT).isPresent()){
                //Checks for the DECREMENT TokenType.
                Optional<Node> parseTemp = ParseOperation();//Calls ParseOperation for the expression.
                if(parseTemp.isEmpty()){
                    //If ParseOperation returns nothing, throws an exception.
                    throw new Exception("Missing reference after '--'");
                }else{
                    //If it returns something, return the new OperationNode.
                    OperationNode dec = new OperationNode(parseTemp.get(), OperationNode.Operations.PREDEC);
                    return Optional.of(new AssignmentNode(parseTemp.get(), dec));
                }
            }
        }
        return PostOperations();//Calls PostOperations for Post INC/DEC.

    }
    public Optional<Node> PostOperations() throws Exception{
        Optional<Node> functionCall = ParseFunctionCall();
        if(functionCall.isPresent()){
            return functionCall;
        }

        Optional<Node> firstExp = ParseLValue();//Grabs a variable.
        if(firstExp.isPresent()){//Checks if the variable exists.
            if(tokenList.MatchAndRemove(TokenType.INCREMENT).isPresent()){//Checks if ++ exists as the current token.
                OperationNode inc = new OperationNode(firstExp.get(), OperationNode.Operations.POSTINC);
                return Optional.of(new AssignmentNode(firstExp.get(), inc));
            }else if(tokenList.MatchAndRemove(TokenType.DECREMENT).isPresent()){//Checks if -- exists as the current token.
                OperationNode dec = new OperationNode(firstExp.get(), OperationNode.Operations.POSTDEC);
                return Optional.of(new AssignmentNode(firstExp.get(), dec));
            }
        }
        return firstExp;
    }

    public Optional<Node> ParseLValue() throws Exception {
        //Checks if there are tokens.
        if(tokenList.MoreTokens()){
            //Checks if there is a DOLLAR token.
            if(tokenList.MatchAndRemove(TokenType.DOLLAR).isPresent()){
                Optional<Node> parseTemp = ParseBottomLevel();//Calls ParseBottomLevel.
                if(parseTemp.isEmpty()){
                    //If parseTemp gets nothing, throw an exception.
                    throw new Exception("Missing reference value for DOLLAR");
                }else{
                    //If parseTemp has something, return the new OperationNode.
                    return Optional.of(new OperationNode(parseTemp.get(), OperationNode.Operations.DOLLAR));
                }
            }else{
                if(tokenList.Peek(0).get().getToken() == TokenType.WORD){
                    //Checks if it is a WORD.
                    Optional<Token> varName = tokenList.MatchAndRemove(TokenType.WORD);//Save that WORD.
                    if(tokenList.MatchAndRemove(TokenType.LEFT_HARD_BRACKET).isPresent()){
                        //Checks if the next token is a [
                        //The start of an Array reference.
                        Optional<Node> parseTemp = ParseOperation();//Calls ParseOperation for the expression between [].
                        if(parseTemp.isEmpty()){
                            //If it returns empty, throws an exception.
                            throw new Exception("No expression/value for array reference");
                        }else{
                            //If it does return something, check if there is a ].
                            if(tokenList.MatchAndRemove(TokenType.RIGHT_HARD_BRACKET).isPresent()){
                                //If so, return a new VariableReferenceNode.
                                return Optional.of(new VariableReferenceNode(varName.get(), parseTemp));
                            }else{
                                //If not, throw an exception.
                                throw new Exception("Missing ']' for array reference");
                            }
                        }
                    }else{
                        //If it is just a WORD token, return a new VariableReferenceNode.
                        return Optional.of(new VariableReferenceNode(varName.get()));
                    }
                }
            }
        }
        //If All fails, return a Optional.empty()
        return Optional.empty();
    }
    public Optional<Node> AssignmentOperations() throws Exception{
        Optional<Node> leftValue = TernaryOperations();//Calls the next lower level.
        if(leftValue.isPresent()){//Checks if something got sent back.
            if(tokenList.MatchAndRemove(TokenType.EXPONENT_EQUALS).isPresent()){//Checks if ^= exists.
                Optional<Node> rightExpression = TernaryOperations();//Calls for the second expression.
                if(rightExpression.isPresent()){//Checks if the second expression exists.
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.EXPONENT);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));//Returns a new AssignmentNode.
                }else{
                    throw new Exception("Missing expression after ^=");//Throws a exception if it is missing the second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.REMAINDER_EQUALS).isPresent()){//Checks for %=
                Optional<Node> rightExpression = TernaryOperations();//Calls for second expression
                if(rightExpression.isPresent()){//Checks for second expression
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.MODULO);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));
                }else{
                    throw new Exception("Missing expression after %=");//Throws a exception if it is missing the second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.MULTIPLY_EQUALS).isPresent()){//Checks for *=
                Optional<Node> rightExpression = TernaryOperations();//Calls for second Expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.MULTIPLY);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));
                }else{
                    throw new Exception("Missing expression after *=");//Throws a exception if it is missing the second expression.
                }

            }else if(tokenList.MatchAndRemove(TokenType.DIVIDE_EQUALS).isPresent()){//Checks for /=
                Optional<Node> rightExpression = TernaryOperations();//Calls for second Expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.DIVIDE);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));
                }else{
                    throw new Exception("Missing expression after /=");//Throws a exception if it is missing the second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.ADD_EQUALS).isPresent()){//Checks for +=
                Optional<Node> rightExpression = TernaryOperations();//Calls for second expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.ADD);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));
                }else{
                    throw new Exception("Missing expression after +=");//Throws a exception if it is missing the second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.SUBTRACT_EQUALS).isPresent()){//Checks for -=
                Optional<Node> rightExpression = TernaryOperations();//Calls for second expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    OperationNode expression = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.SUBTRACT);
                    return Optional.of(new AssignmentNode(leftValue.get(), expression));
                }else{
                    throw new Exception("Missing expression after -=");//Throws a exception if it is missing the second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.ASSIGN).isPresent()){//Checks for =
                Optional<Node> rightExpression = TernaryOperations();//Calls for second expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    OperationNode assignNode = new OperationNode(leftValue.get(), rightExpression, OperationNode.Operations.ASSIGN);
                    return Optional.of(new AssignmentNode(leftValue.get(), assignNode));
                }else{
                    throw new Exception("Missing expression after =");//Throws a exception if it is missing the second expression.
                }
            }
        }
        return leftValue;//If all fails, return what leftValue obtains.
    }
    public Optional<Node> TernaryOperations() throws Exception{
        Optional<Node> leftExpression = OrLogicalOperations();//Calls the next lower level.
        if(tokenList.MatchAndRemove(TokenType.TERNARY).isEmpty()){//If the current token isn't ?, we can return left.
            return leftExpression;
        }else{
            Optional<Node> trueExpression = ParseOperation();//Calls for the second expression, the true case.
            if(trueExpression.isPresent()){//Checks if the true case exists.
                if(tokenList.MatchAndRemove(TokenType.COLON).isPresent()){//Checks for Colon.
                    Optional<Node> falseExpression = ParseOperation();//Calls for the third expression, false case.
                    if(falseExpression.isPresent()){//Checks if the false case exists.
                        return Optional.of(new TernaryNode(leftExpression.get(), trueExpression.get(), falseExpression.get()));
                    }else{
                        throw new Exception("Missing false case in Ternary Expression");//Throw a exception if there isn't a false case.
                    }
                }
            }else{
                throw new Exception("Missing true case in Ternary Expression");//Throw a exception if there isn't a true case.
            }
        }
        return leftExpression;//If all fails, it just returns what left obtains.
    }
    public Optional<Node> OrLogicalOperations() throws Exception{
        Optional<Node> leftExpression = AndLogicalOperations();//Calls the next lower level.
        OperationNode.Operations currentOperation;
        do{
            if(tokenList.MatchAndRemove(TokenType.OR).isPresent()){//if there is a OR token, then we can proceed.
                currentOperation = OperationNode.Operations.OR;
            }else{
                return leftExpression;//If not, just return what left contains.
            }
            Optional<Node> rightExpression = ParseOperation();//Calls for second expression.
            leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, currentOperation));
        }while(true);
    }
    public Optional<Node> AndLogicalOperations() throws Exception{
        Optional<Node> leftExpression = ArrayOperations();//Calls for the next lower level.
        OperationNode.Operations currentOperation;
        do{
            if(tokenList.MatchAndRemove(TokenType.AND).isPresent()){//if there is AND token, then we can proceed.
                currentOperation = OperationNode.Operations.AND;
            }else{
                return leftExpression;//If not, just return what left contains.
            }
            Optional<Node> rightExpression = ParseOperation();//Calls for second expression.
            leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, currentOperation));
        }while(true);
    }
    public Optional<Node> ArrayOperations() throws Exception{
        Optional<Node> leftExpression = MatchOperations();//Calls the next lower level.
        if(tokenList.MatchAndRemove(TokenType.IN).isPresent()){//Checks for IN.
            Optional<Node> arrayReference = ParseLValue();//Calls for the array name.
            if(arrayReference.isPresent()){//checks if there is a array name.
                return Optional.of(new OperationNode(leftExpression.get(), arrayReference, OperationNode.Operations.IN));
            }else{
                throw new Exception("Missing array reference at IN");//throws a exception when missing the array name/reference.
            }
        }
        return leftExpression;
    }
    public Optional<Node> MatchOperations() throws Exception{
        Optional<Node> leftExpression = ComparisonOperations();//Calls the next lower level.
        if(leftExpression.isPresent()){//Checks for the left's existence.
            if(tokenList.MatchAndRemove(TokenType.MATCH).isPresent()){//Checks for MATCH.
                Optional<Node> rightExpression = ParseOperation();//Calls for second Expression
                if(rightExpression.isPresent()){//Checks for second Expression
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.MATCH));
                }else{
                    throw new Exception("Missing expression after ~");//Throws an exception where the second expression is missing.
                }
            }else if(tokenList.MatchAndRemove(TokenType.NO_MATCH).isPresent()){//Checks for NO-MATCH
                Optional<Node> rightExpression = ParseOperation();//Calls for second expression.
                if(rightExpression.isPresent()){//Checks for second expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.NOTMATCH));
                }else{
                    throw new Exception("Missing expression after !~");//Throws an exception where the second expression is missing.
                }
            }
        }
        return leftExpression;//If it all fails, return what left contains.
    }
    public Optional<Node> ComparisonOperations() throws Exception{
        Optional<Node> leftExpression = StringConcatenation();//Calls for the next lower level.
        if(leftExpression.isPresent()){//Checks if left exists.
            if(tokenList.MatchAndRemove(TokenType.LESS_THAN).isPresent()){//Checks for <
                Optional<Node> rightExpression = ParseOperation();//Calls second expression
                if(rightExpression.isPresent()){//Checks second expression
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.LT));
                }else{
                    throw new Exception("Missing expression after <");//Throws an exception when missing second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.LESS_EQUAL).isPresent()){//Checks for <=
                Optional<Node> rightExpression = ParseOperation();//Calls second expression.
                if(rightExpression.isPresent()){//Checks second Expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.LE));
                }else{
                    throw new Exception("Missing expression after <=");//Throws an exception when missing second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.NOT_EQUALS).isPresent()){//Checks for !=
                Optional<Node> rightExpression = ParseOperation();//Calls second expression.
                if(rightExpression.isPresent()){//Checks second Expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.NE));
                }else{
                    throw new Exception("Missing expression after !=");//throws an exception when missing second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.EQUALS).isPresent()){//Checks for ==
                Optional<Node> rightExpression = ParseOperation();//Calls second expression.
                if(rightExpression.isPresent()){//Checks second expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.EQ));
                }else{
                    throw new Exception("Missing expression after ==");//throws an exception when missing second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.GREATER_THAN).isPresent()){//Checks for >
                Optional<Node> rightExpression = ParseOperation();//Calls second expression.
                if(rightExpression.isPresent()){//Checks second expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.GT));
                }else{
                    throw new Exception("Missing expression after >");//throws an exception when missing second expression.
                }
            }else if(tokenList.MatchAndRemove(TokenType.GREATER_EQUAL).isPresent()){//Checks for >=
                Optional<Node> rightExpression = ParseOperation();//Calls second expression.
                if(rightExpression.isPresent()){//Checks second expression.
                    return Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.GE));
                }else{
                    throw new Exception("Missing expression after >=");//throws an exception when missing second expression.
                }
            }
        }
        return leftExpression;//if all fails, return what left contains.
    }
    public Optional<Node> StringConcatenation() throws Exception{
        Optional<Node> leftExpression = ParseExpression();//Calls the next lower level.
        do{
            if(!tokenList.MoreTokens()){
                return leftExpression;
            }
            Optional<Node> rightExpression = ParseExpression();//Calls the next lower level.
            if(rightExpression.isEmpty()){
                return leftExpression;//If empty, return what left contains.
            }
            //Creates OperationNode with Left Associativity.
            leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.CONCATENATION));
        }while(true);
    }
    public Optional<Node> ParseExpression() throws Exception{
        Optional<Node> leftExpression = ParseTerm();//Calls the next lower level.
        OperationNode.Operations currentOperation;
        do{
            if(tokenList.MatchAndRemove(TokenType.ADD).isPresent()){
                currentOperation = OperationNode.Operations.ADD;//Set currentOperation to ADD when there is a +
            }else if(tokenList.MatchAndRemove(TokenType.SUBTRACT).isPresent()){
                currentOperation = OperationNode.Operations.SUBTRACT;//Set currentOperation to ADD when there is a -
            }else{
                return leftExpression;//If all fails, return what left contains.
            }
            Optional<Node> rightExpression = ParseTerm();//Calls for second Expression.
            //Creates a OperationNode with left Associativity.
            leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, currentOperation));
        }while(true);
    }
    public Optional<Node> ParseTerm() throws Exception{
        Optional<Node> leftExpression = ParseFactor();//Calls the next lower level.
        OperationNode.Operations currentOperation;
        do{
            if(tokenList.MatchAndRemove(TokenType.MULTIPLY).isPresent()){
                currentOperation = OperationNode.Operations.MULTIPLY;//Set currentOperation to ADD when there is a *
            }else if(tokenList.MatchAndRemove(TokenType.DIVIDE).isPresent()){
                currentOperation = OperationNode.Operations.DIVIDE;//Set currentOperation to ADD when there is a /
            }else if(tokenList.MatchAndRemove(TokenType.REMAINDER).isPresent()){
                currentOperation = OperationNode.Operations.MODULO;//Set currentOperation to ADD when there is a %
            } else{
                return leftExpression;//If all fails, return what left contains.
            }
            Optional<Node> rightExpression = ParseFactor();//Calls second factor.
            //Creates OperationNodes with Left Associativity.
            leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, currentOperation));
        }while(true);
    }
    public Optional<Node> ParseFactor() throws Exception{
        Optional<Node> exponentCase = ParseExponents();//Calls for Exponents.
        if(exponentCase.isPresent()){//If exponent returns something, return it.
            return exponentCase;
        }else{
            return ParseBottomLevel();//If not, call the next level, ParseBottomLevel.
        }
    }
    public Optional<Node> ParseExponents() throws Exception{
        Optional<Node> leftExpression = ParseBottomLevel();//Calls the next lower level.
        if(tokenList.MatchAndRemove(TokenType.EXPONENT).isEmpty()){//If there isn't a ^, return what left contains.
            return leftExpression;
        }
        Optional<Node> rightExpression = ParseBottomLevel();//Calls for the second expression
        while(tokenList.MatchAndRemove(TokenType.EXPONENT).isPresent()){//Loops for multiple ^
            Optional<Node> rightMostExpression = ParseExponents();//Calls itself.
            if(rightMostExpression.isPresent()){//If it returns something.
                //Creates a OperationNode with Right Associativity.
                rightExpression = Optional.of(new OperationNode(rightExpression.get(), rightMostExpression, OperationNode.Operations.EXPONENT));
            }
        }
        //Then using left, create a OperationNode between left and right and return left.
        leftExpression = Optional.of(new OperationNode(leftExpression.get(), rightExpression, OperationNode.Operations.EXPONENT));
        return leftExpression;
    }
}
