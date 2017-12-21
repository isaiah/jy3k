package org.python.modules.sys;

import org.python.core.PyObject;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;

@ExposedType(name = "sys.flags")
public class Flags extends PyObject {
    @ExposedGet
    public int debug, inspect, interactive, optimize, dont_write_bytecode, no_user_site, no_site,
            ignore_environment, verbose, bytes_warning, quiet, hash_randomization, isolated;
}
