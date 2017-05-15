package org.python.util;

import org.python.core.PyFrame;

/**
 * Created by isaiah on 3/16/17.
 */
public class Asmcheck {
    public static void main(String[] args) {
        Integer a = 0;
        Object[] arr = new Object[3];
        arr[a] = 2;
        System.out.println(a);
    }
}
