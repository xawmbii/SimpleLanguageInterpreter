import java.util.*;
import java.util.regex.*;

public class SimpleLanguageInterpreter {

    private final Map<String, Integer> variables = new HashMap<>();

    public void interpret(String program) {
        String[] lines = program.split(";");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                parseAndExecute(line);
            } catch (RuntimeException e) {
                System.out.println("error");
                return;
            }
        }
        printVariables();
    }

    private void parseAndExecute(String line) {
        String[] parts = line.split("=", 2);
        if (parts.length != 2) throw new RuntimeException("Syntax error");

        String identifier = parts[0].trim();
        String expression = parts[1].trim();

        if (!isValidIdentifier(identifier)) throw new RuntimeException("Invalid identifier");
        int value = evaluateExpression(expression);
        variables.put(identifier, value);
    }

    private int evaluateExpression(String expr) {
        List<String> tokens = tokenize(expr);
        int[] index = {0};
        return parseExpression(tokens, index);
    }

    private int parseExpression(List<String> tokens, int[] index) {
        int value = parseTerm(tokens, index);
        while (index[0] < tokens.size()) {
            String operator = tokens.get(index[0]);
            if (!operator.equals("+") && !operator.equals("-")) break;

            index[0]++;
            int term = parseTerm(tokens, index);
            value = operator.equals("+") ? value + term : value - term;
        }
        return value;
    }

    private int parseTerm(List<String> tokens, int[] index) {
        int value = parseFactor(tokens, index);
        while (index[0] < tokens.size()) {
            String operator = tokens.get(index[0]);
            if (!operator.equals("*")) break;

            index[0]++;
            int factor = parseFactor(tokens, index);
            value *= factor;
        }
        return value;
    }

    private int parseFactor(List<String> tokens, int[] index) {
        if (index[0] >= tokens.size()) throw new RuntimeException("Syntax error");

        String token = tokens.get(index[0]);
        index[0]++;

        if (token.equals("(")) {
            int value = parseExpression(tokens, index);
            if (index[0] >= tokens.size() || !tokens.get(index[0]).equals(")")) {
                throw new RuntimeException("Mismatched parentheses");
            }
            index[0]++;
            return value;
        } else if (token.equals("-")) {
            return -parseFactor(tokens, index);
        } else if (token.equals("+")) {
            return parseFactor(tokens, index);
        } else if (isValidLiteral(token)) {
            return Integer.parseInt(token);
        } else if (isValidIdentifier(token)) {
            if (!variables.containsKey(token)) throw new RuntimeException("Uninitialized variable");
            return variables.get(token);
        } else {
            throw new RuntimeException("Invalid token");
        }
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*|\\d+|[-+*()]").matcher(expr);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private boolean isValidIdentifier(String identifier) {
        return identifier.matches("[a-zA-Z_][a-zA-Z_0-9]*");
    }

    private boolean isValidLiteral(String literal) {
        return literal.matches("0|[1-9][0-9]*");
    }

    private void printVariables() {
        for (String key : variables.keySet()) {
            System.out.println(key + " = " + variables.get(key));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SimpleLanguageInterpreter interpreter = new SimpleLanguageInterpreter();

        StringBuilder program = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) break;
            program.append(line).append("\n");
        }

        interpreter.interpret(program.toString());
    }
}
