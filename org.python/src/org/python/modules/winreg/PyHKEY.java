package org.python.modules.winreg;

import org.python.annotations.ExposedType;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.util.prefs.Preferences;

@ExposedType(name = "HKEYType")
public class PyHKEY extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyHKEY.class);
    private int handle;

    private Preferences prefs;
    private PyHKEY parent;

    public PyHKEY(int handle, Preferences prefs, PyHKEY parent) {
        this.handle = handle;
        this.prefs = prefs;
        this.parent = parent;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public PyHKEY getParent() {
        return parent;
    }

    public PyObject Detach() {
        PyLong ret = new PyLong(handle);
        handle = 0;
        return ret;
    }

    public PyObject Close() {
        WinregModule.closeKey(parent.prefs,this);
        return Py.None;
    }

    public int getHandle() {
        return handle;
    }
}
