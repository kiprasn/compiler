package Analyzer;

import Analyzer.Types.BasicTypes;
import Analyzer.Types.Type;
import Common.SyntaxNode;

import java.util.*;

import static java.lang.Integer.parseInt;

public class Analyzer {
    private ArrayList<FormItem> resultForm;
    private SyntaxNode root;
    private SymbolTable symbols = new SymbolTable();
    private HashMap<String, Type> types = new HashMap<>();
    private Queue<SyntaxNode> statements = new ArrayDeque<>();
    private int arraySize = 0;

    public Analyzer(SyntaxNode root) {
        this.root = root;
        types.putAll(Analyzer.basicTypes());
    }

    public void analyze() {
        statements.addAll(root.children);

        SyntaxNode node;
        System.out.println("PROGRAM");
        while ((node = statements.poll()) != null) {
            if (node.empty)
                continue;
            analyze(node);
        }
        System.out.println("PROGRAMEND");
    }

    private void analyze(SyntaxNode node) {
        switch (node.symbol.getName()) {
            case "Statements":
                statements.addAll(node.children);
                break;
            case "Expression":
                // TODO: Check returned type
                analyzeExpression(node);
                break;
            case "SingleDeclaration":
                analyzeDeclaration(node);
                break;
            case "FunctionDeclaration":
                analyzeFunctionDeclaration(node);
                break;
            case "ClassDeclaration":
                analyzeClassDeclaration(node);
                break;
            case "Statement":
                analyze(node.children.get(0));
                break;
            case "ReturnStatement":
                /* No check required, return in root exits */
                break;
            case "LoopStatements":
                throw new Error("Not implemented");
            case "ConditionalStatements":
                throw new Error("Not implemented");
        }
    }

    private void analyzeLoopStatements(SyntaxNode node) {
    }

    private void analyzeConditionalStatements(SyntaxNode node) {
    }

    private void analyzeClassDeclaration(SyntaxNode node) {
        SyntaxNode identifierNode = node.child(1);
    }

    private void analyzeFunctionDeclaration(SyntaxNode node) {
        SyntaxNode identifierNode = node.child(1);
        SyntaxNode argumentNode = node.child(3);
        SyntaxNode returnTypeNode = node.child(6);
        SyntaxNode blockNode = node.child(7);


        String identifier = identifierNode.lexeme.getData();
        System.out.println("FUNCTION " + identifier);
        if (symbols.lookupFunctionReturn(identifier) != null)
            throw new Error("Function " + identifier + " already exists");

        symbols.pushScope();

        List<Type> argumentTypes = analyzeFunctionArguments(argumentNode);
        Type returnType = resolveType(returnTypeNode);
        symbols.insertFunction(identifier, argumentTypes, returnType);
        analyzeFunctionBlock(blockNode, returnType);

        symbols.popStack();
        System.out.println("FUNCTION END");
    }

    private void analyzeFunctionBlock(SyntaxNode node, Type expectedType) {
        System.out.println("FUNCTION BLOCK");
        SyntaxNode statementsNode = node.child(1);
        while (!statementsNode.empty) {
            SyntaxNode statementNode = statementsNode.child(0);
            statementsNode = statementsNode.child(1);
            SyntaxNode statement = statementNode.child(0);

            if (statement.symbol.getName().equals("ReturnStatement")) {
                Type returnType = analyzeReturnStatement(statement);
                if (!expectedType.equals(returnType))
                    throw new Error("Function should return " + expectedType + "\n" +
                            "   instead returns " + returnType);
                continue;
            }

            analyze(statement);
        }
    }

    private Type analyzeReturnStatement(SyntaxNode node) {
        SyntaxNode extensionNode = node.child(1);
        SyntaxNode expressionNode = extensionNode.child(0);
        if (expressionNode.symbol.getName().equals("Semicolon")) {
            System.out.println("NULL");
            System.out.println("RETURN");
            return null;
        }
        Type t1 = analyzeExpression(expressionNode);
        System.out.println("RETURN");
        return t1;
    }

    private List<Type> analyzeFunctionArguments(SyntaxNode argumentNode) {
        List<Type> argTypes = new ArrayList<>();

        if (argumentNode.empty)
            return argTypes;

        String identifier = resolveIdentifier(argumentNode.child(0));
        Type type = resolveType(argumentNode.child(2));
        argTypes.add(type);
        symbols.insertSymbol(identifier, type);

        SyntaxNode extension = argumentNode.child(3);
        while (!extension.empty) {
            identifier = resolveIdentifier(extension.child(1));
            type = resolveType(extension.child(3));
            argTypes.add(type);
            symbols.insertSymbol(identifier, type);
            extension = extension.child(4);
        }

        return argTypes;
    }

    private String resolveIdentifier(SyntaxNode identifierNode) {
        return identifierNode.lexeme.getData();
    }

    private Type resolveType(SyntaxNode typeNode) {
        String alias = typeNode.children.get(0).lexeme.getData();
        return types.get(alias);
    }

    private void analyzeDeclaration(SyntaxNode node) {
        SyntaxNode typeNode = node.children.get(0);
        SyntaxNode identifierNode = node.children.get(1);
        SyntaxNode arrayNode = node.children.get(2);
        SyntaxNode extensionNode = node.children.get(3);

        if (arrayNode.empty) {
            Type t1 = resolveType(typeNode);
            if (t1 == null)
                throw new Error("No such type: " + t1);
            String identifier = identifierNode.lexeme.getData();
            System.out.println("PUSH IDENTIFIER " + identifier);
            symbols.insertSymbol(identifier, t1);
            if (!extensionNode.empty) {
                SyntaxNode relNode = extensionNode.children.get(1);

                Type t2 = analyzeExpression(relNode);
                resolveDeclarationExpression(t1, t2);
            } else {
                System.out.println("NULL");
            }
            System.out.println("ASSIGN");
        } else {
            if(identifierNode == null)
                throw new Error("Identifier null");
            System.out.println("ARRAY " + resolveIdentifier(identifierNode));
            Type identifier = resolveType(typeNode);
            Type arrayElements = analyzeExpression(extensionNode.children.get(1));
            if (!identifier.equals(arrayElements)) {
                throw new Error("Array \""
                        + resolveIdentifier(identifierNode)
                        + "\" type \""
                        + identifier
                        + "\" does not match arrays' elements: "
                        + arrayElements);
            } else {
                SyntaxNode arraySizeNode = arrayNode.children.get(1);

                if (arraySizeNode.empty) {

                    symbols.insertSymbol(resolveIdentifier(identifierNode), arrayElements);
                } else if (parseInt(arrayNode.children.get(1).child(0).lexeme.getData()) != 0) {
                    if (parseInt(arrayNode.children.get(1).child(0).lexeme.getData()) == arraySize) {
                        symbols.insertSymbol(resolveIdentifier(identifierNode), arrayElements);
                    } else {
                        throw new Error("Array \""
                                + resolveIdentifier(identifierNode)
                                + "\" size \""
                                + parseInt(arrayNode.children.get(1).child(0).lexeme.getData())
                                + "\" does not match the count of arrays' elements: "
                                + arraySize);

                    }
                } else {
                    throw new Error("Array size can't be 0!");
                }
            }
        }
    }

    private Type resolveDeclarationExpression(Type t1, Type t2) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }

        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        if (t1.equals(types.get("string"))) {
            if ((result = resolveString(t2)) != null) {
                return result;
            }
        }

        throw new Error("Cannot assign " + t1 + " to " + t2);
    }

    private Type analyzeExpression(SyntaxNode node) {
        if (node.empty)
            throw new Error("No expression to analyze");

        switch (node.symbol.getName()) {
            case "Expression":
                return analyzeLogicalExpression(node.children.get(0));
            case "AndExpression":
                return analyzeAndExpression(node);
            case "CompareExpression":
                return analyzeCompareExpression(node);
            case "ArithmeticExpression":
                return analyzeArithmeticExpression(node);
            case "Term":
                return analyzeTermExpression(node);
            case "TermMult":
                return analyzeTermMultExpression(node);
            case "TermDiv":
                return analyzeTermDivExpression(node);
            case "Factor":
                return analyzeFactorExpression(node);
            default:
                throw new Error("Unrecognized expression");
        }
    }

    private Type analyzeFactorExpression(SyntaxNode node) {
        SyntaxNode n1 = node.children.get(0);

        switch (n1.symbol.getName()) {
            case "Number":
                System.out.print("PUSH ");
                return analyzeNumberType(n1);
            case "String":
                System.out.print("PUSH ");
                System.out.println(node.child(0).lexeme.getData());
                return types.get("string");
            case "LeftBracket":
                return analyzeExpression(node.children.get(1));
            default:
                // TODO: Verify that this will be an identifier
                return analyzeIdentifierFactor(n1);
        }
    }

    private Type analyzeIdentifierFactor(SyntaxNode node) {
        SyntaxNode n1 = node.children.get(0);
        if (n1.symbol.getName().equals("" +
                "ArrayLiteral"))
            return analyzeArray(n1);
        SyntaxNode identifierExtension = node.child(1);
        String identifier = resolveIdentifier(n1);
        System.out.println("PUSH " + identifier);
        // TODO: identifierExtension is loopable
        if (identifierExtension.empty) {
            Type idType = symbols.lookupSymbol(identifier);
            if (idType == null)
                throw new Error("Symbol " + identifier + " doesn't exist");
            return idType;
        } else {
            identifierExtension = identifierExtension.child(0);
            return analyzeIdentifierFactorExtension(identifierExtension, identifier);
        }
    }

    private Type
    analyzeIdentifierFactorExtension(
            SyntaxNode identifierExtension,
            String identifier
    ) {
        switch (identifierExtension.symbol.getName()) {
            case "ArrayCall":
                Type arrayIdentifier = symbols.lookupSymbol(identifier);
                if (arrayIdentifier == null) {
                    throw new Error("No such array found");
                }
                return arrayIdentifier;
            case "FunctionCall":
                Type returnType = analyzeFunctionCall(
                        identifierExtension.child(1), identifier);
                if (returnType == null)
                    throw new Error("No such function");
                System.out.println("CALL " + identifier);
                return returnType;
            case "MemberCall":
                /*
                Type memberReturn = symbols.lookupSymbol(identifier);
                if(memberReturn == null){
                    System.out.println(identifier);
                    throw new Error("No such function member found");
                }
                return memberReturn;
                */
                throw new Error("Not implemented");
            default:
                throw new Error("Unrecognized operation on identifier");
        }
    }

    private Type
    analyzeFunctionCall(
            SyntaxNode argumentsNode,
            String functionName
    ) {
        Type returnType = symbols.lookupFunctionReturn(functionName);
        List<Type> expectedArgs = symbols.lookupFunctionArguments(functionName);

        if (argumentsNode.empty) {
            if (expectedArgs.size() != 0)
                throw new Error("Invalid argument count");
            return returnType;
        }

        int idx = 0;
        while (!argumentsNode.empty) {
            SyntaxNode argumentNode = argumentsNode.child(0);
            SyntaxNode extensionNode = argumentsNode.child(1);

            Type actualType = analyzeExpression(argumentNode);
            Type expectedType = expectedArgs.get(idx);

            if (!expectedType.equals(actualType))
                throw new Error("Type mismatch in function call, expected: " +
                        expectedType + "\n" + "actual: " + actualType);

            if (extensionNode.empty)
                break;

            argumentsNode = extensionNode.child(1);
            idx++;
        }

        return returnType;
    }

    private Type analyzeArray(SyntaxNode n1) {
        arraySize = 0;
        SyntaxNode variables = n1.children.get(1);
        if (variables.empty)
            return null;
        SyntaxNode extension = variables.children.get(1);

        SyntaxNode type = variables.children.get(0);
        Type t1 = analyzeVariableType(type);
        arraySize++;
        //System.out.println(variables.child(0).toString());
        String typee = variables.child(0).toString();
        if(typee.equals("Number"))
            System.out.println(variables.child(0).child(0).lexeme.getData());
        else
            System.out.println(variables.child(0).lexeme.getData());
        while (!extension.empty) {
            SyntaxNode relNode = extension.child(1);
            if(typee.equals("Number"))
                System.out.println(relNode.children.get(0).child(0).lexeme.getData());
            else
                System.out.println(relNode.child(0).lexeme.getData());
            Type t2 = analyzeVariableType(relNode.child(0));
            if (!t1.equals(t2))
                throw new Error("Type mismatch in array: " + t1 + ", " + t2);
            extension = relNode.children.get(1);
            arraySize++;
        }
        return t1;
    }

    private Type analyzeVariableType(SyntaxNode variableNode) {
        switch (variableNode.symbol.getName()) {
            case "String":
                return types.get("string");
            case "Number":
                return analyzeNumberType(variableNode);
            case "Identifier":
                return symbols.lookupSymbol(variableNode.lexeme.getData());
            default:
                throw new Error("Unknown type variable");
        }
    }

    private Type analyzeNumberType(SyntaxNode node) {
        if (node.children.get(0).symbol.getName().equals("Integer")) {
            System.out.println(node.child(0).lexeme.getData());
            return types.get("int");
        } else {
            System.out.println(node.child(0).lexeme.getData());
            return types.get("float");
        }
    }

    private Type analyzeTermMultExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        while (!extExpression.empty) {
            SyntaxNode relNode = extExpression.children.get(1);

            Type t2 = analyzeExpression(relNode);
            t1 = resolveTermMultExpression(t1, t2);

            extExpression = relNode.children.get(1);
            System.out.println("OPERATION *");
        }
        return t1;
    }

    private Type resolveTermMultExpression(Type t1, Type t2) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }

        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        throw new Error("Cannot multiply " + t1 + " and " + t2);
    }

    private Type analyzeTermDivExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        while (!extExpression.empty) {
            SyntaxNode relExpression = extExpression.children.get(1);

            t1 = analyzeExpression(relExpression);
            t1 = resolveTermDivExpression(t1);

            extExpression = relExpression.children.get(1);
            System.out.println("OPERATION /");
        }

        return t1;
    }

    private Type resolveTermDivExpression(Type t1) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t1)) != null)
                return result;
        }
        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t1)) != null) {
                return result;
            }
        }

        throw new Error("Cannot divide " + t1);
    }

    private Type resolveFloat(Type t2) {
        if (t2.equals(types.get("float")))
            return t2;

        if (t2.equals(types.get("int")))
            return types.get("float");

        return null;
    }

    private Type resolveInt(Type t2) {
        if (t2.equals(types.get("int")))
            return t2;

        if (t2.equals(types.get("float")))
            return t2;

        return null;
    }

    private Type resolveString(Type t2) {
        if (t2.equals(types.get("string")))
            return t2;

        if (t2.equals(types.get("char")))
            return t2;

        return null;
    }

    private Type analyzeTermExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);

        while (!extExpression.empty) {
            SyntaxNode relNode = extExpression.children.get(1);

            Type t2 = analyzeExpression(relNode);
            t1 = resolveTermExpression(t1, t2);

            extExpression = relNode.children.get(1);
            System.out.println("OPERATION /");
        }

        return t1;
    }

    private Type resolveTermExpression(Type t1, Type t2) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }

        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        throw new Error("Can't divide " + t1 + " and " + t2);
    }

    private Type analyzeArithmeticExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        while (!extExpression.empty) {
            SyntaxNode operator = extExpression.children.get(0);
            SyntaxNode relExpression = extExpression.children.get(1);

            Type t2 = analyzeExpression(relExpression);
            t1 = resolveArithmeticExpression(operator, t1, t2);

            extExpression = relExpression.children.get(1);
            System.out.println("OPERATION " + operator.toString().substring(1, 2));
        }

        return t1;
    }

    private Type
    resolveArithmeticExpression(
            SyntaxNode operator,
            Type t1,
            Type t2
    ) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        } else if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        } else if (t1.equals(types.get("string"))) {
            if ((result = resolveString(t2)) != null) {
                return result;
            }
        } else if (t1.equals(types.get("char"))) {
            if ((result = resolveString(t2)) != null) {
                return result;
            }
        }


        throw new Error("Cannot " + t1 + " "
                + operator.toString() + " " + t2);
    }

    private Type analyzeCompareExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        if (!extExpression.empty) {
            SyntaxNode operator = extExpression.children.get(0);
            SyntaxNode relExpression = extExpression.children.get(1);

            Type t2 = analyzeExpression(relExpression);
            t1 = resolveCompareExpression(operator, t1, t2);

            System.out.println("OPERATION " + operator.child(0).lexeme.getData());
        }

        return t1;
    }

    private Type resolveCompareExpression(
            SyntaxNode operator,
            Type t1,
            Type t2
    ) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }
        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        String op = operator.children.toString();

        throw new Error("Cannot compare " +
                t1 + " " + op
                + " " + t2);
    }

    private Type analyzeAndExpression(SyntaxNode node) {

        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        while (!extExpression.empty) {
            SyntaxNode relExpression = extExpression.children.get(1);

            Type t2 = analyzeExpression(relExpression);
            t1 = resolveAndExpression(t1, t2);
            extExpression = extExpression.children.get(2);

            System.out.println("OPERATION &&");
        }

        return t1;
    }

    private Type resolveAndExpression(Type t1, Type t2) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }

        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        throw new Error("AndExpression " + t1 + " " + t2);

    }

    private Type analyzeLogicalExpression(SyntaxNode node) {
        SyntaxNode subExpression = node.children.get(0);
        SyntaxNode extExpression = node.children.get(1);

        Type t1 = analyzeExpression(subExpression);
        while (!extExpression.empty) {
            SyntaxNode relExpression = extExpression.children.get(1);

            Type t2 = analyzeExpression(relExpression);
            t1 = resolveLogicalExpression(t1, t2);

            extExpression = extExpression.children.get(2);
            System.out.println("OPERATION ||");
        }
        return t1;
    }

    private Type resolveLogicalExpression(Type t1, Type t2) {
        Type result;

        if (t1.equals(types.get("int"))) {
            if ((result = resolveInt(t2)) != null)
                return result;
        }

        if (t1.equals(types.get("float"))) {
            if ((result = resolveFloat(t2)) != null) {
                return result;
            }
        }

        throw new Error("resolveLogicalExpression" + t1 + " and " + t2);

    }

    private static HashMap<String, Type>
    basicTypes() {
        HashMap<String, Type> types = new HashMap<>();

        types.put("int",
                BasicTypes.Integer.getValue());
        types.put("string",
                BasicTypes.String.getValue());
        types.put("bool",
                BasicTypes.Boolean.getValue());
        types.put("char",
                BasicTypes.Character.getValue());
        types.put("float",
                BasicTypes.Float.getValue());

        return types;
    }
}
