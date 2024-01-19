package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a For Loop that uses an IN Operation.
//For example, for(ab in arrayName)
public class ForEachNode extends StatementNode {
    Optional<Node> condition;
    BlockNode statements;
    public ForEachNode(Optional<Node> c, BlockNode s){
        condition = c;
        statements = s;
    }

    public Optional<Node> getCondition(){
        return condition;
    }

    @Override
    public String toString(){
        return "ForEachNode:: Condition: " + condition.toString()+ " Block: " + statements.toString();
    }
}
