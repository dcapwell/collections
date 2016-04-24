package com.github.dcapwell.collections;

import java.util.Iterator;

public abstract class AbstractIterator<A> implements Iterator<A> {
    private State state = State.REMAINING;
    private A next;

    private enum State {
        REMAINING,
        DONE
    }

    @Override
    public final boolean hasNext() {
        switch (state) {
            case DONE: return false;
            case REMAINING: return tryComputeNext();
            default: throw new UnsupportedOperationException("Unknown state: " + state);
        }
    }

    private boolean tryComputeNext() {
        next = computeNext();
        return state == State.REMAINING;
    }

    protected abstract A computeNext();

    @Override
    public final A next() {
        return next;
    }

    protected final A endOfData() {
        state = State.DONE;
        return null;
    }
}
