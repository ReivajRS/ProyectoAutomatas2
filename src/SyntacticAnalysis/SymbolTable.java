package SyntacticAnalysis;

import Utilities.Token;
import Utilities.SymbolData;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, SymbolData> symbols;
    private static int currentOffset;

    public SymbolTable() {
        symbols = new HashMap<>();
        // This first offset is after writing the template in binary code
        currentOffset = 23;
    }

    public boolean hasSymbol(String id) {
        return symbols.containsKey(id);
    }

    public SymbolData addSymbol(String id, Token dataType, int position) {
        symbols.put(id, new SymbolData(dataType, position, id + "_" + position, currentOffset));
        if (dataType == Token.INT) {
            currentOffset += 2;
        }
        else if (dataType == Token.BOOLEAN) {
            currentOffset++;
        }
        else if (dataType == Token.STRING) {
            currentOffset += 100;
        }
        return symbols.get(id);
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
