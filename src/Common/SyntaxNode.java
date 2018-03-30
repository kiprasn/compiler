package Common;

import Common.Symbols.Symbol;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SyntaxNode {
    public Symbol symbol;

    public Lexeme lexeme;

    public ArrayList<SyntaxNode> children = new ArrayList<>();

    public boolean empty = false;

    public SyntaxNode(Symbol symbol) {
        this.symbol = symbol;
    }

    public SyntaxNode() {
    }

    public SyntaxNode child(int index) {
        return children.get(index);
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("name", symbol != null ? symbol.getName() : "");
        object.put("data", lexeme != null ? lexeme.getData() : "");
        object.put("empty", empty);
        JSONArray children = new JSONArray();
        for (SyntaxNode childNode : this.children) {
            children.put(childNode.toJSONObject());
        }
        if (children.length() > 0) {
            object.put("children", children);
        }
        return object;
    }

    public String toString() {
        return this.symbol.toString();
    }
}
