package Scanner;

import Common.Symbols.Symbol;

import javax.swing.plaf.nimbus.State;
import java.util.HashMap;
import java.util.Map;

public class StateNode {
    StateNode defaultTransition;
    Map<Character, StateNode> transitions = new HashMap<>();
    private String name;
    private Symbol symbol;
    private ScannerActions action = ScannerActions.Continue;

    public StateNode(String name) {
        this.name = name;
    }

    StateNode(Symbol symbol) {
        this.symbol = symbol;
    }

    public StateNode(
            String name,
            StateNode defaultTransition
    ) {
        this.name = name;
        this.defaultTransition = defaultTransition;
    }

    StateNode(
            String name,
            ScannerActions action
    ) {
        this.name = name;
        this.action = action;
    }

    StateNode(
            String name,
            ScannerActions action,
            StateNode defaultTransition
    ) {
        this.name = name;
        this.action = action;
        this.defaultTransition = defaultTransition;
    }

    public StateNode(
            Symbol symbol,
            ScannerActions action
    ) {
        this.symbol = symbol;
        this.action = action;
    }

    public StateNode(
            Symbol symbol,
            ScannerActions action,
            StateNode defaultTransition
    ) {
        this.symbol = symbol;
        this.action = action;
        this.defaultTransition = defaultTransition;
    }

    public String getName() {
        return symbol == null ? name : symbol.toString();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    ScannerActions getAction() {
        return action;
    }

    public StateNode setDefaultTransition(StateNode defaultTransition) {
        this.defaultTransition = defaultTransition;
        return this;
    }

    public StateNode addTransitions(Map<Character, StateNode> transitions) {
        for (Character key : transitions.keySet()) {
            addTransition(key, transitions.get(key));
        }
        return this;
    }

    public StateNode addTransition(Character character, StateNode transition) {
        if (transitions.containsKey(character))
            throw new Error(String.format(
                    "Transition for character %c already exists", character));
        transitions.put(character, transition);
        return this;
    }

    public StateNode getTransition(Character character) {
        if (transitions.containsKey(character))
            return transitions.get(character);
        return defaultTransition;
    }
}
