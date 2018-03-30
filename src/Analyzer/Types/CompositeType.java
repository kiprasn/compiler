package Analyzer.Types;

public class CompositeType extends Type {
    private final Type subtype;

    CompositeType(String name, Type subtype) {
        super(name);
        this.subtype = subtype;
    }

    @Override
    public boolean equals(Type type) {
        return subtype.equals(type);
    }
}
