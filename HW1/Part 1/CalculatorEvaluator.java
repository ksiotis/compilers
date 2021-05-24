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

    private boolean isLowOp(int c) {
        return c == '+' || c == '-';
    }

    private boolean isHighOp(int c) {
        return c == '*';
    }

    private boolean isOp(int c) {
        return isLowOp(c) || isHighOp(c);
    }

    private int evalDigit(int c) {
        return c - '0';
    }

    private int pow(int base, int exponent) {
        if (exponent < 0)
            return 0;
        if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;
    
        if (exponent % 2 == 0) //even exp -> b ^ exp = (b^2)^(exp/2)
            return pow(base * base, exponent/2);
        else                   //odd  exp -> b ^ exp = b * (b^2)^(exp/2)
            return base * pow(base * base, exponent/2);
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
        if (isDigit(lookahead) || lookahead == '(') {
            int value = term();
            return exptail(value);
        }
        throw new ParseError();
    }

    private int exptail(int value1) throws IOException, ParseError {
        int value2;
        switch (lookahead) {
            case '+':
                consume('+');
                value2 = term();
                return exptail(value1 + value2);
            case '-':
                consume('-');
                value2 = term();
                return exptail(value1 - value2);
        }
        if (lookahead == ')' || lookahead == -1 || lookahead == '\n')
            return(value1);
        throw new ParseError();
    }

    private int term() throws IOException, ParseError {
        if (isDigit(lookahead) || lookahead == '(') {
            int value = factor();
            return termtail(value);
        }
        throw new ParseError();
    }

    private int termtail(int value1) throws IOException, ParseError {
        if (isOp(lookahead) || lookahead == ')' || lookahead == -1 || lookahead == '\n') {
            int value2;
            switch (lookahead) {
                case '*':
                    consume('*');
                    if (lookahead != '*')
                        throw new ParseError();
                    consume('*');

                    value2 = factor();
                    return pow(value1, termtail(value2));
                default:
                    return(value1);
            }
        }
        throw new ParseError();
    }

    private int factor() throws IOException, ParseError {
        if (isDigit(lookahead)) {
            return num();
        }
        else if (lookahead == '(') {
            consume('(');
            int value = exp();
            consume(')');
            return value;
        }
        throw new ParseError();
    }

    private int num() throws IOException, ParseError {
        if (lookahead == '0') {
            consume('0');
            return 0;
        }
        else if (isNonZero(lookahead)) {
            int value = nonzero();
            return numtail(value);
        }
        throw new ParseError();
    }

    private int numtail(int value1) throws IOException, ParseError {
        if (isDigit(lookahead)) {
            int value2 = digit();
            return numtail(value1 * 10 + value2);
        }
        else if (isOp(lookahead) || lookahead == ')' || lookahead == -1 || lookahead == '\n') {
            return value1;
        }
        throw new ParseError();
    }

    private int nonzero() throws IOException, ParseError {
        if (isNonZero(lookahead)) {
            int value = evalDigit(lookahead);
            consume(lookahead);
            return value;
        }
        throw new ParseError();
    }

    private int digit() throws IOException, ParseError {
        if (isDigit(lookahead)) {
            int value = evalDigit(lookahead);
            consume(lookahead);
            return value;
        }
        throw new ParseError();
    }
}