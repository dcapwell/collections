package com.github.dcapwell.collections.internal;

public final class Predicates {

    public static <A> A notNull(A value) {
        if (value == null)
            throw new NullPointerException();
        return value;
    }
}
