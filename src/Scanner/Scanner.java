package Scanner;

import Common.Lexeme;

import java.io.*;

public class Scanner {
    private final BufferedReader reader;
    private StateNode currentState;
    private Lexeme bufferedLexeme;
    private Character character;
    private StringBuilder data = new StringBuilder();

    public Scanner(
            FileInputStream stream,
            StateNode rootState
    ) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        this.reader = new BufferedReader(reader);
        this.currentState = rootState;
        readNextCharacter();
        bufferLexeme();
    }

    public boolean hasLexeme() {
        return bufferedLexeme != null;
    }

    public Lexeme getLexeme() throws IOException {
        Lexeme lexeme = bufferedLexeme;
        bufferedLexeme = null;
        bufferLexeme();
        return lexeme;
    }

    private void bufferLexeme() throws IOException {
        while (true) {
            if (character == null)
                break;

            updateState();
            takeAction();

            if (bufferedLexeme != null)
                break;
        }
    }

    private void updateState() {
        StateNode newState = currentState.getTransition(character);
        if (newState == null)
            throw new Error("Unexpected symbol: '" + character + "'");
        currentState = newState;
    }

    private void takeAction() throws IOException {
        ScannerActions action = currentState.getAction();

        // TODO: Concentrate the steps of completing each action into this switch
        switch (action) {
            case SkipInput:
                break;
            case ResetData:
                Continue();
                data = new StringBuilder();
                break;
            case ResetDataSkipInput:
                data = new StringBuilder();
                break;
            case Continue:
                Continue();
                break;
            case Output:
                OutputAction();
                break;
            case OutputAndSkipInput:
                OutputAndSkipInput();
                break;
            case Stop:
                OutputAction();
                character = null;
        }
    }

    private void Continue() throws IOException {
        collectData();
        readNextCharacter();
    }

    private void OutputAction() throws IOException {
        data.append(character);
        bufferedLexeme = new Lexeme(currentState.getSymbol(), data.toString());
        data = new StringBuilder();
        readNextCharacter();
    }

    private void OutputAndSkipInput() {
        bufferedLexeme = new Lexeme(currentState.getSymbol(), data.toString());
        data = new StringBuilder();
    }

    private void collectData() {
        data.append(character);
    }

    private void readNextCharacter() throws IOException {
        int charValue = reader.read();
        character = (char) charValue;
    }
}

