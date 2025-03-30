package com.tcl.tools.profilers.memory.perflib.captures;



import com.tcl.tools.profilers.memory.perflib.ByteBufferUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

public class MemoryMappedFileBuffer implements DataBuffer {
    private static final int DEFAULT_SIZE = 1073741824;
    private static final int DEFAULT_PADDING = 1024;
    private final int mBufferSize;
    private final int mPadding;
    private final ByteBuffer[] mByteBuffers;
    private final long mLength;
    private long mCurrentPosition;

    public MemoryMappedFileBuffer(File f, int bufferSize, int padding) throws IOException {
        this.mBufferSize = bufferSize;
        this.mPadding = padding;
        this.mLength = f.length();
        int shards = (int)(this.mLength / (long)this.mBufferSize) + 1;
        this.mByteBuffers = new ByteBuffer[shards];
        FileInputStream inputStream = new FileInputStream(f);

        try {
            long offset = 0L;

            for(int i = 0; i < shards; ++i) {
                long size = Math.min(this.mLength - offset, (long)(this.mBufferSize + this.mPadding));
                this.mByteBuffers[i] = inputStream.getChannel().map(MapMode.READ_ONLY, offset, size);
                this.mByteBuffers[i].order(HPROF_BYTE_ORDER);
                offset += (long)this.mBufferSize;
            }

            this.mCurrentPosition = 0L;
        } finally {
            inputStream.close();
        }
    }

    public MemoryMappedFileBuffer(File f) throws IOException {
        this(f, 1073741824, 1024);
    }

    public void dispose() {
        ByteBuffer[] var1 = this.mByteBuffers;
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ByteBuffer buffer = var1[var3];
            ByteBufferUtil.cleanBuffer(buffer);
        }

    }

    public byte readByte() {
        byte result = this.mByteBuffers[this.getIndex()].get(this.getOffset());
        ++this.mCurrentPosition;
        return result;
    }

    public void append(byte[] data) {
    }

    public void read(byte[] b) {
        int index = this.getIndex();
        this.mByteBuffers[index].position(this.getOffset());
        if (b.length <= this.mByteBuffers[index].remaining()) {
            this.mByteBuffers[index].get(b, 0, b.length);
        } else {
            int split = this.mBufferSize - this.mByteBuffers[index].position();
            this.mByteBuffers[index].get(b, 0, split);
            this.mByteBuffers[index + 1].position(0);
            this.mByteBuffers[index + 1].get(b, split, b.length - split);
        }

        this.mCurrentPosition += (long)b.length;
    }

    public void readSubSequence(byte[] b, int sourceStart, int length) {
        assert (long)length < this.mLength;

        this.mCurrentPosition += (long)sourceStart;
        int index = this.getIndex();
        this.mByteBuffers[index].position(this.getOffset());
        if (b.length <= this.mByteBuffers[index].remaining()) {
            this.mByteBuffers[index].get(b, 0, b.length);
        } else {
            int split = this.mBufferSize - this.mByteBuffers[index].position();
            this.mByteBuffers[index].get(b, 0, split);
            int start = split;
            int remainingMaxLength = Math.min(length - split, b.length - split);
            int remainingShardCount = (remainingMaxLength + this.mBufferSize - 1) / this.mBufferSize;

            for(int i = 0; i < remainingShardCount; ++i) {
                int maxToRead = Math.min(remainingMaxLength, this.mBufferSize);
                this.mByteBuffers[index + 1 + i].position(0);
                this.mByteBuffers[index + 1 + i].get(b, start, maxToRead);
                start += maxToRead;
                remainingMaxLength -= maxToRead;
            }
        }

        this.mCurrentPosition += (long)Math.min(b.length, length);
    }

    public char readChar() {
        char result = this.mByteBuffers[this.getIndex()].getChar(this.getOffset());
        this.mCurrentPosition += 2L;
        return result;
    }

    public short readShort() {
        short result = this.mByteBuffers[this.getIndex()].getShort(this.getOffset());
        this.mCurrentPosition += 2L;
        return result;
    }

    public int readInt() {
        int result = this.mByteBuffers[this.getIndex()].getInt(this.getOffset());
        this.mCurrentPosition += 4L;
        return result;
    }

    public long readLong() {
        long result = this.mByteBuffers[this.getIndex()].getLong(this.getOffset());
        this.mCurrentPosition += 8L;
        return result;
    }

    public float readFloat() {
        float result = this.mByteBuffers[this.getIndex()].getFloat(this.getOffset());
        this.mCurrentPosition += 4L;
        return result;
    }

    public double readDouble() {
        double result = this.mByteBuffers[this.getIndex()].getDouble(this.getOffset());
        this.mCurrentPosition += 8L;
        return result;
    }

    public void setPosition(long position) {
        this.mCurrentPosition = position;
    }

    public long position() {
        return this.mCurrentPosition;
    }

    public boolean hasRemaining() {
        return this.mCurrentPosition < this.mLength;
    }

    public long remaining() {
        return this.mLength - this.mCurrentPosition;
    }

    private int getIndex() {
        return (int)(this.mCurrentPosition / (long)this.mBufferSize);
    }

    private int getOffset() {
        return (int)(this.mCurrentPosition % (long)this.mBufferSize);
    }
}
