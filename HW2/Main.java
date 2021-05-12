import syntaxtree.*;
import visitor.*;
import java.io.*;

public class Main {
    public static void main (String [] args) {
        int success = 0;
        if (args.length < 1) {
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ...");
            System.exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            try {
                // proccess each file
                
                FileInputStream fis = new FileInputStream(args[i]);
                SymbolTable symbols = new SymbolTable();
                MiniJavaParser parser = new MiniJavaParser(fis);
                
                FillSymbolTableVisitor symb = new FillSymbolTableVisitor();
                TypecheckVisitor types = new TypecheckVisitor();
                Goal root = parser.Goal();

                // fill symbol table
                root.accept(symb, symbols);

                // typechecking
                root.accept(types, symbols);

                // print offsets
                symbols.print();
                
                System.out.println("File " + args[i] + " parsed successfully.");
                System.out.println("----------------------------------------------");

                success++;
            }
            catch(FileNotFoundException e) {
                System.err.println(e);
            }
            catch(ParseException e) {
                System.err.println(e);
            }
            catch(RuntimeException e) {
                System.out.println("File " + args[i] + " not parsed successfully. Error encountered:");
                System.out.println("\t"+e);
                System.out.println("----------------------------------------------");
            }

        }
        System.out.println("Successfully parsed "+success+"/"+args.length+" files.");
    }
}
