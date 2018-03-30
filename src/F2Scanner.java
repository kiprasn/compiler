import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LexemePattern {
    String name = null;
    private Pattern pattern = null;
    private Matcher matcher = null;

    LexemePattern(String regex, String name) {
        this.pattern = Pattern.compile("^" + regex);
        this.name = name;
    }

    boolean matcher(String line) {
        this.matcher = this.pattern.matcher(line);
        return this.matcher.find();
    }

    String newState() {
        return this.name;
    }

    String group() {
        return this.matcher.group(0);
    }
}

class LexerConfig {
    private static ArrayList<LexemePattern> parseState(
            BufferedReader reader) throws IOException {
        String line;
        ArrayList<LexemePattern> rules = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0)
                break;
            String parts[] = line.split("\\s+");
            rules.add(new LexemePattern(parts[0], parts[1]));

        }
        return rules;
    }

    static Map<String, ArrayList<LexemePattern>> parseRules(String filename) throws IOException {
        BufferedReader ruleFile = new BufferedReader(new FileReader(filename));
        HashMap<String, ArrayList<LexemePattern>> map = new HashMap<>();

        String line;
        Scanner nameScanner;
        while ((line = ruleFile.readLine()) != null) {
            if (line.length() == 0)
                continue;

            nameScanner = new Scanner(line);
            if (nameScanner.hasNext()) {
                String stateName = nameScanner.next();
                ArrayList<LexemePattern> stateRules =
                        LexerConfig.parseState(ruleFile);
                map.put(stateName, stateRules);
            }
        }

        return map;
    }
}

class F2Scanner {
    private BufferedReader bufferedReader = null;
    private String currentLine = null;
    private Map<String, ArrayList<LexemePattern>> map = null;
    private String currentState = "Start";

    private int lineCounter = 0;
    private int charCounter = 0;

    private F2Scanner(String filename, String filenameRules) throws IOException {
        FileReader fileReader = new FileReader(filename);
        this.bufferedReader = new BufferedReader(fileReader);
        this.map = LexerConfig.parseRules(filenameRules);
    }

    /**
     * @param codeFile      File containing the code to scan
     * @param filenameRules File containing the rules for scanning
     * @throws IOException Rule file might be inaccessible
     */
    F2Scanner(File codeFile, String filenameRules) throws IOException {
        FileReader codeReader = new FileReader(codeFile);

        this.bufferedReader = new BufferedReader(codeReader);
        this.map = LexerConfig.parseRules(filenameRules);
    }

    public static void main(String[] args) {
        try {
            F2Scanner scanner = new F2Scanner(args[0], "rules.txt");
            String lexeme;
            while ((lexeme = scanner.getLexeme()) != null) {
                System.out.println(lexeme);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    String getLexeme() throws Exception {
        if (this.currentLine == null) {
            try {
                this.readLine();
            } catch (Exception e) {
                return null;
            }
        }

        this.trimCurrentLine();
        return this.consumeLexeme();
    }

    private void trimCurrentLine() {
        Pattern whitespacePattern = Pattern.compile("^\\s*");
        Matcher matcher = whitespacePattern.matcher(this.currentLine);

        if (matcher.find()) {
            int whitespaceLength = matcher.group(0).length();
            this.cutCurrentLine(whitespaceLength);
        }
    }

    private void readLine() throws Exception {
        lineCounter += 1;
        charCounter = 0;
        this.currentLine = this.bufferedReader.readLine();

        if (this.currentLine == null)
            throw new Exception("End of file");

        while (this.currentLine.length() == 0) {
            this.currentLine = this.bufferedReader.readLine();

            if (this.currentLine == null)
                throw new Exception("End of file");
        }
    }

    private void skipComment(String endState) throws Exception {
        ArrayList<LexemePattern> statePatterns =
                this.map.get(this.currentState);

        if (this.currentLine == null)
            this.readLine();

        while (!Objects.equals(this.currentState, endState)) {
            for (LexemePattern pattern : statePatterns) {
                if (pattern.matcher(this.currentLine)) {
                    this.handlePatternMatch(pattern);
                    if (this.currentLine == null)
                        this.readLine();
                    this.currentState = "CommentEnd";
                    return;
                }
            }
            this.readLine();
        }
    }

    private String handleString() throws Exception {
        try {
            if (this.currentLine == null)
                this.readLine();
        } catch (Exception e) {
            throw new Exception("Unterminated String");
        }

        ArrayList<LexemePattern> statePatterns =
                this.map.get("String");

        StringBuilder data = new StringBuilder();
        while (true) {
            boolean matched = false;
            for (LexemePattern pattern : statePatterns) {
                if (pattern.matcher(this.currentLine)) {
                    String matchGroup = pattern.group();
                    String newState = pattern.newState();
                    int matchLength = matchGroup.length();

                    if (!Objects.equals(newState, "StringEnd")) {
                        data.append(matchGroup);
                        this.cutCurrentLine(matchLength);
                        if (this.currentLine == null)
                            this.readLine();
                        matched = true;
                    } else {
                        this.cutCurrentLine(matchLength);
                        return data.toString();
                    }
                }
            }

            if (this.currentLine == null ||
                    this.currentLine.length() == 0)
                this.readLine();

            if (!matched)
                throw new Exception("Unterminated String");
        }
    }

    private String consumeLexeme() throws Exception {
        if (Objects.equals(this.currentState, "CommentStart")) {
            this.skipComment("CommentEnd");
            return null;
        }

        if (Objects.equals(this.currentState, "String")) {
            String stringData = this.handleString();
            this.resetState();
            return "(String, " + stringData + ")";
        }

        ArrayList<LexemePattern> statePatterns =
                this.map.get(this.currentState);

        if (this.currentLine == null)
            this.readLine();

        for (LexemePattern pattern : statePatterns) {
            if (pattern.matcher(this.currentLine)) {
                String match = this.handlePatternMatch(pattern);
                if (match == null)
                    return this.getLexeme();
                return match;
            }
        }

        if (Objects.equals(this.currentState, "Start")) {
            throw new Exception(
                    MessageFormat.format(
                            "Unknown lexeme at {0}, {1}: {2}",
                            this.lineCounter,
                            this.charCounter,
                            this.currentLine));
        } else {
            return null;
        }
    }

    private void cutCurrentLine(int length) {
        charCounter += length;
        if (this.currentLine.length() == length) {
            this.currentLine = null;
        } else {
            this.currentLine =
                    this.currentLine.substring(length);
        }
    }

    private String handlePatternMatch(LexemePattern pattern) throws Exception {
        String matchGroup = pattern.group();
        String newState = pattern.newState();
        int matchLength = matchGroup.length();
        this.cutCurrentLine(matchLength);

        if (this.map.containsKey(newState)) {
            this.currentState = newState;

            String extendedLexeme = this.consumeLexeme();
            if (extendedLexeme != null) {
                String[] parts = extendedLexeme.split(", ");
                if (Objects.equals(parts[0], "(CommentSingleLine"))
                    return null;
                if (Objects.equals(parts[0], "(CommentStart"))
                    return null;
                if (Objects.equals(parts[0], "(String"))
                    return extendedLexeme;

                extendedLexeme = parts[0] + ", " + matchGroup + parts[1];
                return extendedLexeme;
            } else {
                this.resetState();
                return "(" + pattern.name + ", " + matchGroup + ")";
            }
        } else {
            this.resetState();
            return "(" + pattern.name + ", " + matchGroup + ")";
        }
    }

    private void resetState() {
        this.currentState = "Start";
    }
}
