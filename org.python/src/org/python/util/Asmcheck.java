package org.python.util;

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
            System.out.println(message);
            System.out.println(errorType);
        });
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader("<a>"));
        while (reader.hasNext()) {
            reader.next();
        }
    }
}
