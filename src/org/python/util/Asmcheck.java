package org.python.util;

/**
 * Created by isaiah on 3/16/17.
 */
public class Asmcheck {
    public static void main(String[] args) {
        int a = 0;
            a = 1;
        System.out.println(a);
    }

    public int test() {
        int a = 0;
        try {
            if (a > 0) {
                return 2;
            }
            a = 1;
            return a;
        } catch (RuntimeException e) {
            a = 2;
            return a + 1;
        } finally {
            a = 3;
        }
    }
}
