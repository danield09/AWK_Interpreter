package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a return statement in AWK code.
public class ReturnNode extends StatementNode {
    Optional<Node> statement;
    public ReturnNode(Optional<Node> s){
        statement = s;
    }

    public Optional<Node> getStatement(){
        return statement;
    }

    @Override
    public String toString(){
        return "ReturnNode:: Statement: " + statement.toString();
    }
}
