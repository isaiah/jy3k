package org.python.modules._sha3;

import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;
import org.python.internal.keccak.KeccackSponge;
import org.python.internal.keccak.Shake128;

@ExposedType(name = "_sha3.shake_128")
public class PyShake128 extends PyObject {
    public static final PyType TYPE = PyType.fromClass(PyShake128.class);

    private KeccackSponge keccak;

    public PyShake128() {
        super(TYPE);
        keccak = new Shake128();
    }

    @ExposedMethod
    public PyObject shake_128_digest(PyObject len) {
        byte[] bytes = new byte[len.__len__()];
        keccak.getSqueezeStream().read(bytes);
        return new PyBytes(bytes);
    }

    @ExposedMethod
    public PyObject shake_128_hexdigest(PyObject len) {
        byte[] bytes = new byte[len.__len__()];
        keccak.getSqueezeStream().read(bytes);
        StringBuffer sb = new StringBuffer(bytes.length);
        for(int i=0; i < bytes.length; i++){
            sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
            sb.append(Character.forDigit((bytes[i] & 0xF), 16));
        }
        return new PyUnicode(sb);
    }

    @ExposedMethod
    public PyObject shake_128_update(PyObject val) {
        byte[] bytes = Py.unwrapBuffer(val);
        keccak.getAbsorbStream().write(bytes);
        return Py.None;
    }
}
