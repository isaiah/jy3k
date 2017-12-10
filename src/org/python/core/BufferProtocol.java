package org.python.core;

import java.nio.ByteBuffer;

/**
 * This is not a real Python class, even though it has the Py* prefix. It's a simplified version of the PyBuffer in CPython
 * Since Java has already provides all kinds of Buffers in java.nio package, we should take advantage of that
 *
 * This interface provides two possibilities, one is to read the content of BufferProtocol classes as ByteBuffer, and
 * the other is to write data into the buf.
 *
 * As in practice, the BufferProtocol is only used in the senarios stated above, in case applications like NumPy for java
 * comes up, this could be extended to have better control over the life cycle of the exported bytes view.
 */
public interface BufferProtocol {
    PyBuffer getBuffer(int flags) throws PyException;

    /**
     * Get a bytebuffer as the view
     * @return ByteBuffer
     */
    ByteBuffer getBuffer();

    /**
     * Write the passed ByteBuffer into the object
     *
     * @param buf the data to be written
     * @return n the length that been written
     *
     * @throws PyException if the buffer is immutable
     */
    int write(ByteBuffer buf) throws PyException;
}
