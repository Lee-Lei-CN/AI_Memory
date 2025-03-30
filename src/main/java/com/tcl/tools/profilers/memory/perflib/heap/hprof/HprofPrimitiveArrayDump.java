package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofPrimitiveArrayDump implements HprofDumpRecord {
    public static final byte SUBTAG = 35;
    public final long arrayObjectId;
    public final int stackTraceSerialNumber;
    public final byte elementType;
    public final long[] elements;

    public HprofPrimitiveArrayDump(long arrayObjectId, int stackTraceSerialNumber, byte elementType, long[] elements) {
        this.arrayObjectId = arrayObjectId;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.elementType = elementType;
        this.elements = elements;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1((byte)35);
        hprof.writeId(this.arrayObjectId);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeU4(this.elements.length);
        hprof.writeU1(this.elementType);
        long[] var2 = this.elements;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            long element = var2[var4];
            hprof.writeValue(this.elementType, element);
        }

    }

    public int getLength(int idSize) {
        return 1 + idSize + 4 + 4 + 1 + this.elements.length * HprofType.sizeOf(this.elementType, idSize);
    }
}

