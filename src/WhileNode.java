package ICSI311_Interpreter4;

import java.util.Optional;

//Use for code that represents a While loop with its block of code.
public class WhileNode extends StatementNode {
    Optional<Node> condition;
    BlockNode statements;

    public WhileNode(Optional<Node> c, BlockNode b){
        condition = c;
        statements = b;
    }

    public Optional<Node> getCondition(){
        return condition;
    }

    @Override
    public String toString(){
        return "WhileNode:: Condition: " + condition.toString() + " Block: " + statements.toString();
    }
}
