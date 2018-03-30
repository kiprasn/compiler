package EBNF;

import java.io.FileInputStream;
import java.io.IOException;

public class EBNF {
    public EBNF(FileInputStream stream) throws IOException {
        EBNFScanner scanner = new EBNFScanner(stream);

        while (scanner.hasLexeme()) {
            System.out.println(scanner.getLexeme());
        }
    }
}
