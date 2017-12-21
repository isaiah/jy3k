package org.python.modules.sys;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;

@ExposedType(name = "sys.version_info")
public class VersionInfo extends PyTuple {
    public static final PyType TYPE = PyType.fromClass(VersionInfo.class);

    public VersionInfo(PyObject... data) {
        super(TYPE, data);
    }

    @ExposedGet
    public PyObject major() {
        return pyget(0);
    }

    @ExposedGet
    public PyObject minor() {
        return pyget(1);
    }

    @ExposedGet
    public PyObject micro() {
        return pyget(2);
    }

    @ExposedGet
    public PyObject releaselevel() {
        return pyget(3);
    }

    @ExposedGet
    public PyObject serial() {
        return pyget(4);
    }
}
