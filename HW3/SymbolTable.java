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

    public void print() {
        // container for offsets
        LinkedHashMap<String,Offset> classOffsets = new LinkedHashMap<String,Offset>();

        // for each class go store its offsets
        for(Map.Entry<String,ClassTable> entry : this.classes.entrySet()) {
            

            // System.out.println("-----------Class "+entry.getKey()+"-----------");
            // Offset currentOffsets = entry.getValue().print(classOffsets);
            // classOffsets.put(entry.getKey(), currentOffsets);

            // System.out.println("");
        }
    }
}