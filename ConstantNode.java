package ICSI311_Interpreter4;

//Holds one value for STRINGLITERAL and NUMBER
public class ConstantNode extends Node {
    private Token value;

    public ConstantNode(Token v){
        value = v;
    }

    public Token getValue(){
        return value;
    }

    @Override
    public String toString(){
        return "Constant Node: " + value.toString();
    }
}
