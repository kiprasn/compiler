package Common;

import Common.Symbols.Symbol;

public class Lexeme {
    private final Symbol symbol;
    private String data;

    Lexeme(Symbol symbol) {
        this.symbol = symbol;
    }

    public Lexeme(Symbol symbol, String data) {
        this.symbol = symbol;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "(" + symbol.toString() + (data == null ? "" : ", " + data) + ")";
    }
}
