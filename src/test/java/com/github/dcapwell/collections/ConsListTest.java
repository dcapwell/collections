package com.github.dcapwell.collections;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ConsListTest {

    @Test
    public void toStr() {
        Assert.assertEquals(ConsList.of("a", "b", "c", "d").toString(), "[a,b,c,d]");
    }

    @Test
    public void map() {
        ConsList<Integer> list = ConsList.of(1, 2, 3, 4);
        Assert.assertEquals(list.map(x -> x + 1).toString(), "[2,3,4,5]");
    }

    @Test
    public void cons() {
        ConsList<Integer> list = ConsList.of(2,3,4);
        Assert.assertEquals(list.cons(1).toString(), "[1,2,3,4]");
    }

    @Test
    public void iterator() {
        ConsList<Integer> list = ConsList.of(0, 1, 2, 3, 4);
        int counter = 0;
        for (int e : list)
            Assert.assertEquals(e, counter++);
    }

    @Test
    public void flatMap() {
        Assert.assertEquals(ConsList.of(1, 2, 3, 4).flatMap(x -> ConsList.of(x, x)), ConsList.of(1, 1, 2, 2, 3, 3, 4, 4));
    }

    @Test
    public void eq() {
        Assert.assertEquals(ConsList.of(1, 2, 3), ConsList.of(1, 2, 3));
    }

    @Test
    public void notEq() {
        Assert.assertNotEquals(ConsList.of(1, 2, 3), ConsList.of(1, 2, 4));
    }

    @Test
    public void hash() {
        Assert.assertEquals(ConsList.of(1, 2, 3).hashCode(), ConsList.of(1, 2, 3).hashCode());
    }

    @Test
    public void notHash() {
        Assert.assertNotEquals(ConsList.of(1, 2, 3).hashCode(), ConsList.of(1, 2, 4
            ).hashCode());
    }
}