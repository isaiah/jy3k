package org.python.io;

import org.python.io.util.FilenoUtil;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;

/**
 * A primary representation of open files in java, which holds a Channel, and acts like a file descriptor
 */
public class ChannelFD {
    public Channel ch;
    public FilenoUtil filenoUtil;
    public int fileno;

    public ChannelFD(Channel ch, FilenoUtil filenoUtil) {
        this.ch = ch;
        this.filenoUtil = filenoUtil;
        initFileno();

        filenoUtil.registerWrapper(this.fileno, this);
    }

    public void close() throws IOException {
        if (!ch.isOpen()) {
            throw new ClosedChannelException();
        }
        try {
            ch.close();
        } finally {
            filenoUtil.unregisterWrapper(fileno);
        }
    }

    private void initFileno() {
        this.fileno = filenoUtil.getNewFileno();
    }
}
