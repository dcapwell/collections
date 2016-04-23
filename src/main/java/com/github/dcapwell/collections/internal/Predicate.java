package com.github.dcapwell.collections.internal;

public final class Predicate {

    public static <A> A notNull(A value) {
        if (value == null)
            throw new NullPointerException();
        return value;
    }
}
