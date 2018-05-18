package org.python.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class Source {
    private Data data;

    private interface Data {
        int length();
        char[] array();
        long lastModified();
    }

    private static char[] readFully(final File file) throws IOException {
        if (!file.isFile()) {
            throw new IOException(file + " is not a file");
        }
        return bytesToCharArray(Files.readAllBytes(file.toPath()));
    }

    private static char[] bytesToCharArray(final byte[] bytes) {
        Charset cs = StandardCharsets.UTF_8;
        int start = 0;
        // BOM detection.
        if (bytes.length > 1 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            start = 2;
            cs = StandardCharsets.UTF_16BE;
        } else if (bytes.length > 1 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            if (bytes.length > 3 && bytes[2] == 0 && bytes[3] == 0) {
                start = 4;
                cs = Charset.forName("UTF-32LE");
            } else {
                start = 2;
                cs = StandardCharsets.UTF_16LE;
            }
        } else if (bytes.length > 2 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            start = 3;
            cs = StandardCharsets.UTF_8;
        } else if (bytes.length > 3 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
            start = 4;
            cs = Charset.forName("UTF-32BE");
        }

        return new String(bytes, start, bytes.length - start, cs).toCharArray();
    }

    private static class RawData implements Data {
        private char[] array;

        private RawData(final char[] array) {
            this.array = array;
        }

        private RawData(final File file) {
            try {
                this.array = readFully(file);
            } catch (IOException e) {
                this.array = new char[0];
            }
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public char[] array() {
            return array;
        }

        @Override
        public long lastModified() {
            return 0;
        }
    }
}
