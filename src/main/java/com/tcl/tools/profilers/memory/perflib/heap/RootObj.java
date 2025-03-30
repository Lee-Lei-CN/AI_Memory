package com.tcl.tools.profilers.memory.perflib.heap;


public class RootObj extends Instance {
    public static final String UNDEFINED_CLASS_NAME = "no class defined!!";
    RootType mType;
    int mThread;

    public RootObj(RootType type) {
        this(type, 0L, 0, (StackTrace)null);
    }

    public RootObj(RootType type, long id) {
        this(type, id, 0, (StackTrace)null);
    }

    public RootObj(RootType type, long id, int thread, StackTrace stack) {
        super(id, stack);
        this.mType = RootType.UNKNOWN;
        this.mType = type;
        this.mThread = thread;
    }

    public final String getClassName(Snapshot snapshot) {
        ClassObj theClass;
        if (this.mType == RootType.SYSTEM_CLASS) {
            theClass = snapshot.findClass(this.mId);
        } else {
            theClass = snapshot.findInstance(this.mId).getClassObj();
        }

        return theClass == null ? "no class defined!!" : theClass.mClassName;
    }

    public void resolveReferences() {
    }

    public final void accept(Visitor visitor) {
        visitor.visitRootObj(this);
        Instance instance = this.getReferredInstance();
        if (instance != null) {
            visitor.visitLater((Instance)null, instance);
        }

    }

    public final String toString() {
        return String.format("%s@0x%08x", this.mType.getName(), this.mId);
    }

    public Instance getReferredInstance() {
        return (Instance)(this.mType == RootType.SYSTEM_CLASS ? this.mHeap.mSnapshot.findClass(this.mId) : this.mHeap.mSnapshot.findInstance(this.mId));
    }

    public RootType getRootType() {
        return this.mType;
    }
}
