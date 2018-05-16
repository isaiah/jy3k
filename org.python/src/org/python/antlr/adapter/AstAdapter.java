package org.python.antlr.adapter;

import java.util.ArrayList;
import java.util.List;

import org.python.antlr.PythonTree;
import org.python.antlr.ast.alias;
import org.python.core.Py;
import org.python.core.PyObject;

public class AstAdapter {

    public <T extends PythonTree> T py2ast(PyObject o) {
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return null;
        }
    }

    public <T extends PythonTree> List<T> iter2ast(PyObject iter) {
        List<T> list = new ArrayList<>();
        if (iter != Py.None) {
            for(Object o : iter.asIterable()) {
                if (o == Py.None) {
                    throw Py.ValueError("None is not allowed");
                }
                list.add(py2ast((PyObject)o));
            }
        }
        return list;
    }
}
