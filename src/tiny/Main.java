package tiny;

import tiny.parser.*;
import tiny.lexer.*;
import tiny.node.*;
import java.io.*;
import java.util.ArrayList;

class Main {

    private static double gVersion = 1.0;
    private static boolean mPrettyPrint = false;
    private static boolean mSymbolTableDump = false;
    private static boolean mSymbolTableDumpAll = false;

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Please add a file as an argument");
            System.exit(1);
        }
        
        if (args[0].toString().toUpperCase().contains("-H"))
        {
            PrintHelpInfo();
            System.exit(0);
        } 
        else if (args[0].toString().toUpperCase().contains("-V"))
        {
            PrintVersionInfo();
            System.exit(0);
        }
        
        String fullfileName = args[0];
        String fileName = args[0];
        if (fileName.lastIndexOf('.') != -1) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.')); //fileName.substring(0, fileName.length()-4);
        }
        for (int i = 1; i < args.length; i++) {
            String currFlag = args[i].toUpperCase();
            if (currFlag.toUpperCase().equals("-PPTYPE")) {
                mPrettyPrint = true;
                File currentFile = new File(fileName + ".pptype.go");
                currentFile.delete();
            } else if (currFlag.toUpperCase().equals("-DUMPSYMTAB")) {
                mSymbolTableDump = true;
                File currentFile = new File(fileName + ".symtab");
                currentFile.delete();
            } else if (currFlag.toUpperCase().equals("-DUMPSYMTABALL")) {
                mSymbolTableDumpAll = true;
                File currentFile = new File(fileName + ".symtab");
                currentFile.delete();
            } else if (currFlag.toUpperCase().equals("-H"))
            {
                PrintHelpInfo();
            } else if (currFlag.toUpperCase().equals("-V"))
            {
                PrintVersionInfo();
            }
            
        }
        try {
            FileReader infile = new FileReader(fullfileName);
            Lexer lexer = new GoLexer(new PushbackReader(new BufferedReader(infile), 1024));
            Parser p = new Parser(lexer);

            Start tree = p.parse();

            Weeder currWeeder = new Weeder();
            tree.apply(currWeeder);

            TypeChecker tc = new TypeChecker(fileName, mSymbolTableDump, mSymbolTableDumpAll);
            tree.apply(tc);
            tc.outputSymtableEnd();

            if (mPrettyPrint) {
                PrettyPrinter pp = new PrettyPrinter();
                tree.apply(pp);
                pp.createFile(fileName);
            }

            CodeGenerator currGenerator = new CodeGenerator(tc);
            tree.apply(currGenerator);
            currGenerator.createFile(fileName);

            System.out.print("VALID");

        } catch (Exception e) {
            System.out.print("INVALID " + e);
            System.exit(1);
        }

        System.exit(0);
    }

    
    private static void PrintVersionInfo()
    {
        System.out.println("Current COMP520-Group 13 GoLite To Python Compiler version: " + gVersion);
        System.out.println("@authors : Laurence Stolzenberg & Maxence Hull");
        System.out.println("Compiler uses SableCC, all rights to original authors");
        System.out.println("Compiler based on GoLite Standard defined in COMP520 Compiler Course, Winter 2017");
    }
    
        private static void PrintHelpInfo()
    {
        System.out.println("Welcome to the GoLite to Python compiler, by COMP520 Group 13");
        System.out.println("Call the program giving, as the first parameter, the GoLite file to compile");
        System.out.println("You can then specify one or more of five flags specified below");
        System.out.println("");
        System.out.println("");
        System.out.println("-DUMPSYMTAB specifies to dump the symbol table each time you exit a scope");
        System.out.println("The symbol table is dumped into a file with the same name as the imput file and extension .SYMTAB");
        System.out.println("it will be in the same directory as the input file");
        System.out.println("");
        System.out.println("");
        System.out.println("-DUMPSYMTABALL is almost identical to the above information, but we dump the entire symbol table, not just the scope");
        System.out.println("");
        System.out.println("");
        System.out.println("-PPYTPE is a \"pretty Printer\", meaning it takes the input file, parses it, beautifies it to make it easier to read");
        System.out.println("and prints that out in the same directory as the input file, with the same name and a \".pptype.go\" extension.");
        System.out.println("");
        System.out.println("");
        System.out.println("-V gives the version information");
        System.out.println("");
        System.out.println("");
        System.out.println("-H gives the help (that you are reading...)");
        System.out.println("");
        System.out.println("");
        System.out.println("Please note that the help and version flags can be used without specifying a file name, in which case the information is shown and the program exits");
    }

    /*public static void PrettyPrinterTestCase(String iOriginalFileName) {
        String fileName = iOriginalFileName;
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        try {
            for (int i = 0; i < 5; i++) {
                FileReader infile = new FileReader(iOriginalFileName);
                Lexer lexer = new GoLexer(new PushbackReader(new BufferedReader(infile), 1024));
                Parser p = new Parser(lexer);

                Start tree = p.parse();

                Weeder currWeeder = new Weeder();
                tree.apply(currWeeder);

                PrettyPrinter pp = new PrettyPrinter();
                tree.apply(pp);

                pp.createFile(fileName + i);
                iOriginalFileName = fileName + i + ".pretty.min";
            }
        } catch (Exception e) {
        }
        System.out.println("Pretty Printer did nor crash. Compare printed files manually");
    }*/
}
