package com.tcl.tools.profilers.memory.perflib.heap;


import com.google.common.collect.ImmutableList;
import com.google.common.primitives.UnsignedBytes;
import com.tcl.tools.profilers.memory.perflib.captures.DataBuffer;

import java.util.*;

public abstract class Instance {
    protected final long mId;
    protected final StackTrace mStack;
    long mClassId;
    Heap mHeap;
    int mSize;
    long mNativeSize;
    int mTopologicalOrder;
    int mDistanceToGcRoot = 2147483647;
    Instance mNextInstanceToGcRoot = null;
    private Instance mImmediateDominator;
    private long[] mRetainedSizes;
    protected final ArrayList<Instance> mHardForwardReferences = new ArrayList();
    protected Instance mSoftForwardReference = null;
    protected final ArrayList<Instance> mHardReverseReferences = new ArrayList();
    protected ArrayList<Instance> mSoftReverseReferences = null;

    Instance(long id, StackTrace stackTrace) {
        this.mId = id;
        this.mStack = stackTrace;
    }

    public abstract void resolveReferences();

    public abstract void accept(Visitor visitor);

    public void compactMemory() {
        this.mHardReverseReferences.trimToSize();
        if (this.mSoftReverseReferences != null) {
            this.mSoftReverseReferences.trimToSize();
        }

    }

    public long getId() {
        return this.mId;
    }

    public long getUniqueId() {
        return this.getId() & this.mHeap.mSnapshot.getIdSizeMask();
    }

    public void setClassId(long classId) {
        this.mClassId = classId;
    }

    public ClassObj getClassObj() {
        return this.mHeap.mSnapshot.findClass(this.mClassId);
    }

    public final int getCompositeSize() {
        Instance.CompositeSizeVisitor visitor = new Instance.CompositeSizeVisitor();
        visitor.doVisit(ImmutableList.of(this));
        return visitor.getCompositeSize();
    }

    public int getSize() {
        return this.mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public long getNativeSize() {
        return this.mNativeSize;
    }

    public void setNativeSize(long nativeSize) {
        this.mNativeSize = nativeSize;
    }

    public void setHeap(Heap heap) {
        this.mHeap = heap;
    }

    public Heap getHeap() {
        return this.mHeap;
    }

    public int getTopologicalOrder() {
        return this.mTopologicalOrder;
    }

    public void setTopologicalOrder(int topologicalOrder) {
        this.mTopologicalOrder = topologicalOrder;
    }

    public Instance getImmediateDominator() {
        return this.mImmediateDominator;
    }

    public void setImmediateDominator(Instance dominator) {
        this.mImmediateDominator = dominator;
    }

    public int getDistanceToGcRoot() {
        return this.mDistanceToGcRoot;
    }

    public Instance getNextInstanceToGcRoot() {
        return this.mNextInstanceToGcRoot;
    }

    public void setDistanceToGcRoot(int newDistance) {
        assert newDistance < this.mDistanceToGcRoot;

        this.mDistanceToGcRoot = newDistance;
    }

    public void setNextInstanceToGcRoot(Instance instance) {
        this.mNextInstanceToGcRoot = instance;
    }

    public boolean isReachable() {
        return this.mDistanceToGcRoot != 2147483647;
    }

    public void resetRetainedSize() {
        List<Heap> allHeaps = this.mHeap.mSnapshot.mHeaps;
        if (this.mRetainedSizes == null) {
            this.mRetainedSizes = new long[allHeaps.size()];
        } else {
            Arrays.fill(this.mRetainedSizes, 0L);
        }

        this.mRetainedSizes[allHeaps.indexOf(this.mHeap)] = (long)this.getSize() + this.getNativeSize();
    }

    public void addRetainedSize(int heapIndex, long size) {
        long[] var10000 = this.mRetainedSizes;
        var10000[heapIndex] += size;
    }

    public void addRetainedSizes(Instance other) {
        for(int i = 0; i < this.mRetainedSizes.length; ++i) {
            long[] var10000 = this.mRetainedSizes;
            var10000[i] += other.mRetainedSizes[i];
        }

    }

    public long getRetainedSize(int heapIndex) {
        return this.mRetainedSizes[heapIndex];
    }

    public long getTotalRetainedSize() {
        if (this.mRetainedSizes == null) {
            return 0L;
        } else {
            long totalSize = 0L;
            long[] var3 = this.mRetainedSizes;
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                long mRetainedSize = var3[var5];
                totalSize += mRetainedSize;
            }

            return totalSize;
        }
    }

    public void addReverseReference(Field field, Instance reference) {
        if (field != null && field.getName().equals("referent") && reference.getIsSoftReference()) {
            if (this.mSoftReverseReferences == null) {
                this.mSoftReverseReferences = new ArrayList();
            }

            this.mSoftReverseReferences.add(reference);
        } else {
            this.mHardReverseReferences.add(reference);
        }

    }

    public ArrayList<Instance> getHardForwardReferences() {
        return this.mHardForwardReferences;
    }

    public Instance getSoftForwardReference() {
        return this.mSoftForwardReference;
    }

    public ArrayList<Instance> getHardReverseReferences() {
        return this.mHardReverseReferences;
    }

    public ArrayList<Instance> getSoftReverseReferences() {
        return this.mSoftReverseReferences;
    }

    public void dedupeReferences() {
        Set<Instance> dedupeSet = new HashSet(this.mHardReverseReferences.size());
        dedupeSet.addAll(this.mHardReverseReferences);
        dedupeSet.remove(this);
        this.mHardReverseReferences.clear();
        this.mHardReverseReferences.addAll(dedupeSet);
        this.mHardReverseReferences.trimToSize();
        if (this.getSoftReverseReferences() != null) {
            dedupeSet.clear();
            dedupeSet.addAll(this.getSoftReverseReferences());
            this.mSoftReverseReferences.clear();
            this.mSoftReverseReferences.addAll(dedupeSet);
            this.mSoftReverseReferences.trimToSize();
        }

    }

    public boolean getIsSoftReference() {
        return false;
    }

    protected Object readValue(Type type) {
        switch(type) {
            case OBJECT:
                long id = this.readId();
                return this.mHeap.mSnapshot.findInstance(id);
            case BOOLEAN:
                return this.getBuffer().readByte() != 0;
            case CHAR:
                return this.getBuffer().readChar();
            case FLOAT:
                return this.getBuffer().readFloat();
            case DOUBLE:
                return this.getBuffer().readDouble();
            case BYTE:
                return this.getBuffer().readByte();
            case SHORT:
                return this.getBuffer().readShort();
            case INT:
                return this.getBuffer().readInt();
            case LONG:
                return this.getBuffer().readLong();
            default:
                return null;
        }
    }

    protected long readId() {
        switch(this.mHeap.mSnapshot.getTypeSize(Type.OBJECT)) {
            case 1:
                return (long)this.getBuffer().readByte();
            case 2:
                return (long)this.getBuffer().readShort();
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                return 0L;
            case 4:
                return (long)this.getBuffer().readInt();
            case 8:
                return this.getBuffer().readLong();
        }
    }

    protected int readUnsignedByte() {
        return UnsignedBytes.toInt(this.getBuffer().readByte());
    }

    protected int readUnsignedShort() {
        return this.getBuffer().readShort() & '\uffff';
    }

    protected DataBuffer getBuffer() {
        return this.mHeap.mSnapshot.getBuffer();
    }

    public StackTrace getStack() {
        return this.mStack;
    }

    public static class CompositeSizeVisitor extends NonRecursiveVisitor {
        int mSize = 0;

        public CompositeSizeVisitor() {
        }

        protected void defaultAction(Instance node) {
            this.mSize = (int)((long)this.mSize + (long)node.getSize() + node.getNativeSize());
        }

        public int getCompositeSize() {
            return this.mSize;
        }
    }
}
