package ICSI311_Interpreter4;

import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType {
    HashMap<String, InterpreterDataType> variableStorage;
    public InterpreterArrayDataType(){
        variableStorage = new HashMap<>();
    }
    //Used to populate the HashMap.
    public void addVariables(String var, InterpreterDataType data){
        variableStorage.put(var, data);
    }
    //Used to retrieve the HashMap.
    public HashMap<String, InterpreterDataType> getVariableStorage() {
        return variableStorage;
    }

    public void setVariableStorage(HashMap<String, InterpreterDataType> newMap){
        variableStorage = newMap;
    }
    public String toString(){
        return "Array Value: " + variableStorage.toString();
    }

}
