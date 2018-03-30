package Analyzer.Types;

public class Type {
    private String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Type type) {
        return this == type;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
