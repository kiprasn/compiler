package Analyzer.Types;

import Analyzer.Analyzer;
import Analyzer.Types.Type;

public enum BasicTypes {
    Integer(new Type("Integer")),
    String(new Type("String")),
    Boolean(new Type("Boolean")),
    Float(new Type("Float")),
    Character(new Type("Character"));

    private Type type;

    BasicTypes(Type type) {
        this.type = type;
    }

    public Type getValue() {
        return type;
    }

    public String getName() {
        return type.getName();
    }
}
