package com.tcl.tools.profilers.memory.perflib.heap;


public class StackFrame {
    public static final int NO_LINE_NUMBER = 0;
    public static final int UNKNOWN_LOCATION = -1;
    public static final int COMPILED_METHOD = -2;
    public static final int NATIVE_METHOD = -3;
    long mId;
    String mMethodName;
    String mSignature;
    String mFilename;
    int mSerialNumber;
    int mLineNumber;

    public StackFrame(long id, String method, String sig, String file, int serial, int line) {
        this.mId = id;
        this.mMethodName = method;
        this.mSignature = sig;
        this.mFilename = file;
        this.mSerialNumber = serial;
        this.mLineNumber = line;
    }

    private String lineNumberString() {
        switch(this.mLineNumber) {
            case -3:
                return "Native method";
            case -2:
                return "Compiled method";
            case -1:
                return "Unknown line number";
            case 0:
                return "No line number";
            default:
                return String.valueOf(this.mLineNumber);
        }
    }

    public String getMethodName() {
        return this.mMethodName;
    }

    public String getSignature() {
        return this.mSignature;
    }

    public String getFilename() {
        return this.mFilename;
    }

    public int getLineNumber() {
        return this.mLineNumber;
    }

    public final String toString() {
        return this.mMethodName + this.mSignature.replace('/', '.') + " - " + this.mFilename + ":" + this.lineNumberString();
    }
}