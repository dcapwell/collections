package com.github.dcapwell.collections;

import com.github.dcapwell.collections.specialized.Specialized;

//@Specialized
public abstract class ConsList<A> implements Iterable<A> {
    private ConsList() {}

    public static <A> ConsList<A> nil() {
        return (ConsList<A>) Nil.instance;
    }

    public static <A> ConsList<A> of(A... values) {
        ConsList<A> current = nil();
        for (int i = values.length - 1; i >=0 ;i--)
            current = new Cons<>(values[i], current);
        return current;
    }

    public ConsList<A> cons(A value) {
        return new Cons<>(value, this);
    }

    public abstract ConsList<A> cons(ConsList<A> next);

    public abstract <B> ConsList<B> map(Fn1<A, B> fn);
    public abstract <B> ConsList<B> flatMap(Fn1<A, ConsList<B>> fn);

    @Override
    public abstract int hashCode();
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public String toString() {
        return Iterators.toString(this.iterator());
    }

    public static final class Cons<A> extends ConsList<A> {
        private final A value;
        private final ConsList<A> parent;

        private Cons(A value, ConsList<A> parent) {
            this.value = value;
            this.parent = parent;
        }

        public A getValue() {
            return value;
        }

        public ConsList<A> getParent() {
            return parent;
        }

        @Override
        public Iterator<A> iterator() {
            return new ConsIterator<>(this);
        }

        @Override
        public ConsList<A> cons(ConsList<A> next) {
            return new Cons<>(value, parent.cons(next));
        }

        @Override
        public <B> ConsList<B> map(Fn1<A, B> fn) {
            return new Cons<>(fn.apply(value), parent.map(fn));
        }

        @Override
        public <B> ConsList<B> flatMap(Fn1<A, ConsList<B>> fn) {
            return fn.apply(value).cons(parent.flatMap(fn));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cons<?> cons = (Cons<?>) o;

            if (value != null ? !value.equals(cons.value) : cons.value != null) return false;
            return parent != null ? parent.equals(cons.parent) : cons.parent == null;

        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + (parent != null ? parent.hashCode() : 0);
            return result;
        }
    }

    public static final class Nil extends ConsList<Object> {
        private static final Nil instance = new Nil();

        @Override
        public Iterator<Object> iterator() {
            return Iterators.empty();
        }

        @Override
        public ConsList<Object> cons(ConsList<Object> next) {
            return next;
        }

        @Override
        public <B> ConsList<B> map(Fn1<Object, B> fn) {
            return (ConsList<B>) this;
        }

        @Override
        public <B> ConsList<B> flatMap(Fn1<Object, ConsList<B>> fn) {
            return (ConsList<B>) this;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == instance;
        }
    }

    private static final class ConsIterator<A> extends AbstractIterator<A> {
        private ConsList<A> current;

        private ConsIterator(ConsList<A> current) {
            this.current = current;
        }

        @Override
        protected A computeNext() {
            if (current == Nil.instance)
                return endOfData();
            Cons<A> con = (Cons<A>) current;
            A value = con.value;
            current = con.parent;
            return value;
        }
    }
}
