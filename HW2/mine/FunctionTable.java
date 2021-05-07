import java.util.Map;
import java.util.LinkedHashMap;

public class FunctionTable {
    String name, type;
    LinkedHashMap<String, String> args;
    String[] vars; //current var types

    LinkedHashMap<String, String> localVars;

    FunctionTable(String name, String type){
        this.name = name;
        this.type = type;
        this.args = new LinkedHashMap<String, String>();
        this.localVars = new LinkedHashMap<String, String>();
    }

    @Override
    public String toString() {
        String ret = "";

        for(Map.Entry<String,String> entry : this.args.entrySet()){
            ret += entry.getValue()+" "+entry.getKey()+",";
        }
        
        if (ret != "") {
            ret = ret.substring(0, ret.length()-1);
        }

        return ret;
    }
}
