package com.tcl.tools.profilers.memory.perflib.heap.hprof;


import java.io.IOException;
import java.io.OutputStream;

public class HprofOutputStream extends OutputStream {
    private int mIdSize;
    private OutputStream mOutputStream;

    public HprofOutputStream(int idSize, OutputStream os) {
        this.mIdSize = idSize;
        if (idSize != 1 && idSize != 2 && idSize != 4 && idSize != 8) {
            throw new IllegalArgumentException("Unsupproted id size: " + idSize);
        } else {
            this.mOutputStream = os;
        }
    }

    public void close() throws IOException {
        this.mOutputStream.close();
    }

    public void flush() throws IOException {
        this.mOutputStream.flush();
    }

    public void write(byte[] b) throws IOException {
        this.mOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.mOutputStream.write(b, off, len);
    }

    public void write(int b) throws IOException {
        this.mOutputStream.write(b);
    }

    public void writeU1(byte data) throws IOException {
        this.mOutputStream.write(data);
    }

    public void writeU2(short data) throws IOException {
        this.writeU1((byte)(data >> 8));
        this.writeU1((byte)(data >> 0));
    }

    public void writeU4(int data) throws IOException {
        this.writeU1((byte)(data >> 24));
        this.writeU1((byte)(data >> 16));
        this.writeU1((byte)(data >> 8));
        this.writeU1((byte)(data >> 0));
    }

    public void writeU8(long data) throws IOException {
        this.writeU1((byte)((int)(data >> 56)));
        this.writeU1((byte)((int)(data >> 48)));
        this.writeU1((byte)((int)(data >> 40)));
        this.writeU1((byte)((int)(data >> 32)));
        this.writeU1((byte)((int)(data >> 24)));
        this.writeU1((byte)((int)(data >> 16)));
        this.writeU1((byte)((int)(data >> 8)));
        this.writeU1((byte)((int)(data >> 0)));
    }

    public void writeId(long data) throws IOException {
        this.writeSized(this.mIdSize, data);
    }

    public void writeValue(byte type, long data) throws IOException {
        this.writeSized(HprofType.sizeOf(type, this.mIdSize), data);
    }

    public void writeRecordHeader(byte tag, int time, int length) throws IOException {
        this.writeU1(tag);
        this.writeU4(time);
        this.writeU4(length);
    }

    public int getIdSize() {
        return this.mIdSize;
    }

    private void writeSized(int size, long data) throws IOException {
        switch(size) {
            case 1:
                this.writeU1((byte)((int)data));
                break;
            case 2:
                this.writeU2((short)((int)data));
                break;
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                throw new IllegalStateException("Unexpected size: " + size);
            case 4:
                this.writeU4((int)data);
                break;
            case 8:
                this.writeU8(data);
        }

    }
}
