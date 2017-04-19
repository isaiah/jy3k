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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.python.modules.sre.SRE_STATE.*;

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
        int flag = flags.asInt();
        int javaFlags = 0;
        if ((flag & SRE_STATE.SRE_FLAG_MULTILINE) != 0) {
            javaFlags |= Pattern.MULTILINE;
        }
        if ((flag & SRE_STATE.SRE_FLAG_VERBOSE) != 0) {
            javaFlags |= Pattern.COMMENTS;
        }
        if ((flag & SRE_FLAG_IGNORECASE) != 0) {
            javaFlags |= Pattern.CASE_INSENSITIVE;
        }
        if ((flag & SRE_FLAG_DOTALL) != 0) {
            javaFlags |= Pattern.DOTALL;
        }
        try {
            this.reg = Pattern.compile(pattern, javaFlags);
        } catch (PatternSyntaxException e) {
            PyType error;
            PyObject re = imp.importName("sre_constants", true);
            error = (PyType) re.__getattr__("error");
            throw new PyException(error, new PyTuple(new PyUnicode(e.getMessage()), new PyUnicode(pattern)));
        }
        this.pattern = s;
    }

    @ExposedMethod
    public PyObject SRE_Pattern_match(PyObject s) {
        String str = getString(s);
        Matcher m = reg.matcher(str);
        if (!m.lookingAt()) {
            return Py.None;
        }
        return new PySRE_Match(m, s, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_search(PyObject s) {
        String str = getString(s);
        Matcher m = reg.matcher(str);
        if (!m.find()) {
            return Py.None;
        }
        return new PySRE_Match(m, s, this);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_finditer(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("finditer", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        PySRE_Scanner scanner = new PySRE_Scanner(this, args[0], 0);
        PyObject callable = scanner.__findattr__("search");
        return new PyCallIter(callable, Py.None);
    }

    @ExposedMethod
    public PyObject SRE_Pattern_findall(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("finditer", args, keywords, "string", "pos", "endpos");
        String s = ap.getString(0);
        boolean isByte = args[0] instanceof PyBytes;
        List<PyObject> list = new ArrayList<>();
        Matcher matcher = reg.matcher(s);
        for (int pos = 0; pos < s.length(); ) {
            if (!matcher.find(pos)) {
                break;
            }
            pos = matcher.end() + 1;
            switch (matcher.groupCount()) {
                case 0:
                    list.add(wrap(matcher.group(), isByte));
                    break;
                case 1:
                    list.add(wrap(matcher.group(1), isByte));
                    break;
                default:
                    PyObject[] objs = new PyObject[matcher.groupCount()];
                    for (int i = 1; i <= objs.length; i++) {
                        objs[i] = wrap(matcher.group(i), isByte);
                    }
                    list.add(new PyTuple(objs));
            }
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
        if (matcher.matches()) {
            return new PySRE_Match(matcher, args[0], this);
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject SRE_Pattern_split(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("split", args, keywords, "string", "maxsplit");
        String s = ap.getString(0);
        int limit = ap.getInt(1, 0);
        List<String> list = new ArrayList<>();
        Matcher matcher = reg.matcher(s);
        for (int pos = 0, i = 0; pos < s.length(); i++) {
            if (!matcher.find(pos) || (limit > 0 && i >= limit)) {
                list.add(s.substring(pos));
                break;
            }
            list.add(s.substring(pos, matcher.start()));
            for (int j = 0; j < matcher.groupCount(); j++) {
                list.add(matcher.group(j));
            }
            pos = matcher.end();
        }
        return new PyList(list.stream().map(ret -> {
            if (args[0] instanceof PyBytes) {
                return new PyBytes(ret);
            }
            return new PyUnicode(ret);
        }).collect(Collectors.toList()));
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
            if (replacement.indexOf('\\') >= 0) {
                filter = call("re", "_subx", this, filter);
                replCallable = filter.isCallable();
                if (replCallable) {
                    replacement = filter.toString();
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        Matcher matcher = reg.matcher(s);
        for (int pos = 0, i = 1; pos < s.length(); i++) {
            if (!matcher.find(pos) || (count > 0 && i > count)) {
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
                PyObject match = new PySRE_Match(matcher, args[1], this);
                replacement = ((PyUnicode) filter.__call__(match)).getString();
            }
            sb.append(replacement);
        }
        if (args[1] instanceof PyBytes) {
            return new PyBytes(sb);
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

    @Override
    public PyUnicode __repr__() {
        return SRE_Pattern___repr__();
    }

    @ExposedMethod
    public PyUnicode SRE_Pattern___repr__() {
        int flags = this.flags.asInt();
//        if (!isBytes && (flags & (SRE_FLAG_LOCALE|SRE_FLAG_UNICODE| SRE_FLAG_ASCII)) == SRE_FLAG_UNICODE) {
//            flags &= ~SRE_FLAG_UNICODE;
//        }
        List<String> flagItems = new ArrayList<>();
        for (FlagName flagName : flagNames) {
            if ((flags & flagName.value) > 0) {
                flagItems.add(flagName.name);
                flags &= ~flagName.value;
            }
        }

        if (flags > 0) {
            flagItems.add(String.format("0x%x", flags));
        }
        if (flagItems.size() > 0) {
            String flagsResult = flagItems.stream().collect(Collectors.joining("|"));
            return new PyUnicode(String.format("re.compile(%.200s, %s)", pattern.__repr__(), flagsResult));
        }
        return new PyUnicode(String.format("re.compile(%.200s)", pattern.__repr__()));
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

    private PyObject wrap(String s, boolean isByte) {
        return isByte ? new PyBytes(s) : new PyUnicode(s);
    }
        private static FlagName[] flagNames = {
            new FlagName("re.TEMPLATE", SRE_FLAG_TEMPLATE),
            new FlagName("re.IGNORECASE", SRE_FLAG_IGNORECASE),
            new FlagName("re.LOCALE", SRE_FLAG_LOCALE),
            new FlagName("re.MULTILINE", SRE_FLAG_MULTILINE),
            new FlagName("re.DOTALL", SRE_FLAG_DOTALL),
            new FlagName("re.UNICODE", SRE_FLAG_UNICODE),
            new FlagName("re.VERBOSE", SRE_FLAG_VERBOSE),
            new FlagName("re.DEBUG", SRE_FLAG_DEBUG),
            new FlagName("re.ASCII", SRE_FLAG_ASCII),
    };

    static class FlagName {
        String name;
        int value;

        FlagName(String n, int v) {
            name = n;
            value = v;
        }
    }
}
