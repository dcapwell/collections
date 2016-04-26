package com.github.dcapwell.collections;

import com.github.dcapwell.collections.specialized.Specialized;

@Specialized
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
        try {
            next = computeNext();
        } catch (EndOfData e) {
            return false;
        }
        return state == State.REMAINING;
    }

    protected abstract A computeNext();

    @Override
    public final A next() {
        return next;
    }

    protected final A endOfData() {
        state = State.DONE;
        throw new EndOfData();
    }

    private static final class EndOfData extends RuntimeException {
        private static final EndOfData instance = new EndOfData();
    }
}
