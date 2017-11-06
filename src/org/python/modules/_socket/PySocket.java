package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Errno;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.constants.platform.SocketOption;
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
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;

@ExposedType(name = "socket")
public class PySocket extends PyObject {
    public static PyType TYPE = PyType.fromClass(PySocket.class);

    private AddressFamily domain;
    private ProtocolFamily protocolFamily;
    private Sock sockType;
    private ChannelFD fd;
    private SocketAddress bindAddr;
    private boolean connected;
    private boolean inUse;

    public PySocket(PyType subtype, AddressFamily family, Sock socket, ProtocolFamily proto) {
        super(subtype);
        domain = family;
        sockType = socket;
        protocolFamily = proto;
        fd = initChannelFD();
    }

    public PySocket(PyType subtype, int fileno) {
        super(subtype);
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
        protocolFamily = ProtocolFamily.PF_INET;
        this.fd = fd;
    }

    @ExposedNew
    public static final PyObject socket___new__(PyNewWrapper new_, boolean init, PyType subtype,
                                         PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("socket", args, keywords, "family", "type", "proto", "fileno");
        long af = ap.getInt(0, AddressFamily.AF_INET.intValue());
        long st = ap.getInt(1, Sock.SOCK_STREAM.intValue());
        int p = ap.getInt(2, ProtocolFamily.PF_INET.intValue());
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
    public final PyObject bind(PyObject address) {
        bindAddr = getsockaddrarg(address);
        return this;
    }

    @ExposedMethod(defaults = "-1")
    public final PyObject listen(int backlog) {
        NetworkChannel channel = (NetworkChannel) fd.ch;
        ServerSocketChannel ch;
        if (!(channel instanceof ServerSocketChannel)) {
            try {
                channel.close();
                ch = ServerSocketChannel.open();
                fd.ch = ch;
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            ch = (ServerSocketChannel) channel;
        }
        try {
            ch.bind(bindAddr, backlog);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        return this;
    }

    @ExposedMethod
    public final PyObject accept() {
        checkConnected();
        NetworkChannel channel = (NetworkChannel) fd.ch;
        ServerSocketChannel ch;
        if (!(channel instanceof ServerSocketChannel)) {
            try {
                channel.close();
                ch = ServerSocketChannel.open();
                fd.ch = ch;
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            ch = (ServerSocketChannel) channel;
        }
        try {
            ch.accept();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        inUse = true;
        return Py.None;
    }

    @ExposedMethod
    public final PyObject _accept() {
        checkConnected();

        NetworkChannel channel = (NetworkChannel) fd.ch;
        ServerSocketChannel ch;
        SocketChannel client;
        if (!(channel instanceof ServerSocketChannel)) {
            try {
                channel.close();
                ch = ServerSocketChannel.open();
                fd.ch = ch;
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            ch = (ServerSocketChannel) channel;
        }
        try {
            client = ch.accept();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        inUse = true;
        ChannelFD fd = new ChannelFD(client, FilenoUtil.getInstance());
        InetSocketAddress remoteAddress = null;
        try {
            remoteAddress = (InetSocketAddress) client.getRemoteAddress();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        PyTuple addr = new PyTuple(new PyUnicode(remoteAddress.getHostName()), new PyLong(remoteAddress.getPort()));
        return new PyTuple(new PyLong(fd.fileno), addr);
    }

    @ExposedMethod
    public final PyObject connect(PyObject address) {
        inUse = true;
        SocketChannel ch = (SocketChannel) fd.ch;
        try {
            ch.connect(getsockaddrarg(address));
        } catch(ConnectException e) {
            throw Py.IOError(Errno.ECONNREFUSED);
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        return this;

    }

    private SocketAddress getsockaddrarg(PyObject address) {
        String host;
        int port;
        if (address instanceof PyTuple) {
            host = ((PyTuple) address).pyget(0).asString();
            port = ((PyTuple) address).pyget(1).asInt();
            if (port > 65535 || port < 0) {
                throw Py.OverflowError("getsockaddrarg: port must be 0-65535");
            }
            return new InetSocketAddress(host, port);
        }
        throw Py.TypeError("expected a (address, port) tuple");
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
        int port = 0;
        switch (domain) {
            case AF_INET:
            case AF_INET6:
                NetworkChannel sockChannel = (NetworkChannel) fd.ch;
                InetSocketAddress inetAddress = null;
                try {
                    inetAddress = (InetSocketAddress) sockChannel.getLocalAddress();
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
                if (inetAddress == null) {
                    addr = domain == AddressFamily.AF_INET ? "0.0.0.0" : "::";
                } else {
                    addr = inetAddress.toString();
                    port = inetAddress.getPort();
                }
                break;
            default:
                break;
        }
        return new PyTuple(new PyUnicode(addr), new PyLong(port));
    }

    @ExposedMethod
    public PyObject getpeername() {
        if (fd.ch instanceof SocketChannel) {
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
        throw Py.OSError(Errno.valueOf(107));
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

    @ExposedMethod
    public PyObject getsockopt(int level, int name) {
        SocketOption opt = SocketOption.valueOf(name);
        NetworkChannel ch = (NetworkChannel) fd.ch;
        Object value;
        try {
            value = ch.getOption(getOption(opt));
        } catch (IOException e) {
            throw Py.IOError(e);
        }
        if (opt == SocketOption.SO_REUSEADDR || opt == SocketOption.SO_REUSEPORT) {
            return new PyLong((boolean) value ? 1 : 0);
        }
        return Py.None;
    }

    @ExposedMethod
    public PyObject setsockopt(int level, int name, PyObject value) {
        SocketOption opt = SocketOption.valueOf(name);
        NetworkChannel ch = (NetworkChannel) fd.ch;
        try {
            switch (opt) {
                case SO_REUSEADDR:
                    ch.setOption(SO_REUSEADDR, value.__bool__());
                    break;
                case SO_REUSEPORT:
                    ch.setOption(SO_REUSEPORT, value.__bool__());
                    break;
                default:
                    break;
            }
            return Py.None;
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    private java.net.SocketOption<?> getOption(SocketOption opt) {
        switch(opt) {
            case SO_REUSEADDR:
                return SO_REUSEADDR;
            case SO_REUSEPORT:
                return SO_REUSEPORT;
            default:
                break;
        }
        throw Py.ValueError("unrecognised socket option");
    }

    private ChannelFD initChannelFD() {
        try {
            NetworkChannel channel;
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

    private void checkConnected() {
        if (inUse) {
            throw Py.IOError(Errno.EISCONN);
        }
    }
}
