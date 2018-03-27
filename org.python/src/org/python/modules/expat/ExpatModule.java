package org.python.modules.expat;

import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyLong;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PyUnicode;

@ExposedModule(name = "pyexpat")
public class ExpatModule {
    @ExposedConst
    public static final int XML_PARAM_ENTITY_PARSING_NEVER = 0;
    @ExposedConst
    public static final int XML_PARAM_ENTITY_PARSING_UNLESS_STANDALONE = 1;
    @ExposedConst
    public static final int XML_PARAM_ENTITY_PARSING_ALWAYS = 2;

    public static final String XML_ERROR_NO_MEMORY = "out of memory";
    public static final String XML_ERROR_SYNTAX = "syntax error";
    public static final String XML_ERROR_NO_ELEMENTS = "no element found";
    public static final String XML_ERROR_INVALID_TOKEN = "not well-formed (invalid token)";
    public static final String XML_ERROR_UNCLOSED_TOKEN = "unclosed token";
    public static final String XML_ERROR_PARTIAL_CHAR = "partial character";
    public static final String XML_ERROR_TAG_MISMATCH = "mismatched tag";
    public static final String XML_ERROR_DUPLICATE_ATTRIBUTE = "duplicate attribute";
    public static final String XML_ERROR_JUNK_AFTER_DOC_ELEMENT = "junk after document element";
    public static final String XML_ERROR_PARAM_ENTITY_REF = "illegal parameter entity reference";
    public static final String XML_ERROR_UNDEFINED_ENTITY = "undefined entity";
    public static final String XML_ERROR_RECURSIVE_ENTITY_REF = "recursive entity reference";
    public static final String XML_ERROR_ASYNC_ENTITY = "asynchronous entity";
    public static final String XML_ERROR_BAD_CHAR_REF = "reference to invalid character number";
    public static final String XML_ERROR_BINARY_ENTITY_REF = "reference to binary entity";
    public static final String XML_ERROR_ATTRIBUTE_EXTERNAL_ENTITY_REF = "reference to external entity in attribute";
    public static final String XML_ERROR_MISPLACED_XML_PI = "XML or text declaration not at start of entity";
    public static final String XML_ERROR_UNKNOWN_ENCODING = "unknown encoding";
    public static final String XML_ERROR_INCORRECT_ENCODING = "encoding specified in XML declaration is incorrect";
    public static final String XML_ERROR_UNCLOSED_CDATA_SECTION = "unclosed CDATA section";
    public static final String XML_ERROR_EXTERNAL_ENTITY_HANDLING = "error in processing external entity reference";
    public static final String XML_ERROR_NOT_STANDALONE = "document is not standalone";
    public static final String XML_ERROR_UNEXPECTED_STATE = "unexpected parser state - please send a bug report";
    public static final String XML_ERROR_ENTITY_DECLARED_IN_PE = "entity declared in parameter entity";
    public static final String XML_ERROR_FEATURE_REQUIRES_XML_DTD = "requested feature requires XML_DTD support in Expat";
    public static final String XML_ERROR_CANT_CHANGE_FEATURE_ONCE_PARSING = "cannot change setting once parsing has begun";
    public static final String XML_ERROR_UNBOUND_PREFIX = "unbound prefix";
    public static final String XML_ERROR_UNDECLARING_PREFIX = "must not undeclare prefix";
    public static final String XML_ERROR_INCOMPLETE_PE = "incomplete markup in parameter entity";
    public static final String XML_ERROR_XML_DECL = "XML declaration not well-formed";
    public static final String XML_ERROR_TEXT_DECL = "text declaration not well-formed";
    public static final String XML_ERROR_PUBLICID = "illegal character(s) in public id";
    public static final String XML_ERROR_SUSPENDED = "parser suspended";
    public static final String XML_ERROR_NOT_SUSPENDED = "parser not suspended";
    public static final String XML_ERROR_ABORTED = "parsing aborted";
    public static final String XML_ERROR_FINISHED = "parsing finished";
    public static final String XML_ERROR_SUSPEND_PE = "cannot suspend in external parameter entity";
    public static final String XML_ERROR_RESERVED_PREFIX_XML = "reserved prefix (xml) must not be undeclared or bound to another namespace name";
    public static final String XML_ERROR_RESERVED_PREFIX_XMLNS = "reserved prefix (xmlns) must not be declared or undeclared";
    public static final String XML_ERROR_RESERVED_NAMESPACE_URI = "prefix must not be bound to one of the reserved namespace names";
    public static PyObject ExpatError;

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("XMLParserType", PyXMLParser.TYPE);
        PyModule modelModule = new PyModule("pyexpat.model");
        dict.__setitem__("model", modelModule);
        PyModule errorsModule = new PyModule("pyexpat.errors");
        dict.__setitem__("errors", errorsModule);
        ExpatError = createErrorType();
        dict.__setitem__("ExpatError", ExpatError);
        dict.__setitem__("error", ExpatError);

        initErrorModule(errorsModule.__dict__);
    }

    @ExposedFunction
    public static PyObject ParserCreate(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("ParserCreate", args, keywords, "encoding", "namespace_separator", "intern");

        String encoding = ap.getString(0, "utf-8");
        String namespaceSeparator = ap.getString(1, null);
        if (namespaceSeparator != null && namespaceSeparator.length() > 1) {
            throw Py.ValueError("namespace_separator must be at most one character, omitted, or None");
        }
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

    private static PyObject createErrorType() {
        return Py.makeClass("pyexpat.ExpotError", new PyStringMap(), Py.BaseException);
    }

    private static void initErrorModule(PyObject errorModule) {
        String[] pairs = {"XML_ERROR_NO_MEMORY", "out of memory",
                "XML_ERROR_SYNTAX", "syntax error",
                "XML_ERROR_NO_ELEMENTS", "no element found",
                "XML_ERROR_INVALID_TOKEN", "not well-formed (invalid token)",
                "XML_ERROR_UNCLOSED_TOKEN", "unclosed token",
                "XML_ERROR_PARTIAL_CHAR", "partial character",
                "XML_ERROR_TAG_MISMATCH", "mismatched tag",
                "XML_ERROR_DUPLICATE_ATTRIBUTE", "duplicate attribute",
                "XML_ERROR_JUNK_AFTER_DOC_ELEMENT", "junk after document element",
                "XML_ERROR_PARAM_ENTITY_REF", "illegal parameter entity reference",
                "XML_ERROR_UNDEFINED_ENTITY", "undefined entity",
                "XML_ERROR_RECURSIVE_ENTITY_REF", "recursive entity reference",
                "XML_ERROR_ASYNC_ENTITY", "asynchronous entity",
                "XML_ERROR_BAD_CHAR_REF", "reference to invalid character number",
                "XML_ERROR_BINARY_ENTITY_REF", "reference to binary entity",
                "XML_ERROR_ATTRIBUTE_EXTERNAL_ENTITY_REF", "reference to external entity in attribute",
                "XML_ERROR_MISPLACED_XML_PI", "XML or text declaration not at start of entity",
                "XML_ERROR_UNKNOWN_ENCODING", "unknown encoding",
                "XML_ERROR_INCORRECT_ENCODING", "encoding specified in XML declaration is incorrect",
                "XML_ERROR_UNCLOSED_CDATA_SECTION", "unclosed CDATA section",
                "XML_ERROR_EXTERNAL_ENTITY_HANDLING", "error in processing external entity reference",
                "XML_ERROR_NOT_STANDALONE", "document is not standalone",
                "XML_ERROR_UNEXPECTED_STATE", "unexpected parser state - please send a bug report",
                "XML_ERROR_ENTITY_DECLARED_IN_PE", "entity declared in parameter entity",
                "XML_ERROR_FEATURE_REQUIRES_XML_DTD", "requested feature requires XML_DTD support in Expat",
                "XML_ERROR_CANT_CHANGE_FEATURE_ONCE_PARSING", "cannot change setting once parsing has begun",
                "XML_ERROR_UNBOUND_PREFIX", "unbound prefix",
                "XML_ERROR_UNDECLARING_PREFIX", "must not undeclare prefix",
                "XML_ERROR_INCOMPLETE_PE", "incomplete markup in parameter entity",
                "XML_ERROR_XML_DECL", "XML declaration not well-formed",
                "XML_ERROR_TEXT_DECL", "text declaration not well-formed",
                "XML_ERROR_PUBLICID", "illegal character(s) in public id",
                "XML_ERROR_SUSPENDED", "parser suspended",
                "XML_ERROR_NOT_SUSPENDED", "parser not suspended",
                "XML_ERROR_ABORTED", "parsing aborted",
                "XML_ERROR_FINISHED", "parsing finished",
                "XML_ERROR_SUSPEND_PE", "cannot suspend in external parameter entity",
                "XML_ERROR_RESERVED_PREFIX_XML", "reserved prefix (xml) must not be undeclared or bound to another namespace name",
                "XML_ERROR_RESERVED_PREFIX_XMLNS", "reserved prefix (xmlns) must not be declared or undeclared",
                "XML_ERROR_RESERVED_NAMESPACE_URI", "prefix must not be bound to one of the reserved namespace names"};

        PyDictionary messages = new PyDictionary();
        PyDictionary codes = new PyDictionary();
        for (int i = 0; i < pairs.length; ) {
            PyObject msg = new PyUnicode(pairs[i + 1]);
            errorModule.__setitem__(new PyUnicode(pairs[i++]), msg);
            PyObject code = new PyLong(i++);
            messages.__setitem__(code, msg);
            codes.__setitem__(msg, code);
        }
        errorModule.__setitem__("messages", messages);
        errorModule.__setitem__("codes", codes);
    }
}
