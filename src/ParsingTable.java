import Common.Production;
import Common.Symbols.Symbol;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

class ParsingTable {
    private HashMap<Symbol, HashMap<Symbol, Production>> table =
            new HashMap<>();

    ParsingTable() {
    }

    Production lookup(Symbol nonTerminal, String terminalName) {
        HashMap<Symbol, Production> row = table.get(nonTerminal);
        if (row == null)
            return null;

        Symbol match = null;
        for (Symbol symbol : row.keySet()) {
            if (Objects.equals(symbol.getName(), terminalName)) {
                match = symbol;
                break;
            }
        }
        if (match == null)
            return null;

        return row.get(match);
    }

    void setCell(Symbol nonTerminal, Symbol terminal, Production production) {
        if (!table.containsKey(nonTerminal)) {
            table.put(nonTerminal, new HashMap<>());
        }
        HashMap<Symbol, Production> row = table.get(nonTerminal);

        if (!row.containsKey(terminal)) {
            row.put(terminal, production);
        } else {
            String errorMessage = String.format(
                    "Duplicate rule at: (%s, %s)\n Existing rule: %s\n New rule %s",
                    nonTerminal, terminal, row.get(terminal), production);
            throw new Error(errorMessage);
        }
    }
}
