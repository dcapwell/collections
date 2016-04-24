package com.github.dcapwell.collections.benchmarks;

import com.github.dcapwell.collections.SkipList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Defaults;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class SkipListBenchmarks {
    private final SkipList<Integer> list = SkipList.create();

    @Param({"1", "10", "100", "1000"})
    private int count;

    @TearDown
    public void cleanup() {
        list.clear();
    }

    @Benchmark
    public void add(Blackhole bh) {
        for (int i = 0; i < count; i++)
            bh.consume(list.add(i));
    }

    @Benchmark
    public void addReverse(Blackhole bh) {
        for (int i = count; i > 0; i--)
            bh.consume(list.add(i));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SkipListBenchmarks.class.getSimpleName())
                .measurementIterations(5)
                .warmupIterations(5)
                .forks(5)
                .jvmArgs("-ea")
//                .addProfiler(GCProfiler.class)
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();
    }
}
