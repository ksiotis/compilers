import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SymbolTable {
    LinkedHashMap<String,ClassTable> classes;

    ClassTable currentClass;
    FunctionTable currentFunction;
    List<String> vars; //current var types

    SymbolTable() {
        this.classes = new LinkedHashMap<String,ClassTable>();
        this.vars = new ArrayList<String>();

        currentClass = null;
        currentFunction = null;
    }

    //for debugging
    public void print() {
        for(Map.Entry<String,ClassTable> entry : this.classes.entrySet()) {
            entry.getValue().print();
        }
    }
}