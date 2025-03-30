package com.tcl.tools.profilers.memory.perflib.heap;

import com.tcl.tools.profilers.memory.perflib.analyzer.Capture;
import com.tcl.tools.profilers.memory.perflib.captures.DataBuffer;
import com.tcl.tools.profilers.memory.perflib.heap.analysis.*;
import com.tcl.tools.profilers.memory.perflib.heap.ext.SnapshotPostProcessor;
import com.tcl.tools.profilers.memory.perflib.heap.ext.NativeRegistryPostProcessor;
import gnu.trove.THashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TLongObjectHashMap;
import gnu.trove.TObjectProcedure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Snapshot extends Capture {
    public static final String TYPE_NAME = "hprof";
    private static final String JAVA_LANG_CLASS = "java.lang.Class";
    public static final Instance SENTINEL_ROOT;
    private static final int DEFAULT_HEAP_ID = 0;
    private final DataBuffer mBuffer;
    ArrayList<Heap> mHeaps = new ArrayList();
    Heap mCurrentHeap;
    ArrayList<RootObj> mRoots = new ArrayList();
    TIntObjectHashMap<StackTrace> mTraces = new TIntObjectHashMap();
    TLongObjectHashMap<StackFrame> mFrames = new TLongObjectHashMap();
    private List<Instance> mTopSort;
    private DominatorsBase mDominators;
    private volatile Snapshot.DominatorComputationStage mDominatorComputationStage;
    private THashSet<ClassObj> mReferenceClasses;
    private int[] mTypeSizes;
    private long mIdSizeMask;

    public static Snapshot createSnapshot(DataBuffer buffer) {
        return createSnapshot(buffer, new ProguardMap());
    }

    public static Snapshot createSnapshot(DataBuffer buffer, ProguardMap map) {
        return createSnapshot(buffer, map, Arrays.asList(new NativeRegistryPostProcessor()));
    }

    public static Snapshot createSnapshot(DataBuffer buffer, ProguardMap map, List<SnapshotPostProcessor> postProcessors) {
        try {
            Snapshot snapshot = new Snapshot(buffer);
            HprofParser.parseBuffer(snapshot, buffer, map);
            Iterator var4 = postProcessors.iterator();

            while(var4.hasNext()) {
                SnapshotPostProcessor processor = (SnapshotPostProcessor)var4.next();
                processor.postProcess(snapshot);
            }

            return snapshot;
        } catch (RuntimeException var6) {
            buffer.dispose();
            throw var6;
        }
    }

    public Snapshot(DataBuffer buffer) {
        this.mDominatorComputationStage = Snapshot.DominatorComputationStage.INITIALIZING;
        this.mReferenceClasses = new THashSet();
        this.mIdSizeMask = 4294967295L;
        this.mBuffer = buffer;
        this.setToDefaultHeap();
    }

    public void dispose() {
        this.mBuffer.dispose();
    }

    DataBuffer getBuffer() {
        return this.mBuffer;
    }

    public Heap setToDefaultHeap() {
        return this.setHeapTo(0, "default");
    }

    public Heap setHeapTo(int id, String name) {
        Heap heap = this.getHeap(id);
        if (heap == null) {
            heap = new Heap(id, name);
            heap.mSnapshot = this;
            this.mHeaps.add(heap);
        }

        this.mCurrentHeap = heap;
        return this.mCurrentHeap;
    }

    public int getHeapIndex(Heap heap) {
        return this.mHeaps.indexOf(heap);
    }

    public Heap getHeap(int id) {
        for(int i = 0; i < this.mHeaps.size(); ++i) {
            if (((Heap)this.mHeaps.get(i)).getId() == id) {
                return (Heap)this.mHeaps.get(i);
            }
        }

        return null;
    }

    public Heap getHeap(String name) {
        for(int i = 0; i < this.mHeaps.size(); ++i) {
            if (name.equals(((Heap)this.mHeaps.get(i)).getName())) {
                return (Heap)this.mHeaps.get(i);
            }
        }

        return null;
    }

    public Collection<Heap> getHeaps() {
        return this.mHeaps;
    }

    public Collection<RootObj> getGCRoots() {
        return this.mRoots;
    }

    public final void addStackFrame(StackFrame theFrame) {
        this.mFrames.put(theFrame.mId, theFrame);
    }

    public final StackFrame getStackFrame(long id) {
        return (StackFrame)this.mFrames.get(id);
    }

    public final void addStackTrace(StackTrace theTrace) {
        this.mTraces.put(theTrace.mSerialNumber, theTrace);
    }

    public final StackTrace getStackTrace(int traceSerialNumber) {
        return (StackTrace)this.mTraces.get(traceSerialNumber);
    }

    public final StackTrace getStackTraceAtDepth(int traceSerialNumber, int depth) {
        StackTrace trace = (StackTrace)this.mTraces.get(traceSerialNumber);
        if (trace != null) {
            trace = trace.fromDepth(depth);
        }

        return trace;
    }

    public final void addRoot(RootObj root) {
        this.mRoots.add(root);
        root.setHeap(this.mCurrentHeap);
    }

    public final void addThread(ThreadObj thread, int serialNumber) {
        this.mCurrentHeap.addThread(thread, serialNumber);
    }

    public final ThreadObj getThread(int serialNumber) {
        return this.mCurrentHeap.getThread(serialNumber);
    }

    public final void setIdSize(int size) {
        int maxId = -1;

        int i;
        for(i = 0; i < Type.values().length; ++i) {
            maxId = Math.max(Type.values()[i].getTypeId(), maxId);
        }

        assert maxId > 0 && maxId <= Type.LONG.getTypeId();

        this.mTypeSizes = new int[maxId + 1];
        Arrays.fill(this.mTypeSizes, -1);

        for(i = 0; i < Type.values().length; ++i) {
            this.mTypeSizes[Type.values()[i].getTypeId()] = Type.values()[i].getSize();
        }

        this.mTypeSizes[Type.OBJECT.getTypeId()] = size;
        this.mIdSizeMask = -1L >>> (8 - size) * 8;
    }

    public final int getTypeSize(Type type) {
        return this.mTypeSizes[type.getTypeId()];
    }

    public final long getIdSizeMask() {
        return this.mIdSizeMask;
    }

    public final void addInstance(long id, Instance instance) {
        this.mCurrentHeap.addInstance(id, instance);
        instance.setHeap(this.mCurrentHeap);
    }

    public final void addClass(long id, ClassObj theClass) {
        this.mCurrentHeap.addClass(id, theClass);
        theClass.setHeap(this.mCurrentHeap);
    }

    public final Instance findInstance(long id) {
        for(int i = 0; i < this.mHeaps.size(); ++i) {
            Instance instance = ((Heap)this.mHeaps.get(i)).getInstance(id);
            if (instance != null) {
                return instance;
            }
        }

        return this.findClass(id);
    }

    public final ClassObj findClass(long id) {
        for(int i = 0; i < this.mHeaps.size(); ++i) {
            ClassObj theClass = ((Heap)this.mHeaps.get(i)).getClass(id);
            if (theClass != null) {
                return theClass;
            }
        }

        return null;
    }

    public final ClassObj findClass(String name) {
        for(int i = 0; i < this.mHeaps.size(); ++i) {
            ClassObj theClass = ((Heap)this.mHeaps.get(i)).getClass(name);
            if (theClass != null) {
                return theClass;
            }
        }

        return null;
    }

    public final Collection<ClassObj> findClasses(String name) {
        ArrayList<ClassObj> classObjs = new ArrayList();

        for(int i = 0; i < this.mHeaps.size(); ++i) {
            classObjs.addAll(((Heap)this.mHeaps.get(i)).getClasses(name));
        }

        return classObjs;
    }

    public void resolveClasses() {
        ClassObj clazz = this.findClass("java.lang.Class");
        int javaLangClassSize = clazz != null ? clazz.getInstanceSize() : 0;
        Iterator var3 = this.mHeaps.iterator();

        while(var3.hasNext()) {
            Heap heap = (Heap)var3.next();
            Iterator var5 = heap.getClasses().iterator();

            while(var5.hasNext()) {
                ClassObj classObj = (ClassObj)var5.next();
                ClassObj superClass = classObj.getSuperClassObj();
                if (superClass != null) {
                    superClass.addSubclass(classObj);
                }

                int classSize = javaLangClassSize;
                Field[] var9 = classObj.mStaticFields;
                int var10 = var9.length;

                for(int var11 = 0; var11 < var10; ++var11) {
                    Field f = var9[var11];
                    classSize += this.getTypeSize(f.getType());
                }

                classObj.setSize(classSize);
            }

            final int heapId = heap.getId();
            heap.forEachInstance(new TObjectProcedure<Instance>() {
                public boolean execute(Instance instance) {
                    ClassObj classObj = instance.getClassObj();
                    if (classObj != null) {
                        classObj.addInstance(heapId, instance);
                    }

                    return true;
                }
            });
        }

    }

    public void identifySoftReferences() {
        List<ClassObj> referenceDescendants = this.findAllDescendantClasses(ClassObj.getReferenceClassName());
        Iterator var2 = referenceDescendants.iterator();

        while(var2.hasNext()) {
            ClassObj classObj = (ClassObj)var2.next();
            classObj.setIsSoftReference();
            this.mReferenceClasses.add(classObj);
        }

    }

    public void resolveReferences() {
        Iterator var1 = this.getHeaps().iterator();

        while(var1.hasNext()) {
            Heap heap = (Heap)var1.next();
            Iterator var3 = heap.getClasses().iterator();

            while(var3.hasNext()) {
                ClassObj clazz = (ClassObj)var3.next();
                clazz.resolveReferences();
            }

            heap.forEachInstance(new TObjectProcedure<Instance>() {
                public boolean execute(Instance instance) {
                    instance.resolveReferences();
                    return true;
                }
            });
        }

    }

    public void compactMemory() {
        Iterator var1 = this.getHeaps().iterator();

        while(var1.hasNext()) {
            Heap heap = (Heap)var1.next();
            heap.forEachInstance(new TObjectProcedure<Instance>() {
                public boolean execute(Instance instance) {
                    instance.compactMemory();
                    return true;
                }
            });
        }

    }

    public List<ClassObj> findAllDescendantClasses(String className) {
        Collection<ClassObj> ancestorClasses = this.findClasses(className);
        List<ClassObj> descendants = new ArrayList();
        Iterator var4 = ancestorClasses.iterator();

        while(var4.hasNext()) {
            ClassObj ancestor = (ClassObj)var4.next();
            descendants.addAll(ancestor.getDescendantClasses());
        }

        return descendants;
    }

    public void computeDominators() {
        this.prepareDominatorComputation();
        this.doComputeDominators(new LinkEvalDominators(this));
    }

    public void prepareDominatorComputation() {
        if (this.mDominators == null) {
            this.mDominatorComputationStage = Snapshot.DominatorComputationStage.RESOLVING_REFERENCES;
            this.resolveReferences();
            this.compactMemory();
            this.mDominatorComputationStage = Snapshot.DominatorComputationStage.COMPUTING_SHORTEST_DISTANCE;
            ShortestDistanceVisitor shortestDistanceVisitor = new ShortestDistanceVisitor();
            shortestDistanceVisitor.doVisit(this.getGCRoots());
            this.mDominatorComputationStage = Snapshot.DominatorComputationStage.COMPUTING_TOPOLOGICAL_SORT;
            this.mTopSort = TopologicalSort.compute(this.getGCRoots());
            Iterator var2 = this.mTopSort.iterator();

            while(var2.hasNext()) {
                Instance instance = (Instance)var2.next();
                instance.dedupeReferences();
            }

        }
    }

    public void doComputeDominators(DominatorsBase computable) {
        if (this.mDominators == null) {
            this.mDominators = computable;
            this.mDominatorComputationStage = Snapshot.DominatorComputationStage.COMPUTING_DOMINATORS;
            this.mDominators.computeDominators();
            this.mDominatorComputationStage = Snapshot.DominatorComputationStage.COMPUTING_RETAINED_SIZES;
            this.mDominators.computeRetainedSizes();
        }
    }

    public ComputationProgress getComputationProgress() {
        return this.mDominatorComputationStage == Snapshot.DominatorComputationStage.COMPUTING_DOMINATORS ? this.mDominators.getComputationProgress() : this.mDominatorComputationStage.getInitialProgress();
    }

    public Snapshot.DominatorComputationStage getDominatorComputationStage() {
        return this.mDominatorComputationStage;
    }

    public List<Instance> getReachableInstances() {
        List<Instance> result = new ArrayList(this.mTopSort.size());
        Iterator var2 = this.mTopSort.iterator();

        while(var2.hasNext()) {
            Instance node = (Instance)var2.next();
            if (node.getImmediateDominator() != null) {
                result.add(node);
            }
        }

        return result;
    }

    public List<Instance> getTopologicalOrdering() {
        return this.mTopSort;
    }

    public final void dumpInstanceCounts() {
        Iterator var1 = this.mHeaps.iterator();

        while(var1.hasNext()) {
            Heap heap = (Heap)var1.next();
            System.out.println("+------------------ instance counts for heap: " + heap.getName());
            heap.dumpInstanceCounts();
        }

    }

    public final void dumpSizes() {
        Iterator var1 = this.mHeaps.iterator();

        while(var1.hasNext()) {
            Heap heap = (Heap)var1.next();
            System.out.println("+------------------ sizes for heap: " + heap.getName());
            heap.dumpSizes();
        }

    }

    public final void dumpSubclasses() {
        Iterator var1 = this.mHeaps.iterator();

        while(var1.hasNext()) {
            Heap heap = (Heap)var1.next();
            System.out.println("+------------------ subclasses for heap: " + heap.getName());
            heap.dumpSubclasses();
        }

    }

    public <T> T getRepresentation(Class<T> asClass) {
        return asClass.isAssignableFrom(this.getClass()) ? asClass.cast(this) : null;
    }

    public String getTypeName() {
        return "hprof";
    }

    static {
        SENTINEL_ROOT = new RootObj(RootType.UNKNOWN);
    }

    public static enum DominatorComputationStage {
        INITIALIZING(new ComputationProgress("Preparing for dominator calculation...", 0.0D), 0.1D, 0.0D),
        RESOLVING_REFERENCES(new ComputationProgress("Resolving references...", 0.0D), 0.1D, 0.2D),
        COMPUTING_SHORTEST_DISTANCE(new ComputationProgress("Computing depth to nodes...", 0.0D), 0.3D, 0.03D),
        COMPUTING_TOPOLOGICAL_SORT(new ComputationProgress("Performing topological sorting...", 0.0D), 0.33D, 0.3D),
        COMPUTING_DOMINATORS(new ComputationProgress("Calculating dominators...", 0.0D), 0.63D, 0.35D),
        COMPUTING_RETAINED_SIZES(new ComputationProgress("Calculating retained sizes...", 0.0D), 0.98D, 0.02D);

        private final ComputationProgress mInitialProgress;
        private final double mOffset;
        private final double mScale;

        private DominatorComputationStage(ComputationProgress initialProgress, double offset, double scale) {
            this.mInitialProgress = initialProgress;
            this.mOffset = offset;
            this.mScale = scale;
        }

        public ComputationProgress getInitialProgress() {
            return this.mInitialProgress;
        }

        public static double toAbsoluteProgressPercentage(DominatorComputationStage baseStage, ComputationProgress computationProgress) {
            return computationProgress.getProgress() * baseStage.mScale + baseStage.mOffset;
        }
    }
}
