import java.util.LinkedHashMap;
import java.util.Map;

public class ClassOffsets {
    LinkedHashMap<String, Integer> variableOffsets;
    LinkedHashMap<String, Integer> methodOffsets;
    Integer varOffset;
    Integer methodOffset;

    ClassOffsets() {
        this.variableOffsets = new LinkedHashMap<String, Integer>();
        this.methodOffsets = new LinkedHashMap<String, Integer>();
    }

    public void print() {
        System.out.println("---Variables---");
        for (Map.Entry<String, Integer> entry : this.variableOffsets.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("---Methods---");
        for (Map.Entry<String, Integer> entry : this.methodOffsets.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
