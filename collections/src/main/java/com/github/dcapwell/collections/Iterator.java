package com.github.dcapwell.collections;

import com.github.dcapwell.collections.specialized.Specialized;

@Specialized
public interface Iterator<A> {
    boolean hasNext();
    A next();
}
