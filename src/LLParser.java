import Common.Lexeme;
import Common.Production;
import Common.Symbols.EmptySymbol;
import Common.Symbols.EndSymbol;
import Common.Symbols.Symbol;
import Common.SyntaxNode;

import java.util.*;

class LLParser {

    private final Symbol startingSymbol;
    /**
     * Common.Symbols of the language
     */
    private final List<Symbol> symbols;

    /**
     * Language productions
     */
    private final List<Production> productions;

    /**
     * Map of first sets for symbols
     */
    private Map<Symbol, Set<Symbol>> firstSetMap = new HashMap<>();

    /**
     * Map of follow sets for symbols
     */
    private Map<Symbol, Set<Symbol>> followSetMap = new HashMap<>();

    private ParsingTable table = new ParsingTable();

    /**
     * @param symbols Language symbols
     */
    LLParser(List<Symbol> symbols, List<Production> productions) {
        this.symbols = symbols;
        this.productions = productions;
        this.startingSymbol = symbols.get(0);

        removeLeftRecursion();
        generateFirstSets();
        generateFollowSets();
        generateParsingTable();
    }

    private static String unpackName(String lexeme) {
        String[] parts = lexeme.split(",");
        parts[0] = parts[0].substring(1, parts[0].length());
        return parts[0];
    }

    SyntaxNode getSyntaxAnalysisTree(List<String> lexemes) {
        SyntaxNode root;

        Stack<Symbol> stack = new Stack<>();
        stack.push(EndSymbol.getInstance());
        stack.push(startingSymbol);

        root = new SyntaxNode(startingSymbol);
        Stack<SyntaxNode> treeStack = new Stack<>();
        treeStack.push(new SyntaxNode());
        treeStack.push(root);

        while (!stack.empty()) {
            Symbol x = stack.pop();
            String a = lexemes.get(0);

            SyntaxNode currentNode = treeStack.pop();

            if (x.isTerminal() || x == EndSymbol.getInstance()) {
                if (Objects.equals(x.getName(), unpackName(a))) {
                    lexemes.remove(0);
                    currentNode.lexeme = new Lexeme(x, unpackData(a));
                } else
                    throw new Error("Invalid syntax");
            } else {
                Production prediction = table.lookup(x, unpackName(a));
                if (prediction == null)
                    throw new Error("Invalid syntax");
                List<Symbol> rhs = new ArrayList<>(
                        prediction.getRightHandSide());

                Collections.reverse(rhs);
                for (Symbol symbol : rhs) {
                    currentNode.children.add(
                            new SyntaxNode(symbol));
                }

                if (!prediction.isEmpty()) {
                    treeStack.addAll(currentNode.children);
                } else {
                    currentNode.empty = true;
                }
                Collections.reverse(currentNode.children);
                stack.addAll(rhs);
            }
        }
        if (lexemes.size() != 0) {
            System.out.println("Expected end of file, but not found");
        }

        return root;
    }

    private String unpackData(String a) {
        String[] parts = a.split(",");
        return parts[1].substring(0, parts[1].length() - 1).trim();
    }

    private void generateParsingTable() {
        for (Production production : productions) {
            Symbol lhs = production.getLeftHandSide();
            if (lhs.isTerminal())
                continue;

            List<Symbol> rhs = production.getRightHandSide();
            Set<Symbol> firstSet = getFirstSet(rhs);
            for (Symbol symbol : firstSet) {
                if (!symbol.isTerminal())
                    continue;
                table.setCell(lhs, symbol, production);
            }

            if (!firstSet.contains(EmptySymbol.getInstance()))
                continue;

            Set<Symbol> followSet = getFollowSet(lhs);
            for (Symbol symbol : followSet) {
                if (!symbol.isTerminal())
                    continue;
                table.setCell(lhs, symbol, production);
            }
        }
    }

    private Set<Symbol> getFollowSet(Symbol symbol) {
        return followSetMap.get(symbol);
    }

    private void removeLeftRecursion() {
        List<Symbol> substituteSymbols = new ArrayList<>();
        for (Symbol symbol : symbols) {
            Symbol substitute = detectLeftRecursion(symbol);

            if (substitute == null)
                continue;

            substituteSymbols.add(substitute);

            List<Production> productions = symbol.getLeftHandProductions();
            for (Production production : productions) {
                production.getRightHandSide().add(substitute);
            }

            Production emptyProduction =
                    substitute.createProduction(EmptySymbol.getInstance());
            this.productions.add(emptyProduction);

        }
        symbols.addAll(substituteSymbols);
    }

    private Symbol detectLeftRecursion(Symbol symbol) {
        List<Production> substitutedProductions = new ArrayList<>();
        List<Production> productions = symbol.getLeftHandProductions();
        Symbol substitute = null;

        for (Production production : productions) {
            if (production.isEmpty())
                continue;
            Symbol lhs = production.getLeftHandSide();
            List<Symbol> rhs = production.getRightHandSide();

            if (lhs != rhs.get(0))
                continue;

            if (substitute == null) {
                substitute = new Symbol(lhs.getName() + "`", new ArrayList<>());
            }

            production.setLeftHandSide(substitute);
            rhs.remove(0);
            rhs.add(substitute);

            substitutedProductions.add(production);
        }

        if (substitute != null) {
            symbol.getLeftHandProductions().removeAll(substitutedProductions);
            substitute.addProductions(substitutedProductions);
        }

        return substitute;
    }

    /**
     * Assumes that left recursion is eliminated
     */
    private void generateFirstSets() {
        for (Symbol symbol : symbols) {
            if (firstSetMap.containsKey(symbol))
                continue;

            generateFirstSet(symbol);
        }
    }

    private void generateFollowSets() {
        for (Symbol symbol : symbols) {
            followSetMap.put(symbol, new HashSet<>());
        }

        generateTerminalFollows();
        followSetMap
                .get(startingSymbol)
                .add(EndSymbol.getInstance());

        boolean changes;
        do {
            changes = generateFollowingFollows();
        } while (changes);
    }

    private boolean generateFollowingFollows() {
        boolean changes = false;

        for (Production production : productions) {
            List<Symbol> rhs = production.getRightHandSide();
            for (int i = 0; i < rhs.size(); i++) {
                Symbol symbol = rhs.get(i);
                if (symbol.isTerminal() || symbol == EmptySymbol.getInstance())
                    continue;

                Set<Symbol> followSet = followSetMap.get(symbol);
                int initialSize = followSet.size();

                Set<Symbol> additionalSet =
                        getFirstSet(rhs.subList(i + 1, rhs.size()));

                if (additionalSet.contains(EmptySymbol.getInstance())) {
                    additionalSet.remove(EmptySymbol.getInstance());
                    Symbol lhs = production.getLeftHandSide();
                    followSet.addAll(followSetMap.get(lhs));
                }

                followSet.addAll(additionalSet);

                if (initialSize != followSet.size())
                    changes = true;
            }
        }

        return changes;
    }

    private Set<Symbol> getFirstSet(Symbol symbol) {
        return firstSetMap.get(symbol);
    }

    private Set<Symbol> getFirstSet(List<Symbol> symbols) {
        Set<Symbol> set = new HashSet<>();

        boolean empty = true;
        for (Symbol symbol : symbols) {
            empty = false;
            if (symbol.isTerminal()) {
                set.add(symbol);
                break;
            }
            Set<Symbol> firstSet = getFirstSet(symbol);

            if (firstSet.contains(EmptySymbol.getInstance()))
                empty = true;

            firstSet.remove(EmptySymbol.getInstance());
            set.addAll(firstSet);

            if (!empty)
                break;

            firstSet.add(EmptySymbol.getInstance());
        }

        if (empty)
            set.add(EmptySymbol.getInstance());

        return set;
    }

    private void generateTerminalFollows() {
        for (Symbol symbol : symbols) {
            if (symbol.isTerminal())
                continue;

            List<Production> productions = symbol.getRightHandProductions();
            for (Production production : productions) {
                List<Symbol> rhs = production.getRightHandSide();
                int idx = rhs.indexOf(symbol);
                if (idx == rhs.size() - 1)
                    continue;

                Symbol following = rhs.get(idx + 1);
                if (following.isTerminal())
                    followSetMap
                            .get(symbol)
                            .add(following);
            }
        }
    }

    private void generateFirstSet(Symbol symbol) {
        Set<Symbol> set = new HashSet<>();
        List<Production> productions = symbol.getLeftHandProductions();

        if (symbol.isTerminal()) {
            set.add(symbol);
            firstSetMap.put(symbol, set);
            return;
        }

        for (Production production : productions) {
            if (production.isEmpty()) {
                set.add(EmptySymbol.getInstance());
                continue;
            }

            Set<Symbol> productionSet = generateProductionFirstSet(production);
            set.addAll(productionSet);
        }
        firstSetMap.put(symbol, set);
    }

    private Set<Symbol> generateProductionFirstSet(Production production) {
        Set<Symbol> set = new HashSet<>();
        boolean empty = true;

        for (Symbol symbol : production.getRightHandSide()) {
            if (!firstSetMap.containsKey(symbol))
                generateFirstSet(symbol);

            Set<Symbol> firstSet = getFirstSet(symbol);
            if (!firstSet.contains(EmptySymbol.getInstance()))
                empty = false;

            firstSet.remove(EmptySymbol.getInstance());
            set.addAll(firstSet);

            if (empty)
                firstSet.add(EmptySymbol.getInstance());
            else
                break;
        }

        if (empty)
            set.add(EmptySymbol.getInstance());

        return set;
    }
}
