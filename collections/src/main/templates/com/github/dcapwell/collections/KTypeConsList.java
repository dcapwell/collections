package com.github.dcapwell.collections;

public abstract class KTypeConsList<KType> {
    private KTypeConsList() {}

    public static <KType> KTypeConsList<KType> nil() {
        return KTypeNil.instance;
    }

    public static <KType> KTypeConsList<KType> of(KType... values) {
        KTypeConsList<KType> c = nil();
        for (int i = values.length - 1; i >= 0; i--)
            c = new KTypeCons(values[i], c);
        return c;
    }

    public final KTypeConsList<KType> cons(KType value) {
        return new KTypeCons(value, this);
    }

    public final KTypeConsList<KType> cons(KTypeConsList<KType> next) {
        // the preprocessor doesn't seem to like abstract methods, so need to define
        // everything within this method
        if (this instanceof KTypeCons) {
            KTypeCons self = ((KTypeCons) this);
            return new KTypeCons(self.value, self.parent.cons(next));
        } else {
            return next;
        }
    }
    /*! #if ($TemplateOptions.KTypeGeneric)

    public final <B> ObjectConsList<B> map(java.util.function.Function<KType, B> fn) {
        // preprocessor doesn't look like its meant for functions, so can only support this
        // for the same type, or with objects
        return null;
    }
    #else

    public final KTypeConsList<KType> map(KTypeKTypeFunction<KType, KType> fn) {
      return this;
    }#end !*/

    public static final class KTypeCons<KType> extends KTypeConsList<KType> {
        private final KType value;
        private final KTypeConsList<KType> parent;

        private KTypeCons(KType value, KTypeConsList<KType> parent) {
            this.value = value;
            this.parent = parent;
        }

        public KType getValue() {
            return value;
        }

        public KTypeConsList<KType> getParent() {
            return parent;
        }
    }

    public static final class KTypeNil<KType> extends KTypeConsList<KType> {
        private static final KTypeNil instance = new KTypeNil();
    }
}