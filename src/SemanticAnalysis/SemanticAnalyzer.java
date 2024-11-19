package SemanticAnalysis;

import SyntacticAnalysis.AbstractSyntaxTree;
import SyntacticAnalysis.Node;
import Utilities.SymbolData;
import Utilities.Token;
import Utilities.TokenTuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SemanticAnalyzer {
    private AbstractSyntaxTree syntaxTree;
    private HashMap<String, SymbolData> symbolDataMap;

    public void initialize(AbstractSyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
        this.symbolDataMap = new HashMap<>();
    }

    public boolean analyze() {
        return analyze(syntaxTree.getRoot(), 0);
    }

    private boolean analyze(Node node, int level) {
        if (node instanceof Node.Declaration declarationNode) {
            String id = declarationNode.getId();
            Token dataType = declarationNode.getDataType();
            int position = declarationNode.getBegin();
            Node scopeNode = declarationNode.getParent();
            if (checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
            SymbolData symbolData = scopeNode.getSymbolTable().addSymbol(id, dataType, position);
            declarationNode.setFullId(symbolData.fullId());
            symbolDataMap.put(declarationNode.getFullId(), symbolData);
        }
        else if (node instanceof Node.Assignment assignmentNode) {
            String id = assignmentNode.getId();
            Node scopeNode = assignmentNode.getParent();
            if (!checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
            SymbolData symbolData = Objects.requireNonNull(getSymbolData(scopeNode, id));
            Token dataType = symbolData.dataType();
            assignmentNode.setFullId(symbolData.fullId());
            if (!checkExpression(scopeNode, assignmentNode.getExpression(), dataType)) {
                return false;
            }
        }
        else if (node instanceof Node.Scan scanNode) {
            String id = scanNode.getId();
            Node scopeNode = scanNode.getParent();
            if (!checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
            String fullId = Objects.requireNonNull(getSymbolData(scopeNode, id)).fullId();
            scanNode.setFullId(fullId);
        }
        else if (node instanceof Node.Print printNode) {
            String id = printNode.getId();
            Node scopeNode = printNode.getParent();
            if (!checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
            String fullId = Objects.requireNonNull(getSymbolData(scopeNode, id)).fullId();
            printNode.setFullId(fullId);
        }
        else if (node instanceof Node.If ifNode) {
            Node scopeNode = ifNode.getParent();
            if (!checkExpression(scopeNode, ifNode.getExpression(), null)) {
                return false;
            }
        }
        else if (node instanceof Node.While whileNode) {
            Node scopeNode = whileNode.getParent();
            if (!checkExpression(scopeNode, whileNode.getExpression(), null)) {
                return false;
            }
        }
        if (node.isBlockNode()) {
            for (Node child : node.getChildren()) {
                if (!analyze(child, level + 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkExistenceOfSymbol(Node node, String id) {
        if (node.getSymbolTable().hasSymbol(id)) {
            return true;
        }
        if (node.getParent() != null) {
            return checkExistenceOfSymbol(node.getParent(), id);
        }
        return false;
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

    private boolean checkExpression(Node node, ArrayList<TokenTuple> expression, Token dataTypeOfVariable) {
        boolean hasNumber = false;
        boolean hasBoolean = false;
        boolean hasString = false;
        boolean hasRelationalOperator = false;
        boolean hasEqualityOperator = false;
        int operatorCount = 0;
        for (TokenTuple tokenTuple : expression) {
            if (tokenTuple.getToken() == Token.IDENTIFIER) {
                if (!checkExistenceOfSymbol(node, tokenTuple.getId())) {
                    return false;
                }
                SymbolData symbolData = Objects.requireNonNull(getSymbolData(node, tokenTuple.getId()));
                Token dataType = symbolData.dataType();
                tokenTuple.setFullId(symbolData.fullId());
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
            if (tokenTuple.getToken() == Token.NUMBER) {
                hasNumber = true;
            }
            else if (tokenTuple.getToken() == Token.TRUE || tokenTuple.getToken() == Token.FALSE) {
                hasBoolean = true;
            }
            else if (tokenTuple.getToken() == Token.STRING_VALUE) {
                hasString = true;
            }
            else if (tokenTuple.getToken() == Token.LESS || tokenTuple.getToken() == Token.LESS_EQ
                    || tokenTuple.getToken() == Token.GREATER || tokenTuple.getToken() == Token.GREATER_EQ
            ) {
                hasRelationalOperator = true;
                operatorCount++;
            }
            else if (tokenTuple.getToken() == Token.EQUALS || tokenTuple.getToken() == Token.DIFFERENT) {
                hasEqualityOperator = true;
                hasRelationalOperator = true;
                operatorCount++;
            }
        }
        boolean isIntExpression = hasNumber && !hasBoolean && !hasString;
        boolean isBooleanExpression = hasBoolean && !hasNumber && !hasString;
        boolean isStringExpression = hasString && !hasNumber && !hasBoolean;
        if (dataTypeOfVariable != null) {
            if (hasEqualityOperator || hasRelationalOperator) {
                return false;
            }
            if (dataTypeOfVariable == Token.INT && isIntExpression) {
                return true;
            }
            if (dataTypeOfVariable == Token.BOOLEAN && isBooleanExpression && operatorCount == 0) {
                return true;
            }
            if (dataTypeOfVariable == Token.STRING && isStringExpression && operatorCount == 0) {
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

    public HashMap<String, SymbolData> getSymbolDataMap() {
        return symbolDataMap;
    }
}
