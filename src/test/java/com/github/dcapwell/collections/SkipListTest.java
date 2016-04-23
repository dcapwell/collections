package com.github.dcapwell.collections;

import org.testng.annotations.Test;

public class SkipListTest {

    @Test
    public void addDebug() {
        SkipList<Integer> list = SkipList.create();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(-4);
        list.add(2);
        System.out.println(list);
    }
}