package org.python.modules.sre;

import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyCallIter;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.imp;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;
import org.python.internal.regex.Matcher;
import org.python.internal.regex.Pattern;
import org.python.internal.regex.PatternSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by isaiah on 3/24/17.
 */
@ExposedType(name = "_sre.SRE_Pattern")
public class PySRE_Pattern extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySRE_Pattern.class);

    protected Pattern reg;

    @ExposedGet
    public PyObject flags;

    @ExposedGet
    public PyObject pattern;


    public PySRE_Pattern(PyObject s, PyObject flags) {
        super(TYPE);
        if (flags == null) flags = Py.None;
        this.flags = flags;
        String pattern = getString(s);
        try {
            this.reg = Pattern.compile(pattern, flags.asInt());
        } catch (PatternSyntaxException e) {
            PyType error;
            PyObject re = imp.importName("re", true);
            error = (PyType) re.__getattr__("error");
            throw new PyException(error, new PyTuple(new PyUnicode(e.getMessage()), new PyUnicode(pattern)));
        }
        this.pattern = s;
    }

    @ExposedMethod
    public PyObject SRE_Pattern_match(PyObject s) {
        String str = getString(s);
        Matcher m = reg.matcher(str);
        if (!m.matches()) {
            return Py.None;
        }
        return new PySRE_Match(m, str, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_search(PyObject s) {
        String str = getString(s);
        Matcher m = reg.matcher(str);
        if (!m.find()) {
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
        Matcher matcher = reg.matcher(s);
        for (int pos = 0; pos < s.length(); ) {
            if (!matcher.find(pos)) {
                break;
            }
            pos = matcher.end() + 1;
            list.add(new PyUnicode(matcher.group()));
        }
        return new PyList(list);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_fullmatch(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("fullmatch", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        int pos = ap.getInt(1, 0);
        int endpos = ap.getInt(2, s.length());
        Matcher matcher = reg.matcher(s);
        if (matcher.matches() && matcher.hitEnd()) {
            return new PySRE_Match(matcher, s, this);
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject SRE_Pattern_split(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("split", args, keywords, "string", "maxsplit");
        String s = ap.getString(0);
        int limit = ap.getInt(1, 0);
        return new PyList(Arrays.asList(reg.split(s, limit)));
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
        Matcher matcher = reg.matcher(s);
        for (int pos = 1; pos < s.length(); ) {
            if (!matcher.find(pos)) {
                sb.append(s.substring(pos));
                break;
            }

            if (matcher.start() != pos) {
                sb.append(s.substring(pos, matcher.start()));
                pos = matcher.end();
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
        return new PyLong(reg.groupCount());
    }

    @ExposedGet(name = "groupindex")
    public PyObject SRE_Pattern_groupindex() {
        Map<PyObject, PyObject> map = new HashMap<>();
        Map<String, Integer> groups = reg.namedGroups();
        groups.keySet().stream().forEach(name -> {
            map.put(new PyUnicode(name), new PyLong(groups.get(name)));
        });
        return new PyDictionary(map);
    }

    private String getString(PyObject s) {
        if (s instanceof PyBytes) {
            return ((PyBytes) s).getString();
        }
        return ((PyUnicode) s).getString();
    }

    private PyObject call(String module, String function, PyObject... args) {
        PyObject sre = imp.importName(module, true);
        return sre.invoke(function, args);
    }
}
