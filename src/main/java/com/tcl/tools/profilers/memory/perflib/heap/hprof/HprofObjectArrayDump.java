package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofObjectArrayDump implements HprofDumpRecord {
    public static final byte SUBTAG = 34;
    public final long arrayObjectId;
    public final int stackTraceSerialNumber;
    public final long arrayClassObjectId;
    public final long[] elements;

    public HprofObjectArrayDump(long arrayObjectId, int stackTraceSerialNumber, long arrayClassObjectId, long[] elements) {
        this.arrayObjectId = arrayObjectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.arrayClassObjectId = arrayClassObjectId;
        this.elements = elements;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)34);
        hprof.writeId(this.arrayObjectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeU4(this.elements.length);
        hprof.writeId(this.arrayClassObjectId);
        long[] var2 = this.elements;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            long element = var2[var4];
            hprof.writeId(element);
        }

    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + idSize + 4 + this.elements.length * idSize;
    }
}
