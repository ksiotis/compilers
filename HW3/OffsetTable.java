public class OffsetTable {
    LinkedHashMap<String,ClassTable> offsets;

    OffsetTable(Integer var, Integer method) {
        this.varOffset = var;
        this.methodOffset = method;
    }
}
