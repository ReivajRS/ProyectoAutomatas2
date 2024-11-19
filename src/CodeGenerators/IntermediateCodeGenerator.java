package CodeGenerators;

import SyntacticAnalysis.AbstractSyntaxTree;
import SyntacticAnalysis.Node;
import Utilities.SymbolData;
import Utilities.Token;
import Utilities.TokenTuple;

import java.util.ArrayList;
import java.util.HashMap;

public class IntermediateCodeGenerator {
    private AbstractSyntaxTree syntaxTree;
    private HashMap<String, SymbolData> symbolDataMap;
    private StringBuilder header, data, code, macrosAndProcesses;
    private HashMap<Token, String> inverseJumpsMap;
    private int ifCount, whileCount, level;

    public void initialize(AbstractSyntaxTree syntaxTree, HashMap<String, SymbolData> symbolDataMap) {
        this.syntaxTree = syntaxTree;
        this.symbolDataMap = symbolDataMap;
        header = new StringBuilder();
        data = new StringBuilder();
        macrosAndProcesses = new StringBuilder();
        code = new StringBuilder();
        inverseJumpsMap = new HashMap<>();

        ifCount = 0;
        whileCount = 0;
        level = 1;

        inverseJumpsMap.put(Token.EQUALS, "JNE");
        inverseJumpsMap.put(Token.DIFFERENT, "JE");
        inverseJumpsMap.put(Token.LESS, "JGE");
        inverseJumpsMap.put(Token.LESS_EQ, "JG");
        inverseJumpsMap.put(Token.GREATER, "JLE");
        inverseJumpsMap.put(Token.GREATER_EQ, "JL");
    }

    public String getIntermediateCode() {
        addIntToString();
        addPrint();
        generate(syntaxTree.getRoot());
        StringBuilder intermediateCode = new StringBuilder();
        intermediateCode.append(header.toString());
        intermediateCode.append(".DATA").append("\n");
        intermediateCode.append(data.toString());
        intermediateCode.append("\n");
        intermediateCode.append(".CODE").append("\n");
        intermediateCode.append(macrosAndProcesses.toString()).append("\n");
        intermediateCode.append("main PROC").append("\n");
        intermediateCode.append("\t.startup").append("\n");
        intermediateCode.append(code.toString());
        intermediateCode.append("\t.exit").append("\n");
        intermediateCode.append("main ENDP").append("\n");
        intermediateCode.append("END main").append("\n");
        return intermediateCode.toString();
    }

    private void generate(Node node) {
        int openIf = -1, openWhile = -1;

        if (node instanceof Node.Code codeNode) {
            addHeader(codeNode.getId());
        }
        else if (node instanceof Node.Declaration declarationNode) {
            String fullId = declarationNode.getFullId();
            Token dataType = declarationNode.getDataType();
            data.append(fullId)
                    .append("\t").append(getSize(dataType))
                    .append("\t").append(getDefaultValue(dataType))
                    .append("\n");
        }
        else if (node instanceof Node.Assignment assignmentNode) {
            String fullId = assignmentNode.getFullId();
            Token dataType = symbolDataMap.get(fullId).dataType();
            if (dataType == Token.INT) {
                assignInt(assignmentNode, fullId);
            }
            if (dataType == Token.BOOLEAN) {
                assignBoolean(assignmentNode, fullId);
            }
            if (dataType == Token.STRING) {
                assignString(assignmentNode, fullId);
            }
        }
        else if (node instanceof Node.Print printNode) {
            Token dataType = symbolDataMap.get(printNode.getFullId()).dataType();
            String var = printNode.getFullId();
            addIndentation();
            if (dataType == Token.INT) {
                code.append("print_int ").append(var).append("\n");
            }
            if (dataType == Token.BOOLEAN) {
                code.append("print_boolean ").append(var).append("\n");
            }
            if (dataType == Token.STRING) {
                code.append("print ").append(var).append("\n");
            }
            addIndentation();
            code.append("print endl").append("\n");
        }
        else if (node instanceof Node.If ifNode) {
            addFlowControl(ifNode.getExpression(), Token.IF);
            openIf = ifCount++;
        }
        else if (node instanceof Node.While whileNode) {
            addFlowControl(whileNode.getExpression(), Token.WHILE);
            openWhile = whileCount++;
        }
        if (node.isBlockNode()) {
            for (Node child : node.getChildren()) {
                generate(child);
            }
        }
        if (openIf != -1) {
            addIndentation();
            code.append("if_continue").append(openIf).append(":").append("\n");
            level--;
        }
        if (openWhile != -1) {
            addIndentation();
            code.append("JMP while").append(openWhile).append("\n");
            addIndentation();
            code.append("while_continue").append(openWhile).append(":").append("\n");
            level--;
        }
    }

    private void addHeader(String codeId) {
        header.append("TITLE ").append(codeId).append("\n");
        header.append(".MODEL SMALL").append("\n");
        header.append(".486").append("\n");
        header.append(".STACK").append('\n');
    }

    private void addIntToString() {
        macrosAndProcesses.append("int_to_string MACRO n").append("\n");
        macrosAndProcesses.append("\t").append("MOV DL, 10").append("\n");
        macrosAndProcesses.append("\t").append("MOV AX, n").append("\n");
        macrosAndProcesses.append("\t").append("DIV DL").append("\n");
        for (int i = 4; i >= 0; i--) {
            macrosAndProcesses.append("\t").append("ADD AH, 48").append("\n");
            macrosAndProcesses.append("\t").append("MOV num[").append(i).append("], AH").append("\n");
            if (i > 0) {
                macrosAndProcesses.append("\t").append("CBW").append("\n");
                macrosAndProcesses.append("\t").append("DIV DL").append("\n");
            }
        }
        macrosAndProcesses.append("ENDM").append("\n");
    }

    private void addPrint() {
        data.append("num\tDB\t5 DUP('0'), '$'").append("\n");
        data.append("bool\tDB\t?").append("\n");
        data.append("true\tDB\t'true', '$'").append("\n");
        data.append("false\tDB\t'false', '$'").append("\n");
        data.append("endl\tDB\t0AH, 0DH, '$'").append("\n");

        macrosAndProcesses.append("print MACRO msg").append("\n");
        macrosAndProcesses.append("\t").append("MOV BX, 01H").append("\n");
        macrosAndProcesses.append("\t").append("LEA DX, msg").append("\n");
        macrosAndProcesses.append("\t").append("MOV AH, 09H").append("\n");
        macrosAndProcesses.append("\t").append("INT 21H").append("\n");
        macrosAndProcesses.append("ENDM").append("\n");

        macrosAndProcesses.append("print_int MACRO n").append("\n");
        macrosAndProcesses.append("\t").append("int_to_string n").append("\n");
        macrosAndProcesses.append("\t").append("print num").append("\n");
        macrosAndProcesses.append("ENDM").append("\n");

        macrosAndProcesses.append("print_boolean MACRO b").append("\n");
        macrosAndProcesses.append("\t").append("MOV AL, b").append("\n");
        macrosAndProcesses.append("\t").append("MOV bool, AL").append("\n");
        macrosAndProcesses.append("\t").append("CALL print_boolean_util").append("\n");
        macrosAndProcesses.append("ENDM").append("\n");

        macrosAndProcesses.append("print_boolean_util PROC").append("\n");
        macrosAndProcesses.append("\t").append("MOV AL, bool").append("\n");
        macrosAndProcesses.append("\t").append("CMP AL, 1").append("\n");
        macrosAndProcesses.append("\t").append("JE is_true").append("\n");
        macrosAndProcesses.append("\t").append("JNE is_false").append("\n");
        macrosAndProcesses.append("\t").append("is_true:").append("\n");
        macrosAndProcesses.append("\t\t").append("print true").append("\n");
        macrosAndProcesses.append("\t\t").append("JE continue").append("\n");
        macrosAndProcesses.append("\t").append("is_false:").append("\n");
        macrosAndProcesses.append("\t\t").append("print false").append("\n");
        macrosAndProcesses.append("\t\t").append("JNE continue").append("\n");
        macrosAndProcesses.append("\t").append("continue:").append("\n");
        macrosAndProcesses.append("\t").append("RET").append("\n");
        macrosAndProcesses.append("print_boolean_util ENDP").append("\n");
    }

    private void assignInt(Node.Assignment assignmentNode, String var) {
        String idOrVar = assignmentNode.getExpression().getFirst().getFullId();
        addIndentation();
        code.append("MOV AX, ").append(idOrVar).append("\n");
        for (int i = 1; i < assignmentNode.getExpression().size(); i += 2) {
            addIndentation();
            if (assignmentNode.getExpression().get(i).getToken() == Token.PLUS) {
                code.append("ADD AX, ");
            }
            else if (assignmentNode.getExpression().get(i).getToken() == Token.MINUS) {
                code.append("SUB AX, ");
            }
            String value = assignmentNode.getExpression().get(i + 1).getFullId();
            code.append(value).append("\n");
        }
        addIndentation();
        code.append("MOV ").append(var).append(", AX").append("\n");
    }

    private void assignBoolean(Node.Assignment assignmentNode, String var) {
        Token firstToken = assignmentNode.getExpression().getFirst().getToken();
        if (firstToken == Token.IDENTIFIER) {
            String value = assignmentNode.getExpression().getFirst().getFullId();
            addIndentation();
            code.append("MOV AL, ").append(value).append("\n");
            addIndentation();
            code.append("MOV ").append(var).append(", AL").append("\n");
            return;
        }
        addIndentation();
        code.append("MOV ").append(var).append(", ").append(getBooleanValue(firstToken)).append("\n");
    }

    private void assignString(Node.Assignment assignmentNode, String var) {
        String value = assignmentNode.getExpression().getFirst().getId();
        for (int i = 0; i < value.length(); i++) {
            addIndentation();
            code.append("MOV ").append(var).append("[").append(i).append("], '").append(value.charAt(i)).append("'").append("\n");
        }
        addIndentation();
        code.append("MOV ").append(var).append("[").append(value.length()).append("], '").append("$").append("'").append("\n");
    }

    private void addFlowControl(ArrayList<TokenTuple> expression, Token flowControlType) {
        Token firstToken = expression.getFirst().getToken(), dataType = null;
        if (firstToken == Token.IDENTIFIER) {
            dataType = symbolDataMap.get(expression.getFirst().getFullId()).dataType();
        }
        else if (firstToken == Token.NUMBER) {
            dataType = Token.INT;
        }
        else if (firstToken == Token.TRUE || firstToken == Token.FALSE) {
            dataType = Token.BOOLEAN;
        }
        else if (firstToken == Token.STRING_VALUE) {
            dataType = Token.STRING;
        }

        if (flowControlType == Token.WHILE) {
            addIndentation();
            code.append("while").append(whileCount).append(":").append("\n");
            level++;
        }

        int operatorPosition = 0;
        for (int i = 0; i < expression.size(); i++) {
            if (inverseJumpsMap.containsKey(expression.get(i).getToken())) {
                operatorPosition = i;
                break;
            }
        }

        if (dataType == Token.INT) {
            addIndentation();
            String idOrVar = expression.getFirst().getFullId();
            code.append("MOV AX, ").append(idOrVar).append("\n");
            for (int i = 1; i < operatorPosition - 1; i += 2) {
                addIndentation();
                if (expression.get(i).getToken() == Token.PLUS) {
                    code.append("ADD AX, ");
                }
                else if (expression.get(i).getToken() == Token.MINUS) {
                    code.append("SUB AX, ");
                }
                String value = expression.get(i + 1).getFullId();
                code.append(value).append("\n");
            }
            addIndentation();
            code.append("MOV BX, ").append(expression.get(operatorPosition + 1).getId()).append("\n");
            for (int i = operatorPosition + 2; i < expression.size(); i += 2) {
                addIndentation();
                if (expression.get(i).getToken() == Token.PLUS) {
                    code.append("ADD BX, ");
                }
                else if (expression.get(i).getToken() == Token.MINUS) {
                    code.append("SUB BX, ");
                }
                String value = expression.get(i + 1).getFullId();
                code.append(value).append("\n");
            }
            addIndentation();
            code.append("CMP AX, BX").append("\n");
        }
        else if (dataType == Token.BOOLEAN) {
            String leftValue;
            if (expression.getFirst().getToken() != Token.IDENTIFIER) {
                leftValue = getBooleanValue(expression.getFirst().getToken());
            }
            else {
                leftValue = expression.getFirst().getFullId();
            }
            addIndentation();
            code.append("MOV AL, ").append(leftValue).append("\n");
            String rightValue;
            if (expression.getFirst().getToken() != Token.IDENTIFIER) {
                rightValue = getBooleanValue(expression.getLast().getToken());
            }
            else {
                rightValue = expression.getLast().getFullId();
            }
            addIndentation();
            code.append("MOV BL, ").append(rightValue).append("\n");
            addIndentation();
            code.append("CMP AL, BL").append("\n");
        }

        addIndentation();
        if (flowControlType == Token.IF) {
            code.append(inverseJumpsMap.get(expression.get(operatorPosition).getToken())).append(" if_continue").append(ifCount).append("\n");
        }
        else if (flowControlType == Token.WHILE) {
            code.append(inverseJumpsMap.get(expression.get(operatorPosition).getToken())).append(" while_continue").append(whileCount).append("\n");
        }
    }

    private void addIndentation() {
        code.append("\t".repeat(level));
    }

    private String getBooleanValue(Token token) {
        return token == Token.TRUE ? "1" : "0";
    }

    private String getDefaultValue(Token dataType) {
        if (dataType == Token.INT || dataType == Token.BOOLEAN) {
            return "?";
        }
        return "100 DUP('$')";
    }

    private String getSize(Token dataType) {
        if (dataType == Token.INT) {
            return "DW";
        }
        return "DB";
    }
}
