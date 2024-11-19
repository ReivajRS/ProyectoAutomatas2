package CodeGenerators;

import SyntacticAnalysis.AbstractSyntaxTree;
import SyntacticAnalysis.Node;
import Utilities.SymbolData;
import Utilities.Token;
import Utilities.TokenTuple;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectCodeGenerator {
    private AbstractSyntaxTree syntaxTree;
    private HashMap<String, SymbolData> symbolDataMap;
    private StringBuilder data, code;
    private HashMap<Token, String> inverseJumpsMap;
    private int ifCount, whileCount, codeSegmentOffset, codeOffset, lastLength;
    private String dataSegment, codeSegment;

    private HashMap<String, HashMap<String, String>> binariesMap;
    private HashMap<String, String> directBinariesMap;
    private HashMap<String, Integer> flowControlOffsetMap, flowControlContinuesPositionsMap;

    public void initialize(AbstractSyntaxTree syntaxTree, HashMap<String, SymbolData> symbolDataMap) {
        this.syntaxTree = syntaxTree;
        this.symbolDataMap = symbolDataMap;
        data = new StringBuilder();
        code = new StringBuilder();
        inverseJumpsMap = new HashMap<>();
        flowControlOffsetMap = new HashMap<>();
        flowControlContinuesPositionsMap = new HashMap<>();

        ifCount = 0;
        whileCount = 0;
        codeOffset = 0;
        codeSegmentOffset = 160;

        dataSegment = "0000";
        codeSegment = "00A0";

        inverseJumpsMap.put(Token.EQUALS, "JNE");
        inverseJumpsMap.put(Token.DIFFERENT, "JE");
        inverseJumpsMap.put(Token.LESS, "JGE");
        inverseJumpsMap.put(Token.LESS_EQ, "JG");
        inverseJumpsMap.put(Token.GREATER, "JLE");
        inverseJumpsMap.put(Token.GREATER_EQ, "JL");

        binariesMap = new HashMap<>();

        binariesMap.put("MOV", new HashMap<>());
        binariesMap.get("MOV").put("M_AX", "1010 0001");
        binariesMap.get("MOV").put("M_AL", "1010 0000");
        binariesMap.get("MOV").put("M_BX", "1000 1011 0001 1101");
        binariesMap.get("MOV").put("M_BL", "1000 1010 0001 1101");
        binariesMap.get("MOV").put("AX_M", "1010 0011");
        binariesMap.get("MOV").put("AL_M", "1010 0010");
        binariesMap.get("MOV").put("IMM_M16", "1100 0111 0000 0101");
        binariesMap.get("MOV").put("IMM_M8", "1100 0110 0000 0101");
        binariesMap.get("MOV").put("IMM_AX", "1100 0111 1100 0000");
        binariesMap.get("MOV").put("IMM_BX", "1100 0111 1100 0011");
        binariesMap.get("MOV").put("IMM_BL", "1100 0110 1100 0011");
        binariesMap.get("MOV").put("IMM_AL", "1100 0110 1100 0000");
        binariesMap.get("MOV").put("IMM_AH", "1100 0110 1100 0100");
        binariesMap.get("MOV").put("IMM_DL", "1100 0110 1100 0010");
        binariesMap.get("MOV").put("AH_M[]", "1000 1000 0010 0101");
        binariesMap.get("MOV").put("IMM_M[]", "1100 0110 0000 0101");

        binariesMap.put("ADD", new HashMap<>());
        binariesMap.get("ADD").put("IMM_AX", "0000 0101");
        binariesMap.get("ADD").put("IMM_AH", "1000 0000 1100 0100");
        binariesMap.get("ADD").put("IMM_BX", "1000 0001 1100 0011");
        binariesMap.get("ADD").put("M_AX", "0000 0011 0000 0101");
        binariesMap.get("ADD").put("M_BX", "0000 0011 0001 1101");

        binariesMap.put("SUB", new HashMap<>());
        binariesMap.get("SUB").put("IMM_AX", "0010 1101");
        binariesMap.get("SUB").put("IMM_BX", "1000 0001 1110 1011");
        binariesMap.get("SUB").put("M_AX", "0010 1011 0000 0101");
        binariesMap.get("SUB").put("M_BX", "0010 1011 0001 1101");

        binariesMap.put("CMP", new HashMap<>());
        binariesMap.get("CMP").put("AX_BX", "0011 1001 1100 0011");
        binariesMap.get("CMP").put("AL_BL", "0011 1000 1100 0011");
        binariesMap.get("CMP").put("IMM_AL", "0011 1100");

        binariesMap.put("DIV", new HashMap<>());
        binariesMap.get("DIV").put("DL", "1111 0110 1111 0010");

        binariesMap.put("LEA", new HashMap<>());
        binariesMap.get("LEA").put("DX", "1000 1101 0001 0101");

        binariesMap.put("INT", new HashMap<>());
        binariesMap.get("INT").put("21H", "1100 1101 0010 0001");

        directBinariesMap = new HashMap<>();

        directBinariesMap.put("CBW", "1001 1000");

        directBinariesMap.put("JMP", "1110 1001");
        directBinariesMap.put("JL", "0000 1111 1000 1100");
        directBinariesMap.put("JLE", "0000 1111 1000 1110");
        directBinariesMap.put("JG", "0000 1111 1000 1111");
        directBinariesMap.put("JGE", "0000 1111 1000 1101");
        directBinariesMap.put("JE", "0000 1111 1000 0100");
        directBinariesMap.put("JNE", "0000 1111 1000 0101");
    }

    public String getObjectCode() {
        addPrintUtilities();
        generate(syntaxTree.getRoot());
        return data.toString() + "\n" + code.toString();
    }

    private void generate(Node node) {
        int openIf = -1, openWhile = -1;

        if (node instanceof Node.Declaration declarationNode) {
            String fullId = declarationNode.getFullId();
            Token dataType = declarationNode.getDataType();
            data.append(dataSegment).append(":").append(getHexOffset(symbolDataMap.get(fullId).offset())).append(" ");
            data.append(getDefaultValue(dataType)).append("\n");
        } else if (node instanceof Node.Assignment assignmentNode) {
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
            SymbolData symbolData = symbolDataMap.get(printNode.getFullId());
            if (symbolData.dataType() == Token.INT) {
                addIntToString(symbolData.offset());
                addPrint(0);
            }
            if (symbolData.dataType() == Token.BOOLEAN) {
                addBooleanToInt(symbolData.offset());
                addPrint(21);
            }
            if (symbolData.dataType() == Token.STRING) {
                addPrint(symbolData.offset());
            }
        }
        else if (node instanceof Node.If ifNode) {
            addFlowControl(ifNode.getExpression(), Token.IF);
            openIf = ifCount++;
        } else if (node instanceof Node.While whileNode) {
            addFlowControl(whileNode.getExpression(), Token.WHILE);
            openWhile = whileCount++;
        }
        if (node.isBlockNode()) {
            for (Node child : node.getChildren()) {
                generate(child);
            }
        }
        if (openIf != -1) {
            String ifContinueString = "if_continue" + openIf;
            int index = flowControlContinuesPositionsMap.get(ifContinueString);
            String offset = getBinaryValueOrDisplacement(codeOffset, 16) + getBinaryValueOrDisplacement(codeSegmentOffset, 16);
            code.replace(index, index + 39, offset);
        }
        if (openWhile != -1) {
            String whileContinueString = "while_continue" + openWhile;
            int index = flowControlContinuesPositionsMap.get(whileContinueString);

            String openWhileString = "while" + openWhile;
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            String whileBeginOffset = getBinaryValueOrDisplacement(flowControlOffsetMap.get(openWhileString), 16) + getBinaryValueOrDisplacement(codeSegmentOffset, 16);
            code.append(directBinariesMap.get("JMP")).append(" ").append(whileBeginOffset).append("\n");
            updateCodeOffset();

            String offset = getBinaryValueOrDisplacement(codeOffset, 16) + getBinaryValueOrDisplacement(codeSegmentOffset, 16);
            code.replace(index, index + 39, offset);
        }
    }

    private void addPrintUtilities() {
        data.append("0000:0000 0011 0000 0011 0000 0011 0000 0011 0000 0011 0000 0010 0100").append("\n");
        data.append("0000:0006 0000 0000").append("\n");
        data.append("0000:0007 0111 0100 0111 0010 0111 0101 0110 0101 0010 0100").append("\n");
        data.append("0000:000C 0110 0110 0110 0001 0110 1100 0111 0011 0110 0101 0010 0100").append("\n");
        data.append("0000:0012 0000 1010 0000 1101 0010 0100").append("\n");

        data.append("0000:0015 0000 0000 0000 0000").append("\n");
    }

    private void addIntToString(int numOffset) {
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("IMM_DL")).append(" ").append(getBinaryValueOrDisplacement(10, 8)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("M_AX")).append(" ").append(getBinaryValueOrDisplacement(numOffset, 32)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("DIV").get("DL")).append("\n");
        updateCodeOffset();
        for (int i = 4; i >= 0; i--) {
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("ADD").get("IMM_AH")).append(" ").append(getBinaryValueOrDisplacement(48, 8)).append("\n");
            updateCodeOffset();
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("MOV").get("AH_M[]")).append(" ").append(getBinaryValueOrDisplacement(i, 32)).append("\n");
            updateCodeOffset();
            if (i > 0) {
                code.append(getCodePrefix()).append(" ");
                updateCodeLength();
                code.append(directBinariesMap.get("CBW")).append("\n");
                updateCodeOffset();
                code.append(getCodePrefix()).append(" ");
                updateCodeLength();
                code.append(binariesMap.get("DIV").get("DL")).append("\n");
                updateCodeOffset();
            }
        }
    }

    private void addBooleanToInt(int booleanOffset) {
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("M_AL")).append(" ").append(getBinaryValueOrDisplacement(booleanOffset, 32)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("AX_M")).append(" ").append(getBinaryValueOrDisplacement(21, 32)).append("\n");
        updateCodeOffset();
    }

    private void addPrint(int msgOffset) {
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("IMM_BX")).append(" ").append(getBinaryValueOrDisplacement(1, 16)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("LEA").get("DX")).append(" ").append(getBinaryValueOrDisplacement(msgOffset, 32)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("IMM_AH")).append(" ").append(getBinaryValueOrDisplacement(9, 8)).append("\n");
        updateCodeOffset();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("INT").get("21H")).append("\n");
        updateCodeOffset();
    }

    public boolean isImmediate(String fullId) {
        for (int i = 0; i < fullId.length(); i++) {
            if (fullId.charAt(i) < '0' || fullId.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private String getCodePrefix() {
        return codeSegment + ":" + getHexOffset(codeOffset);
    }

    private void assignInt(Node.Assignment assignmentNode, String var) {
        String idOrVar = assignmentNode.getExpression().getFirst().getFullId();
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();

        boolean immediateOperation = isImmediate(idOrVar);
        if (immediateOperation) {
            code.append(binariesMap.get("MOV").get("IMM_AX")).append(" ");
            code.append(getBinaryValueOrDisplacement(Integer.parseInt(idOrVar), 16)).append("\n");
        }
        else {
            code.append(binariesMap.get("MOV").get("M_AX")).append(" ");
            code.append(getBinaryValueOrDisplacement(symbolDataMap.get(idOrVar).offset(), 32)).append("\n");
        }
        updateCodeOffset();

        for (int i = 1; i < assignmentNode.getExpression().size(); i += 2) {
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            String value = assignmentNode.getExpression().get(i + 1).getFullId();
            immediateOperation = isImmediate(value);
            if (assignmentNode.getExpression().get(i).getToken() == Token.PLUS) {
                if (immediateOperation) {
                    code.append(binariesMap.get("ADD").get("IMM_AX")).append(" ");
                }
                else {
                    code.append(binariesMap.get("ADD").get("M_AX")).append(" ");;
                }
            }
            else if (assignmentNode.getExpression().get(i).getToken() == Token.MINUS) {
                if (immediateOperation) {
                    code.append(binariesMap.get("SUB").get("IMM_AX")).append(" ");;
                }
                else {
                    code.append(binariesMap.get("SUB").get("M_AX")).append(" ");;
                }
            }
            if (immediateOperation) {
                code.append(getBinaryValueOrDisplacement(Integer.parseInt(value), 16)).append("\n");
            }
            else {
                code.append(getBinaryValueOrDisplacement(symbolDataMap.get(value).offset(), 32)).append("\n");
            }
            updateCodeOffset();
        }
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("AX_M")).append(" ");
        code.append(getBinaryValueOrDisplacement(symbolDataMap.get(var).offset(), 32)).append("\n");
        updateCodeOffset();
    }

    private void assignBoolean(Node.Assignment assignmentNode, String var) {
        Token firstToken = assignmentNode.getExpression().getFirst().getToken();
        if (firstToken == Token.IDENTIFIER) {
            String value = assignmentNode.getExpression().getFirst().getFullId();
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("MOV").get("M_AL")).append(" ");
            code.append(getBinaryValueOrDisplacement(symbolDataMap.get(value).offset(), 32)).append("\n");
            updateCodeOffset();
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("MOV").get("AL_M")).append(" ");
            code.append(getBinaryValueOrDisplacement(symbolDataMap.get(var).offset(), 32)).append("\n");
            updateCodeOffset();
            return;
        }
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("IMM_M8")).append(" ");
        code.append(getBinaryValueOrDisplacement(symbolDataMap.get(var).offset(), 32));
        code.append(getBinaryValueOrDisplacement(firstToken == Token.TRUE ? 1 : 0, 8)).append("\n");
        updateCodeOffset();
    }

    private void assignString(Node.Assignment assignmentNode, String var) {
        String value = assignmentNode.getExpression().getFirst().getId();
        int varOffset = symbolDataMap.get(var).offset();
        for (int i = 0; i < value.length(); i++) {
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("MOV").get("IMM_M[]")).append(" ");
            code.append(getBinaryValueOrDisplacement(varOffset + i, 32));
            code.append(getBinaryValueOrDisplacement(value.charAt(i), 8)).append("\n");
            updateCodeOffset();
        }
        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        code.append(binariesMap.get("MOV").get("IMM_M[]")).append(" ");
        code.append(getBinaryValueOrDisplacement(varOffset + value.length(), 32));
        code.append(getBinaryValueOrDisplacement('$', 8)).append("\n");
        updateCodeOffset();
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
            String openWhileString = "while" + whileCount;
            flowControlOffsetMap.put(openWhileString, codeOffset);
        }

        int operatorPosition = 0;
        for (int i = 0; i < expression.size(); i++) {
            if (inverseJumpsMap.containsKey(expression.get(i).getToken())) {
                operatorPosition = i;
                break;
            }
        }

        if (dataType == Token.INT) {
            String idOrVar = expression.getFirst().getFullId();
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            boolean immediateOperation = isImmediate(idOrVar);
            if (immediateOperation) {
                code.append(binariesMap.get("MOV").get("IMM_AX")).append(" ");
                code.append(getBinaryValueOrDisplacement(Integer.parseInt(idOrVar), 16)).append("\n");
            }
            else {
                code.append(binariesMap.get("MOV").get("M_AX")).append(" ");
                code.append(getBinaryValueOrDisplacement(symbolDataMap.get(idOrVar).offset(), 32)).append("\n");
            }
            updateCodeOffset();

            for (int i = 1; i < operatorPosition - 1; i += 2) {
                code.append(getCodePrefix()).append(" ");
                updateCodeLength();
                String value = expression.get(i + 1).getFullId();
                immediateOperation = isImmediate(value);
                if (expression.get(i).getToken() == Token.PLUS) {
                    if (immediateOperation) {
                        code.append(binariesMap.get("ADD").get("IMM_AX")).append(" ");
                    }
                    else {
                        code.append(binariesMap.get("ADD").get("M_AX")).append(" ");;
                    }
                }
                else if (expression.get(i).getToken() == Token.MINUS) {
                    if (immediateOperation) {
                        code.append(binariesMap.get("SUB").get("IMM_AX")).append(" ");;
                    }
                    else {
                        code.append(binariesMap.get("SUB").get("M_AX")).append(" ");;
                    }
                }
                if (immediateOperation) {
                    code.append(getBinaryValueOrDisplacement(Integer.parseInt(value), 16)).append("\n");
                }
                else {
                    code.append(getBinaryValueOrDisplacement(symbolDataMap.get(value).offset(), 32)).append("\n");
                }
                updateCodeOffset();
            }


            idOrVar = expression.get(operatorPosition + 1).getFullId();
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            immediateOperation = isImmediate(idOrVar);
            if (immediateOperation) {
                code.append(binariesMap.get("MOV").get("IMM_BX")).append(" ");
                code.append(getBinaryValueOrDisplacement(Integer.parseInt(idOrVar), 16)).append("\n");
            }
            else {
                code.append(binariesMap.get("MOV").get("M_BX")).append(" ");
                code.append(getBinaryValueOrDisplacement(symbolDataMap.get(idOrVar).offset(), 32)).append("\n");
            }
            updateCodeOffset();

            for (int i = operatorPosition + 2; i < expression.size(); i += 2) {
                code.append(getCodePrefix()).append(" ");
                updateCodeLength();
                String value = expression.get(i + 1).getFullId();
                immediateOperation = isImmediate(value);
                if (expression.get(i).getToken() == Token.PLUS) {
                    if (immediateOperation) {
                        code.append(binariesMap.get("ADD").get("IMM_BX")).append(" ");
                    }
                    else {
                        code.append(binariesMap.get("ADD").get("M_BX")).append(" ");;
                    }
                }
                else if (expression.get(i).getToken() == Token.MINUS) {
                    if (immediateOperation) {
                        code.append(binariesMap.get("SUB").get("IMM_BX")).append(" ");;
                    }
                    else {
                        code.append(binariesMap.get("SUB").get("M_BX")).append(" ");;
                    }
                }
                if (immediateOperation) {
                    code.append(getBinaryValueOrDisplacement(Integer.parseInt(value), 16)).append("\n");
                }
                else {
                    code.append(getBinaryValueOrDisplacement(symbolDataMap.get(value).offset(), 32)).append("\n");
                }
                updateCodeOffset();
            }

            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("CMP").get("AX_BX")).append("\n");
            updateCodeOffset();
        }
        else if (dataType == Token.BOOLEAN) {
            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            String firstFullId = expression.getFirst().getFullId();
            boolean immediateOperation = isImmediate(firstFullId);
            if (immediateOperation) {
                code.append(binariesMap.get("MOV").get("IMM_AL")).append(" ").append(expression.getFirst().getToken() == Token.TRUE ? 1 : 0).append("\n");
            }
            else {
                code.append(binariesMap.get("MOV").get("M_AL")).append(" ").append(getBinaryValueOrDisplacement(symbolDataMap.get(firstFullId).offset(), 32)).append("\n");
            }
            updateCodeOffset();


            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            String lastFullId = expression.getLast().getFullId();
            immediateOperation = isImmediate(lastFullId);
            if (immediateOperation) {
                code.append(binariesMap.get("MOV").get("IMM_BL")).append(" ").append(expression.getLast().getToken() == Token.TRUE ? 1 : 0).append("\n");
            }
            else {
                code.append(binariesMap.get("MOV").get("M_BL")).append(" ").append(getBinaryValueOrDisplacement(symbolDataMap.get(lastFullId).offset(), 32)).append("\n");
            }
            updateCodeOffset();

            code.append(getCodePrefix()).append(" ");
            updateCodeLength();
            code.append(binariesMap.get("CMP").get("AL_BL")).append("\n");
            updateCodeOffset();
        }

        code.append(getCodePrefix()).append(" ");
        updateCodeLength();
        String jump = inverseJumpsMap.get(expression.get(operatorPosition).getToken());
        code.append(directBinariesMap.get(jump)).append(" ");

        if (flowControlType == Token.IF) {
            String ifContinueString = "if_continue" + ifCount;
            flowControlContinuesPositionsMap.put(ifContinueString, code.length());
        }
        else if (flowControlType == Token.WHILE) {
            String whileContinueString = "while_continue" + whileCount;
            flowControlContinuesPositionsMap.put(whileContinueString, code.length());
        }

        code.append(getBinaryValueOrDisplacement(0, 32)).append("\n");
        updateCodeOffset();
    }

    private String getDefaultValue(Token dataType) {
        if (dataType == Token.INT) {
            return "0000 0000 0000 0000";
        }
        if (dataType == Token.STRING) {
            return "0010 0100" + " 0010 0100".repeat(99);
        }
        return "0000 0000";
    }

    private String getHexOffset(int offset) {
        String hexOffset = Integer.toHexString(offset).toUpperCase();
        hexOffset = addLeadingZeros(hexOffset, 4 - hexOffset.length());
        return hexOffset;
    }

    private void updateCodeLength() {
        lastLength = code.length();
    }

    private void updateCodeOffset() {
        codeOffset += getOffsetSize(code.length() - 1 - lastLength);
    }

    private int getOffsetSize(int diff) {
        return diff / 8 - diff / 40;
    }

    private String getBinary(int value) {
        return Integer.toBinaryString(value);
    }

    private String addLeadingZeros(String string, int count) {
        return "0".repeat(count) + string;
    }

    private String format(String string, int size) {
        StringBuilder formattedString = new StringBuilder();
        int i = 0, cnt = 1;
        while (i < string.length()) {
            if (string.charAt(i) == ' ') {
                i++;
            }
            formattedString.append(string.charAt(i));
            if (cnt % size == 0) {
                formattedString.append(" ");
            }
            i++;
            cnt++;
        }
        return formattedString.toString();
    }
    private String formatToBytes(String string) {
        return format(string, 8);
    }

    private String formatToNibbles(String string) {
        return format(string, 4);
    }

    private String toLittleEndian(String string) {
        String[] nibbles = string.split(" ");
        StringBuilder littleEndianString = new StringBuilder();
        for (int i = nibbles.length - 1; i >= 0; i--) {
            littleEndianString.append(nibbles[i]);
            if (i != 0) {
                littleEndianString.append(" ");
            }
        }
        return littleEndianString.toString();
    }

    private String getBinaryValueOrDisplacement(int dataOffset, int size) {
        String binaryDisplacement = getBinary(dataOffset);
        binaryDisplacement = addLeadingZeros(binaryDisplacement, size - binaryDisplacement.length());
        binaryDisplacement = formatToBytes(binaryDisplacement);
        binaryDisplacement = toLittleEndian(binaryDisplacement);
        binaryDisplacement = formatToNibbles(binaryDisplacement);
        return binaryDisplacement;
    }
}
