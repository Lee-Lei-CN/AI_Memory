package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofConstant {
    public final short constantPoolIndex;
    public final byte typeOfEntry;
    public final long value;

    public HprofConstant(short constantPoolIndex, byte typeOfEntry, long value) {
        this.constantPoolIndex = constantPoolIndex;
        this.typeOfEntry = typeOfEntry;
        this.value = value;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU2(this.constantPoolIndex);
        hprof.writeU1(this.typeOfEntry);
        hprof.writeValue(this.typeOfEntry, this.value);
    }

    public int getLength(int idSize) {
        return 3 + HprofType.sizeOf(this.typeOfEntry, idSize);
    }
}
