package ICSI311_Interpreter4;

//Ternary Nodes are used for Ternary expression, which contains an expression, true case, and a false case.
public class TernaryNode extends Node {
    Node booleanExpression;
    Node trueCase;
    Node falseCase;

    //Initializes the values.
    public TernaryNode(Node bE, Node tC, Node fC){
        booleanExpression = bE;
        trueCase = tC;
        falseCase = fC;
    }

    public Node getBooleanExpression(){
        return booleanExpression;
    }
    public Node getTrueCase(){
        return trueCase;
    }

    public Node getFalseCase(){
        return falseCase;
    }

    public String toString(){
        return "Ternary Node: Boolean Exp: " + booleanExpression.toString() + " True Case: " + trueCase.toString() +
                " False Case: " + falseCase.toString();
    }
}
