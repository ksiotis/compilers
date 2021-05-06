import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    Map<String,ClassTable> classes;
    Map<String,FunctionTable> methods;
    Map<String, String> vars;

    ClassTable currentClass;
    FunctionTable currentFunction;

    SymbolTable() {
        this.classes = new LinkedHashMap<String,ClassTable>();
        this.methods = new LinkedHashMap<String,FunctionTable>();
        this.vars = new LinkedHashMap<String, String>();

        currentClass = null;
        currentFunction = null;
    }
}