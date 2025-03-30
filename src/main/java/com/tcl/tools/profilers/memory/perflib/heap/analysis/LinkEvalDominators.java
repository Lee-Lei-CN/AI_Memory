package com.tcl.tools.profilers.memory.perflib.heap.analysis;


import com.tcl.tools.profilers.memory.perflib.heap.Instance;
import com.tcl.tools.profilers.memory.perflib.heap.RootObj;
import com.tcl.tools.profilers.memory.perflib.heap.Snapshot;
import gnu.trove.TIntArrayList;
import gnu.trove.TObjectHashingStrategy;
import gnu.trove.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public final class LinkEvalDominators extends DominatorsBase {
    private volatile int mNodeCount;
    private volatile int mSemiDominatorProgress = 0;
    private volatile int mDominatorProgress = 0;
    private static final int INVALID_ANCESTOR = -1;

    public LinkEvalDominators(Snapshot snapshot) {
        super(snapshot);
    }

    public ComputationProgress getComputationProgress() {
        String progressMessage;
        double progress;
        if (this.mSemiDominatorProgress < this.mNodeCount) {
            progressMessage = String.format("Calculating semi-dominators %d/%d", this.mSemiDominatorProgress, this.mNodeCount);
            progress = 0.5D * (double)this.mSemiDominatorProgress / (double)this.mNodeCount;
        } else {
            progressMessage = String.format("Calculating immediate dominators %d/%d", this.mDominatorProgress, this.mNodeCount);
            progress = 0.5D + 0.5D * (double)this.mDominatorProgress / (double)this.mNodeCount;
        }

        this.mCurrentProgress.setMessage(progressMessage);
        this.mCurrentProgress.setProgress(progress);
        return this.mCurrentProgress;
    }

    public void computeDominators() {
        LinkEvalDominators.DFSResult result = this.computeIndicesAndParents();
        Instance[] instances = result.instances;
        int[] parents = result.parents;
        int[][] preds = result.predecessors;
        int[] semis = makeIdentityIntArray(instances.length);
        TIntArrayList[] buckets = new TIntArrayList[instances.length];

        for(int i = 0; i < buckets.length; ++i) {
            buckets[i] = new TIntArrayList();
        }

        int[] doms = new int[instances.length];
        int[] ancestors = new int[instances.length];
        Arrays.fill(ancestors, -1);
        int[] labels = makeIdentityIntArray(instances.length);
        this.mNodeCount = instances.length;

        int currentNode;
        for(currentNode = instances.length - 1; currentNode > 0; --currentNode) {
            this.mSemiDominatorProgress = instances.length - currentNode;
            int[] var11 = preds[currentNode];
            int node = var11.length;

            int nodeEvaled;
            for(nodeEvaled = 0; nodeEvaled < node; ++nodeEvaled) {
                int predecessor = var11[nodeEvaled];
                int evaledPredecessor = eval(ancestors, labels, semis, predecessor);
                if (semis[evaledPredecessor] < semis[currentNode]) {
                    semis[currentNode] = semis[evaledPredecessor];
                }
            }

            buckets[semis[currentNode]].add(currentNode);
            ancestors[currentNode] = parents[currentNode];

            for(int i = 0; i < buckets[parents[currentNode]].size(); ++i) {
                node = buckets[parents[currentNode]].get(i);
                nodeEvaled = eval(ancestors, labels, semis, node);
                doms[node] = semis[nodeEvaled] < semis[node] ? nodeEvaled : parents[currentNode];
                instances[node].setImmediateDominator(instances[doms[node]]);
            }

            buckets[parents[currentNode]].clear();
        }

        for(currentNode = 1; currentNode < instances.length; this.mDominatorProgress = currentNode++) {
            if (doms[currentNode] != semis[currentNode]) {
                doms[currentNode] = doms[doms[currentNode]];
                instances[currentNode].setImmediateDominator(instances[doms[currentNode]]);
            }
        }

    }

    private LinkEvalDominators.DFSResult computeIndicesAndParents() {
        TObjectIntHashMap<Instance> parents = new TObjectIntHashMap(TObjectHashingStrategy.IDENTITY);
        ArrayList<Instance> instances = new ArrayList();
        Stack<Instance> nodeStack = new Stack();
        instances.add(Snapshot.SENTINEL_ROOT);
        Set<Instance> gcRoots = (Set)this.mSnapshot.getGCRoots().stream().map(RootObj::getReferredInstance).filter(Objects::nonNull).collect(Collectors.toSet());
        gcRoots.forEach((gcRoot) -> {
            parents.put(gcRoot, 0);
            nodeStack.push(gcRoot);
        });
        dfs(nodeStack, instances, parents);
        return LinkEvalDominators.DFSResult.of(instances, parents, gcRoots);
    }

    private static void dfs(Stack<Instance> nodeStack, ArrayList<Instance> instances, TObjectIntHashMap<Instance> parents) {
        Set touched = Collections.newSetFromMap(new IdentityHashMap());

        while(!nodeStack.empty()) {
            Instance node = (Instance)nodeStack.pop();
            if (!touched.contains(node)) {
                node.setTopologicalOrder(instances.size());
                touched.add(node);
                instances.add(node);
            }

            Iterator var5 = node.getHardForwardReferences().iterator();

            while(var5.hasNext()) {
                Instance succ = (Instance)var5.next();
                if (!touched.contains(succ)) {
                    parents.put(succ, node.getTopologicalOrder());
                    nodeStack.push(succ);
                }
            }
        }

    }

    private static int eval(int[] ancestors, int[] labels, int[] semis, int node) {
        return ancestors[node] == -1 ? node : compress(ancestors, labels, semis, node);
    }

    private static int compress(int[] ancestors, int[] labels, int[] semis, int node) {
        TIntArrayList compressArray = new TIntArrayList();

        assert ancestors[node] != -1;

        int i;
        for(i = node; ancestors[ancestors[i]] != -1; i = ancestors[i]) {
            compressArray.add(i);
        }

        for(i = compressArray.size() - 1; i >= 0; --i) {
            int toCompress = compressArray.get(i);
            int ancestor = ancestors[toCompress];

            assert ancestor != -1;

            if (semis[labels[ancestor]] < semis[labels[toCompress]]) {
                labels[toCompress] = labels[ancestor];
            }

            ancestors[toCompress] = ancestors[ancestor];
        }

        return labels[node];
    }

    private static int[] makeIdentityIntArray(int size) {
        int[] ints = new int[size];

        for(int i = 0; i < size; ints[i] = i++) {
        }

        return ints;
    }

    private static class DFSResult {
        final Instance[] instances;
        final int[] parents;
        final int[][] predecessors;

        DFSResult(Instance[] instances, int[] parents, int[][] predecessors) {
            this.instances = instances;
            this.parents = parents;
            this.predecessors = predecessors;
        }

        static LinkEvalDominators.DFSResult of(ArrayList<Instance> instances, TObjectIntHashMap<Instance> parents, Set<Instance> gcRoots) {
            int[] parentIndices = new int[instances.size()];
            int[][] predIndices = new int[instances.size()][];

            for(int i = 1; i < instances.size(); ++i) {
                Instance instance = (Instance)instances.get(i);
                int order = instance.getTopologicalOrder();
                parentIndices[order] = parents.get(instance);
                int[] backRefs = instance.getHardReverseReferences().stream().filter(Instance::isReachable).mapToInt(Instance::getTopologicalOrder).toArray();
                predIndices[order] = gcRoots.contains(instance) ? prepend(0, backRefs) : backRefs;
            }

            return new LinkEvalDominators.DFSResult((Instance[])instances.toArray(new Instance[0]), parentIndices, predIndices);
        }

        private static int[] prepend(int n, int[] ns) {
            int[] ns1 = new int[ns.length + 1];
            System.arraycopy(ns, 0, ns1, 1, ns.length);
            ns1[0] = n;
            return ns1;
        }
    }
}
