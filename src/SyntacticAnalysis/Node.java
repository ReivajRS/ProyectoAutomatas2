package SyntacticAnalysis;

import Utilities.Token;
import Utilities.TokenPair;
import java.util.ArrayList;

public class Node {
    private String id;
    private Node parent;
    private SymbolTable symbolTable;
    private ArrayList<Node> children;
    private final boolean isBlockNode;
    private final int begin;
    private int end;

    public Node(boolean isBlockNode, int begin) {
        id = null;
        parent = null;
        this.isBlockNode = isBlockNode;
        this.begin = begin;
        if (isBlockNode) {
            symbolTable = new SymbolTable();
            children = new ArrayList<>();
        }
    }

    protected void addChild(Node node) {
        children.add(node);
        node.parent = this;
    }

    public String getId() {
        return id;
    }

    public Node getParent() {
        return parent;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public boolean isBlockNode() {
        return isBlockNode;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    protected void setId(String id) {
        this.id = id;
    }

    protected void setEnd(int end) {
        this.end = end;
    }

    public static class Code extends Node {
        public Code(int begin) {
            super(true, begin);
        }
    }

    public static class Declaration extends Node {
        Token dataType;

        public Declaration(int begin) {
            super(false, begin);
            this.dataType = null;
        }

        public Token getDataType() {
            return dataType;
        }

        public void setDataType(Token dataType) {
            this.dataType = dataType;
        }
    }

    public static class Assignment extends Node implements Expression {
        ArrayList<TokenPair> expression;

        public Assignment(int begin) {
            super(false, begin);
            expression = new ArrayList<>();
        }

        @Override
        public void addToExpression(String id, Token token) {
            expression.add(new TokenPair(id, token));
        }

        public ArrayList<TokenPair> getExpression() {
            return expression;
        }
    }

    public static class Scan extends Node {
        public Scan(int begin) {
            super(false, begin);
        }
    }

    public static class Print extends Node {
        public Print(int begin) {
            super(false, begin);
        }
    }

    public static class If extends Node implements Expression {
        ArrayList<TokenPair> expression;

        public If(int begin) {
            super(true, begin);
            expression = new ArrayList<>();
        }

        @Override
        public void addToExpression(String id, Token token) {
            expression.add(new TokenPair(id, token));
        }

        public ArrayList<TokenPair> getExpression() {
            return expression;
        }
    }

    public static class While extends Node implements Expression {
        ArrayList<TokenPair> expression;

        public While(int begin) {
            super(true, begin);
            expression = new ArrayList<>();
        }

        @Override
        public void addToExpression(String id, Token token) {
            expression.add(new TokenPair(id, token));
        }

        public ArrayList<TokenPair> getExpression() {
            return expression;
        }
    }
}