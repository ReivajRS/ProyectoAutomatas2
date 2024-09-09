import java.util.ArrayList;

public class Parser {
    private final Token[] tokens;
    private final int numTokens;
    private final int ERROR;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = new Token[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            this.tokens[i] = tokens.get(i);
        }
        numTokens = tokens.size();
        ERROR = -1;
    }

    public boolean parse() {
        int i = 0;
        if (outOfBounds(i) || tokens[i] != Token.CODE) {
            return false;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            return false;
        }
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
        i++;
        return true;
    }

    private int checkInstructions(int i) {
        int ii = checkInstruction(i);
        if (ii == ERROR) {
            return ERROR;
        }
        System.out.println(tokens[i]);
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
        ii = Math.max(ii, checkAssignment(i));
        ii = Math.max(ii, checkScanOrPrint(i));
        if (ii != ERROR) {
            i = ii;
            if (outOfBounds(i) || tokens[i] != Token.SEMICOLON) {
                return ERROR;
            }
            i++;
            return i;
        }
        ii = checkIfOrWhile(i);
        if (ii == ERROR) {
            return ERROR;
        }
        i = ii;
        return i;
    }

    private int checkDeclaration(int i) {
        if (outOfBounds(i) || !isDataType(tokens[i])) {
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            return ERROR;
        }
        i++;
        return i;
    }

    private int checkAssignment(int i) {
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER) {
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.ASSIGN) {
            return ERROR;
        }
        i++;
        int ii = checkExpression(i);
        if (ii == ERROR) {
            return ERROR;
        }
        i = ii;
        return i;
    }

    private int checkScanOrPrint(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.SCAN && tokens[i] != Token.PRINT)) {
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_PAREN){
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.IDENTIFIER){
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.CL_PAREN){
            return ERROR;
        }
        i++;
        return i;
    }

    private int checkIfOrWhile(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.IF && tokens[i] != Token.WHILE)) {
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_PAREN) {
            return ERROR;
        }
        i++;
        int ii = checkExpression(i);
        if (ii == ERROR) {
            return ERROR;
        }
        i = ii;
        if (outOfBounds(i) || tokens[i] != Token.CL_PAREN) {
            return ERROR;
        }
        i++;
        if (outOfBounds(i) || tokens[i] != Token.OP_CURLY) {
            return ERROR;
        }
        i++;
        ii = checkInstructions(i);
        if (ii != ERROR) {
            i = ii;
        }
        if (outOfBounds(i) || tokens[i] != Token.CL_CURLY) {
            return ERROR;
        }
        i++;
        return i;
    }

    private int checkExpression(int i) {
        if (outOfBounds(i) || (tokens[i] != Token.IDENTIFIER && !isValue(tokens[i]))) {
            return ERROR;
        }
        i++;
        if (!outOfBounds(i) && isOperator(tokens[i])) {
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
        return token == Token.INT || token == Token.BOOLEAN;
    }

    private boolean isValue(Token token) {
        return token == Token.NUMBER || token == Token.TRUE || token == Token.FALSE;
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
}
