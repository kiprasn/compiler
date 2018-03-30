package Common.Symbols;

/**
 * Special symbol to indicate end of stream
 */
public class EndSymbol extends Symbol {
    private static EndSymbol instance;

    private EndSymbol() {
    }

    public static EndSymbol getInstance() {
        if (instance == null)
            instance = new EndSymbol();

        return instance;
    }

    @Override
    public String getName() {
        return "$";
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public String toString() {
        return "$";
    }
}
