package com.tcl.tools.profilers.memory.perflib.captures;


import java.nio.ByteOrder;

public interface DataBuffer {
    ByteOrder HPROF_BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    void dispose();

    void append(byte[] data);

    void read(byte[] out);

    void readSubSequence(byte[] b, int sourceStart, int sourceEnd);

    byte readByte();

    char readChar();

    short readShort();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    void setPosition(long position);

    long position();

    boolean hasRemaining();

    long remaining();
}