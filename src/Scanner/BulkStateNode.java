package Scanner;

import Common.Symbols.Symbol;

public class BulkStateNode extends StateNode {
    private StateNode alphabeticTransition;
    private StateNode digitTransition;
    private StateNode whitespaceTransition;

    public BulkStateNode(String name) {
        super(name);
    }

    public BulkStateNode(Symbol symbol) {
        super(symbol);
    }

    public BulkStateNode(String name, ScannerActions action) {
        super(name, action);
    }

    public BulkStateNode(
            String name,
            ScannerActions action,
            StateNode defaultTransition
    ) {
        super(name, action, defaultTransition);
    }

    public BulkStateNode(Symbol symbol, ScannerActions action) {
        super(symbol, action);
    }

    public BulkStateNode setAlphabeticTransition(
            StateNode alphabeticTransition
    ) {
        this.alphabeticTransition = alphabeticTransition;
        return this;
    }

    public BulkStateNode setDigitTransition(StateNode digitTransition) {
        this.digitTransition = digitTransition;
        return this;
    }

    public BulkStateNode setWhitespaceTransition(
            StateNode whitespaceTransition
    ) {
        this.whitespaceTransition = whitespaceTransition;
        return this;
    }

    @Override
    public StateNode getTransition(Character character) {
        StateNode transition = super.transitions.get(character);
        if (transition != null)
            return transition;

        if (Character.isAlphabetic(character))
            transition = alphabeticTransition;

        if (Character.isDigit(character))
            transition = digitTransition;

        if (Character.isWhitespace(character))
            transition = whitespaceTransition;

        if (transition == null)
            transition = super.defaultTransition;

        return transition;
    }
}
