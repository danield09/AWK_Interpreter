package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a if statement and multiple if statements (if-else-if) in AWK code.
public class IfNode extends StatementNode {
    Optional<Node> condition;
    BlockNode statements;

    IfNode nextIf;//Only used when there is a if-else-if chain.

    public IfNode(Optional<Node> c, BlockNode b){
        condition = c;
        statements = b;
        nextIf = null;
    }

    public IfNode(Optional<Node> c, BlockNode b, IfNode i){
        condition = c;
        statements = b;
        nextIf = i;
    }

    public IfNode(BlockNode b){
        condition = Optional.empty();
        statements = b;
        nextIf = null;
    }

    public BlockNode returnStatements(){
        return statements;
    }

    public Optional<Node> getCondition(){
        return condition;
    }

    public IfNode getNextIf(){
        return nextIf;
    }


    @Override
    public String toString(){
        if(condition.isEmpty()){
            return "If Node:(Else): Block: " + statements.toString();
        }
        if(nextIf != null){
            return "If Node:: Condition: " + condition.get().toString() + " Block: " + statements.toString() + " next If: " + nextIf.toString();
        }
        return "If Node:: Condition: " + condition.get().toString() + " Block: " + statements.toString();
    }
}
