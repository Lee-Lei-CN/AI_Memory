package com.tcl.tools.profilers.memory.perflib.heap.analysis;

public class ComputationProgress {
    private String mMessage;
    private double mProgress;

    public ComputationProgress(String message, double progress) {
        this.mMessage = message;
        this.mProgress = progress;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public double getProgress() {
        return this.mProgress;
    }

    public void setProgress(double progress) {
        this.mProgress = progress;
    }
}
