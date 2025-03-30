package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;


import com.tcl.tools.profilers.memory.perflib.heap.Instance;

public interface Printer {
    void addHeading(int level, String content);

    void addParagraph(String content);

    void startTable(String... columnHeadings);

    void addRow(String... values);

    void endTable();

    void addImage(Instance instance);

    String formatInstance(Instance instance);
}
