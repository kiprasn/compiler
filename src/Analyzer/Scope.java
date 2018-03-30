package Analyzer;

import Analyzer.Types.Type;

import java.util.HashMap;
import java.util.Map;

class Scope {
    private Map<String, Type> symbols = new HashMap<>();
    private Scope parentScope = null;

    Scope() {}

    Scope(Scope parent) {
        this.parentScope = parent;
    }

    void insertSymbol(String identifier, Type type) {
        if (symbols.containsKey(identifier))
            throw new Error("Identifier already exists");

        symbols.put(identifier, type);
    }

    Type lookupSymbol(String identifier) {
        if (symbols.containsKey(identifier))
            return symbols.get(identifier);
        if (parentScope != null)
            return parentScope.lookupSymbol(identifier);
        return null;
    }

    boolean hasIdentifier(String identifier) {
        return symbols.containsKey(identifier);
    }
}
