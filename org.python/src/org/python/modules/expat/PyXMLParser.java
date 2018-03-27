package org.python.modules.expat;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.JavaIO;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

@ExposedType(name = "pyexpat.xmlparser")
public class PyXMLParser extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyXMLParser.class);

    @ExposedGet
    @ExposedSet
    public boolean ordered_attributes;
    @ExposedGet
    @ExposedSet
    public boolean specified_attributes;
    @ExposedGet
    @ExposedSet
    public boolean namespace_prefixes;
    @ExposedGet
    @ExposedSet
    public boolean buffer_text;
    @ExposedGet
    @ExposedSet
    public int buffer_size;

    @ExposedGet
    @ExposedSet
    public PyObject StartElementHandler;
    @ExposedSet
    @ExposedGet
    public PyObject EndElementHandler;

    @ExposedSet
    @ExposedGet
    public PyObject StartCdataSectionHandler;
    @ExposedSet
    @ExposedGet
    public PyObject EndCdataSectionHandler;

    @ExposedGet
    @ExposedSet
    public PyObject StartNamespaceDeclHandler;
    @ExposedGet
    @ExposedSet
    public PyObject EndNamespaceDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject StartDoctypeDeclHandler;
    @ExposedGet
    @ExposedSet
    public PyObject EndDoctypeDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject EntityDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject SkippedEntityHandler;

    @ExposedGet
    @ExposedSet
    public PyObject XmlDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject ElementDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject AttlistDeclHandler;

    @ExposedGet
    @ExposedSet
    public PyObject DefaultHandler;

    @ExposedGet
    @ExposedSet
    public PyObject DefaultHandlerExpand;

    @ExposedGet
    @ExposedSet
    public PyObject NotStandaloneHandler;

    @ExposedSet
    @ExposedGet
    public PyObject CharacterDataHandler;
    @ExposedSet
    @ExposedGet
    public PyObject ExternalEntityRefHandler;
    @ExposedSet
    @ExposedGet
    public PyObject ProcessingInstructionHandler;
    @ExposedSet
    @ExposedGet
    public PyObject UnparsedEntityDeclHandler;

    @ExposedSet
    @ExposedGet
    public PyObject NotationDeclHandler;
    @ExposedSet
    @ExposedGet
    public PyObject CommentHandler;

    public PyXMLParser() {
        super(TYPE);
    }

    public PyXMLParser(PyType subtype) {
        super(subtype);
    }

    @ExposedMethod
    public final PyObject Parse(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Parse", args, keywords, "data", "isfinal");
        PyObject data = ap.getPyObject(0);
        boolean isFinal = ap.getBoolean(1, false);
        return Py.None;
    }

    @ExposedMethod
    public final PyObject ParseFile(PyObject file) {
        if (xmlReader != null) {
            throw new PyException(ExpatModule.ExpatError, ExpatModule.XML_ERROR_FINISHED);
        }
        if (file instanceof JavaIO) {
            try {
                xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(((JavaIO) file).inputStream());
            } catch (XMLStreamException e) {
                throw Py.JavaError(e);
            }
        }
        return Py.None;
    }

    @ExposedMethod
    public final boolean UseForeignDTD(boolean flag) {
        return flag;
    }

    @ExposedMethod
    public final PyObject SetParamEntityParsing(PyObject flag) {
        return this;
    }

    private XMLInputFactory inputFactory;
    private XMLStreamReader xmlReader;
}

