package org.python.modules._blake2;

import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;
import org.python.internal.blake2.Blake2s;

@ExposedType(name = "_blake2.blake2s")
public class PyBlake2s extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyBlake2s.class);

    private Blake2s blake;
    public PyBlake2s(int digestLength, byte[] key) {
        super(TYPE);
        blake = new Blake2s(digestLength, key);
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper new_, boolean init, PyType subtype,
                                PyObject[] args, String[] keywords) {
        PyBlake2s blake = new PyBlake2s(64, new byte[0]);
        if (args.length > 0) {
            blake.blake2s_update(args[0]);
        }
        return blake;
    }

    @ExposedMethod
    public PyObject blake2s_digest() {
        return new PyBytes(blake.digest());
    }

    @ExposedMethod
    public PyObject blake2s_hexdigest() {
        byte[] bytes = blake.digest();
        StringBuffer sb = new StringBuffer(bytes.length);
        for(int i=0; i < bytes.length; i++){
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit((bytes[i] & 0xF), 16));
        }
        return new PyUnicode(sb);
    }

    @ExposedMethod
    public PyObject blake2s_update(PyObject val) {
        blake.update(Py.unwrapBuffer(val));
        return Py.None;
    }
}
