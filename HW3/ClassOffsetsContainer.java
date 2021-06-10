import java.util.LinkedHashMap;
import java.util.Map;

public class ClassOffsetsContainer {
    LinkedHashMap<String, ClassOffsets> offsets;

    ClassOffsetsContainer(SymbolTable symbols) {
        this.offsets = symbols.getOffsets();
    }

    public void print() {
        // printing for debugging
        for (Map.Entry<String, ClassOffsets> entry : offsets.entrySet()) {
            System.out.println("-----------Class " + entry.getKey() + "-----------");
            entry.getValue().print();
            System.out.println("---------------------------------------\n");
        }
    }
}
