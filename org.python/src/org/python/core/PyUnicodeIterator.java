package org.python.core;

import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;

@ExposedType(name = "str_iterator", iter = true)
public class PyUnicodeIterator extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyUnicodeIterator.class);

    private final String str;

    private int index;

    public PyUnicodeIterator(String str) {
        super(TYPE);
        this.str = str;
        index = 0;
    }

    @ExposedSlot(SlotFunc.ITER_NEXT)
    public static PyObject next(PyObject iter) {
        PyUnicodeIterator self = (PyUnicodeIterator) iter;
        if (self.index >= self.str.codePointCount(0, self.str.length())) {
            throw Py.StopIteration();
        }

        return new PyUnicode(String.valueOf(Character.toChars(self.str.codePointAt(self.index++))));
    }
}
