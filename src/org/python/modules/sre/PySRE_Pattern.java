package org.python.modules.sre;

import org.python.core.*;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.internal.joni.Matcher;
import org.python.internal.joni.Option;
import org.python.internal.joni.Regex;
import org.python.expose.ExposedType;
import org.python.internal.joni.Region;

import java.util.ArrayList;
import java.util.List;

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

    @ExposedMethod
    public PyObject SRE_Pattern_findall(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("finditer", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        List<PyObject> list = new ArrayList<>();
        Matcher matcher = reg.matcher(s.getBytes());
        for (int pos = 0; pos < s.length();) {
            int result = matcher.search(pos, s.length(), Option.DEFAULT);
            if (result == -1) {
                break;
            }
            pos = ++result;
            Region region = matcher.getRegion();
            list.add(new PyUnicode(s.substring(region.beg[0], region.end[0])));
        }
        return new PyList(list);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_fullmatch(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("fullmatch", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        int pos = ap.getInt(1, 0);
        int endpos = ap.getInt(2, s.length());
        Matcher matcher = reg.matcher(s.getBytes());
         int result = matcher.match(pos, endpos, Option.DEFAULT);
        if (result < endpos - 1) {
            return Py.None;
        }
        return new PySRE_Match(matcher, s, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_split(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("split", args, keywords, "string", "maxsplit");
        String s = ap.getString(0);
        List<PyObject> list = new ArrayList<>();
        Matcher matcher = reg.matcher(s.getBytes());
        for (int pos = 0; pos < s.length();) {
            int result = matcher.search(pos, s.length(), Option.DEFAULT);
            if (result == -1) {
                list.add(new PyUnicode(s.substring(pos)));
                break;
            }

            Region region = matcher.getEagerRegion();
            if (region.beg[0] != pos) {
                list.add(new PyUnicode(s.substring(pos, region.beg[0])));
            }

            for (int i = 1; i < region.beg.length; i++) {
                list.add(new PyUnicode(s.substring(region.beg[i], region.end[i])));
            }
            pos = ++result;
        }
        return new PyList(list);
    }
}
