import java.io.InputStream;
import java.io.IOException;

class CalculatorEvaluator {
    public final InputStream in;
    private int lookahead;

    public CalculatorEvaluator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    public boolean isZero(int c) {
        return c == '0';
    }

    private boolean isNonZero(int c) {
        return '1' <= c && c <= '9';
    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private boolean isOp(int c) {
        return c == '+' || c == '-' || c == '*';
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    public int eval() throws IOException, ParseError {
        int value = S();

        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();

        return value;
    }

    private int S() throws IOException, ParseError {
        if (isDigit(lookahead) || lookahead == '(') {
            return exp(); 
        }
        throw new ParseError();
    }

    private int exp() throws IOException, ParseError {
        if (isDigit(lookahead)) {
            int result = num();
            return exptail(result);
        }
        if (lookahead == '(') {
            consume('(');
            int result = exp();
            consume(')');
            return exptail(result);
        }

        throw new ParseError();
    }

    private int exptail(int term1) throws IOException, ParseError {
        if (isOp(lookahead)) {
            int operator = op();
            int term2 = exp();

            int result;
            switch (operator) {
                case '+':
                    result = term1 + term2;
                    break;
                case '-':
                    result = term1 - term2;
                    break;
                case '*':
                    result = power(term1, term2);
                    break;
                default:
                    break;
            }
        }

        throw new ParseError();
    }
}
