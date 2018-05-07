package org.python.util;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * Created by isaiah on 3/16/17.
 */
public class Asmcheck {
    public static void main(String[] args) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setXMLReporter((message, errorType, relatedInformation, location) -> {
            System.out.println("message:" + message);
            System.out.println("error key:"+ errorType);
        });
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader("<a>"));
        while (reader.hasNext()) {
            reader.next();
        }
    }

    public void test() {
        Foo foo = new Foo(Str::str_new);
    }

    interface Bar {
        PyObject _new(PyType tp, PyObject[] args, String[] keywords);
    }

    class Foo {
        private Bar bar;

        public Foo(Bar b) {
            bar = b;
        }
    }

    static class Str {
        public static PyObject str_new(PyType tp, PyObject[] args, String[] kw) {
            return Py.One;
        }
    }
}
