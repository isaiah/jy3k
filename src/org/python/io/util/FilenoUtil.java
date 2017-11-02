package org.python.io.util;

import org.python.io.ChannelFD;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class manages fake integer file descriptors
 */
public class FilenoUtil {
    public static final int FIRST_FAKE_FD = 100000;
    protected final AtomicInteger internalFilenoIndex = new AtomicInteger(FIRST_FAKE_FD);
    private final Map<Integer, ChannelFD> filenoMap = new ConcurrentHashMap<>();

    private static FilenoUtil sing = new FilenoUtil();

    public static FilenoUtil getInstance() {
        return sing;
    }

    public int getNewFileno() {
        return internalFilenoIndex.getAndIncrement();
    }

    public void registerWrapper(int fileno, ChannelFD wrapper) {
        filenoMap.put(fileno, wrapper);
    }

    public void unregisterWrapper(int fileno) {
        filenoMap.remove(fileno);
    }

    public ChannelFD getWrapperFromFileno(int fileno) {
        return filenoMap.get(fileno);
    }
}
