package ICSI311_Interpreter4;

import java.util.LinkedList;
import java.util.Optional;

public class TokenManager {
    private LinkedList<Token> tokensList;
    public TokenManager(LinkedList<Token> tL){
        tokensList = tL;
    }

    public Optional<Token> Peek(int j){
        //Checks if the index is within the linkedList.
        if (j < tokensList.size()) {
            return Optional.ofNullable((tokensList.get(j)));
        }else{
            return Optional.empty();
        }
    }

    public Optional<Token> MatchAndRemove(TokenType t){
        //Check if there are ANY tokens in the list.
        if(!MoreTokens()){
            return Optional.empty();
        }
        if(tokensList.getFirst().getToken() == t){
            //If the first token (head) matches with the parameter t.
            //Save the token for return.
            Token returnToken = tokensList.getFirst();
            //Remove the head from the list.
            tokensList.remove(0);
            return Optional.ofNullable(returnToken);
        }else{
            return Optional.empty();
        }
    }

    public boolean MoreTokens(){
        return !tokensList.isEmpty();
    }
}
