import java.util.LinkedHashMap;
import java.util.Map;


public class ClassTable {

    String name, parent;
    LinkedHashMap<String, String> fields;
    LinkedHashMap<String, FunctionTable> methods;
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
        for (String key : this.fields.keySet()) {
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
            System.out.println("\t"+entry.getValue().type+' '+entry.getKey()+
                                                '('+entry.getValue().toString()+") :");

            for(Map.Entry<String,String> innerEntry : entry.getValue().localVars.entrySet()) {
                System.out.println("\t\t"+innerEntry.getValue()+' '+innerEntry.getKey());
            }
        }
    }
}