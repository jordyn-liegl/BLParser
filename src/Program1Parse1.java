import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary method {@code parse} for {@code Program}.
 *
 * @author Kamilia Kamal Arifin and Jordyn Liegl
 *
 */
public final class Program1Parse1 extends Program1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Parses a single BL instruction from {@code tokens} returning the
     * instruction name as the value of the function and the body of the
     * instruction in {@code body}.
     *
     * @param tokens
     *            the input tokens
     * @param body
     *            the instruction body
     * @return the instruction name
     * @replaces body
     * @updates tokens
     * @requires <pre>
     * [<"INSTRUCTION"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an instruction string is a proper prefix of #tokens]  and
     *    [the beginning name of this instruction equals its ending name]  and
     *    [the name of this instruction does not equal the name of a primitive
     *     instruction in the BL language] then
     *  parseInstruction = [name of instruction at start of #tokens]  and
     *  body = [Statement corresponding to the block string that is the body of
     *          the instruction string at start of #tokens]  and
     *  #tokens = [instruction string at start of #tokens] * tokens
     * else
     *  [report an appropriate error message to the console and terminate client]
     * </pre>
     */
    private static String parseInstruction(Queue<String> tokens,
            Statement body) {
        assert tokens != null : "Violation of: tokens is not null";
        assert body != null : "Violation of: body is not null";
        assert tokens.length() > 0 && tokens.front().equals("INSTRUCTION") : ""
                + "Violation of: <\"INSTRUCTION\"> is proper prefix of tokens";

        /*
         * Ensure the syntax is correct as the first value of tokens should be
         * the instruction name which is also an identifier and the next should
         * be IS.
         */
        String instr = tokens.dequeue();
        String instrName = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(instrName),
                "The instruction name must be an identifier");
        Reporter.assertElseFatalError(tokens.dequeue().equals("IS"),
                "Instruction line syntax is INSTRUCTION instruction name IS");

        /*
         * Check that the beginning and ending syntax of the body is correct,
         * while adding the body of the block to this.
         */
        body.parseBlock(tokens);
        Reporter.assertElseFatalError(tokens.dequeue().equals("END"),
                "The closing line must start with END");
        Reporter.assertElseFatalError(tokens.dequeue().equals(instrName),
                "The closing line must end with the instruction name");

        return instrName;
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Program1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(SimpleReader in) {
        assert in != null : "Violation of: in is not null";
        assert in.isOpen() : "Violation of: in.is_open";
        Queue<String> tokens = Tokenizer.tokens(in);
        this.parse(tokens);
    }

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        /*
         * If END_OF_INPUT is the only String in tokens, then this must be
         * empty.
         */
        if (tokens.front().equals(Tokenizer.END_OF_INPUT)) {
            this.clear();
        } else {
            /*
             * Check that the syntax is correct following PROGRAM programName
             * IS.
             */
            Reporter.assertElseFatalError(tokens.dequeue().equals("PROGRAM"),
                    "BL file must start with PROGRAM");
            String progName = tokens.dequeue();
            Reporter.assertElseFatalError(Tokenizer.isIdentifier(progName),
                    "The program name must be an identifier");
            Reporter.assertElseFatalError(tokens.dequeue().equals("IS"),
                    "BL file must start with PROGRAM programName IS");

            /*
             * Loop through the instructions to ensure they follow the correct
             * syntax. Add each instruction name and body to the key and value
             * respectively to a map.
             */
            Map<String, Statement> context = this.newContext();
            while (tokens.front().equals("INSTRUCTION")) {
                Statement instrBody = this.newBody();
                String instrName = parseInstruction(tokens, instrBody);
                Reporter.assertElseFatalError(!context.hasKey(instrName),
                        "The user-defined instruction name must be unique");
                context.add(instrName, instrBody);
            }

            /*
             * Check that the beginning and ending syntax of the body is
             * correct, while adding the body of the block to this.
             */
            Reporter.assertElseFatalError(tokens.dequeue().equals("BEGIN"),
                    "The body must start with BEGIN");
            Statement body = this.newBody();
            body.parseBlock(tokens);
            Reporter.assertElseFatalError(tokens.dequeue().equals("END"),
                    "The closing line must start with END");
            Reporter.assertElseFatalError(tokens.dequeue().equals(progName),
                    "BL file must end with the program name");

            /*
             * Ensure the only value in tokens is END_OF_INPUT
             */
            Reporter.assertElseFatalError(
                    tokens.front().equals(Tokenizer.END_OF_INPUT),
                    "The only value in tokens should be the end of input");

            /*
             * Replace the program name, context, and body to the updated this.
             */
            this.setName(progName);
            this.swapContext(context);
            this.swapBody(body);
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
        out.print("Enter valid BL program file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Program p = new Program1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        p.parse(tokens);
        /*
         * Pretty print the program
         */
        out.println("*** Pretty print of parsed program ***");
        p.prettyPrint(out);

        in.close();
        out.close();
    }

}
