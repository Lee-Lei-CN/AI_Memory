package com.tcl.tools.profilers.memory.perflib.heap.io;


import com.tcl.tools.profilers.memory.perflib.captures.DataBuffer;

import java.nio.ByteBuffer;

public class InMemoryBuffer implements DataBuffer {
    private final ByteBuffer mBuffer;

    public InMemoryBuffer(int capacity) {
        this.mBuffer = ByteBuffer.allocateDirect(capacity);
    }

    public InMemoryBuffer(byte[] data) {
        this.mBuffer = ByteBuffer.wrap(data);
    }

    public InMemoryBuffer(ByteBuffer data) {
        this.mBuffer = data;
        this.mBuffer.rewind();
    }

    public void dispose() {
    }

    public ByteBuffer getDirectBuffer() {
        return this.mBuffer;
    }

    public byte readByte() {
        return this.mBuffer.get();
    }

    public void append(byte[] data) {
    }

    public void read(byte[] b) {
        this.mBuffer.get(b);
    }

    public void readSubSequence(byte[] b, int sourceStart, int sourceEnd) {
        this.mBuffer.slice().position(sourceStart).get(b);
    }

    public char readChar() {
        return this.mBuffer.getChar();
    }

    public short readShort() {
        return this.mBuffer.getShort();
    }

    public int readInt() {
        return this.mBuffer.getInt();
    }

    public long readLong() {
        return this.mBuffer.getLong();
    }

    public float readFloat() {
        return this.mBuffer.getFloat();
    }

    public double readDouble() {
        return this.mBuffer.getDouble();
    }

    public void setPosition(long position) {
        this.mBuffer.position((int)position);
    }

    public long position() {
        return (long)this.mBuffer.position();
    }

    public boolean hasRemaining() {
        return this.mBuffer.hasRemaining();
    }

    public long remaining() {
        return (long)this.mBuffer.remaining();
    }
}
