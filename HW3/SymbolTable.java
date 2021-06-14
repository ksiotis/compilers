import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SymbolTable {
    LinkedHashMap<String, ClassTable> classes;

    ClassTable currentClass;
    FunctionTable currentFunction;
    List<String> vars; // current var types

    SymbolTable() {
        this.classes = new LinkedHashMap<String, ClassTable>();
        this.vars = new ArrayList<String>();

        currentClass = null;
        currentFunction = null;
    }

    public LinkedHashMap<String, ClassOffsets> getOffsets() {
        LinkedHashMap<String, ClassOffsets> classOffsets = new LinkedHashMap<String, ClassOffsets>();

        // for each class go store its offsets
        for (Map.Entry<String, ClassTable> entry : this.classes.entrySet()) {
            // add offsets to current class entry
            entry.getValue().setOffsets(classOffsets);
        }

        return classOffsets;
    }

    Integer fieldNum(String name) {
        Integer count = 0;

        ClassTable tarClass = this.classes.get(name);
        while (tarClass != null) {
            count += tarClass.fields.size();
            tarClass = tarClass.parent;
        }

        return count;
    }
}