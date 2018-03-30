package Common.Symbols;

/**
 * Singleton representing an empty symbol
 */
public class EmptySymbol extends Symbol {
    /**
     * Singleton instance
     */
    private static EmptySymbol instance;

    /**
     * Special empty symbol constructor
     */
    private EmptySymbol() {
        super();
    }

    /**
     * @return Class instance
     */
    public static EmptySymbol getInstance() {
        if (instance == null)
            instance = new EmptySymbol();

        return instance;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    /**
     * @return String representation
     */
    @Override
    public String toString() {
        return "ε";
    }
}
