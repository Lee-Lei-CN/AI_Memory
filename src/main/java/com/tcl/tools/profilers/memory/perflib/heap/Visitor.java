package com.tcl.tools.profilers.memory.perflib.heap;



public interface Visitor {
    void visitRootObj(RootObj root);

    void visitArrayInstance(ArrayInstance instance);

    void visitClassInstance(ClassInstance instance);

    void visitClassObj(ClassObj instance);

    void visitLater(Instance parent, Instance child);
}
