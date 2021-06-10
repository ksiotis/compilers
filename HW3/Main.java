import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
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

                // get offsets
                LinkedHashMap<String, ClassOffsets> offsets = symbols.getOffsets();
                // printing for debugging //TODO remove
                for (Map.Entry<String, ClassOffsets> entry : offsets.entrySet()) {
                    System.out.println("-----------Class " + entry.getKey() + "-----------");
                    entry.getValue().print();
                    System.out.println("---------------------------------------\n");
                }

                System.out.println("File " + args[i] + " parsed successfully.");
                System.out.println("----------------------------------------------");

                success++;
            } catch (FileNotFoundException e) {
                System.err.println(e);
            } catch (ParseException e) {
                System.err.println(e);
            } catch (RuntimeException e) {
                System.out.println("File " + args[i] + " not parsed successfully. Error encountered:");
                System.out.println("\t" + e);
                System.out.println("----------------------------------------------");
            }

        }
        System.out.println("Successfully parsed " + success + "/" + args.length + " files.");
    }
}
