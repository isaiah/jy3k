package org.python.modules.sre;

import org.python.expose.ExposedMethod;
import org.python.internal.joni.Matcher;
import org.python.internal.joni.Option;
import org.python.internal.joni.Regex;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
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
        m.match(0, str.length(), Option.DEFAULT);
        return new PySRE_Match(m, str, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_search(PyObject s) {
        String str = ((PyUnicode) s).getString();
        Matcher m = reg.matcher(str.getBytes());
        m.search(0, str.length(), Option.DEFAULT);
        return new PySRE_Match(m, str, this);
    }
}
