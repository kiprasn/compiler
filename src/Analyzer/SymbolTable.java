package Analyzer;

import Analyzer.Types.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

class SymbolTable {
    private Stack<Scope> scopeStack = new Stack<>();

    private HashMap<String, List<Type>> functionArguments = new HashMap<>();
    private HashMap<String, Type> functionReturns = new HashMap<>();

    SymbolTable() {
        scopeStack.push(new Scope());
    }

    void insertSymbol(String identifier, Type type) {
        if (!identifierAvailable(identifier))
            throw new Error("Duplicate declaration of " + identifier);
        Scope topScope = scopeStack.peek();
        topScope.insertSymbol(identifier, type);
    }

    private boolean identifierAvailable(String identifier) {
        Scope topScope = scopeStack.peek();
        return !topScope.hasIdentifier(identifier);
    }

    Type lookupSymbol(String identifier) {
        Scope topScope = scopeStack.peek();
        return topScope.lookupSymbol(identifier);
    }

    void insertFunction(String identifier, List<Type> arguments, Type returnType) {
        if (lookupFunctionReturn(identifier) != null)
            throw new Error("Duplicate function declaration of " + identifier);
        functionArguments.put(identifier, arguments);
        functionReturns.put(identifier, returnType);
    }

    Type lookupFunctionReturn(String identifier) {
        return functionReturns.get(identifier);
    }

    List<Type> lookupFunctionArguments(String identifier) {
        return functionArguments.get(identifier);
    }

    void popStack() {
        scopeStack.pop();
    }

    void pushScope() {
        //System.out.println("-------- New Scope ---------");
        Scope topScope = scopeStack.peek();
        scopeStack.push(new Scope(topScope));
    }
}
