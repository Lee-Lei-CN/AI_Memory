package com.tcl.tools.profilers.memory.perflib.heap;


import com.google.common.collect.Maps;

import java.util.Map;

public enum Type {
    OBJECT(2, 0, (String)null, (String)null),
    BOOLEAN(4, 1, "boolean[]", "[Z"),
    CHAR(5, 2, "char[]", "[C"),
    FLOAT(6, 4, "float[]", "[F"),
    DOUBLE(7, 8, "double[]", "[D"),
    BYTE(8, 1, "byte[]", "[B"),
    SHORT(9, 2, "short[]", "[S"),
    INT(10, 4, "int[]", "[I"),
    LONG(11, 8, "long[]", "[J");

    private static Map<Integer, Type> sTypeMap = Maps.newHashMap();
    private int mId;
    private int mSize;
    private String mAndroidArrayName;
    private String mJavaArrayName;

    private Type(int type, int size, String androidArrayName, String javaArrayName) {
        this.mId = type;
        this.mSize = size;
        this.mAndroidArrayName = androidArrayName;
        this.mJavaArrayName = javaArrayName;
    }

    public static Type getType(int id) {
        return (Type)sTypeMap.get(id);
    }

    public int getSize() {
        return this.mSize;
    }

    public int getTypeId() {
        return this.mId;
    }

    public String getClassNameOfPrimitiveArray(boolean useJavaName) {
        if (this == OBJECT) {
            throw new IllegalArgumentException("OBJECT type is not a primitive type");
        } else {
            return useJavaName ? this.mJavaArrayName : this.mAndroidArrayName;
        }
    }

    static {
        Type[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            Type type = var0[var2];
            sTypeMap.put(type.mId, type);
        }

    }
}
