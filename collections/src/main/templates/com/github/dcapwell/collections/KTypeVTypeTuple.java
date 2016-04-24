package com.github.dcapwell.collections;

public final class KTypeVTypeTuple<KType, VType> {
    private final KType _1;
    private final VType _2;

    public KTypeVTypeTuple(KType a, VType b) {
        _1 = a;
        _2 = b;
    }

    public KType get1() {
        return _1;
    }

    public VType get2() {
        return _2;
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ")";
    }
}
