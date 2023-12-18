package ICSI311_Interpreter4;

enum TokenType{
    WORD, NUMBER, SEPERATOR, WHILE, IF, DO, FOR, BREAK, CONTINUE, ELSE, RETURN,
    BEGIN, END, PRINT, PRINTF, NEXT, IN, DELETE, GETLINE, EXIT, NEXTFILE, FUNCTION,
    STRINGLITERAL, LITERAL, GREATER_EQUAL, INCREMENT, DECREMENT, LESS_EQUAL, EQUALS,
    NOT_EQUALS, EXPONENT_EQUALS, REMAINDER_EQUALS, MULTIPLY_EQUALS, DIVIDE_EQUALS,
    ADD_EQUALS, SUBTRACT_EQUALS, NO_MATCH, AND, APPEND, OR, LEFT_CURLY_BRACKET,
    RIGHT_CURLY_BRACKET, LEFT_HARD_BRACKET, RIGHT_HARD_BRACKET, LEFT_SOFT_BRACKET,
    RIGHT_SOFT_BRACKET, DOLLAR, MATCH, ASSIGN, LESS_THAN, GREATER_THAN, NOT, ADD,
    EXPONENT, SUBTRACT, TERNARY, COLON, MULTIPLY, DIVIDE, REMAINDER, BAR, COMMA;
}
public class Token {
    private TokenType tokenT;
    private int lineNumber;
    private int linePosition;
    private String value;

    public Token(TokenType tType, int lineN, int lineP){
        tokenT = tType;
        lineNumber = lineN;
        linePosition = lineP;
    }

    public Token(TokenType tType, int lineN, int lineP, String v){
        tokenT = tType;
        lineNumber = lineN;
        linePosition = lineP;
        value = v;
    }
    public String getTokenValue(){
        return value;
    }

    public TokenType getToken(){
        return tokenT;
    }
    @Override
    public String toString(){
        if(value == null){
            return tokenT + "";
        }
        return tokenT + "(" + value + ")";
    }
}