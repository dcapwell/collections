package com.github.dcapwell.collections;

public interface KTypeVTypeFunction<KType, VType> {
    VType apply(KType value);
}