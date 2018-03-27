package org.python.core;

import java.io.InputStream;
import java.io.OutputStream;

public interface JavaIO {
    InputStream inputStream();
    OutputStream outputStream();
}
