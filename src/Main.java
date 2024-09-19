import LexicalAnalysis.Lexer;
import SemanticAnalysis.SemanticAnalyzer;
import SyntacticAnalysis.Parser;

public class Main {
    public static void main(String[] args) {
        GUI userInterface = new GUI();
        Lexer lexer = new Lexer();
        Parser parser = new Parser();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        Controller controller = new Controller(userInterface, lexer, parser, semanticAnalyzer);
    }
}