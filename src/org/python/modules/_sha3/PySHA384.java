package org.python.modules._sha3;

import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedGet;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedNew;
import org.python.expose.ExposedType;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ExposedType(name = "sha3_384")
public class PySHA384 extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PySHA384.class);

    private MessageDigest md;

    @ExposedGet
    public int block_size = 104;

    @ExposedGet
    public int digest_size;

    @ExposedGet
    public String name;

    private ByteBuffer buffer;

    public PySHA384() {
        super(TYPE);
        try {
            md = MessageDigest.getInstance("SHA3-384");
            name = md.getAlgorithm();
            digest_size = md.getDigestLength();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        buffer = ByteBuffer.allocate(10);
    }

    @ExposedNew
    public static PyObject _new(PyNewWrapper new_, boolean init, PyType subtype,
                                PyObject[] args, String[] keywords) {
        PySHA384 sha = new PySHA384();
        if (args.length > 0) {
            byte[] buf = Py.unwrapBuffer(args[0]);
            sha.buffer.put(buf);
        }
        return sha;
    }

    @ExposedMethod
    public PyObject sha3_384_digest() {
        return new PyBytes(md.digest(buffer.array()));
    }

    @ExposedMethod
    public PyObject sha3_384_hexdigest() {
        byte[] bytes = md.digest();
        StringBuffer sb = new StringBuffer(bytes.length);
        for(int i=0; i < bytes.length; i++){
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit((bytes[i] & 0xF), 16));
        }
        return new PyUnicode(sb);
    }

    @ExposedMethod
    public PyObject sha3_384_update(PyObject val) {
        byte[] bytes = Py.unwrapBuffer(val);
        md.update(bytes);
        return Py.None;
    }
}
