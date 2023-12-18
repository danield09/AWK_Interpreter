package ICSI311_Interpreter4;

import java.util.Optional;

//Represents a delete statement, where it can delete either a array name or a array reference.
public class DeleteNode extends StatementNode {
    Optional<Node> arrayReference;
    public DeleteNode(Optional<Node> arrayR){
        arrayReference = arrayR;
    }

    public Optional<Node> getArrayReference(){
        return arrayReference;
    }

    @Override
    public String toString(){
        return "Delete Node:: Array Reference: " + arrayReference.toString();
    }
}
