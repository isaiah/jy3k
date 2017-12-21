package org.python.modules.cjkcodecs;

import org.python.core.PyObject;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

@ExposedModule
public class _multibytecodec {

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("MultibyteStreamReader", PyMultibyteStreamReader.TYPE);
        dict.__setitem__("MultibyteStreamWriter", PyMultibyteStreamWriter.TYPE);
        dict.__setitem__("MultibyteIncrementalDecoder", PyMultibyteIncrementalDecoder.TYPE);
        dict.__setitem__("MultibyteIncrementalEncoder", PyMultibyteIncrementalEncoder.TYPE);
    }
}
