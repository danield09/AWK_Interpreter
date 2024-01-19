package ICSI311_Interpreter4;

public class StringHandler {
    private String document;
    private int fingerIndex;

    public StringHandler(String doc){
        document = doc;
        fingerIndex = -1;//Starts at -1 so it counts the first character properly.
    }

    public char Peek(int i){
        return document.charAt(fingerIndex + i);
    }

    public String PeekString(int i){
        return document.substring(fingerIndex, fingerIndex + i);
    }

    public char GetChar(){
        //Since its -1, no need to worry about the index starting at -1.
        fingerIndex++;
        return document.charAt(fingerIndex);
    }

    public void Swallow(int i){
        fingerIndex += i;
    }

    public boolean IsDone(){
        return !(fingerIndex+1 < document.length());
    }

    public String Remainder(){
        return document.substring(fingerIndex);
    }
}