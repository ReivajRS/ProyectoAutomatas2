package SyntacticAnalysis;

import Utilities.Token;

import java.util.ArrayList;

public class Parser {
    private static int ERROR;
    private Token[] tokens;
    private String[] strings;
    private int numTokens;
    private AbstractSyntaxTree syntaxTree;

    public Parser() {
        ERROR = -1;
    }

    public void initialize(ArrayList<Token> tokens, ArrayList<String> strings) {
        this.tokens = new Token[tokens.size()];
        this.strings = new String[strings.size()];
        for (int i = 0; i < tokens.size(); i++) {
            this.tokens[i] = tokens.get(i);
            this.strings[i] = strings.get(i);
        }
        numTokens = tokens.size();
    }

    public boolean parse() {
        int i = 0;
        Node.Code node = new Node.Code(i);
        syntaxTree = new AbstractSyntaxTree(node);
        if (outOfBounds(i) || tokens[i] != Token.CODE) {
            return false;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            return false;
        }
        node.setId(strings[i]);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_CURLY) {
            return false;
        }
        i++;
        int ii = checkInstructions(i);
        if (ii != ERROR) {
            i = ii;
        }
        if (outOfBounds(i) || tokens[i] != Token.CL_CURLY) {
            return false;
        }
        node.setEnd(i);
        return true;
    }

    private int checkInstructions(int i) {
        int ii = checkInstruction(i);
        if (ii == ERROR) {
            return ERROR;
        }
        i = ii;
        if (!outOfBounds(i) && isReservedWord(tokens[i])) {
            ii = checkInstructions(i);
            if (ii == ERROR) {
                return ERROR;
            }
            i = ii;
        }
        return i;
    }

    private int checkInstruction(int i) {
        int ii = checkDeclaration(i);
        if (ii != ERROR) {
            return ii;
        }
        ii = checkAssignment(i);
        if (ii != ERROR) {
            return ii;
        }
        ii = checkScanOrPrint(i);
        if (ii != ERROR) {
            return ii;
        }
        ii = checkIfOrWhile(i);
        if (ii != ERROR) {
            return ii;
        }
        return ERROR;
    }

    private int checkDeclaration(int i) {
        Node.Declaration node = new Node.Declaration(i);
        syntaxTree.addChild(node, false);
        if (outOfBounds(i) || !isDataType(tokens[i])) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setDataType(tokens[i]);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setId(strings[i]);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.SEMICOLON) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setEnd(i);
        i++;
        return i;
    }

    private int checkAssignment(int i) {
        Node.Assignment node = new Node.Assignment(i);
        syntaxTree.addChild(node, false);
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setId(strings[i]);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.ASSIGN) {
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        syntaxTree.setCurrentNode(node);
        int ii = checkExpression(i);
        if (ii == ERROR) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        syntaxTree.toParent();
        i = ii;
        if (outOfBounds(i) || tokens[i] != Token.SEMICOLON) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setEnd(i);
        i++;
        return i;
    }

    private int checkScanOrPrint(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.SCAN && tokens[i] != Token.PRINT)) {
            return ERROR;
        }
        Node node;
        if (tokens[i] == Token.SCAN) {
            node = new Node.Scan(i);
        }
        else {
            node = new Node.Print(i);
        }
        syntaxTree.addChild(node, false);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_PAREN){
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER){
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setId(strings[i]);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.CL_PAREN){
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.SEMICOLON) {
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setEnd(i);
        i++;
        return i;
    }

    private int checkIfOrWhile(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.IF && tokens[i] != Token.WHILE)) {
            return ERROR;
        }
        Node node;
        if (tokens[i] == Token.IF) {
            node = new Node.If(i);
        }
        else {
            node = new Node.While(i);
        }
        syntaxTree.addChild(node, true);
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_PAREN) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        int ii = checkExpression(i);
        if (ii == ERROR) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        i = ii;
        if (outOfBounds(i) || tokens[i] != Token.CL_PAREN) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_CURLY) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        i++;
        ii = checkInstructions(i);
        if (ii != ERROR) {
            i = ii;
        }
        if (outOfBounds(i) || tokens[i] != Token.CL_CURLY) {
            syntaxTree.toParent();
            syntaxTree.removeChild();
            return ERROR;
        }
        node.setEnd(i);
        syntaxTree.toParent();
        i++;
        return i;
    }

    private int checkExpression(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.IDENTIFIER && !isValue(tokens[i]))) {
            return ERROR;
        }
        if (syntaxTree.getCurrentNode() instanceof Expression node) {
            node.addToExpression(strings[i], tokens[i]);
        }
        i++;
        if (!outOfBounds(i) && isOperator(tokens[i])) {
            if (syntaxTree.getCurrentNode() instanceof Expression node) {
                node.addToExpression(strings[i], tokens[i]);
            }
            i++;
            int ii = checkExpression(i);
            if (ii == ERROR) {
                return ERROR;
            }
            i = ii;
        }
        return i;
    }

    private boolean isDataType(Token token) {
        return token == Token.INT || token == Token.BOOLEAN || token == Token.STRING;
    }

    private boolean isValue(Token token) {
        return token == Token.NUMBER || token == Token.TRUE || token == Token.FALSE || token == Token.STRING_VALUE;
    }

    private boolean isOperator(Token token) {
        return isArithmeticOperator(token) || isRelationalOperator(token);
    }

    private boolean isArithmeticOperator(Token token) {
        return token == Token.PLUS || token == Token.MINUS;
    }

    private boolean isRelationalOperator(Token token) {
        return token == Token.LESS || token == Token.LESS_EQ
                || token == Token.GREATER || token == Token.GREATER_EQ
                || token == Token.EQUALS || token == Token.DIFFERENT;
    }

    private boolean isReservedWord(Token token) {
        return isDataType(token) || token == Token.IDENTIFIER || token == Token.SCAN
                || token == Token.PRINT || token == Token.IF || token == Token.WHILE;
    }

    private boolean outOfBounds(int i) {
        return i >= numTokens;
    }

    public AbstractSyntaxTree getSyntaxTree() {
        return syntaxTree;
    }
}
