package ICSI311_Interpreter4;

import java.util.LinkedList;
import java.util.Optional;

public class BlockNode extends Node {
    LinkedList<StatementNode> statements;
    Optional<Node> condition;

    public BlockNode(){
        //Initializes values.
        statements = new LinkedList<StatementNode>();
        condition = Optional.empty();
    }

    public BlockNode(Optional<Node> c){
        statements = new LinkedList<StatementNode>();
        condition = c;
    }

    public BlockNode (Optional<Node> c, LinkedList<StatementNode> b){
        statements = b;
        condition = c;
    }
    public LinkedList<StatementNode> returnStatement(){
        return statements;
    }

    public Optional<Node> returnCondition(){
        return condition;
    }

    public void addStatement(StatementNode s){
        statements.add(s);
    }

    @Override
    public String toString(){
        //Prints out all the information from members.
        if(condition.isEmpty()){
            return "Statements: " + statements.toString();
        }
        return "Statements: " + statements.toString() + " Condition: " + condition.toString();
    }
}
