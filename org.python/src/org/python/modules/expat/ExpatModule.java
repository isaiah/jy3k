package org.python.modules.expat;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyModule;
import org.python.core.PyModuleDef;
import org.python.core.PyObject;

@ExposedModule(name = "pyexpat")
public class ExpatModule {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("XMLParserType", PyXMLParser.TYPE);
        PyModule modelModule = new PyModule("pyexpat.model");
        dict.__setitem__("model", modelModule);
        PyModule errorsModule = new PyModule("pyexpat.errors");
        dict.__setitem__("errors", errorsModule);
    }

    @ExposedFunction
    public static PyObject ParserCreate(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ParserCreate", args, keywords, "encoding", "namespace_separator", "intern");

        String encoding = ap.getString(0, "utf-8");
        String namespaceSeparator = ap.getString(1, null);
        PyObject intern = ap.getPyObject(2, null);
        if (intern == Py.None) {
            intern = null;
        } else if (intern == null) {
            intern = new PyDictionary();
        } else if (!(intern instanceof PyDictionary)) {
            throw Py.TypeError("intern must be a dictionary");
        }
        return new PyXMLParser();
    }
}
