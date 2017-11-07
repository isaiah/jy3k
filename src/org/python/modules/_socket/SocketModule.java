package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Errno;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.constants.platform.SocketLevel;
import jnr.constants.platform.SocketOption;
import jnr.netdb.Service;
import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.io.util.FilenoUtil;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ExposedModule(name = "_socket")
public class SocketModule {
    static int defaulttimeout = -1;

    @ExposedConst
    public static final boolean has_ipv6 = true;

    @ExposedConst
    public static final int SHUT_RD = 0;

    @ExposedConst
    public static final int SHUT_WR = 1;

    @ExposedConst
    public static final int SHUT_RDWR = 2;

    @ModuleInit
    public static void classDictInit(final PyObject dict) {
        dict.__setitem__("socket", PySocket.TYPE);
        dict.__setitem__("error", Py.OSError);
        for (AddressFamily value : AddressFamily.values()) {
            if (value == AddressFamily.AF_UNIX) {
                continue; // disable unix domain socket
            }
            if (value.defined()) {
                dict.__setitem__(value.description(), new PyLong(value.intValue()));
            }
        }
        for (Sock value : Sock.values()) {
            if (value.defined()) {
                dict.__setitem__(value.description(), new PyLong(value.intValue()));
            }
        };
        for (SocketOption value : SocketOption.values()) {
            if (value.defined()) {
                dict.__setitem__(value.description(), new PyLong(value.intValue()));
            }
        };
        for (SocketLevel value : SocketLevel.values()) {
            if (value.defined()) {
                dict.__setitem__(value.description(), new PyLong(value.intValue()));
            }
        }
        for (ProtocolFamily value : ProtocolFamily.values()) {
             if (value.defined()) {
                dict.__setitem__(value.description(), new PyLong(value.intValue()));
            }
        }
    }

    @ExposedFunction
    public static PyObject gethostbyname(String host) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            return new PyUnicode(addr.getHostAddress());
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction
    public static PyObject gethostname() {
         try {
            InetAddress addr = InetAddress.getLocalHost();
            return new PyUnicode(addr.getHostName());
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction
    public static PyObject gethostbyaddr(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            PyObject host = new PyUnicode(addr.getHostAddress());
            PyObject alias = new PyList();
            PyObject ips = new PyList(new PyObject[]{new PyUnicode(ip)});
            return new PyTuple(host, alias, ips);
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction(defaults = {"tcp"})
    public static int getservbyname(String name, String proto) {
        Service serv = Service.getServiceByName(name, proto);
        return serv.getPort();
    }

    @ExposedFunction(defaults = {"tcp"})
    public static String getservbyport(int port, String proto) {
        if (port < 0 || port > 65535) {
            throw Py.OverflowError("getservbyport: port must be 0-65535");
        }
        Service serv = Service.getServiceByPort(port, proto);
        return serv.getName();
    }

    @ExposedFunction
    public static PyObject getdefaulttimeout() {
        if (defaulttimeout < 0) {
            return Py.None;
        }
        return new PyLong(defaulttimeout);
    }

    @ExposedFunction
    public static void setdefaulttimeout(PyObject timeout) {
        if (timeout == Py.None) {
            defaulttimeout = -1;
        } else if (timeout instanceof PyLong) {
            int t = timeout.asInt();
            if (t < 0) {
                throw Py.ValueError("Timeout value out of range");
            }
            defaulttimeout = t;
        } else {
            throw Py.TypeError(String.format("an integer is required (got type %s)", timeout.getType().fastGetName()));
        }
    }

    @ExposedFunction
    public static PyObject inet_aton(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr instanceof Inet4Address) {
                return new PyBytes(addr.getAddress());
            }
            throw Py.OSError("Illegal ip address string passed to inet_aton");
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction
    public static PyObject inet_pton(int addrFamily, String ip) {
        AddressFamily af = AddressFamily.valueOf(addrFamily);
        switch (af) {
            case AF_INET:
                return inet_aton(ip);
            case AF_INET6:
                try {
                    InetAddress addr = InetAddress.getByName(ip);
                    if (addr instanceof Inet6Address) {
                        return new PyBytes(addr.getAddress());
                    }
                    throw Py.OSError("Illegal ip address string passed to inet_pton");
                } catch (UnknownHostException e) {
                    throw Py.IOError(e);
                }
            default:
                throw Py.IOError(Errno.EAFNOSUPPORT);
        }
    }

    @ExposedFunction
    public static String inet_ntoa(PyObject bytes) {
        byte[] buf = Py.unwrapBuffer(bytes);
        try {
            InetAddress addr = InetAddress.getByAddress(buf);
            if (addr instanceof Inet4Address) {
                return addr.getHostAddress();
            }
            throw Py.OSError("packed IP wrong length for inet_ntoa");
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction
    public static String inet_ntop(int addressFamily, PyObject bytes) {
        byte[] buf = Py.unwrapBuffer(bytes);
        try {
            AddressFamily af = AddressFamily.valueOf(addressFamily);
            InetAddress addr = InetAddress.getByAddress(buf);
            if ((addr instanceof Inet4Address && af == AddressFamily.AF_INET) ||
                    (addr instanceof Inet6Address && af == AddressFamily.AF_INET6)) {
                return addr.getHostAddress();
            }
            throw Py.OSError("packed IP wrong length for inet_ntop");
        } catch (UnknownHostException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedFunction
    public static int dup(int fileno) {
        return FilenoUtil.getInstance().dup(fileno);
    }
}
