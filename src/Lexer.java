import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Lexer {
    private static HashSet<Token> reservedWordsSet;
    private static HashMap<String, Token> reservedWordsMap;
    private ArrayList<String> strings;
    private ArrayList<Token> tokens;

    public Lexer() {
        if (reservedWordsSet == null) {
            reservedWordsSet = new HashSet<>();
            reservedWordsSet.add(Token.CODE);
            reservedWordsSet.add(Token.SCAN);
            reservedWordsSet.add(Token.PRINT);
            reservedWordsSet.add(Token.IF);
            reservedWordsSet.add(Token.WHILE);
            reservedWordsSet.add(Token.INT);
            reservedWordsSet.add(Token.BOOLEAN);
            reservedWordsSet.add(Token.TRUE);
            reservedWordsSet.add(Token.FALSE);
        }

        if (reservedWordsMap == null) {
            reservedWordsMap = new HashMap<>();
            reservedWordsMap.put("code", Token.CODE);
            reservedWordsMap.put("scan", Token.SCAN);
            reservedWordsMap.put("print", Token.PRINT);
            reservedWordsMap.put("if", Token.IF);
            reservedWordsMap.put("while", Token.WHILE);
            reservedWordsMap.put("int", Token.INT);
            reservedWordsMap.put("boolean", Token.BOOLEAN);
            reservedWordsMap.put("true", Token.TRUE);
            reservedWordsMap.put("false", Token.FALSE);
        }
    }

    public void scan(String input) {
        strings = new ArrayList<>();
        tokens = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character c = input.charAt(i);
            if (c == '{') {
                strings.add("{");
                tokens.add(Token.OP_CURLY);
                continue;
            }
            if (c == '}') {
                strings.add("}");
                tokens.add(Token.CL_CURLY);
                continue;
            }
            if (c == '(') {
                strings.add("(");
                tokens.add(Token.OP_PAREN);
                continue;
            }
            if (c == ')') {
                strings.add(")");
                tokens.add(Token.CL_PAREN);
                continue;
            }
            if (c == '+') {
                strings.add("+");
                tokens.add(Token.PLUS);
                continue;
            }
            if (c == '-') {
                strings.add("-");
                tokens.add(Token.MINUS);
                continue;
            }
            if (c == ';') {
                strings.add(";");
                tokens.add(Token.SEMICOLON);
                continue;
            }
            boolean isLastOrNotEqual = i + 1 >= input.length() || input.charAt(i + 1) != '=';
            if (c == '=') {
                if (isLastOrNotEqual) {
                    strings.add("=");
                    tokens.add(Token.ASSIGN);
                }
                else {
                    strings.add("==");
                    tokens.add(Token.EQUALS);
                    i++;
                }
                continue;
            }
            if (c == '<') {
                if (isLastOrNotEqual) {
                    strings.add("<");
                    tokens.add(Token.LESS);
                }
                else {
                    strings.add("<=");
                    tokens.add(Token.LESS_EQ);
                    i++;
                }
                continue;
            }
            if (c == '>') {
                if (isLastOrNotEqual) {
                    strings.add(">");
                    tokens.add(Token.GREATER);
                }
                else {
                    strings.add(">=");
                    tokens.add(Token.GREATER_EQ);
                    i++;
                }
                continue;
            }
            if (c == '!') {
                if (isLastOrNotEqual) {
                    strings.add("!");
                    tokens.add(Token.ERROR);
                }
                else {
                    strings.add("!=");
                    tokens.add(Token.DIFFERENT);
                    i++;
                }
                continue;
            }
            if (Character.isAlphabetic(c)) {
                stringBuilder.append(c);
                if (i + 1 >= input.length() || !Character.isAlphabetic(input.charAt(i + 1))) {
                    String string = stringBuilder.toString();
                    strings.add(string);
                    tokens.add(reservedWordsMap.getOrDefault(string, Token.IDENTIFIER));
                    stringBuilder = new StringBuilder();
                }
                continue;
            }
            if (Character.isDigit(c)) {
                stringBuilder.append(c);
                if (i + 1 >= input.length() || !Character.isDigit(input.charAt(i + 1))) {
                    String string = stringBuilder.toString();
                    strings.add(string);
                    tokens.add(Token.NUMBER);stringBuilder = new StringBuilder();
                }
                continue;
            }
            if (c != ' ' && c != '\n' && c != '\t') {
                strings.add("" + c);
                tokens.add(Token.ERROR);
            }
        }
    }

    public Boolean[] checkReservedWords(ArrayList<Token> tokens) {
        Boolean[] reservedWords = new Boolean[tokens.size()];
        for (int i = 0; i < reservedWords.length; i++) {
            reservedWords[i] = reservedWordsSet.contains(tokens.get(i));
        }
        return reservedWords;
    }

    public ArrayList<String> getStrings() {
        return strings;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }
}