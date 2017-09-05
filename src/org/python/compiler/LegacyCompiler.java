package org.python.compiler;

import org.python.antlr.base.mod;
import org.python.core.BytecodeLoader;
import org.python.core.CompilerFlags;
import org.python.core.PyCode;
import org.python.core.PythonCodeBundle;
import org.python.core.PythonCompiler;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LegacyCompiler implements PythonCompiler {

    public PythonCodeBundle compile(mod node, String name, String filename,
            boolean linenumbers, CompilerFlags cflags) {
        return new LazyLegacyBundle(node, name, filename, linenumbers, cflags);
    }

    private static class LazyLegacyBundle implements PythonCodeBundle {

        private final mod node;
        private final String name;
        private final String filename;
        private final boolean linenumbers;
        private final CompilerFlags cflags;
        private ByteArrayOutputStream ostream = null;

        public LazyLegacyBundle(mod node, String name, String filename,
                boolean linenumbers, CompilerFlags cflags) {
            this.node = node;
            this.name = name;
            this.filename = filename;
            this.linenumbers = linenumbers;
            this.cflags = cflags;
        }

        public PyCode loadCode() throws Exception {
            return BytecodeLoader.makeCode(name, ostream().toByteArray(),
                    filename);
        }

        public void writeTo(OutputStream stream) throws Exception {
            if (this.ostream != null) {
                stream.write(ostream.toByteArray());
            } else {
                Module.compile(node, stream, name, filename, linenumbers, cflags);
            }
        }

        private ByteArrayOutputStream ostream() throws Exception {
            if (ostream == null) {
                ostream = new ByteArrayOutputStream();
                Module.compile(node, ostream, name, filename, linenumbers,
                        cflags);
            }
            return ostream;
        }

    }

}
