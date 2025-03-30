package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;

public class HprofCpuSample {
    public static final int LENGTH = 8;
    public final int numberOfSamples;
    public final int stackTraceSerialNumber;

    public HprofCpuSample(int numberOfSamples, int stackTraceSerialNumber) {
        this.numberOfSamples = numberOfSamples;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU4(this.numberOfSamples);
        hprof.writeU4(this.stackTraceSerialNumber);
    }
}
