import syntaxtree.*;
import visitor.*;
import java.io.*;

public class Main {
    public static void main (String [] args) {
        if(args.length < 1) {
            System.err.println("Usage: java Main <inputFile1> ...");
            System.exit(1);
        }
        FileInputStream fis = null;
        try {
            // for (int i = 1; i < args.length; i++) {
                // fis = new FileInputStream(args[i-1]);
                fis = new FileInputStream(args[0]);
                SymbolTable symbols = new SymbolTable();
                MiniJavaParser parser = new MiniJavaParser(fis);
                // System.err.println("File " + i + " parsed successfully.");
                // System.err.println("File " + 0 + " parsed successfully.");
                FillSymbolTableVisitor symb = new FillSymbolTableVisitor();
                TypecheckVisitor types = new TypecheckVisitor();
                Goal root = parser.Goal();
                root.accept(symb, symbols);
                symbols.print();
                root.accept(types, symbols);
            // }
        }
        catch(ParseException ex) {
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        finally {
            try {
                if (fis != null) fis.close();
            }
            catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
