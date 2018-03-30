package EBNF;

import Common.Lexeme;
import Common.Production;
import Common.Symbols.Symbol;
import Scanner.Scanner;

import java.io.IOException;
import java.util.*;

public class EBNFParser {
    private Scanner scanner;

    private Map<String, Symbol> symbols = new HashMap<>();

    private List<Production> productions = new ArrayList<>();

    EBNFParser(EBNFScanner scanner) {
        this.scanner = scanner;
    }

    public List<Production> getProductions() {
        return productions;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }

    public List<Symbol> getSymbolList() {
        List<Symbol> list = new ArrayList<>();
        for (String key : symbols.keySet()) {
            list.add(symbols.get(key));
        }
        return list;
    }

    private void parse() throws IOException {
        Lexeme lexeme;

        while ((lexeme = scanner.getLexeme()) != null) {
        }
    }
}
