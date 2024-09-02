import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;


public class Lexer {
    private final HashMap<String, Token> tokensMap;
    private final HashSet<Token> reservedWordsSet;
    private final HashMap<String, Token> reservedWordsMap;
    private final Pattern identifierPattern, numberPattern;

    public Lexer() {
        tokensMap = new HashMap<>();
        tokensMap.put(";", Token.SEMICOLON);
        tokensMap.put("(", Token.OP_PAREN);
        tokensMap.put(")", Token.CL_PAREN);
        tokensMap.put("{", Token.OP_CURLY);
        tokensMap.put("}", Token.CL_CURLY);
        tokensMap.put("=", Token.ASSIGN);
        tokensMap.put("+", Token.PLUS);
        tokensMap.put("-", Token.MINUS);
        tokensMap.put("<", Token.LESS);
        tokensMap.put("<=", Token.LESS_EQ);
        tokensMap.put(">", Token.GREATER);
        tokensMap.put(">=", Token.GREATER_EQ);
        tokensMap.put("==", Token.EQUALS);
        tokensMap.put("!=", Token.DIFFERENT);
        tokensMap.put("code", Token.CODE);
        tokensMap.put("scan", Token.SCAN);
        tokensMap.put("print", Token.PRINT);
        tokensMap.put("if", Token.IF);
        tokensMap.put("while", Token.WHILE);
        tokensMap.put("int", Token.INT);
        tokensMap.put("boolean", Token.BOOLEAN);
        tokensMap.put("true", Token.TRUE);
        tokensMap.put("false", Token.FALSE);

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

        identifierPattern = Pattern.compile("[a-zA-Z]+");
        numberPattern = Pattern.compile("\\d+");
    }

    public Token[] scan(String[] input) {
        Token[] tokens = new Token[input.length];
        for (int i = 0; i < input.length; i++) {
            String word = input[i];
            if (tokensMap.containsKey(word)) {
                tokens[i] = tokensMap.get(word);
            }
            else if (numberPattern.matcher(word).matches()) {
                tokens[i] = Token.NUMBER;
            }
            else if (identifierPattern.matcher(word).matches()) {
                tokens[i] = Token.IDENTIFIER;
            }
            else {
                tokens[i] = Token.ERROR;
            }
        }
        return tokens;
    }

    public Pair<ArrayList<String>, ArrayList<Token>> scan2(String input) {
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Token> tokens = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character c = input.charAt(i);
            if (c == '{') {
                strings.add("{");
                tokens.add(Token.OP_CURLY);
            }
            if (c == '}') {
                strings.add("}");
                tokens.add(Token.CL_CURLY);
            }
            if (c == '(') {
                strings.add("(");
                tokens.add(Token.OP_PAREN);
            }
            if (c == ')') {
                strings.add(")");
                tokens.add(Token.CL_PAREN);
            }
            if (c == '+') {
                strings.add("+");
                tokens.add(Token.PLUS);
            }
            if (c == '-') {
                strings.add("-");
                tokens.add(Token.MINUS);
            }
            if (c == ';') {
                strings.add(";");
                tokens.add(Token.SEMICOLON);
            }
            boolean isLastOrNotEqual = i + 1 >= input.length() || input.charAt(i + 1) != '=';
            if (c == '=') {
                strings.add(isLastOrNotEqual ? "=" : "==");
                tokens.add(isLastOrNotEqual ? Token.ASSIGN : Token.EQUALS);
            }
            if (c == '<') {
                strings.add(isLastOrNotEqual ? "<" : "<=");
                tokens.add(isLastOrNotEqual ? Token.LESS : Token.LESS_EQ);
            }
            if (c == '>') {
                strings.add(isLastOrNotEqual ? ">" : ">=");
                tokens.add(isLastOrNotEqual ? Token.GREATER : Token.GREATER_EQ);
            }
            if (c == '!') {
                strings.add(isLastOrNotEqual ? "!" : "!=");
                tokens.add(isLastOrNotEqual ? Token.ERROR : Token.DIFFERENT);
            }
            if (Character.isAlphabetic(c)) {
                stringBuilder.append(c);
                if (i + 1 >= input.length() || !Character.isAlphabetic(input.charAt(i + 1))) {
                    String string = stringBuilder.toString();
                    strings.add(string);
                    tokens.add(reservedWordsMap.getOrDefault(string, Token.IDENTIFIER));
                    stringBuilder = new StringBuilder();
                }
            }
            if (Character.isDigit(c)) {
                stringBuilder.append(c);
                if (i + 1 >= input.length() || !Character.isDigit(input.charAt(i + 1))) {
                    String string = stringBuilder.toString();
                    strings.add(string);
                    tokens.add(Token.NUMBER);stringBuilder = new StringBuilder();
                }
            }
        }
        for (var s : tokens) {
            System.out.println(s);
        }
        return new Pair<>(strings, tokens);
    }

    public Boolean[] checkReservedWords(Token[] tokens) {
        Boolean[] reservedWords = new Boolean[tokens.length];
        for (int i = 0; i < reservedWords.length; i++) {
            reservedWords[i] = reservedWordsSet.contains(tokens[i]);
        }
        return reservedWords;
    }

    public Boolean[] checkReservedWords2(ArrayList<Token> tokens) {
        Boolean[] reservedWords = new Boolean[tokens.size()];
        for (int i = 0; i < reservedWords.length; i++) {
            reservedWords[i] = reservedWordsSet.contains(tokens.get(i));
        }
        return reservedWords;
    }
}
