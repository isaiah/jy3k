package org.python.modules.select;

import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.Py;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.ThreadState;
import org.python.io.ChannelFD;
import org.python.io.util.FilenoUtil;
import org.python.modules._socket.PySocket;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExposedModule(name = "select")
public class SelectModule {

    @ExposedFunction(defaults={"0"})
    public static PyObject select(ThreadState ts, PyObject rlistObj, PyObject wlistObj, PyObject xlistObj, int timeout) {
        Map<SelectableChannel, PyObject> rlist = lookupFilenos(ts, rlistObj.asIterable());
        Map<SelectableChannel, PyObject> wlist = lookupFilenos(ts, wlistObj.asIterable());
        Map<SelectableChannel, PyObject> xlist = lookupFilenos(ts, xlistObj.asIterable());
        Selector selector = ts.selectorPool().get();
        List<PyObject> read = new ArrayList<>();
        List<PyObject> write = new ArrayList<>();
        try {
            for (SelectableChannel ch : rlist.keySet()) {
                int ops = 0;
                if (ch instanceof ServerSocketChannel) {
                    ops = SelectionKey.OP_ACCEPT;
                } else {
                    if (((SocketChannel) ch).isConnected()) {
                        ops = SelectionKey.OP_READ;
                    } else {
                        ops = SelectionKey.OP_CONNECT;
                    }
                }
                ch.register(selector, ops);
            }
            for (SelectableChannel ch : wlist.keySet()) {
                int ops = ch.validOps();
                ops &= ~SelectionKey.OP_READ;
                ch.register(selector, ops);
            }
//            for (SelectableChannel ch : xlist) {
//                ch.register(selector, 0);
//            }
            if (timeout > 0) {
                selector.select(timeout);
            } else {
                selector.select();
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            keys.forEach(key -> {
                if (rlist.containsKey(key.channel())) {
                    read.add(rlist.get(key.channel()));
                } else if (wlist.containsKey(key.channel())) {
                    write.add(wlist.get(key.channel()));
                }
            });
        } catch (ClosedChannelException e) {
            throw Py.IOError(e);
        } catch (IOException e) {
            throw Py.IOError(e);
        } finally {
            ts.selectorPool().put(selector);
        }
        return new PyTuple(new PyList(read), new PyList(write), new PyList());
    }

    private static int[] unwrapIntegerList(PyObject list) {
        int[] ret = new int[list.__len__()];
        int i = 0;
        for (PyObject obj : list.asIterable()) {
            ret[i++] = obj.asInt();
        }
        return ret;
    }

    private static Map<SelectableChannel, PyObject> lookupFilenos(ThreadState ts, Iterable<PyObject> list) {
        FilenoUtil filenoUtil = ts.filenoUtil();
        Map<SelectableChannel, PyObject> channels = new HashMap<>();
        for (PyObject socket : list) {
            ChannelFD fd;
            if (socket instanceof PyLong) {
                fd = filenoUtil.getWrapperFromFileno(socket.asInt());
                if (!(fd.ch instanceof SelectableChannel)) {
                    throw Py.IOError("File descriptor not selectable, only sockets are supported");
                }
                channels.put((SelectableChannel) fd.ch, socket);
            } else if (socket instanceof PySocket){
                fd = ((PySocket) socket).getFD();
            } else {
                throw Py.IOError("File descriptor not selectable, only socket fd is supported");
            }

            if (!(fd.ch instanceof SelectableChannel)) {
                throw Py.IOError("File descriptor not selectable, only socket fd is supported");
            }
            channels.put((SelectableChannel) fd.ch, socket);
        }
        return channels;
    }
}
