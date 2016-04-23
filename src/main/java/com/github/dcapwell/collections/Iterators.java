package com.github.dcapwell.collections;

import java.util.Iterator;

public final class Iterators {
    private Iterators() {}

    public static <A> Iterator<A> empty() {
        return (Iterator<A>) EmptyIterator.instance;
    }

    public static String toString(Iterator<?> it) {
        StringBuilder sb = new StringBuilder("[");
        while (it.hasNext()) {
            sb.append(it.next()).append(',');
        }
        if (sb.length() > 1)
            sb.setLength(sb.length() - 1); // last element, remove trailing ','
        sb.append(']');
        return sb.toString();
    }

    private static final class EmptyIterator implements Iterator<Object> {
        private static final EmptyIterator instance = new EmptyIterator();

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new UnsupportedOperationException("next");
        }
    }
}
