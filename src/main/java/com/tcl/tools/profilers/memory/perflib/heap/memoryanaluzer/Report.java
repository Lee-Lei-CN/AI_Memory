package com.tcl.tools.profilers.memory.perflib.heap.memoryanaluzer;



import com.tcl.tools.profilers.memory.perflib.analyzer.AnalysisResultEntry;

import java.util.List;

public interface Report {
    void generate(List<AnalysisResultEntry<?>> data);

    void print(Printer printer);
}
