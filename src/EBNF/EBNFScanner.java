package EBNF;

import Scanner.BulkStateNode;
import Scanner.Scanner;
import Scanner.ScannerActions;
import Scanner.StateNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class EBNFScanner extends Scanner {
    private static StateNode rootState;

    EBNFScanner(FileInputStream stream) throws IOException {
        super(stream, generateStateMap());
    }

    private static StateNode generateStateMap() {
        if (rootState != null)
            return rootState;

        BulkStateNode root = new BulkStateNode(
                "Root",
                ScannerActions.ResetDataSkipInput);
        StateNode endOfFile = new StateNode(
                EBNFSymbols.EndOfFile.getValue(),
                ScannerActions.Stop);

        root.setWhitespaceTransition(generateWhitespaceStates(root));
        root.setAlphabeticTransition(generateIdentifierStates(root));
        root.addTransitions(generateBracketStates(root));
        root.addTransitions(generateSymbolStates(root));
        root.addTransitions(generateStringStates(root));

        root.addTransition((char) -1, endOfFile);

        rootState = root;
        return rootState;
    }

    private static Map<Character, StateNode> generateStringStates(
            BulkStateNode root
    ) {
        // TODO: Add string escaping
        // TODO: Error on unfinished strings
        StateNode singleQuoteStringStart =
                new StateNode("SingleQuoteStringStart");
        StateNode singleQuoteStringLoop =
                new StateNode("SingleQuoteStringLoop");
        StateNode singleQuoteStringOutput =
                new StateNode(
                        EBNFSymbols.String.getValue(),
                        ScannerActions.Output,
                        root
                );

        singleQuoteStringStart.setDefaultTransition(singleQuoteStringLoop);
        singleQuoteStringLoop
                .setDefaultTransition(singleQuoteStringLoop)
                .addTransition('\'', singleQuoteStringOutput);

        StateNode doubleQuoteStringStart =
                new StateNode("DoubleQuoteStringStart");
        StateNode doubleQuoteStringLoop =
                new StateNode("DoubleQuoteStringLoop");
        StateNode doubleQuoteStringOutput =
                new StateNode(
                        EBNFSymbols.String.getValue(),
                        ScannerActions.Output,
                        root
                );

        doubleQuoteStringStart.setDefaultTransition(singleQuoteStringLoop);
        doubleQuoteStringLoop
                .setDefaultTransition(doubleQuoteStringLoop)
                .addTransition('\'', doubleQuoteStringOutput);

        Map<Character, StateNode> states = new HashMap<>();
        states.put('\'', singleQuoteStringStart);
        states.put('"', doubleQuoteStringStart);
        return states;
    }

    private static Map<Character, StateNode> generateSymbolStates(
            BulkStateNode root
    ) {
        Map<Character, StateNode> states = new HashMap<>();

        states.put(':', new StateNode(
                EBNFSymbols.Colon.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put(',', new StateNode(
                EBNFSymbols.Comma.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put(';', new StateNode(
                EBNFSymbols.Semicolon.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put('|', new StateNode(
                EBNFSymbols.Pipe.getValue(),
                ScannerActions.Output,
                root
        ));

        return states;
    }

    private static StateNode generateIdentifierStates(StateNode root) {
        BulkStateNode identifierLoop = new BulkStateNode("Identifier");
        StateNode identifierOut =
                new StateNode(
                        EBNFSymbols.Identifier.getValue(),
                        ScannerActions.OutputAndSkipInput,
                        root
                );
        identifierLoop
                .setAlphabeticTransition(identifierLoop)
                .setDigitTransition(identifierLoop)
                .addTransition('_', identifierLoop)
                .setDefaultTransition(identifierOut);

        return identifierLoop;
    }

    private static StateNode generateWhitespaceStates(StateNode root) {
        BulkStateNode whitespaceEnd = new BulkStateNode(
                "WhitespaceOut",
                ScannerActions.SkipInput,
                root);
        BulkStateNode whitespaceLoop =
                new BulkStateNode(
                        "WhitespaceLoop",
                        ScannerActions.ResetData,
                        whitespaceEnd
                );
        whitespaceLoop.setWhitespaceTransition(whitespaceLoop);
        return whitespaceLoop;
    }

    private static Map<Character, StateNode> generateBracketStates(
            StateNode root
    ) {
        HashMap<Character, StateNode> states = new HashMap<>();
        states.put('[', new StateNode(
                EBNFSymbols.LeftAngleBracket.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put(']', new StateNode(
                EBNFSymbols.RightAngleBracket.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put('{', new StateNode(
                EBNFSymbols.LeftCurlyBracket.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put('}', new StateNode(
                EBNFSymbols.RightCurlyBracket.getValue(),
                ScannerActions.Output,
                root
        ));

        states.put('(', new StateNode(
                EBNFSymbols.LeftBracket.getValue(),
                ScannerActions.Continue,
                root
        ));

        states.put(')', new StateNode(
                EBNFSymbols.RightBracket.getValue(),
                ScannerActions.Continue,
                root
        ));

        return states;
    }
}
