package EBNF;

import Common.Production;
import Common.Symbols.Symbol;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Class to load EBNF files into program usable data structures
 */
public class EBNFLoader {
    /**
     * Rule hash map
     */
    private final HashMap<String, List<List<Symbol>>> rules = new HashMap<>();
    /**
     * File reader
     */
    private final FileInputStream fileInputStream;
    /**
     * Map for symbols from corresponding names
     */
    private final Map<String, Symbol> symbolMap = new HashMap<>();

    /**
     * List of productions
     */
    private final List<Production> productions = new ArrayList<>();

    private List<Symbol> symbols = new ArrayList<>();

    /**
     * @param ruleFileStream File to load the rules from
     * @throws IOException FileReader
     */
    public EBNFLoader(FileInputStream ruleFileStream) throws Exception {
        fileInputStream = ruleFileStream;
        parseRules();
        // FIXME: Join all common terminals together
        // FIXME: Single terminal constructions are terminals
    }

    /**
     * @return A list of productions
     */
    public List<Production> getProductions() {
        return productions;
    }
    /**
     * Parse rules from file to populate rules data structure
     *
     * @throws Exception reader
     */
    private void parseRules() throws Exception {
        // TODO: Opportunity to optimize by removing regex and parsing
        Scanner scanner = new Scanner(fileInputStream);
        scanner.useDelimiter(";(?![\"'])");

        // FIXME: Make identifier a terminal
        while (scanner.hasNext()) {
            String ruleString = scanner.next().trim();
            parseRule(ruleString);
        }
    }

    private void parseRule(String ruleString) throws Exception {
        ruleString = ruleString.trim();
        if (ruleString.length() == 0)
            return;

        List<String> parts = escapedSplit(ruleString, ':');
        String ruleName = parts.get(0).trim();

        List<List<Symbol>> extensions = parseExtensions(parts.get(1));
        Symbol lhs = addNonTerminal(ruleName, extensions);
        symbols.add(lhs);
        for (List<Symbol> extension : extensions) {
            Production production = new Production(lhs, extension);
            productions.add(production);
            lhs.addLeftHandProduction(production);
            List<Symbol> rhs = production.getRightHandSide();
            for (Symbol symbol : rhs) {
                symbol.addRightHandProduction(production);
            }
        }

        if (rules.containsKey(ruleName))
            throw new Exception("Duplicate rule: " + ruleName);

        rules.put(ruleName, extensions);
    }

    private Symbol addTerminal(String text) {
        Symbol terminal = new Symbol(text);
        symbolMap.put(
                Integer.toString(terminal.hashCode()),
                terminal);
        return terminal;
    }

    private Symbol addNonTerminal(String name, List<List<Symbol>> extensions) {
        if (name.charAt(0) == '{' || name.charAt(0) == '[')
            name = name.substring(1, name.length() - 1);

        Symbol nonTerminal;
        if (symbolMap.containsKey(name)) {
            nonTerminal = symbolMap.get(name);
            if (extensions != null)
                nonTerminal.setExtensions(extensions);
        } else {
            nonTerminal = new Symbol(name, extensions);
            symbolMap.put(name, nonTerminal);
        }
        return nonTerminal;
    }

    private List<List<Symbol>> parseExtensions(String extensionsString) {
        List<String> parts = escapedSplit(extensionsString, '|');
        List<List<Symbol>> extensions = new ArrayList<>();

        for (String part : parts) {
            List<Symbol> extension = parseExtension(part);
            extensions.add(extension);
        }

        return extensions;
    }

    private List<Symbol> parseExtension(String extensionString) {
        if (extensionString.length() == 0)
            return new ArrayList<>();

        List<String> list = escapedSplit(extensionString, ',');
        List<Symbol> extension = new ArrayList<>();

        // FIXME: Add productions for new symbols made here
        for (String item : list) {
            Symbol newSymbol;
            if (item.charAt(0) == '\'' || item.charAt(0) == '"') {
                String text = item.substring(1, item.length() - 1);
                newSymbol = addTerminal(text);
            } else if (item.charAt(0) == '{' &&
                    item.charAt(item.length() - 1) == '}') {
                item = item.substring(1, item.length() - 1);

                Symbol optional = addNonTerminal(item, null);
                // TODO: Don't assign any names as they might conflict with the ebnf file
                newSymbol = addNonTerminal(item + "`", null);

                List<List<Symbol>> rules = new ArrayList<>();
                List<Symbol> rule1 = new ArrayList<>();

                rule1.add(optional);
                rule1.add(newSymbol);

                rules.add(rule1);
                rules.add(new ArrayList<>());

                List<Production> newProductions = new ArrayList<Production>() {{
                    add(new Production(newSymbol, rule1));
                    add(new Production(newSymbol));
                }};

                productions.addAll(newProductions);
                newSymbol.addProductions(newProductions);

                newSymbol.setExtensions(rules);
                symbols.add(newSymbol);
            } else if (item.charAt(0) == '[' &&
                    item.charAt(item.length() - 1) == ']') {
                item = item.substring(1, item.length() - 1);
                newSymbol = addNonTerminal(item, null);
                Symbol optional = addNonTerminal(item + "`", null);

                List<List<Symbol>> rules = new ArrayList<>();
                List<Symbol> rule1 = new ArrayList<>();

                rule1.add(newSymbol);

                rules.add(rule1);
                rules.add(new ArrayList<>());

                optional.setExtensions(rules);
            } else {
                newSymbol = addNonTerminal(item, null);
            }
            extension.add(newSymbol);
        }

        return extension;
    }

    private List<String> escapedSplit(String target, Character splitChar) {
        List<String> tokensList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean doubleQuotes = false;
        boolean singleQuotes = false;

        for (char c : target.toCharArray()) {
            if (Character.isWhitespace(c))
                continue;

            if (c == splitChar) {
                if (!(singleQuotes || doubleQuotes)) {
                    tokensList.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    continue;
                }
            }

            if (c == '\'' && !doubleQuotes)
                singleQuotes = !singleQuotes;
            if (c == '"' && !singleQuotes)
                doubleQuotes = !doubleQuotes;

            stringBuilder.append(c);
        }

        String lastString = stringBuilder.toString();

        if (doubleQuotes || singleQuotes)
            throw new Error("Unterminated string: " + lastString);

        tokensList.add(lastString);
        return tokensList;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }
}
