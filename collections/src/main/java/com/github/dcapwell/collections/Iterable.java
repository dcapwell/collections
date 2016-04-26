package com.github.dcapwell.collections;

import com.github.dcapwell.collections.specialized.Specialized;

@Specialized
public interface Iterable<A> {
    Iterator<A> iterator();
}
