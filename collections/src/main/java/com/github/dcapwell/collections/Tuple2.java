package com.github.dcapwell.collections;

public final class Tuple2<A, B> {
    private final A _1;
    private final B _2;

    public Tuple2(A a, B b) {
        _1 = a;
        _2 = b;
    }

    public A get1() {
        return _1;
    }

    public B get2() {
        return _2;
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ")";
    }
}
