// Copyright (c) Corporation for National Research Initiatives
package org.python.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Bytes;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Suite;
import org.python.antlr.base.mod;
import org.python.core.CodeBootstrap;
import org.python.core.CodeFlag;
import org.python.core.CodeLoader;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyBytes;
import org.python.core.PyComplex;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyFrame;
import org.python.core.PyFunctionTable;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyRunnable;
import org.python.core.PyRunnableBootstrap;
import org.python.core.PyTableCode;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.python.util.CodegenUtils.*;

class PyFloatConstant extends Constant implements ClassConstants, Opcodes {

    private static final double ZERO = 0.0;

    final double value;

    PyFloatConstant(double value) {
        this.value = value;
    }

    @Override
    void get(Code c) {
        c.ldc(Double.valueOf(value));
        c.invokestatic(p(Py.class), "newFloat", sig(PyFloat.class, Double.TYPE));
    }

    @Override
    void put(Code c) {}

    @Override
    public int hashCode() {
        return (int)value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyFloatConstant) {
            // Ensure hashtable works things like for -0.0 and NaN (see java.lang.Double.equals).
            PyFloatConstant pyco = (PyFloatConstant)o;
            return Double.doubleToLongBits(pyco.value) == Double.doubleToLongBits(value);
        } else {
            return false;
        }
    }
}


class PyComplexConstant extends Constant implements ClassConstants, Opcodes {

    final double value;

    PyComplexConstant(double value) {
        this.value = value;
    }

    @Override
    void get(Code c) {
        c.ldc(Double.valueOf(value));
        c.invokestatic(p(Py.class), "newImaginary", sig(PyComplex.class, Double.TYPE));
    }

    @Override
    void put(Code c) {}

    @Override
    public int hashCode() {
        return (int)value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyComplexConstant) {
            // Ensure hashtable works things like for -0.0 and NaN (see java.lang.Double.equals).
            PyComplexConstant pyco = (PyComplexConstant)o;
            return Double.doubleToLongBits(pyco.value) == Double.doubleToLongBits(value);
        } else {
            return false;
        }
    }
}


class PyStringConstant extends Constant implements ClassConstants, Opcodes {

    final String value;

    PyStringConstant(String value) {
        this.value = value;
    }

    @Override
    void get(Code c) {
        c.ldc(value);
        c.invokestatic(p(PyBytes.class), "fromInterned", sig(PyBytes.class, String.class));
    }

    @Override
    void put(Code c) {}

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyStringConstant) {
            return ((PyStringConstant)o).value.equals(value);
        } else {
            return false;
        }
    }
}


class PyUnicodeConstant extends Constant implements ClassConstants, Opcodes {

    final String value;

    PyUnicodeConstant(String value) {
        this.value = value;
    }

    @Override
    void get(Code c) {
        c.ldc(value);
        c.invokestatic(p(PyUnicode.class), "fromInterned", sig(PyUnicode.class, String.class));
    }

    @Override
    void put(Code c) {}

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyUnicodeConstant) {
            return ((PyUnicodeConstant)o).value.equals(value);
        } else {
            return false;
        }
    }
}


class PyLongConstant extends Constant implements ClassConstants, Opcodes {

    final String value;

    PyLongConstant(String value) {
        this.value = value;
    }

    @Override
    void get(Code c) {
        c.ldc(value);
        c.invokestatic(p(Py.class), "newLong", sig(PyLong.class, String.class));
    }

    @Override
    void put(Code c) {}

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PyLongConstant) {
            return ((PyLongConstant)o).value.equals(value);
        } else {
            return false;
        }
    }
}


public class Module implements Opcodes, ClassConstants, CompilationContext {

    ClassFile classfile;
    Constant filename;
    String name;
    String sfilename;
    CompileUnit mainCode;
    boolean linenumbers;
    Future futures;
    Symtable st;
    List<CompileUnit> codes;
    long mtime;
    private int setter_count = 0;
    private final static int USE_SETTERS_LIMIT = 100;
    private final static int MAX_SETTINGS_PER_SETTER = 4096;

    /** The pool of Python Constants */
    Map<Constant, Constant> constants;

    public Module(String name, String filename, boolean linenumbers) {
        this(name, filename, linenumbers, -1);
    }

    public Module(String name, String filename, boolean linenumbers, long mtime) {
        this.name = name;
        this.linenumbers = linenumbers;
        this.mtime = mtime;
        classfile =
                new ClassFile(name, p(PyFunctionTable.class), ACC_SYNCHRONIZED | ACC_PUBLIC, mtime);
        constants = new HashMap<>();
        sfilename = filename;
        if (filename != null) {
            this.filename = stringConstant(filename);
        } else {
            this.filename = null;
        }
        codes = new ArrayList<>();
        futures = new Future();
    }

    public Module(String name) {
        this(name, name + ".py", true, -1);
    }

    private Constant findConstant(Constant c) {
        Constant ret = constants.get(c);
        if (ret != null) {
            return ret;
        }
        ret = c;
        c.module = this;
        // More sophisticated name mappings might be nice
        c.name = "_" + constants.size();
        constants.put(ret, ret);
        return ret;
    }

    Constant constant(PythonTree node) {
        if (node instanceof Num) {
            PyObject n = (PyObject) ((Num) node).getInternalN();
            if (n instanceof PyLong) {
                return longConstant(n.__str__().toString());
            } else if (n instanceof PyFloat) {
                return floatConstant(((PyFloat) n).getValue());
            } else if (n instanceof PyComplex) {
                return complexConstant(((PyComplex)n).imag);
            }
        } else if (node instanceof Str) {
            String s = ((Str) node).getInternalS();
            return unicodeConstant(s);
        } else if (node instanceof Bytes) {
            String s = ((Bytes) node).getInternalS();
            return stringConstant(s);
        }
        throw new RuntimeException("unexpected constant: " + node.toString());
    }

    Constant floatConstant(double value) {
        return findConstant(new PyFloatConstant(value));
    }

    Constant complexConstant(double value) {
        return findConstant(new PyComplexConstant(value));
    }

    Constant stringConstant(String value) {
        return findConstant(new PyStringConstant(value));
    }

    Constant unicodeConstant(String value) {
        return findConstant(new PyUnicodeConstant(value));
    }

    Constant longConstant(String value) {
        return findConstant(new PyLongConstant(value));
    }

    CompileUnit codeConstant(mod tree, String name, boolean fast_locals, String className,
                             int firstlineno, CompilerFlags cflags, boolean needsClassClosure) {
//        CompileUnit code = new CompileUnit(tree, name, fast_locals, firstlineno, scope, cflags, this);
//        codes.add(code);

        CodeCompiler compiler = new CodeCompiler(this);

        CompileUnit code = compiler.enterScope(name, CompilerScope.MODULE, tree, firstlineno);
        Code c = classfile.addMethod(code.fname, //
                sig(PyObject.class, ThreadState.class, PyFrame.class), ACC_PUBLIC);

        compiler.parse(tree, c, fast_locals, className, cflags, needsClassClosure);
        compiler.exitScope();
        return code;
    }

    /** This block of code writes out the various standard methods */
    public void addInit() {
        Code c = classfile.addMethod("<init>", sig(Void.TYPE, String.class), ACC_PUBLIC);
        c.aload(0);
        c.invokespecial(p(PyFunctionTable.class), "<init>", sig(Void.TYPE));
        addConstants(c);
        addCodeInit();
        c.return_();
    }

    public void addRunnable() {
        Code c = classfile.addMethod("getMain", sig(PyTableCode.class), ACC_PUBLIC);
        mainCode.get(c);
        c.areturn();
    }

//    public void addMain() {
//        Code c = classfile.addMethod("main", //
//                sig(Void.TYPE, String[].class), ACC_PUBLIC | ACC_STATIC);
//        c.new_(classfile.name);
//        c.dup();
//        c.ldc(classfile.name);
//        c.invokespecial(classfile.name, "<init>", sig(Void.TYPE, String.class));
//        c.invokevirtual(classfile.name, "getMain", sig(PyCode.class));
//        c.invokestatic(p(CodeLoader.class), CodeLoader.SIMPLE_FACTORY_METHOD_NAME,
//                sig(CodeBootstrap.class, PyCode.class));
//        c.aload(0);
//        c.invokestatic(p(Py.class), "runMain", sig(Void.TYPE, CodeBootstrap.class, String[].class));
//        c.return_();
//    }

    public void addBootstrap() {
        Code c = classfile.addMethod(CodeLoader.GET_BOOTSTRAP_METHOD_NAME, //
                sig(CodeBootstrap.class), ACC_PUBLIC | ACC_STATIC);
        c.ldc(Type.getType("L" + classfile.name + ";"));
        c.invokestatic(p(PyRunnableBootstrap.class), PyRunnableBootstrap.REFLECTION_METHOD_NAME,
                sig(CodeBootstrap.class, Class.class));
        c.areturn();
    }

    void addCodeInit() {
        for (int i = 0; i < codes.size(); i++) {
            CompileUnit pyc = codes.get(i);
            Code c = classfile.addMethod("init" + pyc.fname,
                    sig(Void.TYPE, String.class), ACC_PUBLIC|ACC_FINAL);
            pyc.put(c);
            c.return_();
        }
    }

    void addConstants(Code c) {
        classfile.addField("self", "L" + classfile.name + ";", ACC_STATIC);
        c.aload(0);
        c.putstatic(classfile.name, "self", "L" + classfile.name + ";");

        for (Constant constant : constants.values()) {
            constant.put(c);
        }

        for (int i = 0; i < codes.size(); i++) {
            CompileUnit pyc = codes.get(i);
            c.aload(0); // this
            c.aload(1); // filename
            c.invokevirtual(classfile.name, "init" + pyc.fname, sig(Void.TYPE, String.class));
        }
    }

    public void addFunctions() {
        Code code = classfile.addMethod("call_function", //
                sig(PyObject.class, Integer.TYPE, ThreadState.class, PyFrame.class), ACC_PUBLIC);

        code.aload(0); // this
        code.aload(2); // thread state
        code.aload(3); // frame
        Label def = new Label();
        Label[] labels = new Label[codes.size()];
        int i;
        for (i = 0; i < labels.length; i++) {
            labels[i] = new Label();
        }

        // Get index for function to call
        code.iload(1);
        code.tableswitch(0, labels.length - 1, def, labels);
        for (i = 0; i < labels.length; i++) {
            code.label(labels[i]);
            code.invokevirtual(classfile.name, (codes.get(i)).fname,
                    sig(PyObject.class, ThreadState.class, PyFrame.class));
            code.areturn();
        }
        code.label(def);

        // Should probably throw internal exception here
        code.aconst_null();
        code.areturn();
    }

    public void write(OutputStream stream) throws IOException {
        addInit();
        addRunnable();
//        addMain();
        addBootstrap();

        addFunctions();

        classfile.addInterface(p(PyRunnable.class));
        if (sfilename != null) {
            classfile.setSource(sfilename);
        }
        classfile.write(stream);
    }

    // Implementation of CompilationContext
    @Override
    public Future getFutures() {
        return futures;
    }

    @Override
    public String getFilename() {
        return sfilename;
    }

    @Override
    public PySTEntryObject getScopeInfo(PythonTree node) {
        return st.Symtable_Lookup(node);
    }

    @Override
    public void error(String msg, boolean err, PythonTree node) {
        if (!err) {
            try {
                Py.warning(Py.SyntaxWarning, msg, (sfilename != null) ? sfilename : "?",
                        node.getLine(), null, Py.None);
                return;
            } catch (PyException e) {
                if (!e.match(Py.SyntaxWarning)) {
                    throw e;
                }
            }
        }
        throw Py.SyntaxError(node.getToken(), msg, sfilename);
    }

    public int makeConstArray(Code code, java.util.List<? extends PythonTree> nodes) {
        int n = 1;

        if (nodes != null) {
            n += nodes.size();
        }

        int array = code.getLocal(ci(PyObject[].class));

        code.iconst(n);
        code.anewarray(p(PyObject.class));
        code.astore(array);
        code.aload(array);
        code.iconst(0);
        code.getstatic(p(Py.class), "None", ci(PyObject.class));
        code.aastore();

        for (int i = 1; i < n; i++) {
            code.aload(array);
            code.iconst(i);
            constant(nodes.get(i - 1)).get(code);
            code.aastore();
        }
        return array;
    }


    public static void compile(mod node, OutputStream ostream, String name, String filename,
            boolean linenumbers, CompilerFlags cflags) throws IOException {
        compile(node, ostream, name, filename, linenumbers, cflags, -1);
    }

    public static void compile(mod node, OutputStream ostream, String name, String filename,
            boolean linenumbers, CompilerFlags cflags, long mtime) throws IOException {
        Module module = new Module(name, filename, linenumbers, mtime);
        if (cflags == null) {
            cflags = new CompilerFlags();
        }

        new NameMangler().visit(node);
        /** create class closure if necessary */
        ClassClosureGenerator classClosure = new ClassClosureGenerator();
        classClosure.visit(node);
        new Lower().visit(node);
        new AnnotationsCreator().visit(node);
        /** split long functions into small SplitNode fragments */
//        new Splitter().visit(node);

        module.futures.preprocessFutures(node, cflags);
        /** create symbol table */
//        new ScopesCompiler(module, module.scopes).parse(node);
        module.st = Symtable.buildObject(node, filename, 0);
        /** convert SplitNode to function definitions */
//        new SplitIntoFunctions(module.scopes).visit(node);

        // Add __doc__ if it exists
        CompileUnit main = module.codeConstant(node, "<module>", false, null,
                0, cflags, false);
        module.mainCode = main;
        module.write(ostream);
    }

    public void emitNum(Num node, Code code) {
        if (node.getInternalN() instanceof PyLong) {
            longConstant(((PyObject)node.getInternalN()).__str__().toString()).get(code);
        } else if (node.getInternalN() instanceof PyFloat) {
            floatConstant(((PyFloat)node.getInternalN()).getValue()).get(code);
        } else if (node.getInternalN() instanceof PyComplex) {
            complexConstant(((PyComplex)node.getInternalN()).imag).get(code);
        }
    }

    public void emitStr(Str node, Code code) {
        String s = node.getInternalS();
        unicodeConstant(s).get(code);
    }

    public boolean emitPrimitiveArraySetters(java.util.List<? extends PythonTree> nodes, Code code)
            {
        final int n = nodes.size();
        if (n < USE_SETTERS_LIMIT) {
            return false;  // Too small to matter, so bail
        }

        // Only attempt if all nodes are either Num or Str, otherwise bail
        boolean primitive_literals = true;
        for (int i = 0; i < n; i++) {
            PythonTree node = nodes.get(i);
            if (!(node instanceof Num || node instanceof Str)) {
                primitive_literals = false;
            }
        }
        if (!primitive_literals) {
            return false;
        }

        final int num_setters = (n / MAX_SETTINGS_PER_SETTER) + 1;
        code.iconst(n);
        code.anewarray(p(PyObject.class));
        for (int i = 0; i < num_setters; i++) {
            Code setter = this.classfile.addMethod("set$$" + setter_count, //
                    sig(Void.TYPE, PyObject[].class), ACC_STATIC | ACC_PRIVATE);

            for (int j = 0; (j < MAX_SETTINGS_PER_SETTER)
                    && ((i * MAX_SETTINGS_PER_SETTER + j) < n); j++) {
                setter.aload(0);
                setter.iconst(i * MAX_SETTINGS_PER_SETTER + j);
                PythonTree node = nodes.get(i * MAX_SETTINGS_PER_SETTER + j);
                if (node instanceof Num) {
                    emitNum((Num)node, setter);
                } else if (node instanceof Str) {
                    emitStr((Str)node, setter);
                }
                setter.aastore();
            }
            setter.return_();
            code.dup();
            code.invokestatic(this.classfile.name, "set$$" + setter_count,
                    sig(Void.TYPE, PyObject[].class));
            setter_count++;
        }
        return true;
    }

}
