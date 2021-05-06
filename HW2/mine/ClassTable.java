import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ClassTable {
    String name, parent;
    Map<String, String> fields;
    Map<String, FunctionTable> methods;
    Integer offset, size;

    ClassTable(String newname) {
        this.name = newname;
        this.parent = null;
        this.fields = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, FunctionTable>();
        this.offset = 0;
    }

    ClassTable(String newname, String newparent, Integer newoffset) {
        this.name = newname;
        this.parent = newparent;
        this.fields = new LinkedHashMap<String, String>();
        this.methods = new LinkedHashMap<String, FunctionTable>();
        this.offset = newoffset;
    }

    public Integer size() {
        /*Return the size (sum of offsets) of all fields and methods*/
        Integer sum = 0;
        Set<String> keys = this.fields.keySet();
        for (String key : keys) {
            switch (fields.get(key)) {
                case "Integer":
                    sum += 4;
                    break;
                case "Boolean":
                    sum += 1;
                    break;
                default: //array pointer
                    sum += 8;
                    break;
            }
        }

        for (Integer i = 0; i < this.methods.size(); i++) {
            sum += 8; //every method is a pointer
        }

        return sum;
    }
}