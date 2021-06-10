import java.util.LinkedHashMap;
import java.util.Map;

public class ClassTable {
    // SymbolTable table;
    String name;
    ClassTable parent;
    LinkedHashMap<String, String> fields;
    LinkedHashMap<String, FunctionTable> methods;

    ClassTable(String newname) {
        this.name = newname;
        this.parent = null;
        this.fields = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, FunctionTable>();
    }

    ClassTable(String newname, ClassTable newparent) {
        this.name = newname;
        this.parent = newparent;
        this.fields = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, FunctionTable>();
    }

    String hasField(String name) {
        // check if it exists in local fields
        String ret = this.fields.get(name);

        // if it does't, check in parent recursively
        if (ret == null && this.parent != null) {
            ret = this.parent.hasField(name);
        }

        return ret;
    }

    FunctionTable hasMethod(String name) {
        // check if it exists in local methods
        FunctionTable ret = this.methods.get(name);

        // if it does't, check in parent recursively
        if (ret == null && this.parent != null) {
            ret = this.parent.hasMethod(name);
        }

        return ret;
    }

    public void setOffsets(LinkedHashMap<String, ClassOffsets> classOffsets) {
        // Create current class entry
        String currentClass = this.name;
        ClassOffsets currentClassOffsets = new ClassOffsets();
        classOffsets.put(currentClass, currentClassOffsets);

        // get starting offsets
        Integer varOffset = 0;
        Integer methodOffset = 0;
        if (this.parent != null) {
            varOffset = classOffsets.get(this.parent.name).varOffset;
            methodOffset = classOffsets.get(this.parent.name).methodOffset;
        }

        // Variables
        for (Map.Entry<String, String> entry : this.fields.entrySet()) {
            // skip main function
            if (entry.getKey() == "main") {
                continue;
            }

            // add variable name and offset
            currentClassOffsets.variableOffsets.put(entry.getKey(), varOffset);

            // increase offset
            switch (entry.getValue()) {
                case "int":
                    varOffset += 4;
                    break;
                case "boolean":
                    varOffset += 1;
                    break;
                default:
                    varOffset += 8; // if it is a class or int[]
                    break;
            }
        }

        // Methods
        for (Map.Entry<String, FunctionTable> entry : this.methods.entrySet()) {
            // skip main function
            if (entry.getKey() == "main") {
                continue;
            }
            // check if entry is an overidden method, then skip it
            if (this.parent != null && this.parent.hasMethod(entry.getKey()) != null) {
                continue;
            }

            // add method name and offset
            currentClassOffsets.methodOffsets.put(entry.getKey(), methodOffset);
            methodOffset += 8;
        }

        currentClassOffsets.varOffset = varOffset;
        currentClassOffsets.methodOffset = methodOffset;
    }
}