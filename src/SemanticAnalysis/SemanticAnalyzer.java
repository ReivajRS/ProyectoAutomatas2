package SemanticAnalysis;

import SyntacticAnalysis.AbstractSyntaxTree;
import SyntacticAnalysis.Node;
import Utilities.SymbolData;
import Utilities.Token;
import Utilities.TokenPair;

import java.util.ArrayList;
import java.util.Objects;

public class SemanticAnalyzer {
    private AbstractSyntaxTree syntaxTree;

    public void initialize(AbstractSyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
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
            scopeNode.getSymbolTable().addSymbol(id, dataType, position);
        }
        else if (node instanceof Node.Assignment assignmentNode) {
            String id = assignmentNode.getId();
            Node scopeNode = assignmentNode.getParent();
            if (!checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
            Token dataType = Objects.requireNonNull(getSymbolData(scopeNode, id)).dataType();
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
        }
        else if (node instanceof Node.Print printNode) {
            String id = printNode.getId();
            Node scopeNode = printNode.getParent();
            if (!checkExistenceOfSymbol(scopeNode, id)) {
                return false;
            }
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

    private boolean checkExpression(Node node, ArrayList<TokenPair> expression, Token dataTypeOfVariable) {
        boolean hasNumber = false;
        boolean hasBoolean = false;
        boolean hasRelationalOperator = false;
        boolean hasEqualityOperator = false;
        int operatorCount = 0;
        for (TokenPair tokenPair : expression) {
            if (tokenPair.token() == Token.IDENTIFIER) {
                if (!checkExistenceOfSymbol(node, tokenPair.id())) {
                    return false;
                }
                Token dataType = Objects.requireNonNull(getSymbolData(node, tokenPair.id())).dataType();
                if (dataType == Token.INT) {
                    hasNumber = true;
                }
                else if (dataType == Token.BOOLEAN) {
                    hasBoolean = true;
                }
            }
            if (tokenPair.token() == Token.NUMBER) {
                hasNumber = true;
            }
            else if (tokenPair.token() == Token.TRUE || tokenPair.token() == Token.FALSE) {
                hasBoolean = true;
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
        boolean isIntExpression = hasNumber && !hasBoolean;
        boolean isBooleanExpression = hasBoolean && !hasNumber;
        if (dataTypeOfVariable != null) {
            if (dataTypeOfVariable == Token.INT && isIntExpression) {
                return true;
            }
            if (dataTypeOfVariable == Token.BOOLEAN && isBooleanExpression) {
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
        }
        return false;
    }
}
