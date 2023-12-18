package ICSI311_Interpreter4;

import java.util.HashMap;
import java.util.function.Function;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode {
    private Function<HashMap<String, InterpreterDataType>, String> execute;
    private boolean variadic;

    public BuiltInFunctionDefinitionNode(Function<HashMap<String, InterpreterDataType>, String> exe, boolean var){
        execute = exe;
        variadic = var;
    }
    public boolean getVariadic(){
        return variadic;
    }
    public String executeFunction(HashMap<String, InterpreterDataType> parameters){
        //Used to execute the function with the correct parameters.
        return execute.apply(parameters);
    }
}

