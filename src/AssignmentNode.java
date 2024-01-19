package ICSI311_Interpreter4;

//AssignmentNode is used for expression where the LValue is being operated on itself like +=
public class AssignmentNode extends StatementNode {
    Node variable;//The LValue
    OperationNode assignVar;//The Operations.

    //Initializes the values.
    public AssignmentNode(Node v, OperationNode aV){
        variable = v;
        assignVar = aV;
    }

    public Node getVariable(){
        return variable;
    }

    public OperationNode getOperation(){
        return assignVar;
    }

    public String toString(){
        return "AssignmentNode: Variable: " + variable.toString() + "Operation: " + assignVar.toString();
    }
}
