package org.python.modules;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.stringlib.FieldNameIterator;
import org.python.core.stringlib.MarkupIterator;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;

@ExposedModule(doc = "string helper module")
public class _string {
    @ExposedFunction(doc = "parse the argument as a format string")
    public static PyObject formatter_parser(PyObject str) {
        return new MarkupIterator(str.toString());
    }

    @ExposedFunction(doc = "split the argument as a field name")
    public static PyObject formatter_field_name_split(PyObject str) {
        if (str instanceof PyUnicode) {
            FieldNameIterator iterator = new FieldNameIterator(str.toString(), false);
            return new PyTuple(iterator.pyHead(), iterator);
        }
        throw Py.TypeError(String.format("expected str, get %s", str.getType().fastGetName()));
    }
}
