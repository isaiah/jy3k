package org.python.modules.expat;

import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedSet;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.BufferProtocol;
import org.python.core.JavaIO;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.NotationDeclaration;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

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
    private final XMLInputFactory inputFactory;
    private XMLStreamReader xmlReader;

    public PyXMLParser() {
        super(TYPE);
        inputFactory = XMLInputFactory.newInstance();
        UseForeignDTD(false);
        inputFactory.setProperty("http://apache.org/xml/features/allow-java-encodings", true);
        inputFactory.setXMLReporter((message, errorType, relatedInformation, location) -> {
            if (errorType.equals("EncodingDeclInvalid")) {
                return;
            }
            ExpatModule.FormError(message, errorType, relatedInformation, location);
        });
    }

    public PyXMLParser(PyType subtype) {
        super(subtype);
        inputFactory = XMLInputFactory.newInstance();
        UseForeignDTD(false);
        inputFactory.setProperty("http://apache.org/xml/features/allow-java-encodings", true);
        inputFactory.setXMLReporter((message, errorType, relatedInformation, location) -> {
            if (errorType.equals("EncodingDeclInvalid")) {
                return;
            }
            ExpatModule.FormError(message, errorType, relatedInformation, location);
        });
    }

    private InputStream bufferedInput;

    @ExposedMethod
    public final PyObject Parse(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("Parse", args, keywords, "data", "isfinal");
        PyObject data = ap.getPyObject(0);
        boolean isFinal = ap.getBoolean(1, false);
        try {
            byte[] bytes;
            if (data instanceof BufferProtocol) {
                bytes = Py.unwrapBuffer(data);
            } else if (data instanceof PyUnicode) {
                bytes = ((PyUnicode) data).getString().getBytes();
            } else {
                bytes = new byte[0];
            }
            if (bufferedInput == null) {
                bufferedInput = new ByteArrayInputStream(bytes);
            } else {
                bufferedInput = new SequenceInputStream(bufferedInput, new ByteArrayInputStream(bytes));
            }
            if (isFinal) {
                xmlReader = inputFactory.createXMLStreamReader(bufferedInput);
                parse();
            }
        } catch (XMLStreamException e) {
            ExpatModule.FormError(e);
        }
        return Py.None;
    }

    @ExposedMethod
    public final PyObject ParseFile(PyObject file) {
        if (xmlReader != null) {
            throw new PyException(ExpatModule.ExpatError, ExpatModule.XMLError.XML_ERROR_FINISHED.getMessage());
        }
        if (file instanceof JavaIO) {
            try {
                xmlReader = inputFactory.createXMLStreamReader(((JavaIO) file).inputStream());
            } catch (XMLStreamException e) {
                ExpatModule.FormError(e);
            }
        }
        return Py.None;
    }

    @ExposedMethod(defaults = {"true"})
    public final boolean UseForeignDTD(boolean flag) {
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, flag);
        return flag;
    }

    @ExposedMethod
    public final PyObject SetParamEntityParsing(int flag) {
        return this;
    }

    @ExposedSet(name = "buffer_text")
    public void setBufferText(boolean flag) {
        buffer_text = flag;
        inputFactory.setProperty(XMLInputFactory.IS_COALESCING, buffer_text);
    }

    @ExposedGet
    public int CurrentByteIndex() {
        return xmlReader.getLocation().getCharacterOffset();
    }

    @ExposedGet
    public int CurrentLineNumber() {
        return xmlReader.getLocation().getLineNumber();
    }

    @ExposedGet
    public int CurrentColumnNumber() {
        return xmlReader.getLocation().getColumnNumber();
    }

    private void handleBuffer() {
        if (buffer_text && buffer.length() > 0) {
            if (CharacterDataHandler != null && CharacterDataHandler != Py.None) {
                CharacterDataHandler.__call__(new PyUnicode(buffer));
            }
            buffer = new StringBuilder();
        }
    }

    private StringBuilder buffer = new StringBuilder();

    private void parse() throws XMLStreamException {
        while (xmlReader.hasNext()) {
            int next = xmlReader.next();
            switch (next) {
                case START_DOCUMENT:
                    break;
                case END_DOCUMENT:
                    handleBuffer();
                    break;
                case COMMENT:
                    if (CommentHandler != null) {
                        handleBuffer();
                        CommentHandler.__call__(new PyUnicode(xmlReader.getText()));
                    }
                    break;
                case DTD:
                    if (ExternalEntityRefHandler != null) {
                        List<NotationDeclaration> l = (List<NotationDeclaration>) xmlReader.getProperty("javax.xml.stream.notations");
                        for (NotationDeclaration decl : l) {
                            ExternalEntityRefHandler.__call__(Py.None, Py.None, new PyUnicode(decl.getSystemId()), new PyUnicode(decl.getPublicId()));
                        }
                    }
                    break;
                case CDATA:
                case CHARACTERS:
                case SPACE:
                    if (buffer_text) {
                        buffer.append(xmlReader.getText());
                    } else {
                        if (CharacterDataHandler != null && CharacterDataHandler != Py.None) {
                            CharacterDataHandler.__call__(new PyUnicode(xmlReader.getText()));
                        }
                    }
                    break;
                case START_ELEMENT:
                    PyUnicode tagName;
                    if (StartElementHandler != null) {
                        handleBuffer();
                        tagName = new PyUnicode(xmlReader.getLocalName());
                        PyDictionary attrs = new PyDictionary();
                        for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
                            attrs.put(new PyUnicode(xmlReader.getAttributeLocalName(i)), new PyUnicode(xmlReader.getAttributeValue(i)));
                        }
                        StartElementHandler.__call__(tagName, attrs);
                    }
                    break;
                case END_ELEMENT:
                    if (EndElementHandler != null) {
                        handleBuffer();
                        tagName = new PyUnicode(xmlReader.getLocalName());
                        EndElementHandler.__call__(tagName);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

