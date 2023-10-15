import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Kamilia Kamal Arifin and Jordyn Liegl
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        return Condition.valueOf(c.replace('-', '_').toUpperCase());
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        tokens.dequeue();
        Reporter.assertElseFatalError(tokens.length() > 0,
                "Expected condition");
        String cond = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(cond),
                cond + " is not a condition");
        Condition parseCond = parseCondition(cond);

        Reporter.assertElseFatalError(tokens.length() > 0, "Expected THEN");
        Reporter.assertElseFatalError(tokens.dequeue().equals("THEN"),
                "THEN is expected");

        Statement blockIf = s.newInstance();
        Reporter.assertElseFatalError(tokens.length() > 0,
                "Termination is not allowed");
        blockIf.parseBlock(tokens);

        Reporter.assertElseFatalError(
                tokens.length() > 0 && (tokens.front().equals("ELSE")
                        || tokens.front().equals("END")),
                "Expected ELSE or END IF");

        if (tokens.front().equals("ELSE")) {
            tokens.dequeue();
            Statement blockElse = s.newInstance();
            Reporter.assertElseFatalError(tokens.length() > 0,
                    "Termination is not allowed");
            blockElse.parseBlock(tokens);
            Reporter.assertElseFatalError(
                    tokens.length() > 1 && tokens.dequeue().equals("END")
                            && tokens.dequeue().equals("IF"),
                    "Expected END IF");
            s.assembleIfElse(parseCond, blockIf, blockElse);
        } else {
            tokens.dequeue();
            Reporter.assertElseFatalError(
                    tokens.length() > 0 && tokens.dequeue().equals("IF"),
                    "Expected END IF");
            s.assembleIf(parseCond, blockIf);
        }
    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        tokens.dequeue();
        Reporter.assertElseFatalError(tokens.length() > 0,
                "Expected condition");
        String cond = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(cond),
                "Condition is expected");
        Condition parseCond = parseCondition(cond);

        Reporter.assertElseFatalError(tokens.length() > 0, "Expected DO");
        Reporter.assertElseFatalError(tokens.dequeue().equals("DO"),
                "DO is expected");
        Statement whileBlock = s.newInstance();
        Reporter.assertElseFatalError(tokens.length() > 0,
                "Termination is not allowed");
        whileBlock.parseBlock(tokens);

        Reporter.assertElseFatalError(
                tokens.length() > 1 && tokens.dequeue().equals("END")
                        && tokens.dequeue().equals("WHILE"),
                "Expected WHILE");

        s.assembleWhile(parseCond, whileBlock);
    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";

        String call = tokens.dequeue();
        if (Tokenizer.isIdentifier(call)) {
            s.assembleCall(call);
        }
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        if (tokens.front().equals("IF")) {
            parseIf(tokens, this);
        } else if (tokens.front().equals("WHILE")) {
            parseWhile(tokens, this);
        } else if (Tokenizer.isIdentifier(tokens.front())) {
            parseCall(tokens, this);
        }

    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        this.clear();

        String next = tokens.front();
        int count = 0;

        while (Tokenizer.isIdentifier(next) || next.equals("IF")
                || next.equals("WHILE")) {
            Statement add = this.newInstance();
            add.parse(tokens);
            this.addToBlock(count, add);
            next = tokens.front();
            count++;
        }

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
