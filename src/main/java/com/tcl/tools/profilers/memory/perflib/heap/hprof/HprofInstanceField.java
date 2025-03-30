package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofInstanceField {
    public final long fieldNameStringId;
    public final byte typeOfField;

    public HprofInstanceField(long fieldNameStringId, byte typeOfField) {
        this.fieldNameStringId = fieldNameStringId;
        this.typeOfField = typeOfField;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeId(this.fieldNameStringId);
        hprof.writeU1(this.typeOfField);
    }

    public int getLength(int idSize) {
        return idSize + 1;
    }
}
