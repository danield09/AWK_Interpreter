package ICSI311_Interpreter4;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class mainClass {
    public static void main(String[] args) throws Exception {
        if(args.length > 0){
            Path documentPath = Paths.get(args[0]);
            String documentContent = new String(Files.readAllBytes(documentPath));
            Lexer testLexer = new Lexer(documentContent);
            LinkedList<Token> testLex = testLexer.Lex();
            Parser testParser = new Parser(testLex);
            Interpreter testInterpreter = new Interpreter(testParser.Parse(), Path.of(args[1]));
            testInterpreter.InterpretProgram();
        }else{
            System.out.println("Insufficient arguments amount!");
        }
    }
}
