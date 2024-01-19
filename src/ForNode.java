package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a For Loop with three different statements within them.
//For example: for(int i = 0; i < 100; i++)
public class ForNode extends StatementNode {
    Optional<Node> initialization;
    Optional<Node> condition;
    Optional<Node> update;
    BlockNode statements;

    public ForNode(Optional<Node> in, Optional<Node> c, Optional<Node> up, BlockNode s){
        initialization = in;
        condition = c;
        update = up;
        statements = s;
    }

    public Optional<Node> getInitialization(){
        return initialization;
    }
    public Optional<Node> getCondition(){
        return condition;
    }

    public Optional<Node> getUpdate(){
        return update;
    }
    @Override
    public String toString(){
        return "ForNode:: \nInitialization: " + initialization.toString() + " \nCondition: " + condition.toString()
                + " \nUpdate: " + update.toString() + " " + statements.toString();
    }
}
