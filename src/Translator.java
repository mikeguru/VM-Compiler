import java.io.*;

class Translator {

    //In Class VM Language Exercise Key

    //corresponding addition conversion
    private static final String ADD =
            "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "A=A-1\n" +
                    "M=D+M\n";

    //corresponding subtraction conversion
    private static final String SUB =
            "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "A=A-1\n" +
                    "M=M-D\n";

    //corresponding negation conversion
    private static final String NEG =
            "D=0" +
                    "@SP\n" +
                    "A=M-1\n" +
                    "M=D-M\n";

    //corresponding not, negation conversion
    private static final String NOT =
            "@SP\n" +
                    "A=M-1\n" +
                    "M=!M\n";

    //corresponding and conversion
    private static final String AND =
            "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "A=A-1\n" +
                    "M=D&M\n";

    //corresponding or conversion
    private static final String OR =
            "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "A=A-1\n" +
                    "M=D|M\n";

    //corresponding push conversion, locate and save foremost element on stack and increment pointer
    private static final String PUSH =
            "@SP\n" +
                    "A=M\n" +
                    "M=D\n" +
                    "@SP\n" +
                    "M=M+1\n";

    //corresponding pop conversion locate and pop foremost element on stack and decrement pointer
    private static final String POP =
            "@R13\n" +
                    "M=D\n" +
                    "@SP\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "@R13\n" +
                    "A=M\n" +
                    "M=D\n";


    private static final String RETURN =

            "@LCL\n" +
                    "D=M\n" +
                    "@5\n" +
                    "A=D-A\n" +
                    "D=M\n" +
                    "@R13\n" +
                    "M=D\n" +

                    "@SP\n" +
                    "A=M-1\n" +
                    "D=M\n" +
                    "@ARG\n" +
                    "A=M\n" +
                    "M=D \n" +

                    "D=A+1\n" +
                    "@SP\n" +
                    "M=D\n" +

                    "@LCL\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "@THAT\n" +
                    "M=D\n" +

                    "@LCL\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "@THIS\n" +
                    "M=D\n" +

                    "@LCL\n" +
                    "AM=M-1\n" +
                    "D=M\n" +
                    "@ARG\n" +
                    "M=D\n" +

                    "@LCL\n" +
                    "A=M-1\n" +
                    "D=M\n" +
                    "@LCL\n" +
                    "M=D\n" +
                    // R13 -> A
                    "@R13\n" +
                    "A=M\n" +
                    "0;JMP\n";

    private static int symbolOrder = 0;
    private BufferedReader br;
    private String[] files;
    private int fileOrder = 0;
    private String functionName;

    // instantiates the translator constructor
    public Translator(String[] files) {
        this.files = files;
        return;
    }

    public static void main(String args[]) {

        //initilize buffer io
        BufferedWriter bufferedWriter;

        //get the out file name
        String outName = args[args.length - 1].substring(0, args[args.length - 1].indexOf(".")) + ".asm";

        //instantiates the translator
        Translator p = new TranslatorBuilder().setFiles(args).createTranslator();

        String s;
        try {

            //read input then write output to file
            bufferedWriter = new BufferedWriter(new FileWriter(outName));

            s = p.nextProcess();
            while (s != null) {
                bufferedWriter.write(s + "\n");
                // flush
                bufferedWriter.flush();
                s = p.nextProcess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // specific keyword methods
    public static String getADD() {
        return ADD;
    }

    public static String getSUB() {
        return SUB;
    }

    public static String getNEG() {
        return NEG;
    }

    public static String getNOT() {
        return NOT;
    }

    public static String getAND() {
        return AND;
    }

    public static String getOR() {
        return OR;
    }

    public static String getPUSH() {
        return PUSH;
    }

    public static String getPOP() {
        return POP;
    }

    //keep the ordering of any new symbol
    public String nextCount() {
        symbolOrder += 1;
        return Integer.toString(symbolOrder);
    }

    //check for line
    private String nextCommand() throws IOException {
        if (br == null) {
            if (!open())
                return null;
        }
        String line;
        while (true) {
            line = br.readLine();
            if (line == null) {
                br = null;
                return nextCommand();
            }
            line = line.replaceAll("//.*", "");
            if (line.length() == 0)
                continue;
            return line;
        }
    }

    //look for keyword then call for conversion
    private String nextProcess() throws Exception {
        String s = nextCommand();
        if (s == null)
            return null;
        switch (s) {
            case "add": {
                return getADD();
            }
            case "sub": {
                return getSUB();
            }
            case "neg": {
                return getNEG();
            }
            case "eq": {
                return equalCommand();
            }
            case "and": {
                return getAND();
            }
            case "or": {
                return getOR();
            }
            case "not": {
                return getNOT();
            }
            case "gt": {
                return greaterThan();
            }
            case "lt": {
                return lessThan();
            }
            default: {
                String[] inputs = s.split(" ");
                switch (inputs[0]) {
                    case "push": {
                        return inPush(inputs[1], inputs[2]);
                    }
                    case "pop": {
                        return inPop(inputs[1], inputs[2]);
                    }

                    case "label": {
                        return "(" + functionName + "$" + inputs[1] + ")\n";
                    }
                    case "goto": {
                        return inGoto(inputs[1]);
                    }
                    case "if-goto": {
                        return ifGoto(inputs[1]);
                    }

                    case "function": {
                        functionName = inputs[1];
                        return ifGoto(inFunct(inputs[1], inputs[2]));
                    }

                    case "call":
                        return inCall(inputs[1], inputs[2]);

                    case "return":
                        return RETURN;

                    default:
                        return null;
                }
            }
        }
    }

    //conversion of the keywords to asm
    private String inPop(String first, String sec) throws Exception {
        switch (first) {
            case "local": {
                return
                        "@LCL\n" +
                                "D=M\n" +
                                "@" + sec + "\n" +
                                "D=D+A\n" +
                                getPOP();
            }
            case "static": {
                return
                        "@Static." + sec + "\n" +
                                "D=A\n" +
                                getPOP();
            }
            case "argument": {
                return
                        "@ARG\n" +
                                "D=M\n" +
                                "@" + sec + "\n" +
                                "D=D+A\n" +
                                getPOP();
            }
            case "this": {
                return
                        "@THIS\n" +
                                "D=M\n" +
                                "@" + sec + "\n" +
                                "D=D+A\n" +
                                getPOP();
            }
            case "that": {
                return
                        "@THAT\n" +
                                "D=M\n" +
                                "@" + sec + "\n" +
                                "D=D+A\n" +
                                getPOP();
            }
            case "pointer": {
                if (sec.equals("0"))
                    return
                            "@THIS\n" +
                                    "D=A\n" +
                                    getPOP();
                else
                    return
                            "@THAT\n" +
                                    "D=A\n" +
                                    getPOP();
            }

            case "temp": {
                return
                        "@R5\n" +
                                "D=A\n" +
                                "@" + sec + "\n" +
                                "D=D+A\n" +
                                getPOP();
            }
            default:
                return null;
        }
    }

    //conversion of the push and the keywords to asm
    private String inPush(String base, String paraIndex) throws Exception {
        switch (base) {
            case "local": {
                return
                        "@LCL\n" +
                                "D=M\n" +
                                "@" + paraIndex + "\n" +
                                "A=D+A\n" +
                                "D=M\n" +
                                getPUSH();
            }
            case "argument": {
                return
                        "@ARG\n" +
                                "D=M\n" +
                                "@" + paraIndex + "\n" +
                                "A=D+A\n" +
                                "D=M\n" +
                                getPUSH();
            }
            case "this": {
                return
                        "@THIS\n" +
                                "D=M\n" +
                                "@" + paraIndex + "\n" +
                                "A=D+A\n" +
                                "D=M\n" +
                                getPUSH();
            }
            case "that": {
                return
                        "@THAT\n" +
                                "D=M\n" +
                                "@" + paraIndex + "\n" +
                                "A=D+A\n" +
                                "D=M\n" +
                                getPUSH();
            }


            case "static": {
                return
                        "@Static." + paraIndex + "\n" +
                                "D=M\n" +
                                getPUSH();
            }

            case "constant": {
                return
                        "@" + paraIndex + "\n" +
                                "D=A\n" +
                                getPUSH();
            }

            case "temp": {
                return
                        "@R5\n" +
                                "D=A\n" +
                                "@" + paraIndex + "\n" +
                                "A=D+A\n" +
                                "D=M\n" +
                                getPUSH();
            }

            case "pointer": {
                if (paraIndex.equals("0"))
                    return
                            "@THIS\n" +
                                    "D=M\n" +
                                    getPUSH();
                else
                    return
                            "@THAT\n" +
                                    "D=M\n" +
                                    getPUSH();
            }

            default:
                return null;
        }
    }

    //check for file
    private boolean open() {
        try {
            if (br == null && fileOrder != files.length) {
                br = new BufferedReader(new FileReader(files[fileOrder]));
                fileOrder += 1;
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //arithmetic asm conversion
    private String equalCommand() {
        String n = nextCount();
        String s =
                "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@equalCommand.true." + n + "\n" +
                        "D;JEQ\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@equalCommand.after." + n + "\n" +
                        "0;JMP\n" +
                        "(equalCommand.true." + n + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(equalCommand.after." + n + ")\n";
        return s;
    }

    //arithmetic asm conversion
    private String greaterThan() {
        String n = nextCount();
        String s =
                "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@greaterThan.true." + n + "\n" +
                        "\nD;JGT\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@greaterThan.after." + n + "\n" +
                        "0;JMP\n" +
                        "(greaterThan.true." + n + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(greaterThan.after." + n + ")\n";
        return s;
    }

    //arithmetic asm conversion
    private String lessThan() {
        String n = nextCount();
        String s =
                "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "A=A-1\n" +
                        "D=M-D\n" +
                        "@lessThan.true." + n + "\n" +
                        "D;JLT\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=0\n" +
                        "@lessThan.after." + n + "\n" +
                        "0;JMP\n" +
                        "(lessThan.true." + n + ")\n" +
                        "@SP\n" +
                        "A=M-1\n" +
                        "M=-1\n" +
                        "(lessThan.after." + n + ")\n";
        return s;
    }

    private String inGoto(String label) {
        String s =
                "@" + functionName + "$" + label + "\n" +
                        "0;JMP\n";
        return s;
    }

    private String ifGoto(String label) {
        String s =
                "@SP\n" +
                        "AM=M-1\n" +
                        "D=M\n" +
                        "@" + functionName + "$" + label + "\n" +
                        "D;JNE\n";
        return s;
    }


    private String inFunct(String f, String k) {
        String s =
                "(" + f + ")\n" +
                        "@SP\n" +
                        "A=M\n";
        int kk = Integer.parseInt(k);
        for (int i = 0; i < kk; i += 1) {
            s +=
                    "M=0\n" +
                            "A=A+1\n";
        }
        return s +
                "D=A\n" +
                "@SP\n" +
                "M=D\n";
    }

    private String inCall(String f, String n) {
        String c = nextCount();
        return
                "@SP\n" +
                        "D=M\n" +
                        "@R13\n" +
                        "M=D\n" +

                        "@RET." + c + "\n" +
                        "D=A\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "M=M+1\n" +

                        "@LCL\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "M=M+1\n" +

                        "@ARG\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "M=M+1\n" +

                        "@THIS\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "M=M+1\n" +

                        "@THAT\n" +
                        "D=M\n" +
                        "@SP\n" +
                        "A=M\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "M=M+1\n" +

                        "@R13\n" +
                        "D=M\n" +
                        "@" + n + "\n" +
                        "D=D-A\n" +
                        "@ARG\n" +
                        "M=D\n" +

                        "@SP\n" +
                        "D=M\n" +
                        "@LCL\n" +
                        "M=D\n" +
                        "@" + f + "\n" +
                        "0;JMP\n" +
                        "(RET." + c + ")\n";
    }
    
/*
    
// return
@LCL
D=M
@frame
M=D // FRAME = LCL
@5
D=D-A
A=D
D=M
@ret
M=D // RET = *(FRAME-5)
@SP
M=M-1
A=M
D=M
@ARG
A=M
M=D // *ARG = pop
@ARG
D=M+1
@SP
M=D // SP = ARG+1
@frame
D=M
@1
D=D-A
A=D
D=M
@THAT
M=D // THAT = *(FRAME-1)
@frame
D=M
@2
D=D-A
A=D
D=M
@THIS
M=D // THIS = *(FRAME-2)
@frame
D=M
@3
D=D-A
A=D
D=M
@ARG
M=D // ARG = *(FRAME-3)
@frame
D=M
@4
D=D-A
A=D
D=M
@LCL
M=D // LCL = *(FRAME-4)
@ret
A=M
0;JMP
    
*/

}