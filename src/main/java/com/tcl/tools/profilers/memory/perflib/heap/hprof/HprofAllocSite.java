package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofAllocSite {
    public static final int LENGTH = 25;
    public final byte arrayIndicator;
    public final int classSerialNumber;
    public final int stackTraceSerialNumber;
    public final int numberOfLiveBytes;
    public final int numberOfLiveInstances;
    public final int numberOfBytesAllocated;
    public final int numberOfInstancesAllocated;

    public HprofAllocSite(byte arrayIndicator, int classSerialNumber, int stackTraceSerialNumber, int numberOfLiveBytes, int numberOfLiveInstances, int numberOfBytesAllocated, int numberOfInstancesAllocated) {
        this.arrayIndicator = arrayIndicator;
        this.classSerialNumber = classSerialNumber;
        this.stackTraceSerialNumber = stackTraceSerialNumber;
        this.numberOfLiveBytes = numberOfLiveBytes;
        this.numberOfLiveInstances = numberOfLiveInstances;
        this.numberOfBytesAllocated = numberOfBytesAllocated;
        this.numberOfInstancesAllocated = numberOfInstancesAllocated;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeU1(this.arrayIndicator);
        hprof.writeU4(this.classSerialNumber);
        hprof.writeU4(this.stackTraceSerialNumber);
        hprof.writeU4(this.numberOfLiveBytes);
        hprof.writeU4(this.numberOfLiveInstances);
        hprof.writeU4(this.numberOfBytesAllocated);
        hprof.writeU4(this.numberOfInstancesAllocated);
    }
}
