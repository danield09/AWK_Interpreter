package ICSI311_Interpreter4;

import java.util.Optional;

public class OperationNode extends Node {//Changed to StatementNode in order to use instanceof to see valid statements.

    //A enum List of all operations.
    public enum Operations{
        EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH,
        DOLLAR, PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS,
        UNARYNEG, IN, EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE,
        MODULO, CONCATENATION, ASSIGN
    }
    private Node left;
    private Optional<Node> right;
    private Operations expression;

    //One constructor for expressions with two sides.
    public OperationNode(Node left, Optional<Node> right, Operations exp){
        this.left = left;
        this.right = right;
        expression = exp;
    }
    //One constructor for expressions with one side.
    public OperationNode(Node left, Operations exp){
        this.left = left;
        expression = exp;
        right = Optional.empty();
    }

    /*
    These two accessor methods are used for testing, not for the purpose of the program.
     */
    public Node getLeftNode(){
        return left;
    }
    public Operations getExpression(){
        return expression;
    }
    public Optional<Node> getRightNode(){
        return right;
    }

    @Override
    public String toString(){
        //Checks if the right node is empty.
        if(right.isEmpty()){
            //If so, only print out the Left Node and Expression.
            return "(Left Node: " + left.toString() + " , Expression: " + expression + ")";
        }
        //If not, print out Left and Right Node and Expression.
        return "(Left Node: " + left.toString() + "\nRight Node: " + right.toString()+ "\nExpression: " + expression + ")";
    }
}
