package com.tcl.tools.profilers.memory.perflib.heap.hprof;


public class HprofType {
    public static final byte TYPE_OBJECT = 2;
    public static final byte TYPE_BOOLEAN = 4;
    public static final byte TYPE_CHAR = 5;
    public static final byte TYPE_FLOAT = 6;
    public static final byte TYPE_DOUBLE = 7;
    public static final byte TYPE_BYTE = 8;
    public static final byte TYPE_SHORT = 9;
    public static final byte TYPE_INT = 10;
    public static final byte TYPE_LONG = 11;

    public HprofType() {
    }

    public static int sizeOf(byte type, int idSize) {
        switch(type) {
            case 2:
                return idSize;
            case 3:
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
            case 4:
                return 1;
            case 5:
                return 2;
            case 6:
                return 4;
            case 7:
                return 8;
            case 8:
                return 1;
            case 9:
                return 2;
            case 10:
                return 4;
            case 11:
                return 8;
        }
    }
}

