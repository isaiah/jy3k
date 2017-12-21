package org.python.modules._struct;

import org.python.core.PyObject;

import java.nio.ByteBuffer;

public interface Packer {
    void pack(ByteBuffer buf, PyObject value);
}
