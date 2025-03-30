package com.tcl.tools.profilers.memory.perflib.heap.analysis;


import com.google.common.collect.Lists;
import com.tcl.tools.profilers.memory.perflib.heap.Heap;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;
import gnu.trove.TObjectProcedure;
import java.util.Iterator;
import java.util.List;

public abstract class DominatorsBase {
    protected volatile ComputationProgress mCurrentProgress = new ComputationProgress("Starting dominator computation", 0.0D);
    protected Snapshot mSnapshot;
    protected List<Instance> mTopSort;

    protected DominatorsBase(Snapshot snapshot) {
        this.mSnapshot = snapshot;

        assert this.mSnapshot.getTopologicalOrdering() != null;

        this.mTopSort = this.mSnapshot.getTopologicalOrdering();
        Iterator var2 = this.mSnapshot.getHeaps().iterator();

        while(var2.hasNext()) {
            Heap heap = (Heap)var2.next();
            Iterator var4 = heap.getClasses().iterator();

            while(var4.hasNext()) {
                Instance instance = (Instance)var4.next();
                instance.resetRetainedSize();
            }

            heap.forEachInstance(new TObjectProcedure<Instance>() {
                public boolean execute(Instance instance) {
                    instance.resetRetainedSize();
                    return true;
                }
            });
        }

    }

    public void dispose() {
        this.mSnapshot = null;
    }

    public abstract ComputationProgress getComputationProgress();

    public abstract void computeDominators();

    public void computeRetainedSizes() {
        Iterator var1 = Lists.reverse(this.mSnapshot.getReachableInstances()).iterator();

        while(var1.hasNext()) {
            Instance node = (Instance)var1.next();
            Instance dom = node.getImmediateDominator();
            if (dom != Snapshot.SENTINEL_ROOT) {
                dom.addRetainedSizes(node);
            }
        }

    }
}
