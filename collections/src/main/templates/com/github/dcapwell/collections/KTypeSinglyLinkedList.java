package com.github.dcapwell.collections;

public final class KTypeSinglyLinkedList<KType> {
    private KTypeNode<KType> head;

    private static final class KTypeNode<KType> {
        private KType value;
        private KTypeNode<KType> next;
    }
}