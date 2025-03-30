package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofRootStickyClass implements HprofDumpRecord {
    public static final byte SUBTAG = 5;
    public final long objectId;

    public HprofRootStickyClass(long objectId) {
        this.objectId = objectId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)5);
        hprof.writeId(this.objectId);
    }

    public int getLength(int idSize) {
        return 1 + idSize;
    }
}
