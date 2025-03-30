package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootDebugger implements HprofDumpRecord {
    public static final byte SUBTAG = -117;
    public final long objectId;

    public HprofRootDebugger(long objectId) {
        this.objectId = objectId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)-117);
        hprof.writeId(this.objectId);
    }

    public int getLength(int idSize) {
        return 1 + idSize;
    }
}
