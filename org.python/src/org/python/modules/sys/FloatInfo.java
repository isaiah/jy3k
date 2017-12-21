package org.python.modules.sys;

import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

/**
 * Created by isaiah on 2/23/17.
 */
@ExposedType(name = "sys.float_info")
public class FloatInfo extends PyObject {
    public static final PyType TYPE = PyType.fromClass(FloatInfo.class);
    // sys.float_info(max=1.7976931348623157e+308, max_exp=1024, max_10_exp=308, min=2.2250738585072014e-308, min_exp=-1021, min_10_exp=-307, dig=15, mant_dig=53,
    // epsilon=2.220446049250313e-16, radix=2, rounds=1)

    @ExposedGet
    public double max;
    @ExposedGet
    public int max_exp;
    @ExposedGet
    public int max_10_exp;
    @ExposedGet
    public double min;
    @ExposedGet
    public int min_exp;
    @ExposedGet
    public int min_10_exp;
    @ExposedGet
    public int dig;
    @ExposedGet
    public int mant_dig;
    @ExposedGet
    public double epsilon;
    @ExposedGet
    public int radix;
    @ExposedGet
    public int rounds;

    public FloatInfo() {
        max = 1.7976931348623157e+308;
        max_exp = 1024;
        max_10_exp = 308;
        min = 2.2250738585072014e-308;
        min_exp = -1021;
        min_10_exp = -307;
        dig = 15;
        mant_dig = 53;
        epsilon = 2.220446049250313e-16;
        radix = 2;
        rounds = 1;
    }
}
