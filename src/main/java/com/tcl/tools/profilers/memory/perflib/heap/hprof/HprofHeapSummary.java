package com.tcl.tools.profilers.memory.perflib.heap.hprof;



import java.io.IOException;

public class HprofHeapSummary implements HprofRecord {
    public static final byte TAG = 7;
    public final int time;
    public final int totalLiveBytes;
    public final int totalLiveInstances;
    public final long totalBytesAllocated;
    public final long totalInstancesAllocated;

    public HprofHeapSummary(int time, int totalLiveBytes, int totalLiveInstances, long totalBytesAllocated, long totalInstancesAllocated) {
        this.time = time;
        this.totalLiveBytes = totalLiveBytes;
        this.totalLiveInstances = totalLiveInstances;
        this.totalBytesAllocated = totalBytesAllocated;
        this.totalInstancesAllocated = totalInstancesAllocated;
    }

    public void write(HprofOutputStream hprof) throws IOException {
        hprof.writeRecordHeader((byte)7, this.time, 24);
        hprof.writeU4(this.totalLiveBytes);
        hprof.writeU4(this.totalLiveInstances);
        hprof.writeU8(this.totalBytesAllocated);
        hprof.writeU8(this.totalInstancesAllocated);
    }
}
