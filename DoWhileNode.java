package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a Do-While Loop in AWK code.
//Contains its statements and the condition.
public class DoWhileNode extends StatementNode {
    BlockNode statements;
    Optional<Node> condition;

    public DoWhileNode(Optional<Node> c, BlockNode b){
        statements = b;
        condition = c;
    }

    public Optional<Node> getCondition(){
        return condition;
    }
    @Override
    public String toString(){
        return "DoWhileNode:: Condition: " + condition.toString() + " Block: " + statements.toString();
    }
}
