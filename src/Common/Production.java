package Common;

import Common.Symbols.EmptySymbol;
import Common.Symbols.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for a production
 */
public class Production {
    /**
     * Left hand side of a production
     */
    private Symbol leftHandSide;

    /**
     * Right hand sice of a production
     */
    private final List<Symbol> rightHandSide;

    /**
     * Constructor for an empty production
     * @param leftHandSide
     */
    public Production(Symbol leftHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = new ArrayList<>();
    }

    /**
     * @param leftHandSide  Left hand side
     * @param rightHandSide Right hand side
     */
    public Production(Symbol leftHandSide, List<Symbol> rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    /**
     * @param leftHandSide  Left hand side
     * @param rightHandSide Single symbol for right side
     */
    public Production(Symbol leftHandSide, Symbol rightHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = new ArrayList<Symbol>() {{
            add(rightHandSide);
        }};
    }

    /**
     * @return Left side of the production
     */
    public Symbol getLeftHandSide() {
        return leftHandSide;
    }

    public void setLeftHandSide(Symbol leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    /**
     * @return Right side of the production
     */
    public List<Symbol> getRightHandSide() {
        return rightHandSide;
    }

    /**
     * @return Whether this production is empty
     */
    public boolean isEmpty() {
        return rightHandSide.size() == 0;
    }

    /**
     * @return String representation of this production
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(leftHandSide.getName());
        stringBuilder.append(": ");

        if (isEmpty()) {
            stringBuilder.append(EmptySymbol.getInstance());
            return stringBuilder.toString();
        }

        for (Symbol symbol : rightHandSide) {
            stringBuilder
                    .append(symbol)
                    .append(", ");
        }

        int end = stringBuilder.length();
        stringBuilder.delete(end - 2, end);

        return stringBuilder.toString();
    }
}
