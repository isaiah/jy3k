package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;

@ExposedType(name = "socket")
public class PySocket extends PyObject {
    public static PyType TYPE = PyType.fromClass(PySocket.class);

    private AddressFamily sock_family;
    private ProtocolFamily sock_proto;
    private Sock sock;

    public PySocket(PyType subtype, AddressFamily family, Sock socket, ProtocolFamily proto) {
        super(subtype);
        sock_family = family;
        sock = socket;
        sock_proto = proto;
    }

    @ExposedNew
    public static final PyObject socket___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                         PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("socket", args, keywords, "family", "type", "proto", "fileno");
        long af = ap.getInt(0, AddressFamily.AF_INET.intValue());
        long st = ap.getInt(1, Sock.SOCK_STREAM.intValue());
        int p = ap.getInt(2, 0);
        if (subtype == TYPE) {
            return new PySocket(subtype, AddressFamily.valueOf(af), Sock.valueOf(st), ProtocolFamily.valueOf(p));
        } else {
            return new PySocketDerived(subtype, AddressFamily.valueOf(af), Sock.valueOf(st), ProtocolFamily.valueOf(p));
        }
    }

    @ExposedMethod()
    public final PyObject socket_bind(PyObject address) {
        PyObject host, port;
        if (address instanceof PyTuple) {
            host = ((PyTuple) address).pyget(0);
            port = ((PyTuple) address).pyget(1);
        }
        return this;
    }

    @ExposedMethod
    public PyObject close() {
        return Py.None;
    }
}
