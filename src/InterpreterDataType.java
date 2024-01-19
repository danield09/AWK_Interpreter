package ICSI311_Interpreter4;

public class InterpreterDataType {
    private String value;

    public InterpreterDataType(){
        //Sets to no value.
        value = "";
    }
    public InterpreterDataType(String v){
        value = v;
    }

    public String getValue(){
        return value;
    }
    public String toString(){
        if(value.isEmpty()){//Added it to make it easier to spot in
            return "No value";
        }
        return "Value: " + value;
    }
}
