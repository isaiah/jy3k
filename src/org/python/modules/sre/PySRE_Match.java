package org.python.modules.sre;

import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;
import org.python.internal.joni.Matcher;
import org.python.internal.joni.Region;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedType(name = "_sre.SRE_Match", doc = "The match result")
public class PySRE_Match extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySRE_Match.class);

    @ExposedGet(name = "re")
    public PySRE_Pattern pattern;

    @ExposedGet(name="string")
    public String str;

    @ExposedGet
    public int pos;

    @ExposedGet
    public int endpos;

    private Matcher matcher;

    private Region region;

    public PySRE_Match(Matcher matcher, String str, PySRE_Pattern pattern) {
        this.matcher = matcher;
        this.pattern = pattern;
        this.str = str;
        this.region = matcher.getEagerRegion();
        this.pos = 0;
        this.endpos = str.length() - 1;
    }

    @ExposedMethod
    public PyObject SRE_Match_start(PyObject[] args, String[] keywords) {
        int index = getIndex(args);
        return new PyLong(region.beg[index]);
    }

    @ExposedMethod
    public PyObject SRE_Match_end(PyObject[] args, String[] keywords) {
        int index = getIndex(args[0]);
        return new PyLong(region.end[index]);
    }

    public PyObject group(int index) {
        if (region.beg[index] == -1) {
            return Py.None;
        }
        return new PyUnicode(str.substring(region.beg[index], region.end[index]));
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
        PyObject[] grps = new PyObject[region.beg.length - 1];
        for (int i = 1; i < region.beg.length; i++) {
            grps[i - 1] = group(i);
        }
        return new PyTuple(grps);
    }

    @ExposedMethod
    public PyObject SRE_Match_regs() {
        PyObject[] regs = new PyObject[region.beg.length];
        for(int i = 0; i < regs.length; i++) {
            regs[i] = new PyTuple(new PyLong(region.beg[i]), new PyLong(region.end[i]));
        }
        return new PyTuple(regs);
    }

    @ExposedMethod
    public PyObject SRE_Match_span(PyObject[] args, String[] keywords) {
        int index = getIndex(args);
        return new PyTuple(new PyLong(region.beg[index]), new PyLong(region.end[index]));
    }

    private int getIndex(PyObject arg) {
        if (arg instanceof PyUnicode) {
            byte[] name = ((PyUnicode) arg).getString().getBytes();
            return pattern.reg.nameToBackrefNumber(name, region);
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
