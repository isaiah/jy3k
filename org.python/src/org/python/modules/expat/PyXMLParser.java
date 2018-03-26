package org.python.modules.expat;

import org.python.annotations.ExposedType;
import org.python.core.PyObject;
import org.python.core.PyType;

@ExposedType(name = "pyexpat.xmlparser")
public class PyXMLParser extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyXMLParser.class);

    public PyXMLParser() {
        super(TYPE);
    }

    public PyXMLParser(PyType subtype) {
        super(subtype);
    }
}
