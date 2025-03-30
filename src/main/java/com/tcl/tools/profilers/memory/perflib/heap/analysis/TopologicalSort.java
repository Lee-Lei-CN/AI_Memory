package com.tcl.tools.profilers.memory.perflib.heap.analysis;


import com.google.common.collect.Lists;
import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.NonRecursiveVisitor;
import com.tcl.tools.profilers.memory.perflib.heap.RootObj;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;
import gnu.trove.TLongHashSet;
import java.util.Iterator;
import java.util.List;

public class TopologicalSort {
    public TopologicalSort() {
    }

    public static List<Instance> compute(Iterable<RootObj> roots) {
        TopologicalSort.TopologicalSortVisitor visitor = new TopologicalSort.TopologicalSortVisitor();
        visitor.doVisit(roots);
        List<Instance> instances = visitor.getPreorderedInstances();
        Snapshot.SENTINEL_ROOT.setTopologicalOrder(0);
        int currentIndex = 0;
        Iterator var4 = instances.iterator();

        while(var4.hasNext()) {
            Instance node = (Instance)var4.next();
            ++currentIndex;
            node.setTopologicalOrder(currentIndex);
        }

        return instances;
    }

    private static class TopologicalSortVisitor extends NonRecursiveVisitor {
        private final TLongHashSet mVisited;
        private final List<Instance> mPostorder;

        private TopologicalSortVisitor() {
            this.mVisited = new TLongHashSet();
            this.mPostorder = Lists.newArrayList();
        }

        public void visitLater(Instance parent, Instance child) {
            if (!this.mSeen.contains(child.getId())) {
                this.mStack.push(child);
            }

        }

        public void doVisit(Iterable<? extends Instance> startNodes) {
            Iterator var2 = startNodes.iterator();

            while(var2.hasNext()) {
                Instance node = (Instance)var2.next();
                node.accept(this);
            }

            while(!this.mStack.isEmpty()) {
                Instance node = (Instance)this.mStack.peek();
                if (this.mSeen.add(node.getId())) {
                    node.accept(this);
                } else {
                    this.mStack.pop();
                    if (this.mVisited.add(node.getId())) {
                        this.mPostorder.add(node);
                    }
                }
            }

        }

        List<Instance> getPreorderedInstances() {
            return Lists.reverse(this.mPostorder);
        }
    }
}
