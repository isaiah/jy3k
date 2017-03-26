package org.python.modules.sre;

import org.python.core.*;
import org.python.expose.ExposedMethod;
import org.python.internal.joni.Matcher;
import org.python.internal.joni.Option;
import org.python.internal.joni.Regex;
import org.python.expose.ExposedType;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedType(name = "_sre.SRE_Pattern")
public class PySRE_Pattern extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySRE_Pattern.class);

    protected Regex reg;

    public PySRE_Pattern(String s) {
        super(TYPE);
        reg = new Regex(s.getBytes());
    }

    @ExposedMethod
    public PyObject SRE_Pattern_match(PyObject s) {
        String str = ((PyUnicode) s).getString();
        Matcher m = reg.matcher(str.getBytes());
        int result = m.match(0, str.length(), Option.DEFAULT);
        if (result == -1) {
            return Py.None;
        }
        return new PySRE_Match(m, str, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_search(PyObject s) {
        String str = ((PyUnicode) s).getString();
        Matcher m = reg.matcher(str.getBytes());
        int result = m.search(0, str.length(), Option.DEFAULT);
        if (result == -1) {
            return Py.None;
        }
        return new PySRE_Match(m, str, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_finditer(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("finditer", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        PySRE_Scanner scanner = new PySRE_Scanner(this, s, 0);
        PyObject callable = scanner.__findattr__("search");
        return new PyCallIter(callable, Py.None);
    }
}
