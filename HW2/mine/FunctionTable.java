import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionTable {
    String name, type;
    Map<String, String> args;
    String[] vars; //current var types

    FunctionTable(String name, String type){
        args = new LinkedHashMap<String, String>();
    }

    public static void main(String[] args) {
        
    }
}
