package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofHeapDumpInfo implements HprofDumpRecord {
    public static final byte SUBTAG = -2;
    public static final int HEAP_DEFAULT = 0;
    public static final int HEAP_ZYGOTE = 90;
    public static final int HEAP_APP = 65;
    public static final int HEAP_IMAGE = 73;
    public final int heapType;
    public final long heapNameStringId;

    public HprofHeapDumpInfo(int heapType, long heapNameStringId) {
        this.heapType = heapType;
        this.heapNameStringId = heapNameStringId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)-2);
        hprof.writeU4(this.heapType);
        hprof.writeId(this.heapNameStringId);
    }

    public int getLength(int idSize) {
        return 5 + idSize;
    }
}
