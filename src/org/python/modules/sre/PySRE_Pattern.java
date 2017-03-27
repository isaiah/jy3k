package org.python.modules.sre;

import org.python.core.*;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.internal.joni.*;
import org.python.expose.ExposedType;

import java.util.*;

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
                pos = region.end[0];
            } else {
                pos++;
            }

            for (int i = 1; i < region.beg.length; i++) {
                list.add(new PyUnicode(s.substring(region.beg[i], region.end[i])));
            }
        }
        return new PyList(list);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_sub(PyObject[] args, String[] keywords) {
        PyObject filter = args[0];
        ArgParser ap = new ArgParser("sub", args, keywords, "repl", "string", "count");
        String s = ap.getString(1);
        int count = ap.getInt(2, 0);
        boolean replCallable = filter.isCallable();
        String replacement = null;
        if (!replCallable) {
            replacement = ap.getString(0);
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = reg.matcher(s.getBytes());
        for (int pos = 0; pos < s.length();) {
            int result = matcher.search(pos, s.length(), Option.DEFAULT);
            if (result == -1) {
                sb.append(s.substring(pos));
                break;
            }

            Region region = matcher.getEagerRegion();
            if (region.beg[0] != pos) {
                sb.append(s.substring(pos, region.beg[0]));
                pos = region.end[0];
            } else {
                pos++;
            }

            if (replCallable) {
                PyObject match = new PySRE_Match(matcher, s, this);
                replacement = ((PyUnicode) filter.__call__(match)).getString();
            }
            sb.append(replacement);
        }
        return new PyUnicode(sb);
    }

    @ExposedGet(name = "groups")
    public PyObject SRE_Pattern_groups() {
        return new PyLong(reg.numberOfCaptures());
    }

    @ExposedGet(name = "groupindex")
    public PyObject SRE_Pattern_groupindex() {
        Map<PyObject, PyObject> map = new HashMap<>();
        for (Iterator<NameEntry> iter = reg.namedBackrefIterator(); iter.hasNext();) {
            NameEntry entry = iter.next();
            map.put(new PyUnicode(new String(entry.name, entry.nameP, entry.nameEnd - entry.nameP)), new PyLong(entry.getBackRefs()[0]));
        }
        return new PyDictionary(map);
    }
}
