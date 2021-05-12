import java.util.Map;
import java.util.LinkedHashMap;

public class FunctionTable {
    ClassTable parent;
    String name, type;
    LinkedHashMap<String, String> args;
    // String[] vars; //current var types

    LinkedHashMap<String, String> localVars;

    FunctionTable(ClassTable parent, String name, String type){
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.args = new LinkedHashMap<String, String>();
        this.localVars = new LinkedHashMap<String, String>();
    }

    String canAccessVariable(String name) {
        //check if name is in local variables
        String ret = this.localVars.get(name);

        //if not check if it is an argument
        if (ret == null) {
            ret = this.args.get(name);
        }

        // if not check if it is a field
        if (ret == null) {
            ret = this.parent.hasField(name);
        }

        return ret;
    }

    FunctionTable canAccessMethod(String name) {
        //check if name is in parent class methods
        FunctionTable ret = this.parent.hasMethod(name);

        return ret;
    }

    @Override
    public String toString() {
        String ret = this.type+' '+this.name+'(';
        String temp = "";
        for(Map.Entry<String,String> entry : this.args.entrySet()){
            temp += entry.getValue()+" "+entry.getKey()+",";
        }
        
        if (temp != "") {
            temp = temp.substring(0, temp.length()-1);
        }

        return ret+temp+')';
    }
}
