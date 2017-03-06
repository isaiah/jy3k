package org.python.modules.sys;

import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedType;

/**
 * Created by isaiah on 2/23/17.
 */
@ExposedType(name = "sys.int_info", base = PyTuple.class)
public class IntInfo extends PyObject {
    @ExposedGet(doc = "size of digit in bits")
    public int bits_per_digit;
    @ExposedGet(doc = "size in bytes of the C type used to represent a digit")
    public int size_of_digit;

    public IntInfo() {
        bits_per_digit = 30;
        size_of_digit = 4;
    }
}
