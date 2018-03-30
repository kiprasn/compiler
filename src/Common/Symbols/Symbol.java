package Common.Symbols;

import Common.Production;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    /**
     * Productions where the symbol is on the left hand side
     */
    private final List<Production> leftHandProductions = new ArrayList<>();
    /**
     * Productions where the symbol is on the right hand side
     */
    private final List<Production> rightHandProductions = new ArrayList<>();
    /**
     * Common.Symbols.Symbol name
     */
    private String name;
    /**
     * List of extensions if symbol is not a terminal
     */
    private List<List<Symbol>> extensions;
    /**
     * Common.Symbols.Symbol text if it is a terminal
     */
    private String text;

    /**
     * Constructor for empty symbol
     */
    Symbol() {
    }

    /**
     * Terminal symbol constructor
     *
     * @param text Text of the symbol if it is non terminal
     */
    public Symbol(String text) {
        this.text = text;
    }

    /**
     * Constructor for non terminal symbol
     *
     * @param name       Name of symbol
     * @param extensions List of extensions
     */
    public Symbol(String name, List<List<Symbol>> extensions) {
        this.name = name;
        this.extensions = extensions;
    }

    public String getName() {
        return name;
    }

    public List<Production> getLeftHandProductions() {
        return leftHandProductions;
    }

    public void addLeftHandProduction(Production production) {
        leftHandProductions.add(production);
    }

    public List<Production> getRightHandProductions() {
        return rightHandProductions;
    }

    /**
     * Create a new production with a single substitute
     *
     * @param symbol Right hand side symbol
     */
    public Production createProduction(Symbol symbol) {
        Production production = new Production(this, symbol);
        this.addRightHandProduction(production);
        return production;
    }

    public void addRightHandProduction(Production production) {
        rightHandProductions.add(production);
    }

    public void setExtensions(List<List<Symbol>> extensions) {
        if (this.extensions != null)
            throw new Error("Cannot reset extensions for " + name);
        this.extensions = extensions;
    }

    private boolean isRealTerminal() {
        return text != null;
    }

    public boolean isTerminal() {
        if (extensions == null)
            return text != null;
        if (extensions.size() == 1 && extensions.get(0).size() == 1) {
            if (extensions.get(0).get(0).isRealTerminal())
                return true;
        }
        return text != null;
    }

    public String toString() {
        if (isTerminal()) {
            if (text == null)
                return "'" + extensions.get(0).get(0).text + "'";
            return "'" + text + "'";
        } else {
            return name;
        }
    }

    public void addProductions(List<Production> substitutedProductions) {
        leftHandProductions.addAll(substitutedProductions);
        rightHandProductions.addAll(substitutedProductions);
    }
}
