package com.tcl.tools.profilers.memory.perflib.heap.analysis;


import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.NonRecursiveVisitor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class ShortestDistanceVisitor extends NonRecursiveVisitor {
    private PriorityQueue<Instance> mPriorityQueue = new PriorityQueue(1024, new Comparator<Instance>() {
        public int compare(Instance o1, Instance o2) {
            return o1.getDistanceToGcRoot() - o2.getDistanceToGcRoot();
        }
    });
    private Instance mPreviousInstance = null;
    private int mVisitDistance = 0;

    public ShortestDistanceVisitor() {
    }

    public void visitLater(Instance parent, Instance child) {
        if (this.mVisitDistance < child.getDistanceToGcRoot() && (parent == null || child.getSoftReverseReferences() == null || !child.getSoftReverseReferences().contains(parent) || child.getIsSoftReference())) {
            child.setDistanceToGcRoot(this.mVisitDistance);
            child.setNextInstanceToGcRoot(this.mPreviousInstance);
            this.mPriorityQueue.add(child);
        }

    }

    public void doVisit(Iterable<? extends Instance> startNodes) {
        Iterator var2 = startNodes.iterator();

        while(var2.hasNext()) {
            Instance node = (Instance)var2.next();
            node.accept(this);
        }

        while(!this.mPriorityQueue.isEmpty()) {
            Instance node = (Instance)this.mPriorityQueue.poll();
            this.mVisitDistance = node.getDistanceToGcRoot() + 1;
            this.mPreviousInstance = node;
            node.accept(this);
        }

    }
}
