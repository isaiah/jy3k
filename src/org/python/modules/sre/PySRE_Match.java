package org.python.modules.sre;

import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;
import org.python.internal.regex.Matcher;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedType(name = "_sre.SRE_Match", doc = "The match result")
public class PySRE_Match extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySRE_Match.class);

    @ExposedGet(name = "re")
    public PySRE_Pattern pattern;

    @ExposedGet(name="string")
    public PyObject str;

    @ExposedGet
    public int pos;

    @ExposedGet
    public int endpos() {
        return matcher.end();
    }

    private Matcher matcher;
    private boolean isByte;

    public PySRE_Match(Matcher matcher, PyObject str, PySRE_Pattern pattern) {
        this.matcher = matcher;
        this.pattern = pattern;
        this.str = str;
        this.pos = 0;
        this.isByte = str instanceof PyBytes;
    }

    @ExposedMethod
    public PyObject SRE_Match_start(PyObject[] args, String[] keywords) {
        int group = args.length == 0 ? 0 : getIndex(args[0]);
        return new PyLong(matcher.start(group));
    }

    @ExposedMethod
    public PyObject SRE_Match_end(PyObject[] args, String[] keywords) {
        int index = args.length == 0 ? 0 : getIndex(args[0]);
        return new PyLong(matcher.end(index));
    }

    public PyObject group(int index) {
        if (index > matcher.groupCount()) {
            throw Py.IndexError("no such group");
        }
        String s = matcher.group(index);
        if (s == null) {
            return Py.None;
        }
        if (isByte) {
            return new PyBytes(s);
        }
        return new PyUnicode(s);
    }

    @ExposedMethod
    public PyObject SRE_Match_group(PyObject[] args, String[] keywords) {
        if (args.length == 0) {
            return group(0);
        } else if (args.length == 1) {
            return group(getIndex(args[0]));
        }
        PyObject[] groups = new PyObject[args.length];
        for (PyObject arg : args) {
            int index = getIndex(arg);
            groups[index] = group(index);
        }
        return new PyTuple(groups);
    }

    @ExposedMethod
    public PyObject SRE_Match_groups() {
        PyObject[] grps = new PyObject[matcher.groupCount()];
        for (int i = 0; i < matcher.groupCount(); i++) {
            grps[i] = group(i);
        }
        return new PyTuple(grps);
    }

    @ExposedMethod
    public PyObject SRE_Match_regs() {
        PyObject[] regs = new PyObject[matcher.groupCount()];
        for(int i = 0; i < regs.length; i++) {
            regs[i] = new PyTuple(new PyLong(matcher.start(i)), new PyLong(matcher.end(i)));
        }
        return new PyTuple(regs);
    }

    @ExposedMethod
    public PyObject SRE_Match_span(PyObject[] args, String[] keywords) {
        int index = getIndex(args);
        return new PyTuple(new PyLong(matcher.start()), new PyLong(matcher.end()));
    }

    @Override
    public String toString() {
        return String.format("<%s object; span=%s, match=%s>", getType().fastGetName(),
                SRE_Match_span(Py.EmptyObjects, Py.NoKeywords), SRE_Match_group(Py.EmptyObjects, Py.NoKeywords));
    }

    private int getIndex(PyObject arg) {
        if (arg instanceof PyUnicode) {
            String name = ((PyUnicode) arg).getString();
            return matcher.getMatchedGroupIndex(name);
        }
        return arg.asIndex();
    }

    private int getIndex(PyObject[] args) {
        if (args.length > 0) {
            return getIndex(args[0]);
        }
        return 0;
    }
}
