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

    // public Integer size() {

    //     /*Return the size (sum of offsets) of all fields and methods*/
    //     Integer sum = 0;
    //     for (String key : this.fields.keySet()) {
    //         switch (fields.get(key)) {
    //             case "Integer":
    //                 sum += 4;
    //                 break;
    //             case "Boolean":
    //                 sum += 1;
    //                 break;
    //             default: //array pointer
    //                 sum += 8;
    //                 break;
    //         }
    //     }

    //     for (Integer i = 0; i < this.methods.size(); i++) {
    //         sum += 8; //every method is a pointer
    //     }

    //     return sum;
    // }

    // public void updateSize() {
    //     this.size = this.size();
    // }

    String hasField(String name) {
        //check if it exists in local fields
        String ret = this.fields.get(name);
        
        //if it does't, check in parent recursively
        if (ret == null && this.parent != null) {
            ret = this.parent.hasField(name);
        }

        return ret;
    }

    FunctionTable hasMethod(String name) {
        //check if it exists in local methods
        FunctionTable ret = this.methods.get(name);
        
        //if it does't, check in parent recursively
        if (ret == null && this.parent != null) {
            ret = this.parent.hasMethod(name);
        }

        return ret;
    }

    //for debugging
    public void print() {
        System.out.println("\nClassTable: "+ this.name);
        System.out.println("parent: "+this.parent);
        System.out.println("fields: ");

        for(Map.Entry<String,String> entry : this.fields.entrySet()) {
            System.out.println("\t"+entry.getValue()+" "+entry.getKey());
        }

        System.out.println("methods: ");
        for(Map.Entry<String,FunctionTable> entry : this.methods.entrySet()) {
            System.out.println("\t"+entry.getValue().toString()+" :");

            for(Map.Entry<String,String> innerEntry : entry.getValue().localVars.entrySet()) {
                System.out.println("\t\t"+innerEntry.getValue()+' '+innerEntry.getKey());
            }
        }
    }
}