package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Errno;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedType;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

@ExposedType(name = "socket")
public class PySocket extends PyObject {
    public static PyType TYPE = PyType.fromClass(PySocket.class);

    private AddressFamily domain;
    private ProtocolFamily protocolFamily;
    private Sock sockType;
    private ChannelFD fd;
    private boolean connected;

    public PySocket(PyType subtype, AddressFamily family, Sock socket, ProtocolFamily proto) {
        super(subtype);
        domain = family;
        sockType = socket;
        protocolFamily = proto;
        fd = initChannelFD();
    }

    public PySocket(PyType subtype, int fileno) {
        ChannelFD fd = FilenoUtil.getInstance().getWrapperFromFileno(fileno);
        if (fd != null) {
            initFromFD(fd);
        }
    }

    private void initFromFD(ChannelFD fd) {
        Channel ch = fd.ch;
        if (ch instanceof SocketChannel) {
            domain = AddressFamily.AF_INET;
            sockType = Sock.SOCK_STREAM;
        } else if (ch instanceof DatagramChannel) {
            domain = AddressFamily.AF_INET;
            sockType = Sock.SOCK_DGRAM;
        }
        this.fd = fd;
    }

    @ExposedNew
    public static final PyObject socket___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                         PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("socket", args, keywords, "family", "type", "proto", "fileno");
        long af = ap.getInt(0, AddressFamily.AF_INET.intValue());
        long st = ap.getInt(1, Sock.SOCK_STREAM.intValue());
        int p = ap.getInt(2, 0);
        int fileno = ap.getInt(3, -1);
        if (subtype == TYPE) {
            return new PySocket(subtype, AddressFamily.valueOf(af), Sock.valueOf(st), ProtocolFamily.valueOf(p));
        } else {
            if (fileno >= 0) {
                return new PySocketDerived(subtype, fileno);
            }
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
        try {
            fd.close();
        } catch (IOException e) {
            throw Py.IOError(Errno.EBADF);
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject getsockname() {
        String addr = "";
        switch (domain) {
            case AF_INET:
            case AF_INET6:
                if (sockType == Sock.SOCK_STREAM) {
                    SocketChannel sockChannel = (SocketChannel) fd.ch;
                    InetAddress inetAddress = sockChannel.socket().getInetAddress();
                    if (inetAddress == null) {
                        addr = domain == AddressFamily.AF_INET ? "0.0.0.0" : "::";
                    } else {
                        addr = inetAddress.toString();
                    }
                } else {
                    DatagramChannel datagramChannel = (DatagramChannel) fd.ch;
                    InetAddress inetAddress = datagramChannel.socket().getInetAddress();
                    if (inetAddress == null) {
                        addr = domain == AddressFamily.AF_INET ? "0.0.0.0" : "::";
                    } else {
                        addr = inetAddress.toString();
                    }
                }
                break;
            default:
                break;
        }
        return new PyTuple(new PyUnicode(addr));
    }

    @ExposedMethod
    public PyObject getpeername() {
        SocketChannel sockChannel = (SocketChannel) fd.ch;
        if (!sockChannel.isConnected()) {
            throw Py.OSError(Errno.valueOf(107));
        }
        try {
            InetSocketAddress raddr = (InetSocketAddress) sockChannel.getRemoteAddress();
            return new PyTuple(new PyUnicode(raddr.getHostName()), new PyLong(raddr.getPort()));
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedGet
    public int family() {
        return domain.intValue();
    }

    @ExposedGet
    public int type() {
        return sockType.intValue();
    }

    @ExposedGet
    public int proto() {
        return protocolFamily.intValue();
    }

    @ExposedMethod
    public int fileno() {
        return fd.fileno;
    }

    @ExposedMethod
    public PyObject gettimeout() {
        if (SocketModule.defaulttimeout > 0) {
            return SocketModule.getdefaulttimeout();
        }
        return Py.None;
    }

    private ChannelFD initChannelFD() {
        try {
            Channel channel;
            switch (sockType) {
                case SOCK_STREAM:
                    channel = SocketChannel.open();
                    break;
                case SOCK_DGRAM:
                    channel = DatagramChannel.open();
                    break;
                default:
                    throw Py.TypeError("wrong socket type");
            }
            return new ChannelFD(channel, FilenoUtil.getInstance());
        } catch (IOException e) {
            throw sockerr(e);
        }
    }

    private RuntimeException sockerr(IOException cause) {
        return new RuntimeException(cause);
    }
}
