package org.python.modules._blake2;

import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.internal.blake2.Blake2b;

@ExposedType(name = "_blake2.blake2b")
public class PyBlake2b extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyBlake2b.class);

    @ExposedGet
    public final int SALT_SIZE = _blake2module.BLAKE2B_SALT_SIZE;

    @ExposedGet
    public final int digest_size = Blake2b.Spec.max_digest_bytes;

    @ExposedGet
    public final int block_size = Blake2b.Spec.block_bytes;

    @ExposedGet
    public final String name = "blake2b";

    private Blake2b blake;
    public PyBlake2b() {
        super(TYPE);
        blake = Blake2b.Digest.newInstance();
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper new_, boolean init, PyType subtype,
                                PyObject[] args, String[] keywords) {
        PyBlake2b blake = new PyBlake2b();
        if (args.length > 0) {
            blake.blake2b_update(args[0]);
        }
        return blake;
    }

    @ExposedMethod
    public PyObject blake2b_digest() {
        return new PyBytes(blake.digest());
    }

    @ExposedMethod
    public PyObject blake2b_hexdigest() {
        byte[] bytes = blake.digest();
        StringBuffer sb = new StringBuffer(bytes.length);
        for(int i=0; i < bytes.length; i++){
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit((bytes[i] & 0xF), 16));
        }
        return new PyUnicode(sb);
    }

    @ExposedMethod
    public PyObject blake2b_update(PyObject val) {
        blake.update(Py.unwrapBuffer(val));
        return Py.None;
    }
}
