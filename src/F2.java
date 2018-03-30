import Analyzer.Analyzer;
import Common.SyntaxNode;
import EBNF.EBNFLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Main project class
 */
class F2 {
    /**
     * @param args Arguments
     */
    static public void main(String[] args) throws Exception {
        Boolean scanFlag = false;
        String ruleFilename = "src/F2.ebnf";
        String codeFilename = null;

        if (args.length < 1)
            throw new Exception("Not enough arguments");

        for (String argument : args) {
            if (argument.startsWith("-")) {
                switch (argument) {
                    case "-s":
                        scanFlag = true;
                        break;
                    default:
                        throw new Exception(
                                "Unknown argument         EBNFLoader loader = new EBNFLoader(ruleStream);'" + argument + "'");
                }
            } else {
                codeFilename = argument;
            }
        }

        if (codeFilename == null) {
            throw new Exception("No file supplied");
        }
        File codeFile = new File(codeFilename);
        FileInputStream ruleStream = new FileInputStream(ruleFilename);
        EBNFLoader loader = new EBNFLoader(ruleStream);

        String lexeme;
        ArrayList<String> lexemes = new ArrayList<>();
        F2Scanner codeScanner = new F2Scanner(
                codeFile,
                "src/rules.txt");
        while ((lexeme = codeScanner.getLexeme()) != null) {
            lexemes.add(lexeme);
        }
        lexemes.add("($,)");

        if (scanFlag) {
            System.out.println(lexemes);
            return;
        }

        LLParser parser = new LLParser(
                loader.getSymbols(),
                loader.getProductions());
        SyntaxNode tree = parser.getSyntaxAnalysisTree(lexemes);

        Analyzer analyzer = new Analyzer(tree);
        analyzer.analyze();

        FileWriter fileWriter = new FileWriter("res.json");
        fileWriter.write(tree.toJSONObject().toString(2));
        fileWriter.close();
    }
}
