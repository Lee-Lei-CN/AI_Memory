package com.tcl.tools.profilers.memory.perflib.heap;


import gnu.trove.TLongHashSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class NonRecursiveVisitor implements Visitor {
    protected final Deque<Instance> mStack = new ArrayDeque();
    protected final TLongHashSet mSeen = new TLongHashSet();

    public NonRecursiveVisitor() {
    }

    protected void defaultAction(Instance instance) {
    }

    public void visitRootObj(RootObj root) {
        this.defaultAction(root);
    }

    public void visitArrayInstance(ArrayInstance instance) {
        this.defaultAction(instance);
    }

    public void visitClassInstance(ClassInstance instance) {
        this.defaultAction(instance);
    }

    public void visitClassObj(ClassObj instance) {
        this.defaultAction(instance);
    }

    public void visitLater(Instance parent, Instance child) {
        this.mStack.push(child);
    }

    public void doVisit(Iterable<? extends Instance> startNodes) {
        Iterator var2 = startNodes.iterator();

        while(var2.hasNext()) {
            Instance node = (Instance)var2.next();
            if (node instanceof RootObj) {
                node.accept(this);
            } else {
                this.visitLater((Instance)null, node);
            }
        }

        while(!this.mStack.isEmpty()) {
            Instance node = (Instance)this.mStack.pop();
            if (this.mSeen.add(node.getId())) {
                node.accept(this);
            }
        }

    }
}
