package ICSI311_Interpreter4;

import java.util.LinkedList;

//Represents a function call node in AWK, including both built-in and user-made functions.
public class FunctionCallNode extends StatementNode {
    Token functionName;
    LinkedList<Node> parameters;

    public FunctionCallNode(Token f){
        functionName = f;
        parameters = new LinkedList<>();
    }
    public FunctionCallNode(Token f, LinkedList<Node> p){
        functionName = f;
        parameters = p;
    }

    public Token getFunctionName(){
        return functionName;
    }

    public LinkedList<Node> getParameters(){
        return parameters;
    }

    @Override
    public String toString(){
        if(parameters.isEmpty()){
            return "FunctionCallNode:: FunctionName: " + functionName.toString() + " No parameters";
        }
        return "FunctionCallNode:: FunctionName: " + functionName + " Parameters: " + parameters.toString();
    }

}
