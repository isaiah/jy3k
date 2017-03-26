package org.python.modules.sre;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;
import org.python.internal.joni.Matcher;
import org.python.internal.joni.Option;

/**
 * Created by isaiah on 3/26/17.
 */
@ExposedType(name = "_sre.SRE_Scanner")
public class PySRE_Scanner extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySRE_Scanner.class);

    @ExposedGet
    public PySRE_Pattern pattern;

    private Matcher matcher;
    private int pos;
    private String string;

    public PySRE_Scanner(PySRE_Pattern pattern, String s, int pos) {
        this.pattern = pattern;
        this.matcher = pattern.reg.matcher(s.getBytes());
        this.pos = pos;
        this.string = s;
    }

    @ExposedMethod
    public PyObject SRE_Scanner_search() {
        int result = matcher.search(pos, string.length(), Option.DEFAULT);
        if (result == -1) {
            return Py.None;
        }
        this.pos = ++result;
        return new PySRE_Match(this.matcher, string, pattern);
    }

    @ExposedMethod
    public PyObject SRE_Scanner_match() {
        return Py.None;
    }
}
