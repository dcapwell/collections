package com.github.dcapwell.collections;

import java.util.Comparator;
import java.util.Random;

public final class SkipList<A> {
    private final Comparator<A> comparator;
    private final Random rand;
    private HeadNode<A> head;

    public SkipList(Comparator<A> comparator) {
        this.comparator = comparator;
        this.rand = new Random();
        this.head = new HeadNode<>(0, null, null, null);
    }

    public static <A extends Comparable<A>> SkipList<A> create() {
        return new SkipList<A>(Comparator.naturalOrder());
    }

    public boolean add(A value) {
        Comparator<A> cmp = this.comparator;
        Node<A> n;
        for(Node<A> c = head, r = c.right;;) {
            if (r != null) {
                // as long as the value is larger than right, keep moving to the right
                if (cmp.compare(value, r.value) > 0) {
                    // key is larger, so keep searching
                    c = r;
                    r = r.right;
                    continue;
                }
            }
            // at the most right we can get at this level, attempt to move down
            if (c.down != null) {
                c = c.down;
                r = c.right;
                continue;
            }
            // at the the insertion location either less than right or equal right
            if (r != null && cmp.compare(value, r.value) == 0) {
                // update the value anyways since its not known what the user's intent was by adding twice
                // most likely will act as a set, but in some cases users could override values that have logically
                // different content; keep latest
                r.value = value;
                return false;
            } else {
                // right is empty or the r.value is greater than key so add node before right
                n = new Node<>(value, r, null);
                c.right = n;
            }
            break;
        }
        // a new node was inserted so perform a coin-flip to see how many levels should have the value
        int targetLevel = 0;
        while (rand.nextBoolean())
            targetLevel++;
        if (targetLevel > 0) {
            // no reason to do anything if we shouldn't add to different levels
            if (targetLevel > head.level) {
                // add one level and insert this value into all levels
                head = new HeadNode<>(head.level + 1, null, null, head);
                targetLevel = head.level;
            }
            Node[] nodes = new Node[targetLevel + 1];
            // pre-create nodes so its easier to link them together
            // add n to first nodes just to make the loop simple
            nodes[0] = n;
            for (int i = 1; i <= targetLevel; i++)
                nodes[i] = new Node(value, null, nodes[i - 1]);
            out: while (true) {
                int level = head.level;
                for(Node<A> c = head, r = c.right;;) {
                    if (r != null) {
                        // as long as the value is larger than right, keep moving to the right
                        if (cmp.compare(value, r.value) > 0) {
                            // key is larger, so keep searching
                            c = r;
                            r = r.right;
                            continue;
                        }
                    }
                    if (targetLevel == level) {
                        // insert the node here
                        Node node = nodes[targetLevel];
                        node.right = c.right;
                        c.right = node;
                    }
                    if (--targetLevel > 0)
                        break  out;
                }
            }
        }
        return true;
    }

    private static final class HeadNode<A> extends Node<A> {
        private final int level;

        private HeadNode(int level, A value, Node<A> next, Node<A> down) {
            super(value, next, down);
            this.level = level;
        }
    }
    private static class Node<A> {
        private A value;
        private Node<A> right, down;

        private Node(A value, Node<A> right, Node<A> down) {
            this.value = value;
            this.right = right;
            this.down = down;
        }
    }
}
