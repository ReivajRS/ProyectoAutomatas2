import SyntacticAnalysis.AbstractSyntaxTree;
import SyntacticAnalysis.Node;
import Utilities.SymbolData;
import Utilities.Token;
import Utilities.TokenPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class IntermediateCodeGenerator {
    private AbstractSyntaxTree syntaxTree;
    private StringBuilder header, data, code, macrosAndProcesses;
    private HashSet<String> definedIds;

    public void initialize(AbstractSyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
        header = new StringBuilder();
        data = new StringBuilder();
        macrosAndProcesses = new StringBuilder();
        code = new StringBuilder();
        definedIds = new HashSet<>();
    }

    public String getIntermediateCode() {
        addIntToString();
        addPrint();
        generate(syntaxTree.getRoot());
        StringBuilder intermediateCode = new StringBuilder();
        intermediateCode.append(header.toString());
        intermediateCode.append("\n");
        intermediateCode.append(".DATA").append("\n");
        intermediateCode.append(data.toString());
        intermediateCode.append("\n");
        intermediateCode.append(".CODE").append("\n");
        intermediateCode.append(macrosAndProcesses.toString()).append("\n");
        intermediateCode.append("main PROC").append("\n");
        intermediateCode.append(".startup").append("\n");
        intermediateCode.append(code.toString());
        intermediateCode.append("\n");
        intermediateCode.append(".exit").append("\n");
        intermediateCode.append("main ENDP").append("\n");
        intermediateCode.append("END main").append("\n");
        return intermediateCode.toString();
    }

    private void generate(Node node) {
        if (node instanceof Node.Code codeNode) {
            addHeader(codeNode.getId());
        }
        else if (node instanceof Node.Declaration declarationNode) {
            String id = declarationNode.getId();
            Token dataType = declarationNode.getDataType();
            int position = declarationNode.getBegin();
            if (!definedIds.contains(id)) {
                definedIds.add(id);
                data.append(id).append("_").append(position)
                        .append("\t").append(getSize(dataType))
                        .append("\t").append(getDefaultValue(dataType))
                        .append("\n");
            }
        }
        else if (node instanceof Node.Assignment assignmentNode) {
            String id = assignmentNode.getId();
            Node scopeNode = assignmentNode.getParent();
            SymbolData symbolData = Objects.requireNonNull(getSymbolData(scopeNode, id));
            Token dataType = symbolData.dataType();
            int position = symbolData.position();
            if (dataType == Token.INT) {
                assignInt(assignmentNode, position);
            }
            if (dataType == Token.BOOLEAN) {
                assignBoolean(assignmentNode, position);
            }
            if (dataType == Token.STRING) {
                assignString(assignmentNode, position);
            }
        }
        else if (node instanceof Node.Print printNode) {
            String id = printNode.getId();
            Node scopeNode = printNode.getParent();
            SymbolData symbolData = Objects.requireNonNull(getSymbolData(scopeNode, id));
            Token dataType = symbolData.dataType();
            int position = symbolData.position();
            String var = id + "_" + position;
            if (dataType == Token.INT) {
                code.append("print_int ").append(var).append("\n");
            }
            if (dataType == Token.BOOLEAN) {
                code.append("print_boolean ").append(var).append("\n");
            }
            if (dataType == Token.STRING) {
                code.append("print ").append(var).append("\n");
            }
        }
        if (node.isBlockNode()) {
            for (Node child : node.getChildren()) {
                generate(child);
            }
        }
    }

    private void assignInt(Node.Assignment assignmentNode, int position) {
        String var = assignmentNode.getId() + "_" + position;
        if (assignmentNode.getExpression().size() == 1) {
            code.append("MOV ").append(var).append(", ").append(assignmentNode.getExpression().getFirst().id()).append("\n");
            return;
        }
        code.append("MOV AX, ").append(assignmentNode.getExpression().getFirst().id()).append("\n");
        for (int i = 1; i < assignmentNode.getExpression().size(); i += 2) {
            if (assignmentNode.getExpression().get(i).token() == Token.PLUS) {
                code.append("ADD AX, ");
            }
            if (assignmentNode.getExpression().get(i).token() == Token.MINUS) {
                code.append("SUB AX, ");
            }
            code.append(assignmentNode.getExpression().get(i + 1).id()).append("\n");
        }
        code.append("MOV ").append(var).append(", AX").append("\n");
    }

    private void assignBoolean(Node.Assignment assignmentNode, int position) {
        String var = assignmentNode.getId() + "_" + position;
        code.append("MOV ").append(var).append(", ")
                .append(assignmentNode.getExpression().getFirst().token() == Token.TRUE ? "1" : "0").append("\n");
    }

    private void assignString(Node.Assignment assignmentNode, int position) {
        String var = assignmentNode.getId() + "_" + position;
        String value = assignmentNode.getExpression().getFirst().id();
        for (int i = 0; i < value.length(); i++) {
            code.append("MOV ").append(var).append("[").append(i).append("], '").append(value.charAt(i)).append("'").append("\n");
        }
    }

    private SymbolData getSymbolData(Node node, String id) {
        if (node.getSymbolTable().hasSymbol(id)) {
            return node.getSymbolTable().getSymbol(id);
        }
        if (node.getParent() != null) {
            return getSymbolData(node.getParent(), id);
        }
        return null;
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
        data.append("num\tDB\t5 DUP('0'), 0AH, 0DH, '$'").append("\n");
        data.append("true\tDB\t'true', 0AH, 0DH, '$'").append("\n");
        data.append("false\tDB\t'false', 0AH, 0DH, '$'").append("\n");

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
        macrosAndProcesses.append("ENDM").append("\n");
    }

    private boolean checkExpression(Node node, ArrayList<TokenPair> expression, Token dataTypeOfVariable) {
        boolean hasNumber = false;
        boolean hasBoolean = false;
        boolean hasString = false;
        boolean hasRelationalOperator = false;
        boolean hasEqualityOperator = false;
        int operatorCount = 0;
        for (TokenPair tokenPair : expression) {
            if (tokenPair.token() == Token.IDENTIFIER) {
                Token dataType = Objects.requireNonNull(getSymbolData(node, tokenPair.id())).dataType();
                if (dataType == Token.INT) {
                    hasNumber = true;
                }
                else if (dataType == Token.BOOLEAN) {
                    hasBoolean = true;
                }
                else if (dataType == Token.STRING) {
                    hasString = true;
                }
            }
            if (tokenPair.token() == Token.NUMBER) {
                hasNumber = true;
            }
            else if (tokenPair.token() == Token.TRUE || tokenPair.token() == Token.FALSE) {
                hasBoolean = true;
            }
            else if (tokenPair.token() == Token.STRING_VALUE) {
                hasString = true;
            }
            else if (tokenPair.token() == Token.LESS || tokenPair.token() == Token.LESS_EQ
                    || tokenPair.token() == Token.GREATER || tokenPair.token() == Token.GREATER_EQ
            ) {
                hasRelationalOperator = true;
                operatorCount++;
            }
            else if (tokenPair.token() == Token.EQUALS || tokenPair.token() == Token.DIFFERENT) {
                hasEqualityOperator = true;
                hasRelationalOperator = true;
                operatorCount++;
            }
        }
        boolean isIntExpression = hasNumber && !hasBoolean && !hasString;
        boolean isBooleanExpression = hasBoolean && !hasNumber && !hasString;
        boolean isStringExpression = hasString && !hasNumber && !hasBoolean;
        if (dataTypeOfVariable != null) {
            if (dataTypeOfVariable == Token.INT && isIntExpression) {
                return true;
            }
            if (dataTypeOfVariable == Token.BOOLEAN && isBooleanExpression) {
                return true;
            }
            if (dataTypeOfVariable == Token.STRING && isStringExpression) {
                return true;
            }
        }
        else {
            if (isIntExpression && hasRelationalOperator && operatorCount == 1) {
                return true;
            }
            if (isBooleanExpression && hasEqualityOperator && operatorCount == 1) {
                return true;
            }
            if (isStringExpression && hasEqualityOperator && operatorCount == 1) {
                return true;
            }
        }
        return false;
    }

    private String getDefaultValue(Token dataType) {
        if (dataType == Token.INT || dataType == Token.BOOLEAN) {
            return "?";
        }
        return "100 DUP('$')";
    }

    private DirectiveSize getSize(Token dataType) {
        if (dataType == Token.BOOLEAN || dataType == Token.STRING) {
            return DirectiveSize.DB;
        }
        if (dataType == Token.INT) {
            return DirectiveSize.DW;
        }
        return DirectiveSize.DB;
    }

    private enum DirectiveSize {
        DB, DD, DW
    }
}
