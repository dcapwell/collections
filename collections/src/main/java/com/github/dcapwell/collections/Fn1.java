package com.github.dcapwell.collections;

import com.github.dcapwell.collections.specialized.Specialized;

@Specialized
public interface Fn1<A, B> {
    B apply(A value);
}
