package ICSI311_Interpreter4;

//This class is used to determine what kind of return type the programNode has.
public class ReturnType {
    enum rType{
        NORMAL, BREAK, CONTINUE, RETURN
    }

    private rType returnType;
    private String returnString;
    public ReturnType(rType r, String s){
        returnType = r;
        returnString = s;
    }

    public ReturnType(rType r){
        returnType = r;
        returnString = "";
    }

    public rType getReturnType(){
        return returnType;
    }

    public String getReturnString(){
        return returnString;
    }

    public String toString(){
        return "ReturnType: " + returnType + " String: " + returnString;
    }
}
