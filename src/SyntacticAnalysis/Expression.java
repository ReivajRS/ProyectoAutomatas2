package SyntacticAnalysis;

import Utilities.Token;

public interface Expression {
    void addToExpression(String id, Token token);
}