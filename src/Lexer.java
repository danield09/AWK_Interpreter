package ICSI311_Interpreter4;

import java.util.HashMap;
import java.util.LinkedList;

public class Lexer {
    private StringHandler stringH;
    private int lineNumber;
    private int linePosition;
    private HashMap<String, TokenType> keyWords;
    private HashMap<String, TokenType> twoCharSymbols;
    private HashMap<String, TokenType> oneCharSymbols;
    public Lexer(String doc){

        stringH = new StringHandler(doc);
        lineNumber = 1;
        linePosition = 0;//We start at 0 to increment correctly.

        keyWords = new HashMap<>();
        twoCharSymbols = new HashMap<>();
        oneCharSymbols = new HashMap<>();
        setHashMaps();
    }
    public LinkedList<Token> Lex() throws Exception{
        LinkedList<Token> returnList = new LinkedList<Token>();
        while(!stringH.IsDone()){
            char c = stringH.GetChar();
            linePosition++;
            //Skips \r \t and spaces.
            if(c == '\r' || c == ' ' || c == '\t'){
                ;
            }else if(((c >= '0') && (c <= '9')) || (c == '.')){//Send number into ProcessDigit
                returnList.add(ProcessDigit());
            }else if(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))){//Send words into ProcessWord
                returnList.add(ProcessWord());
            }else if(c == '#'){//Skip to the next comment (no need to save comments)
                while(!stringH.IsDone()){
                    c = stringH.GetChar();
                    linePosition++;
                    if(stringH.IsDone()){
                        break;
                    }else{
                        if(stringH.Peek(1) == '\n'){//When the next character is a newline, break out of the loop.
                            break;
                        }
                    }
                }
            }else if(c == '"'){//The start of a string literal, send to HandleStringLiteral.
                returnList.add(HandleStringLiteral());
            }else if(c == '`'){//The start of a literal using backticks, send to HandlePattern
                returnList.add(HandlePattern());
            }else{
                //Since we can't test EVERY valid symbols within a if statement, we have a null condition in ProcessSymbol.
                Token testToken = ProcessSymbol();
                if(testToken == null){//If it returns null, then it is a invalid character, throw exception.
                    throw new Exception("ERROR: Illegal Character.");
                }
                returnList.add(testToken);
            }
        }
        return returnList;
    }
    public Token ProcessDigit() throws Exception {
        String temp = stringH.Peek(0) + "";//Add the current character to value.
        while(!stringH.IsDone()){
            char c = stringH.GetChar();
            if(((c >= '0') && (c <= '9'))){//If the character is a number, add it to value.
                temp += c;
                linePosition++;

            }else if((c == '.')){//If the character is a period
                if(temp.contains(".")){//Check if there is already a period in value.
                    throw new Exception("ERROR: Double Periods.");
                }else{
                    temp += c;//If not, continue adding into value.
                    linePosition++;
                }
            }else{
                stringH.Swallow(-1);//Since we are looking using GetChar, we need to set the index back by 1 in order
                //to NOT skip any character in the Lex method.
                break;
            }
        }
        return new Token(TokenType.NUMBER, lineNumber, linePosition-temp.length(), temp);
    }
    public Token ProcessWord(){
        String temp = stringH.Peek(0) + "";//Add the current character to value.
        while(!stringH.IsDone()){
            char c = stringH.GetChar();
            //Checks if the current character is A-Za-z 0-9 or a underscore.
            if(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'))  || (c == '_') || (((c >= '0') && (c <= '9')))){
                temp += c;//If so, add to value
                linePosition++;
            }else{
                //If not, break.
                stringH.Swallow(-1);//In order to not skip any character in the Lex method, we set the index back by 1.
                break;
            }
        }
        if(keyWords.containsKey(temp)){//Checks if value is a key word, and create it if so.
            return new Token(keyWords.get(temp), lineNumber, linePosition - temp.length());
        }

        return new Token(TokenType.WORD, lineNumber, linePosition - temp.length(), temp);
    }
    public Token ProcessSymbol(){
        //Checks if we are allowed to peak 2 characters ahead.
        if(stringH.Remainder().length() >= 2){
            String testSymbol = stringH.PeekString(2);
            //Checks if the symbols in the hashMaps for twoSymbols, create and return the correct token.
            if(twoCharSymbols.containsKey(testSymbol)){
                stringH.Swallow(1);//to increment the index to the correct character position
                linePosition += 2;
                return new Token(twoCharSymbols.get(testSymbol), lineNumber, linePosition-2);
            }
        }
        String testOneSymbol = stringH.PeekString(1);
        //Checks if the symbol in the hashMap for oneSymbol.
        if(oneCharSymbols.containsKey(testOneSymbol)){
            linePosition++;
            //If it is \n or ;, then it is a separator, the only symbol that increments the lineNumber and resets linePosition
            if((testOneSymbol.equals("\n") || testOneSymbol.equals(";"))){
                Token separatorToken = new Token(oneCharSymbols.get(testOneSymbol), lineNumber, linePosition-1);
                lineNumber++;
                linePosition = 0;
                return separatorToken;
            }
            return new Token(oneCharSymbols.get(testOneSymbol), lineNumber, linePosition-1);
        }
        return null;//Only reaches this statement when the symbol is NOT found in both twoSymbol and oneSymbol hashMap
        //AKA, illegal character.
    }
    public Token HandleStringLiteral() throws Exception {
        String temp = "";
        //This boolean is used to check if there is an ending quotation to the literal.
        boolean stringLiteralStop = false;
        while(!stringH.IsDone()){
            char c = stringH.GetChar();
            linePosition++;
            //If it is a \" (or in Java string \\\") then it is the start of the quotations.
            if(c == '\\' && stringH.Peek(1) == '\"'){
                while(!stringH.IsDone()){
                    c = stringH.GetChar();
                    linePosition++;
                    //Breaks out if it sees the end of the quotation.
                    if(c == '\\' && stringH.Peek(1) == '\"'){
                        temp += '"';
                        break;
                    }else{
                        temp += c;
                    }
                }
                //Increments to the correct character position.
                stringH.Swallow(1);
                linePosition++;
            }else if(c == '"'){
                //If it finds the second part of the string literal, then there is a definite end.
                stringLiteralStop = true;
                break;
            }else{
                temp += c;
            }
        }
        //Checks if stringLiteralStop knows if there is a definite end to the stringliteral
        if(!stringLiteralStop){
            //If not, throw a exception.
            throw new Exception("Needs a ending double-quote (\")");
        }
        return new Token(TokenType.STRINGLITERAL, lineNumber, linePosition-temp.length(), temp);
    }
    public Token HandlePattern() throws Exception {
        String temp = "";
        //Used to see if there is a definite end to the literal/pattern
        boolean literalStop = false;
        while(!stringH.IsDone()){
            char c = stringH.GetChar();
            linePosition++;
            //If there is a \`, then it is a literal inside a literal.
            if(c == '\\' && stringH.Peek(1) == '`'){
                while(!stringH.IsDone()){
                    c = stringH.GetChar();
                    linePosition++;
                    //Breaks when it sees the end of the literal inside the literal.
                    if(c == '\\' && stringH.Peek(1) == '`'){
                        temp += '`';
                        break;
                    }else{
                        temp += c;
                    }
                }
                //Increments to the correct position.
                stringH.Swallow(1);
                linePosition++;
                //Checks if the character is the second part of the literal.
            }else if(c == '`'){
                //If so, then there is a definite ending, set literalStop to true.
                literalStop = true;
                break;
            }else{
                temp += c;
            }
        }
        //If this if statement is true, then there is a missing backtick in the program, throw a exception.
        if(!literalStop){
            throw new Exception("Needs a ending backtick (`)");
        }
        return new Token(TokenType.LITERAL, lineNumber, linePosition-temp.length(), temp);
    }
    //Used to populate all 3 Hashmaps.
    public void setHashMaps(){
        keyWords.put("while", TokenType.WHILE);
        keyWords.put("if", TokenType.IF);
        keyWords.put("do", TokenType.DO);
        keyWords.put("for", TokenType.FOR);
        keyWords.put("break",  TokenType.BREAK);
        keyWords.put("continue",  TokenType.CONTINUE);
        keyWords.put("else",  TokenType.ELSE);
        keyWords.put("return",  TokenType.RETURN);
        keyWords.put("BEGIN",  TokenType.BEGIN);
        keyWords.put("END",  TokenType.END);
        keyWords.put("print",  TokenType.PRINT);
        keyWords.put("printf",  TokenType.PRINTF);
        keyWords.put("next",  TokenType.NEXT);
        keyWords.put("in",  TokenType.IN);
        keyWords.put("delete",  TokenType.DELETE);
        keyWords.put("getline",  TokenType.GETLINE);
        keyWords.put("exit",  TokenType.EXIT);
        keyWords.put("nextfile",  TokenType.NEXTFILE);
        keyWords.put("function",  TokenType.FUNCTION);

        twoCharSymbols.put(">=",  TokenType.GREATER_EQUAL);
        twoCharSymbols.put("++",  TokenType.INCREMENT);
        twoCharSymbols.put("--",  TokenType.DECREMENT);
        twoCharSymbols.put("<=",  TokenType.LESS_EQUAL);
        twoCharSymbols.put("==",  TokenType.EQUALS);
        twoCharSymbols.put("!=",  TokenType.NOT_EQUALS);
        twoCharSymbols.put("^=",  TokenType.EXPONENT_EQUALS);
        twoCharSymbols.put("%=",  TokenType.REMAINDER_EQUALS);
        twoCharSymbols.put("*=",  TokenType.MULTIPLY_EQUALS);
        twoCharSymbols.put("/=",  TokenType.DIVIDE_EQUALS);
        twoCharSymbols.put("+=",  TokenType.ADD_EQUALS);
        twoCharSymbols.put("-=",  TokenType.SUBTRACT_EQUALS);
        twoCharSymbols.put("!~",  TokenType.NO_MATCH);
        twoCharSymbols.put("&&",  TokenType.AND);
        twoCharSymbols.put(">>",  TokenType.APPEND);
        twoCharSymbols.put("||",  TokenType.OR);

        oneCharSymbols.put("{",  TokenType.LEFT_CURLY_BRACKET);
        oneCharSymbols.put("}",  TokenType.RIGHT_CURLY_BRACKET);
        oneCharSymbols.put("[",  TokenType.LEFT_HARD_BRACKET);
        oneCharSymbols.put("]",  TokenType.RIGHT_HARD_BRACKET);
        oneCharSymbols.put("(",  TokenType.LEFT_SOFT_BRACKET);
        oneCharSymbols.put(")",  TokenType.RIGHT_SOFT_BRACKET);
        oneCharSymbols.put("$",  TokenType.DOLLAR);
        oneCharSymbols.put("~",  TokenType.MATCH);
        oneCharSymbols.put("=",  TokenType.ASSIGN);
        oneCharSymbols.put("<",  TokenType.LESS_THAN);
        oneCharSymbols.put(">",  TokenType.GREATER_THAN);
        oneCharSymbols.put("!",  TokenType.NOT);
        oneCharSymbols.put("+",  TokenType.ADD);
        oneCharSymbols.put("^",  TokenType.EXPONENT);
        oneCharSymbols.put("-",  TokenType.SUBTRACT);
        oneCharSymbols.put("?",  TokenType.TERNARY);
        oneCharSymbols.put(":",  TokenType.COLON);
        oneCharSymbols.put("*",  TokenType.MULTIPLY);
        oneCharSymbols.put("/",  TokenType.DIVIDE);
        oneCharSymbols.put("%",  TokenType.REMAINDER);
        oneCharSymbols.put(";",  TokenType.SEPERATOR);
        oneCharSymbols.put("\n",  TokenType.SEPERATOR);
        oneCharSymbols.put("|",  TokenType.BAR);
        oneCharSymbols.put(",",  TokenType.COMMA);
    }
}