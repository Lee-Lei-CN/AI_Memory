package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofStaticField {
    public final long staticFieldNameStringId;
    public final byte typeOfField;
    public final long value;

    public HprofStaticField(long staticFieldNameStringId, byte typeOfField, long value) {
        this.staticFieldNameStringId = staticFieldNameStringId;
        this.typeOfField = typeOfField;
        this.value = value;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeId(this.staticFieldNameStringId);
        hprof.writeU1(this.typeOfField);
        hprof.writeValue(this.typeOfField, this.value);
    }

    public int getLength(int idSize) {
        return idSize + 1 + HprofType.sizeOf(this.typeOfField, idSize);
    }
}
