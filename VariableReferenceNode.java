package ICSI311_Interpreter4;

import java.util.Optional;

public class VariableReferenceNode extends Node {
    private Token variableName;
    private Optional<Node> index;

    //One constructor for variable reference with no array index.
    public VariableReferenceNode(Token name){
        variableName = name;
        index = Optional.empty();
    }
    //One constructor for variable reference with array index.
    public VariableReferenceNode(Token name, Optional<Node> ind){
        variableName = name;
        index = ind;
    }


    public Token getVariableName(){
        return variableName;
    }
    /*
    getIndex() is only used for testing the program.
     */
    public Optional<Node> getIndex(){
        return index;
    }

    @Override
    public String toString(){
        //If index is empty, just print out the variable name.
        if(index.isEmpty()){
            return "Variable Name: " + variableName;
        }
        //If not, print out both variable name and index.
        return "Variable Name: " + variableName + " , Index: " + index.get();
    }
}
