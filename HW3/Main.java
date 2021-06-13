import syntaxtree.*;
import visitor.*;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

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
                LLVMVisitor llvm = new LLVMVisitor();
                Goal root = parser.Goal();
                
                // fill symbol table
                root.accept(symb, symbols);
                
                // typechecking
                root.accept(types, symbols);
                
                // get offsets
                ClassOffsetsContainer offsets = new ClassOffsetsContainer(symbols);
                
                // create wanted file
                String filename = (getFileName(args[i])+".ll");
                File llfile = new File(filename);
                if (llfile.createNewFile()) {
                    System.out.println("File created: " + llfile.getName());
                } else {
                    System.out.println("File already exists.");
                    throw new RuntimeException();
                }

                // create filewriter to pass to visitor
                BufferedWriter out = new BufferedWriter(new FileWriter(filename));

                // setup llvm visitor
                llvm.setOffsets(symbols, offsets, out);
                llvm.writeStartingLLThings(out);

                // produce ll file
                root.accept(llvm, null);

                // close writer
                out.close();

                System.out.println("File " + args[i] + " parsed successfully.");
                System.out.println("----------------------------------------------");

                success++;
            } catch (FileNotFoundException e) {
                System.err.println(e);
            } catch (ParseException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            } catch (RuntimeException e) {
                System.out.println("File " + args[i] + " not parsed successfully. Error encountered:");
                System.out.println("\t" + e);
                System.out.println("----------------------------------------------");
            }

        }
        System.out.println("Successfully parsed " + success + "/" + args.length + " files.");
    }

    public static String getFileName(String myfile) {
        String filename = "";
        String[] parts = myfile.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            filename = filename + parts[i];
        }

        if (filename != "") {
            return filename;
        }
        else {
            throw new RuntimeException();
        }
    }
}
