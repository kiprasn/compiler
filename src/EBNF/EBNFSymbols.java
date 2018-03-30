package EBNF;

import Common.Symbols.EndSymbol;
import Common.Symbols.Symbol;

public enum EBNFSymbols {
    LeftBracket(new Symbol("(", null)),
    RightBracket(new Symbol(")", null)),
    LeftAngleBracket(new Symbol("[", null)),
    RightAngleBracket(new Symbol("]", null)),
    LeftCurlyBracket(new Symbol("{", null)),
    RightCurlyBracket(new Symbol("}", null)),
    Identifier(new Symbol("identifier", null)),
    Comma(new Symbol(",", null)),
    Colon(new Symbol(":", null)),
    Semicolon(new Symbol(";", null)),
    String(new Symbol("string", null)),
    Pipe(new Symbol("|", null)),
    EndOfFile(EndSymbol.getInstance());

    private final Symbol value;

    EBNFSymbols(Symbol value) {
        this.value = value;
    }

    public Symbol getValue() {
        return value;
    }
}
