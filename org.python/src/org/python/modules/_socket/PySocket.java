package org.python.modules._socket;

import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Errno;
import jnr.constants.platform.ProtocolFamily;
import jnr.constants.platform.Sock;
import jnr.constants.platform.SocketOption;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedNew;
import org.python.annotations.ExposedSlot;
import org.python.annotations.ExposedType;
import org.python.annotations.SlotFunc;
import org.python.core.ArgParser;
import org.python.core.Py;
import org.python.core.PyByteArray;
import org.python.core.PyBytes;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyLong;
import org.python.core.PyMemoryView;
import org.python.core.PyNewWrapper;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.buffer.SimpleBuffer;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;
import org.python.modules.array.PyArrayArray;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_REUSEPORT;
import static org.python.modules._socket.SocketModule.*;

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
    private boolean nonblocking;
    private int timeout;

    public PySocket(PyType subtype) {
        super(subtype);
    }

    public PySocket(PyType subtype, AddressFamily family, Sock socket, ProtocolFamily proto, boolean nonblocking) {
        super(subtype);
        domain = family;
        sockType = socket;
        protocolFamily = proto;
        this.nonblocking = nonblocking;
        fd = initChannelFD();
        timeout = -1;
    }

    public PySocket(PyType subtype, int fileno) {
        super(subtype);
        ChannelFD fd = FilenoUtil.getInstance().getWrapperFromFileno(fileno);
        if (fd != null) {
            initFromFD(fd);
        }
    }

    @ExposedNew
    @ExposedSlot(SlotFunc.NEW)
    public static PyObject socket_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {
        return new PySocket(subtype);
    }

    @ExposedMethod
    public void socket___init__(PyObject[] args, String[] keywords) {
        ArgParser ap = new ArgParser("socket", args, keywords, "family", "type", "proto", "fileno");
        long af = ap.getInt(0, AddressFamily.AF_INET.intValue());
        long st = ap.getInt(1, Sock.SOCK_STREAM.intValue());
        int p = ap.getInt(2, ProtocolFamily.PF_INET.intValue());
        int fileno = ap.getInt(3, -1);
        boolean nonblocking = (st & SOCK_NONBLOCK) > 0;
        if (nonblocking) {
            st &= ~SOCK_NONBLOCK;
        }
        if (fileno >= 0) {
            ChannelFD fd = FilenoUtil.getInstance().getWrapperFromFileno(fileno);
            if (fd != null) {
                initFromFD(fd);
            }
        } else {
            domain = AddressFamily.valueOf(af);
            sockType = Sock.valueOf(st);
            protocolFamily = ProtocolFamily.valueOf(p);
            this.nonblocking = nonblocking;
            fd = initChannelFD();
            timeout = -1;
        }
//        if (subtype == TYPE) {
//            return new PySocket(subtype, AddressFamily.valueOf(af), Sock.valueOf(st), ProtocolFamily.valueOf(p), nonblocking);
//        } else {
//            if (fileno >= 0) {
//                return new PySocketDerived(subtype, fileno);
//            }
//            return new PySocketDerived(subtype, AddressFamily.valueOf(af), Sock.valueOf(st), ProtocolFamily.valueOf(p), nonblocking);
//        }
    }

    private void initFromFD(ChannelFD fd) {
        SelectableChannel ch = (SelectableChannel) fd.ch;
        if (ch instanceof SocketChannel) {
            domain = AddressFamily.AF_INET;
            sockType = Sock.SOCK_STREAM;
        } else if (ch instanceof DatagramChannel) {
            domain = AddressFamily.AF_INET;
            sockType = Sock.SOCK_DGRAM;
        }
        protocolFamily = ProtocolFamily.PF_INET;
        this.nonblocking = ch.isBlocking();
        this.fd = fd;

    }

    @ExposedMethod
    public final PyObject bind(PyObject address) {
        bindAddr = getsockaddrarg(address);
        NetworkChannel channel = (NetworkChannel) fd.ch;
        switch (sockType) {
            case SOCK_STREAM:
                if (!(channel instanceof ServerSocketChannel)) {
                    try {
                        channel.close();
                        channel = ServerSocketChannel.open();
                        if (nonblocking) {
                            ((SelectableChannel)channel).configureBlocking(false);
                        }
                        fd.ch = channel;
                    } catch (IOException e) {
                        throw Py.IOError(e);
                    }
                }
                try {
                    ServerSocket sock = ((ServerSocketChannel) channel).socket();
                    sock.setReuseAddress(true);
                    sock.bind(bindAddr);
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
                break;
            case SOCK_DGRAM:
                try {
                    DatagramSocket sock = ((DatagramChannel) channel).socket();
                    sock.setReuseAddress(true);
                    sock.bind(bindAddr);
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
                break;
            default:
                throw Py.TypeError(String.format("wrong socket type: %s", sockType.description()));
        }
        return this;
    }

    @ExposedMethod(defaults = "-1")
    public final void listen(int backlog) {
        NetworkChannel channel = (NetworkChannel) fd.ch;
        ServerSocketChannel ch;
        if (!(channel instanceof ServerSocketChannel) || backlog != -1) {
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
        if (backlog != -1) {
            try {
                ch.bind(bindAddr, backlog);
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        }
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
            if (timeout > 0) {
                ch.socket().setSoTimeout(timeout);
            }
            ch.socket().accept();
        } catch (IOException e) {
            throw Py.IOError(e);
        }
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
            if (timeout > 0) {
                ch.socket().setSoTimeout(timeout);
            }
            client = ch.socket().accept().getChannel();
        } catch (IllegalBlockingModeException e) {
            throw Py.IOError(Errno.EAGAIN);
        } catch (SocketTimeoutException e) {
            throw new PyException(SocketModule.TimeOutError, e.getMessage());
        } catch (IOException e) {
            throw Py.IOError(e);
        }
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
    public final void connect(PyObject address) {
        inUse = true;
        NetworkChannel ch = (NetworkChannel) fd.ch;
        switch (sockType) {
            case SOCK_STREAM:
                try {
                    if (ch instanceof ServerSocketChannel) {
                        // TODO Warning:
//                    throw Py.OSError("Bind before connect is not possible on JVM");
                        ch = SocketChannel.open();
                    }
                    ((SocketChannel) ch).connect(getsockaddrarg(address));
                } catch (ConnectException e) {
                    throw Py.IOError(Errno.ECONNREFUSED);
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
                break;
            case SOCK_DGRAM:
                 try {
                    ((DatagramChannel) ch).connect(getsockaddrarg(address));
                } catch (ConnectException e) {
                    throw Py.IOError(Errno.ECONNREFUSED);
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
                break;
            default:
                throw Py.TypeError(String.format("wrong socket type: %s", sockType.description()));
        }

    }

    @ExposedMethod(defaults = {"-1"})
    public final int send(PyObject data, int flags) {
        NetworkChannel ch = (NetworkChannel) fd.ch;
        ByteBuffer buf = ByteBuffer.wrap(Py.unwrapBuffer(data));
        try {
            switch (sockType) {
                case SOCK_STREAM:
                    return ((SocketChannel) ch).write(buf);
                case SOCK_DGRAM:
                    throw Py.IOError(Errno.EDESTADDRREQ);
                default:
                    throw Py.TypeError(String.format("wrong socket type: %s", sockType.description()));
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"null"})
    public final int sendto(PyObject data, PyObject flags, PyObject address) {
        // sendto(data[, flags], address) -> count
        if (flags instanceof PyTuple) {
            address = flags;
        } else {
            assert address != null: "address required";
        }
        NetworkChannel ch = (NetworkChannel) fd.ch;
        ByteBuffer buf = ByteBuffer.wrap(Py.unwrapBuffer(data));
        try {
            switch (sockType) {
                case SOCK_STREAM:
                    return ((SocketChannel) ch).write(buf);
                case SOCK_DGRAM:
                    return ((DatagramChannel) ch).send(buf, getsockaddrarg(address));
                default:
                    throw Py.TypeError(String.format("wrong socket type: %s", sockType.description()));
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod(defaults = {"-1"})
    public final void sendall(PyObject data, int flags) {
        SocketChannel ch = (SocketChannel) fd.ch;
        byte[] buf = Py.unwrapBuffer(data);
        ByteBuffer src = ByteBuffer.wrap(buf);
        try {
            while (src.remaining() > 0) {
                ch.write(src);
            }
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public final PyObject recv(PyObject[] args, String[] kws) {
        ArgParser ap = new ArgParser("recv", args, kws, "buffersize", "flags");
        int bufsize = ap.getInt(0);
        int flags = ap.getInt(1, -1);
        ReceiveFromResult ret = recv(bufsize, flags);
        return new PyBytes(ret.data, 0, ret.size);
    }

    @ExposedMethod(defaults = {"-1", "-1"})
    public final PyObject recv_into(PyObject buffer, int buffersize, int flags) {
        if (buffersize < 0) {
            buffersize = buffer.__len__();
        }

        ReceiveFromResult ret = recv(buffersize, flags);

        // EOF
        if (ret.size < 0) {
            return Py.None;
        }
        if (buffer instanceof PyByteArray) {
            ((PyByteArray) buffer).setStorage(ret.data, ret.size);
        } else if (buffer instanceof PyArrayArray) {
            ((PyArrayArray) buffer).write(ByteBuffer.wrap(ret.data));
        } else if (buffer instanceof PyMemoryView) {
            ((PyMemoryView) buffer).setBuf(new SimpleBuffer(ret.data, 0, ret.size));
        }

        return new PyLong(ret.size);
    }

    static class ReceiveFromResult {
        byte[] data;
        SocketAddress address;
        int size;

        ReceiveFromResult(byte[] data, SocketAddress addr, int size) {
            this.data = data;
            this.address = addr;
            this.size = size;
        }
    }

    private final ReceiveFromResult recv(int bufsize, int flags) {
        checkConnected();
        if (bufsize < 0) {
            throw Py.ValueError("negative buffersize in recvfrom");
        }
        NetworkChannel channel = (NetworkChannel) fd.ch;
        switch(sockType) {
            case SOCK_STREAM:
                byte[] buf = new byte[bufsize];
                SocketChannel ch = (SocketChannel) channel;
                try {
                    int size;
                    if (ch.isBlocking()) {
                        size = ch.socket().getInputStream().read(buf);
                    } else {
                        size = ch.read(ByteBuffer.wrap(buf));
                    }
                    if (size == 0 && nonblocking) {
                        throw Py.OSError(Errno.valueOf(11));
                    }
                    return new ReceiveFromResult(buf, null, size);
                } catch (SocketTimeoutException e) {
                    throw new PyException(TimeOutError, e.getMessage());
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
            case SOCK_DGRAM:
                DatagramChannel datagramChannel = (DatagramChannel) channel;
                try {
                    DatagramPacket data = new DatagramPacket(new byte[bufsize], bufsize);
                    datagramChannel.socket().receive(data);
                    SocketAddress addr = data.getSocketAddress();
                    return new ReceiveFromResult(data.getData(), addr, data.getLength());
                } catch (SocketTimeoutException e) {
                    throw new PyException(TimeOutError, e.getMessage());
                } catch (IOException e) {
                    throw Py.IOError(e);
                }
            default:
                throw Py.TypeError("wrong type of socket");
        }
    }

    @ExposedMethod
    public final PyObject recvfrom(PyObject args[], String[] kws) {
        ArgParser ap = new ArgParser("recv", args, kws, "buffersize", "flags");
        int bufsize = ap.getInt(0);
        int flags = ap.getInt(1, -1);
        ReceiveFromResult ret = recv(bufsize, flags);
        PyObject peeraddress;
        if (ret.address == null) {
            peeraddress = getpeername();
        } else {
            InetSocketAddress raddr = (InetSocketAddress) ret.address;
            peeraddress = new PyTuple(new PyUnicode(raddr.getHostName()), new PyLong(raddr.getPort()));
        }
        return new PyTuple(new PyBytes(ret.data), peeraddress);
    }

    @ExposedMethod
    public final int detach() {
        int fileno = fileno();
        fd = null;
        return fileno;
    }

    private SocketAddress getsockaddrarg(PyObject address) {
        String host;
        int port;
        if (address instanceof PyTuple) {
            port = ((PyTuple) address).pyget(1).asInt();
            if (port > 65535 || port < 0) {
                throw Py.OverflowError("getsockaddrarg: port must be 0-65535");
            }
            if (port == 0) {
                return null;
            }
            host = ((PyTuple) address).pyget(0).asString();
            return new InetSocketAddress(host, port);
        }
        throw Py.TypeError("expected a (address, port) tuple");
    }

    @ExposedMethod
    public void shutdown(int flag) {
        NetworkChannel ch = (NetworkChannel) fd.ch;
        if (ch instanceof SocketChannel) {
            Socket sock = ((SocketChannel) ch).socket();
            try {
                switch (flag) {
                    case SHUT_RD:
                        sock.shutdownInput();
                        break;
                    case SHUT_WR:
                        sock.shutdownOutput();
                        break;
                    case SHUT_RDWR:
                        sock.shutdownInput();
                        sock.shutdownOutput();
                        break;
                    default:
                        throw Py.ValueError(String.format("unrecognised flag: %d", flag));
                }
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        } else {
            throw Py.IOError(Errno.EBADF);
        }
    }

    @ExposedMethod
    public void close() {
        if (fd != null && fd.ch.isOpen()) {
            try {
                fd.close();
            } catch (IOException e) {
                throw Py.IOError(Errno.EBADF);
            }
        }
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
                    addr = inetAddress.getHostName();
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
                throw Py.OSError(Errno.ENOTCONN);
            }
            try {
                InetSocketAddress raddr = (InetSocketAddress) sockChannel.getRemoteAddress();
                return new PyTuple(new PyUnicode(raddr.getHostName()), new PyLong(raddr.getPort()));
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        }
        throw Py.OSError(Errno.ENOTCONN);
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
        NetworkChannel ch = (NetworkChannel) fd.ch;
        if (ch instanceof SelectableChannel && ((SelectableChannel) ch).isBlocking()) {
            return Py.None;
        }
        if (ch instanceof SocketChannel) {
            try {
                timeout = ((SocketChannel) ch).socket().getSoTimeout();
            } catch (SocketException e) {
                throw Py.IOError(e);
            }
        } else if (ch instanceof ServerSocketChannel) {
            try {
                timeout = ((ServerSocketChannel) ch).socket().getSoTimeout();
            } catch (IOException e) {
                throw Py.IOError(e);
            }
        }
        return timeout < 0 ? Py.None : new PyFloat(timeout / 1000.0);
    }

    @ExposedMethod
    public void setblocking(boolean blocking) {
        NetworkChannel ch = (NetworkChannel) fd.ch;
        if (!(ch instanceof SelectableChannel)) {
            throw Py.IOError(Errno.ENOPROTOOPT);
        }
        try {
            ((SelectableChannel) ch).configureBlocking(blocking);
            timeout = blocking ? -1 : 0;
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    @ExposedMethod
    public void settimeout(PyObject timeoutObj) {
        if (timeoutObj == Py.None) {
            return;
        }
        timeout = (int) timeoutObj.asDouble() * 1000;
        if (timeout == 0) {
            throw Py.OSError(Errno.EINVAL);
        }
        NetworkChannel ch = (NetworkChannel) fd.ch;
        if (ch instanceof SocketChannel) {
            try {
                ((SocketChannel) ch).socket().setSoTimeout(timeout);
            } catch (SocketException e) {
                throw Py.IOError(e);
            }
        } else if (ch instanceof ServerSocketChannel) {
            try {
                ((ServerSocketChannel) ch).socket().setSoTimeout(timeout);
            } catch (SocketException e) {
                throw Py.IOError(e);
            }
        } else if (ch instanceof DatagramChannel) {
            try {
                ((DatagramChannel) ch).socket().setSoTimeout(timeout);
            } catch (SocketException e) {
                throw Py.IOError(e);
            }
        }
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
                    ch.setOption(SO_REUSEADDR, value.isTrue());
                    break;
                case SO_REUSEPORT:
                    ch.setOption(SO_REUSEPORT, value.isTrue());
                    break;
                default:
                    break;
            }
            return Py.None;
        } catch (IOException e) {
            throw Py.IOError(e);
        }
    }

    public ChannelFD getFD() {
        return fd;
    }

    private java.net.SocketOption<?> getOption(SocketOption opt) {
        switch (opt) {
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
            SelectableChannel channel;
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
            if (nonblocking) {
                channel.configureBlocking(false);
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
        if (fd == null) {
            throw Py.IOError(Errno.EBADF);
        }
//        if (inUse) {
//            throw Py.IOError(Errno.EISCONN);
//        }
    }
}
