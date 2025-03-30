package com.tcl.tools.profilers.memory.perflib.heap.hprof;

import java.io.IOException;

public class HprofRootJniGlobal implements HprofDumpRecord {
    public static final byte SUBTAG = 1;
    public final long objectId;
    public final long jniGlobalRefId;

    public HprofRootJniGlobal(long objectId, long jniGlobalRefId) {
        this.objectId = objectId;
        this.jniGlobalRefId = jniGlobalRefId;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)1);
        hprof.writeId(this.objectId);
        hprof.writeId(this.jniGlobalRefId);
    }

    public int getLength(int idSize) {
        return 1 + 2 * idSize;
    }
}