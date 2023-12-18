package ICSI311_Interpreter4;

import java.util.ArrayList;
import java.util.LinkedList;

public class FunctionDefinitionNode extends Node {
    private Token functionName;
    private ArrayList<Token> parameters;
    private LinkedList<StatementNode> statements;
    public FunctionDefinitionNode(Token fN, ArrayList<Token> p, LinkedList<StatementNode> s){
        //Initializes all the values.
        functionName = fN;
        parameters = p;
        statements = s;
    }

    public FunctionDefinitionNode(){
        functionName = null;
        parameters = new ArrayList<>();
        statements = new LinkedList<>();
    }

    public ArrayList<Token> getParameters() {
        return parameters;
    }

    public Token getFunctionName(){
        return functionName;
    }

    public LinkedList<StatementNode> getStatements(){
        return statements;
    }

    @Override
    public String toString(){
        //Prints out all the information from members.
        return "Function Name: " + functionName.toString() + " Parameters: " + parameters.toString() + " Statements: " + statements.toString();
    }

}
