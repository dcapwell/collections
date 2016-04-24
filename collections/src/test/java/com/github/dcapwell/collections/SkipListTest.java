package com.github.dcapwell.collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

public class SkipListTest {
    private static final int NUM_ELEMENTS = 100000;

    @Test
    public void search() {
        SkipList <Integer> list = SkipList.create();
        for (int i = 0; i < NUM_ELEMENTS; i++)
            list.add(i);
        AssertAscending ascending = new AssertAscending(42);
        list.search(42, 75, ascending);
        Assert.assertEquals(ascending.expected, 76);
    }

    @Test
    public void concurrentSearch() {
        ConcurrentSkipListSet<Integer> list = new ConcurrentSkipListSet<>();
        for (int i = 0; i < NUM_ELEMENTS; i++)
            list.add(i);
        AssertAscending ascending = new AssertAscending(42);
        list.tailSet(42).headSet(75, true).forEach(ascending);
        Assert.assertEquals(ascending.expected, 76);
    }

    private static final class AssertAscending implements Consumer<Integer> {
        private int expected;

        public AssertAscending(int expected) {
            this.expected = expected;
        }

        @Override
        public void accept(Integer integer) {
            Assert.assertEquals(integer.intValue(), expected++);
        }
    }
}