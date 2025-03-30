package com.tcl.tools.profilers.memory.perflib.heap;



public class StackTrace {
    int mSerialNumber;
    int mThreadSerialNumber;
    StackFrame[] mFrames;
    StackTrace mParent = null;
    int mOffset = 0;

    private StackTrace() {
    }

    public StackTrace(int serial, int thread, StackFrame[] frames) {
        this.mSerialNumber = serial;
        this.mThreadSerialNumber = thread;
        this.mFrames = frames;
    }

    public final StackTrace fromDepth(int startingDepth) {
        StackTrace result = new StackTrace();
        if (this.mParent != null) {
            result.mParent = this.mParent;
        } else {
            result.mParent = this;
        }

        result.mOffset = startingDepth + this.mOffset;
        return result;
    }

    public final void dump() {
        int N = this.mFrames.length;

        for(int i = 0; i < N; ++i) {
            System.out.println(this.mFrames[i].toString());
        }

    }

    public StackFrame[] getFrames() {
        return this.mFrames;
    }

    public int getSerialNumber() {
        return this.mSerialNumber;
    }
}
