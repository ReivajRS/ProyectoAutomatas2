package SyntacticAnalysis;

import Utilities.Token;
import Utilities.SymbolData;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, SymbolData> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public boolean hasSymbol(String id) {
        return symbols.containsKey(id);
    }

    public void addSymbol(String id, Token dataType, int position) {
        symbols.put(id, new SymbolData(dataType, position));
    }

    public SymbolData getSymbol(String id) {
        return symbols.get(id);
    }

    public boolean isAvailable(String id, int position) {
        if (!hasSymbol(id)) {
            return false;
        }
        return position <= symbols.get(id).position();
    }
}
