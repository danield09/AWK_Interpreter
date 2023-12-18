package ICSI311_Interpreter4;

//Holds one value for Pattern/LITERAL tokens.
public class PatternNode extends Node {
    private Token value;
    public PatternNode(Token v){
        value = v;
    }

    public Token getTokenPattern(){
        return value;
    }

    @Override
    public String toString(){
        return "Pattern Node: " + value.toString();
    }
}
