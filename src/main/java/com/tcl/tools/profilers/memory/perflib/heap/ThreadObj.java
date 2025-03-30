package com.tcl.tools.profilers.memory.perflib.heap;

public class ThreadObj {
    long mId;
    int mStackTrace;

    public ThreadObj(long id, int stackTrace) {
        this.mId = id;
        this.mStackTrace = stackTrace;
    }
}