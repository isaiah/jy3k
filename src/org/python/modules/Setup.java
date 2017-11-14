// Copyright (c) Corporation for National Research Initiatives
package org.python.modules;

// This is sort of analogous to CPython's Modules/Setup file.  Use this to
// specify additional builtin modules.

import jnr.posix.util.Platform;

public class Setup {
    // Each element of this array is a string naming a builtin module to
    // add to the system.  The string has the following allowable forms:
    //
    // name
    //     The module name is `name' and the class name is
    //     org.python.modules.name
    //
    // name:class
    //     The module name is `name' and the class name is `class' where
    //     class must be a fully qualified Java class name
    //
    // name:null
    //     The module `name' is removed from the list of builtin modules
    //
    // That isn't very useful here, but you can add additional builtin
    // modules by editing the Jython registry file.  See the property
    // python.modules.builtin for details.
    public static String[] builtinModules = {
            "_ast:org.python.antlr.ast.AstModule",
            "_blake2:org.python.modules._blake2._blake2module",
            "_bytecodetools",
            "_bz2:org.python.modules.bz2.bz2",
            "_codecs",
            "_codecs_cn:org.python.modules.cjkcodecs._codecs_cn",
            "_codecs_tw:org.python.modules.cjkcodecs._codecs_tw",
            "_codecs_hk:org.python.modules.cjkcodecs._codecs_hk",
            "_codecs_kr:org.python.modules.cjkcodecs._codecs_kr",
            "_codecs_jp:org.python.modules.cjkcodecs._codecs_jp",
            "_collections:org.python.modules._collections.Collections",
            "_csv:org.python.modules._csv._csv",
            "_datetime:org.python.modules._datetime.DatetimeModule",
            "_functools:org.python.modules._functools._functools",
            "_hashlib",
            "_heapq",
            "_imp:org.python.modules._imp",
            "_io:org.python.modules._io._io",
            "_json:org.python.modules._json._json",
            "_jythonlib:org.python.modules._jythonlib._jythonlib",
            "_multibytecodec:org.python.modules.cjkcodecs._multibytecodec",
            "_multiprocessing:org.python.modules._multiprocessing._multiprocessing",
            "_operator:org.python.modules.operator",
//            "_pickle",
            "_posixsubprocess",
            "_random:org.python.modules.random.RandomModule",
            "_sha3:org.python.modules._sha3._sha3module",
            "_signal",
            "_socket:org.python.modules._socket.SocketModule",
            "_sre:org.python.modules.sre._sre",
            "_string",
            "_struct:org.python.modules._struct._struct",
            "_symtable:org.python.modules._symtable.SymtableModule",
            "_sysconfigdata_m_java9_",
            "_systemrestart",
            "_types",
            "_thread:org.python.modules.thread._thread",
            "_warnings",
            "_weakref:org.python.modules._weakref.WeakrefModule",
            "array:org.python.modules.ArrayModule",
            "binascii",
            "cmath",
            "errno",
            "faulthandler:org.python.modules.FaultHandler",
            "fcntl",
            "gc",
            "itertools:org.python.modules.itertools.itertools",
            "jarray",
            "jffi:org.python.modules.jffi.jffi",
            "marshal",
            "math",
            (Platform.IS_WINDOWS ? "nt" : "posix") + ":org.python.modules.posix.PosixModule",
            "parser:org.python.modules.parser.ParserModule",
            "select:org.python.modules.select.SelectModule",
            "subprocess:org.python.modules.subprocess.SubprocessModule",
            "synchronize",
            "sys:org.python.modules.sys.SysModule",
            "time:org.python.modules.time.TimeModule",
            "unicodedata:org.python.modules.unicodedata.unicodedata",
            "winreg:org.python.modules.winreg.WinregModule",
            "zipimport:org.python.modules.zipimport.ZipImportModule",
            "zlib:org.python.modules.zlib.ZlibModule",
    };
}
