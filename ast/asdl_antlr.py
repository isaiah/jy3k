#! /usr/bin/env python
"""Generate Java code from an ASDL description."""

# TO DO
# handle fields that have a type but no name

import os
import sys
import asdl

TABSIZE = 4
MAX_COL = 100

def is_simple(sum):
    """Return True if a sum is a simple.

    A sum is simple if its types have no fields, e.g.
    unaryop = Invert | Not | UAdd | USub
    """
    if not isinstance(sum, asdl.Sum):
        return False
    for t in sum.types:
        if t.fields:
            return False
    return True

def reflow_lines(s, depth):
    """Reflow the line s indented depth tabs.

    Return a sequence of lines where no line extends beyond MAX_COL
    when properly indented.  The first line is properly indented based
    exclusively on depth * TABSIZE.  All following lines -- these are
    the reflowed lines generated by this function -- start at the same
    column as the first character beyond the opening { in the first
    line.
    """
    size = MAX_COL - depth * TABSIZE
    if len(s) < size:
        return [s]

    lines = []
    cur = s
    padding = ""
    while len(cur) > size:
        i = cur.rfind(' ', 0, size)
        assert i != -1, "Impossible line to reflow: %s" % repr(s)
        lines.append(padding + cur[:i])
        if len(lines) == 1:
            # find new size based on brace
            j = cur.find('{', 0, i)
            if j >= 0:
                j += 2 # account for the brace and the space after it
                size -= j
                padding = " " * j
        cur = cur[i+1:]
    else:
        lines.append(padding + cur)
    return lines

class EmitVisitor(asdl.VisitorBase):
    """Visit that emits lines"""

    def __init__(self, dir):
        self.dir = dir
        super(EmitVisitor, self).__init__()

    def open(self, package, name, refersToPythonTree=1, useDataOutput=0):
        path = os.path.join(self.dir, package, "%s.java" % name)
        open(path, "w")
        self.file = open(os.path.join(self.dir, package, "%s.java" % name), "w")
        print("// Autogenerated AST node", file=self.file)
        print('package org.python.antlr.%s;' % package, file=self.file)
        if refersToPythonTree:
            print('import org.antlr.v4.runtime.CommonToken;', file=self.file)
            print('import org.antlr.v4.runtime.Token;', file=self.file)
            print('import org.antlr.v4.runtime.tree.TerminalNode;', file=self.file)
            print('import org.python.antlr.AST;', file=self.file)
            print('import org.python.antlr.ast.VisitorIF;', file=self.file)
            print('import org.python.antlr.PythonTree;', file=self.file)
            print('import org.python.antlr.adapter.AstAdapters;', file=self.file)
            print('import org.python.antlr.base.excepthandler;', file=self.file)
            print('import org.python.antlr.base.expr;', file=self.file)
            print('import org.python.antlr.base.mod;', file=self.file)
            print('import org.python.antlr.base.slice;', file=self.file)
            print('import org.python.antlr.base.stmt;', file=self.file)
            print('import org.python.core.ArgParser;', file=self.file)
            print('import org.python.core.Py;', file=self.file)
            print('import org.python.core.PyObject;', file=self.file)
            print('import org.python.core.PyUnicode;', file=self.file)
            print('import org.python.core.PyTuple;', file=self.file)
            print('import org.python.core.PyStringMap;', file=self.file)
            print('import org.python.core.PyLong;', file=self.file)
            print('import org.python.core.PyType;', file=self.file)
            print('import org.python.core.PyList;', file=self.file)
            print('import org.python.core.PyNewWrapper;', file=self.file)
            print('import org.python.core.Visitproc;', file=self.file)
            print('import org.python.annotations.ExposedGet;', file=self.file)
            print('import org.python.annotations.ExposedMethod;', file=self.file)
            print('import org.python.annotations.ExposedNew;', file=self.file)
            print('import org.python.annotations.ExposedSet;', file=self.file)
            print('import org.python.annotations.ExposedType;', file=self.file)
            print('import org.python.annotations.ExposedSlot;', file=self.file)
            print('import org.python.annotations.SlotFunc;', file=self.file)
            print('import java.util.Objects;', file=self.file)

        if useDataOutput:
            print('import java.io.DataOutputStream;', file=self.file)
            print('import java.io.IOException;', file=self.file)
            print('import java.util.ArrayList;', file=self.file)
        print(file=self.file)
    
    def close(self):
        self.file.close()

    def emit(self, s, depth):
        # XXX reflow long lines?
        lines = reflow_lines(s, depth)
        for line in lines:
            line = (" " * TABSIZE * depth) + line + "\n"
            self.file.write(line)



# This step will add a 'simple' boolean attribute to all Sum and Product 
# nodes and add a 'typedef' link to each Field node that points to the
# Sum or Product node that defines the field.

class AnalyzeVisitor(EmitVisitor):
    index = 0
    def makeIndex(self):
        self.index += 1
        return self.index

    def visitModule(self, mod):
        self.types = {}
        for dfn in mod.dfns:
            self.types[str(dfn.name)] = dfn.value
        for dfn in mod.dfns:
            self.visit(dfn)

    def visitType(self, type, depth=0):
        self.visit(type.value, type.name, depth)

    def visitSum(self, sum, name, depth):
        for t in sum.types:
            if not is_simple(sum):
                t.index = self.makeIndex()
            self.visit(t, name, depth)

    def visitProduct(self, product, name, depth):
        product.index = self.makeIndex()
        for f in product.fields:
            self.visit(f, depth + 1)

    def visitConstructor(self, cons, name, depth):
        for f in cons.fields:
            self.visit(f, depth + 1)

    def visitField(self, field, depth):
        field.typedef = self.types.get(str(field.type))

# The code generator itself.
#
class JavaVisitor(EmitVisitor):
    def visitModule(self, mod):
        for dfn in mod.dfns:
            self.visit(dfn)

    def visitType(self, type, depth=0):
        self.visit(type.value, type.name, depth)

    def visitSum(self, sum, name, depth):
        if is_simple(sum) and not name == "excepthandler":
            self.simple_sum(sum, name, depth)
            self.simple_sum_wrappers(sum, name, depth)
        else:
            self.sum_with_constructor(sum, name, depth)

    def simple_sum(self, sum, name, depth):
        self.open("ast", "%sType" % name, refersToPythonTree=0)
        self.emit('import org.python.antlr.AST;', depth)
        self.emit('', 0)

        self.emit("public enum %(name)sType {" % locals(), depth)
        self.emit("UNDEFINED,", depth + 1)
        for i in range(len(sum.types) - 1):
            type = sum.types[i]
            self.emit("%s," % type.name, depth + 1)
        self.emit("%s;" % sum.types[len(sum.types) - 1].name, depth + 1)

        self.emit("}", depth)
        self.close()

    def simple_sum_wrappers(self, sum, name, depth):
        for i in range(len(sum.types)):
            type = sum.types[i]
            self.open("op", type.name, refersToPythonTree=0)
            self.emit('import org.python.antlr.AST;', depth)
            self.emit('import org.python.antlr.base.%s;' % name, depth)
            self.emit('import org.python.antlr.PythonTree;', depth)
            self.emit('import org.python.core.Py;', depth)
            self.emit('import org.python.core.PyObject;', depth)
            self.emit('import org.python.core.PyUnicode;', depth)
            self.emit('import org.python.core.PyType;', depth)
            self.emit('import org.python.core.PyNewWrapper;', depth)
            self.emit('import org.python.annotations.ExposedGet;', depth)
            self.emit('import org.python.annotations.ExposedMethod;', depth)
            self.emit('import org.python.annotations.ExposedNew;', depth)
            self.emit('import org.python.annotations.ExposedSet;', depth)
            self.emit('import org.python.annotations.ExposedType;', depth)
            self.emit('import org.python.annotations.ExposedSlot;', depth)
            self.emit('import org.python.annotations.SlotFunc;', depth)

            self.emit('', 0)

            self.emit('@ExposedType(name = "_ast.%s", base = %s.class)' % (type.name, name), depth)
            self.emit("public class %s extends PythonTree {" % type.name, depth)
            self.emit('public static final PyType TYPE = PyType.fromClass(%s.class);' % type.name, depth + 1)
            self.emit('', 0)

            self.emit("public %s() {" % type.name, depth + 1)
            self.emit("super(TYPE);", depth + 2)
            self.emit("}", depth + 1)

            self.emit("public %s(PyType subType) {" % type.name, depth + 1)
            self.emit("super(subType);", depth + 2)
            self.emit("}", depth + 1)



            self.emit("@ExposedNew", depth + 1)
            self.emit("@ExposedSlot(SlotFunc.NEW)", depth+1)
            self.emit("public static PyObject %s_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {" % type.name, depth+1)
            self.emit("return new %s(subtype);" % type.name, depth + 2)
            self.emit("}", depth+1)


            self.emit("@ExposedMethod", depth + 1)
            self.emit("public void %s___init__(PyObject[] args, String[] keywords) {}" % type.name, depth + 1)
            self.emit('', 0)

            self.attributes(type, name, depth);

            self.emit('@ExposedMethod', depth + 1)
            self.emit("public final PyObject %s___int__() {" % type.name, depth + 1)
            self.emit('return Py.newInteger(%s);' % str(i + 1), depth + 2)
            self.emit("}", depth + 1)
            self.emit('', 0)

            # The toStringTree() method
            self.emit("@Override", depth+1)
            self.emit("public String toStringTree() {", depth + 1)
            self.emit("return %s.class.toString();" % type.name, depth + 2)
            self.emit("}", depth + 1)

            self.emit("}", depth)
            self.close()


    def attributes(self, obj, name, depth):
        field_list = []
        if hasattr(obj, "fields"):
            for f in obj.fields:
                field_list.append('new PyUnicode("%s")' % f.name)
        if len(field_list) > 0:
            self.emit("private final static PyUnicode[] fields =", depth + 1)
            self.emit("new PyUnicode[] {%s};" % ", ".join(field_list), depth+1)
            self.emit('@ExposedGet(name = "_fields")', depth + 1)
            self.emit("public PyObject get_fields() { return new PyTuple(fields); }", depth+1)
            self.emit("", 0)
        else:
            self.emit("private final static PyUnicode[] fields = new PyUnicode[0];", depth+1)
            self.emit('@ExposedGet(name = "_fields")', depth + 1)
            self.emit("public PyObject get_fields() { return Py.EmptyTuple; }", depth+1)
            self.emit("", 0)

        if str(name) in ('stmt', 'expr', 'excepthandler'):
            att_list = ['new PyUnicode("lineno")', 'new PyUnicode("col_offset")']
            self.emit("private final static PyUnicode[] attributes =", depth + 1)
            self.emit("new PyUnicode[] {%s};" % ", ".join(att_list), depth + 1)
            self.emit('@ExposedGet(name = "_attributes")', depth + 1)
            self.emit("public PyObject get_attributes() { return new PyTuple(attributes); }", depth + 1)
            self.emit("", 0)
        else:
            self.emit("private final static PyUnicode[] attributes = new PyUnicode[0];", depth+1)
            self.emit('@ExposedGet(name = "_attributes")', depth + 1)
            self.emit("public PyObject get_attributes() { return Py.EmptyTuple; }", depth+1)
            self.emit("", 0)
   
    def sum_with_constructor(self, sum, name, depth):
        self.open("base", "%s" % name)

        self.emit('@ExposedType(name = "_ast.%s", base = AST.class)' % name, depth)
        self.emit("public abstract class %(name)s extends PythonTree {" %
                    locals(), depth)
        self.emit("", 0)
        self.emit("public static final PyType TYPE = PyType.fromClass(%s.class);" % name, depth + 1);

        self.attributes(sum, name, depth);

        if str(name) == 'stmt' or str(name) == 'expr':
            self.emit(f"public abstract {name} copy();", depth+1)
        self.emit("public %(name)s(PyType subtype) {" % locals(), depth+1)
        self.emit("super(subtype);", depth+2)
        self.emit("}", depth+1)
        self.emit("", 0)

        self.emit("public %(name)s(PyType subtype, Token token) {" % locals(), depth+1)
        self.emit("super(subtype, token);", depth+2)
        self.emit("}", depth+1)
        self.emit("", 0)

        self.emit("public %(name)s(PyType subtype, PythonTree node) {" % locals(), depth+1)
        self.emit("super(subtype, node);", depth+2)
        self.emit("}", depth+1)
        self.emit("", 0)

        self.emit("@ExposedNew", depth+1)
        self.emit("@ExposedSlot(SlotFunc.NEW)", depth+1)
        self.emit("public static PyObject %s_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {" % name, depth+1)
        self.emit("return new %s(subtype) {" % name, depth + 2)
        self.emit("public String toString() {", depth + 3)
        self.emit(f"return String.format(\"<_ast.{name} object at 0x%X>\", Objects.hashCode(this));", depth + 4)
        self.emit("}", depth + 3)
        self.emit("public String toStringTree() {", depth + 3)
        self.emit("return toString();", depth + 4)
        self.emit("}", depth + 3)
        self.emit("public %s copy() {" % name,  depth + 3)
        self.emit("return null;", depth + 4)
        self.emit("}", depth + 3)
        self.emit("};", depth + 2)
        self.emit("}", depth+1)

        self.emit("public <R> R accept(VisitorIF<R> visitor) {", depth +1)
        self.emit(f"throw Py.TypeError(String.format(\"expected some sort of {name}, but got %s\", this));", depth+2);
        self.emit("}", depth+1)

        self.emit("}", depth)
        self.close()
        for t in sum.types:
            self.visit(t, name, depth)

    def visitProduct(self, product, name, depth):

        self.open("ast", "%s" % name, useDataOutput=1)
        self.emit('@ExposedType(name = "_ast.%s", base = AST.class)' % name, depth)
        self.emit("public class %(name)s extends PythonTree {" % locals(), depth)
        self.emit("public static final PyType TYPE = PyType.fromClass(%s.class);" % name, depth + 1);
        for f in product.fields:
            self.visit(f, depth + 1)
        self.emit("", depth)

        self.attributes(product, name, depth)

        self.javaMethods(product, name, name, True, product.fields,
                         depth+1)

        self.emit("}", depth)
        self.close()

    def visitConstructor(self, cons, name, depth):
        self.open("ast", cons.name, useDataOutput=1)
        ifaces = []
        for f in cons.fields:
            if str(f.type) == "expr_context":
                ifaces.append("Context")
        if ifaces:
            s = "implements %s " % ", ".join(ifaces)
        else:
            s = ""
        self.emit('@ExposedType(name = "_ast.%s", base = %s.class)' % (cons.name, name), depth);
        self.emit("public class %s extends %s %s{" %
                    (cons.name, name, s), depth)
        self.emit("public static final PyType TYPE = PyType.fromClass(%s.class);" % cons.name, depth);
        for f in cons.fields:
            self.visit(f, depth + 1)
        self.emit("", depth)

        self.attributes(cons, name, depth)

        self.javaMethods(cons, name, cons.name, False, cons.fields, depth+1)

        if "Context" in ifaces:
            self.emit("public void setContext(expr_contextType c) {", depth + 1)
            self.emit('this.ctx = c;', depth + 2)
            self.emit("}", depth + 1)
            self.emit("", 0)

        if str(name) in ('stmt', 'expr', 'excepthandler'):
            # The lineno property
            self.emit('@ExposedGet(name = "lineno")', depth + 1)
            self.emit("public int getLineno() {", depth + 1)
            self.emit('return super.getLineno();', depth + 2)
            self.emit("}", depth + 1)
            self.emit("", 0)
            self.emit('@ExposedSet(name = "lineno")', depth + 1)
            self.emit("public void setLineno(int num) {", depth + 1)
            self.emit("lineno = new PyLong(num);", depth + 2);
            self.emit("}", depth + 1)
            self.emit("", 0)

            # The col_offset property
            self.emit('@ExposedGet(name = "col_offset")', depth + 1)
            self.emit("public int getCol_offset() {", depth + 1)
            self.emit('return super.getCol_offset();', depth + 2)
            self.emit("}", depth + 1)
            self.emit("", 0)
            self.emit('@ExposedSet(name = "col_offset")', depth + 1)
            self.emit("public void setCol_offset(int num) {", depth + 1)
            self.emit("col_offset = new PyLong(num);", depth + 2);
            self.emit("}", depth + 1)
            self.emit("", 0)

        if str(cons.name) in extra_fields:
            self.extraFields(str(cons.name))

        self.emit("}", depth)
        self.close()

    def javaConstructorHelper(self, fields, depth):
        for f in fields:
            #if f.seq:
            #    self.emit("this.%s = new %s(%s);" % (f.name,
            #        self.javaType(f), f.name), depth+1)
            #else:
            self.emit("this.%s = %s;" % (f.name, f.name), depth+1)

            fparg = self.fieldDef(f)

            not_simple = True
            if f.typedef is not None and is_simple(f.typedef):
                not_simple = False
            builtin_type = f.type in self.bltinnames
            if not_simple:
                if f.seq:
                    self.emit("if (%s == null) {" % f.name, depth+1);
                    self.emit("this.%s = new ArrayList<>(0);" % f.name, depth+2)
                    self.emit("}", depth+1)
                    if not builtin_type:
                        self.emit("for(int i = 0; i < this.%(name)s.size(); i++) {" % {"name":f.name}, depth+1)
                        self.emit("PythonTree t = this.%s.get(i);" % f.name, depth+2)
                        if f.type == 'stmt':
                            self.emit("addChild(t, i, this.%s);" % f.name, depth+2)
                        else:
                            self.emit(f"if (t != null)", depth+2)
                            self.emit("t.setParent(this);", depth+3)
                        self.emit("}", depth+1)
                elif not builtin_type:
                    self.emit(f"if (this.{f.name} != null)", depth+1)
                    self.emit(f"this.{f.name}.setParent(this);", depth+2)

    #XXX: this method used to emit a pickle(DataOutputStream ostream) for cPickle support.
    #     If we want to re-add it, see Jython 2.2's pickle method in its ast nodes.
    def javaMethods(self, type, name, clsname, is_product, fields, depth):

        self.javaConstructors(type, name, clsname, is_product, fields, depth)

        # The toString() method
        self.emit('@ExposedGet(name = "repr")', depth)
        self.emit("public String toString() {", depth)
        self.emit('return "%s";' % clsname, depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        # The toStringTree() method
        self.emit("@Override", depth)
        self.emit("public String toStringTree() {", depth)
        self.emit('StringBuffer sb = new StringBuffer("%s(");' % clsname,
                    depth+1)
        for f in fields:
            self.emit('sb.append("%s=");' % f.name, depth+1)
            self.emit("sb.append(dumpThis(%s));" % f.name, depth+1)
            self.emit('sb.append(",");', depth+1)
        self.emit('sb.append(")");', depth+1)
        self.emit("return sb.toString();", depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        # The enter() method
        self.emit("public <R> boolean enter(VisitorIF<R> visitor) {", depth)
        if is_product:
            self.emit('return false;', depth+1)
        else:
            self.emit('return visitor.enter%s(this);' % clsname, depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        # The leave() method
        self.emit("public <R> void leave(VisitorIF<R> visitor) {", depth)
        if not is_product:
            self.emit('visitor.leave%s(this);' % clsname, depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        # The accept() method
        self.emit("public <R> R accept(VisitorIF<R> visitor) {", depth)
        if is_product:
            self.emit('traverse(visitor);', depth+1)
            self.emit('return null;', depth+1)
        else:
            self.emit('return visitor.visit%s(this);' % clsname, depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        # The visitChildren() method
        self.emit("public <R> void traverse(VisitorIF<R> visitor) {", depth)
        for f in fields:
            if str(f.type) in self.bltinnames:
                continue
            if is_simple(f.typedef):
                continue
            if f.seq:
                self.emit('if (%s != null) {' % f.name, depth+1)
                self.emit('for (PythonTree t : %s) {' % f.name,
                        depth+2)
                self.emit('if (t != null)', depth+3)
                self.emit('t.accept(visitor);', depth+4)
                self.emit('}', depth+2)
                self.emit('}', depth+1)
            else:
                self.emit('if (%s != null)' % f.name, depth+1)
                self.emit('%s.accept(visitor);' % f.name, depth+2)
        self.emit('}', depth)
        self.emit("", 0)

        # For ast modification
        self.emit("public void replaceField(expr value, expr newValue) {", depth)
        for f in fields:
            if f.type == 'expr':
                if f.seq:
                    self.emit(f"for (int i=0;i<this.{f.name}.size();i++){{", depth+1)
                    self.emit(f"expr thisVal = this.{f.name}.get(i);",depth+2)
                    self.emit(f"if (value == thisVal) this.{f.name}.set(i,newValue);", depth+2)
                    self.emit("}", depth+1)
                else:
                    self.emit(f"if (value == {f.name}) this.{f.name} = newValue;", depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        self.emit('public PyObject __dict__;', depth)
        self.emit("", 0)
        self.emit('@Override', depth)
        self.emit('public PyObject fastGetDict() {', depth)
        self.emit('ensureDict();', depth+1)
        self.emit('return __dict__;', depth+1)
        self.emit('}', depth)
        self.emit("", 0)

        self.emit('@ExposedGet(name = "__dict__")', depth)
        self.emit('public PyObject getDict() {', depth)
        self.emit('return fastGetDict();', depth+1)
        self.emit('}', depth)
        self.emit("", 0)

        self.emit('private void ensureDict() {', depth)
        self.emit('if (__dict__ == null) {', depth+1)
        self.emit('__dict__ = new PyStringMap();', depth+2)
        self.emit('}', depth+1)
        self.emit('}', depth)
        self.emit("", 0)


    def javaConstructors(self, type, name, clsname, is_product, fields, depth):
        if len(fields) > 0:
            self.emit("public %s() {" % (clsname), depth)
            self.emit("super(TYPE);", depth + 1)
            self.emit("}", depth)

            fnames = ['"%s"' % f.name for f in fields]
        else:
            fnames = []

        if str(name) in ('stmt', 'expr', 'excepthandler'):
            fnames.extend(['"lineno"', '"col_offset"'])
        fpargs = ", ".join(fnames)
        self.emit("@ExposedNew", depth)
        self.emit("@ExposedSlot(SlotFunc.NEW)", depth)
        self.emit("public static PyObject %s_new(PyNewWrapper _new, boolean init, PyType subtype, PyObject[] args, String[] keywords) {" % clsname, depth)
        self.emit("return new %s(subtype);" % clsname, depth + 1)
        self.emit("}", depth)

        self.emit('@ExposedMethod(names={"__init__"})', depth)
        self.emit("public void %s___init__(PyObject[] args, String[] keywords) {" % clsname, depth)
        self.emit('ArgParser ap = new ArgParser("%s", args, keywords, new String[]' % clsname, depth + 1)
        self.emit('{%s}, %s, true);' % (fpargs, len(fields)), depth + 2)
        i = 0
        for f in fields:
            self.emit("set%s(ap.getPyObject(%s, Py.None));" % (self.processFieldName(f.name),
                str(i)), depth+1)
            i += 1
        if str(name) in ('stmt', 'expr', 'excepthandler'):
            self.emit("PyObject lin = ap.getOptionalArg(%s);" % str(i), depth + 1) 
            self.emit("if (lin != null) {", depth + 1) 
            self.emit("lineno = lin;", depth + 2) 
            self.emit("}", depth + 1)
            self.emit("", 0)

            self.emit("PyObject col = ap.getOptionalArg(%s);" % str(i+1), depth + 1) 
            self.emit("if (col != null) {", depth + 1) 
            self.emit("col_offset = col;", depth + 2) 
            self.emit("}", depth + 1)
            self.emit("", 0)

        self.emit("}", depth)
        self.emit("", 0)

        fpargs = ", ".join(["PyObject %s" % f.name for f in fields])
        self.emit("public %s(%s) {" % (clsname, fpargs), depth)
        self.emit("super(TYPE);", depth + 1)
        for f in fields:
            self.emit("set%s(%s);" % (self.processFieldName(f.name), f.name), depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        self.emit("// called from derived class", depth)
        self.emit("public %s(PyType subtype) {" % clsname, depth)
        self.emit("super(subtype);", depth+1)
        self.emit("}", depth)
        self.emit("", 0)

        token = asdl.Field('Token', 'token')
        token.typedef = False
        fpargs = ", ".join([self.fieldDef(f) for f in [token] + fields])
        self.emit("public %s(%s) {" % (clsname, fpargs), depth)
        self.emit("super(TYPE, token);", depth+1)
        self.javaConstructorHelper(fields, depth)
        self.emit("}", depth)
        self.emit("", 0)

        tree = asdl.Field('PythonTree', 'tree')
        tree.typedef = False
        fpargs = ", ".join([self.fieldDef(f) for f in [tree] + fields])
        self.emit("public %s(%s) {" % (clsname, fpargs), depth)
        self.emit("super(TYPE, tree);", depth+1)
        self.javaConstructorHelper(fields, depth)
        self.emit("}", depth)
        self.emit("", 0)

        token = asdl.Field('Token', 'getToken()')
        fpargs = ", ".join([f"this.{f.name}" for f in [token] + fields])
        self.emit(f"public {clsname} copy() {{", depth)
        self.emit(f"return new {clsname}({fpargs});", depth+1)
        self.emit("}", depth)
        self.emit("", 0)


    #This is mainly a kludge to turn get/setType -> get/setExceptType because
    #getType conflicts with a method on PyObject.
    def processFieldName(self, name):
        name = str(name).capitalize()
        if name == "Type":
            name = "ExceptType"
        return name

    def visitField(self, field, depth):
        self.emit("private %s;" % self.fieldDef(field), depth)
        self.emit("public %s getInternal%s() {" % (self.javaType(field),
            str(field.name).capitalize()), depth)
        self.emit("return %s;" % field.name, depth+1)
        self.emit("}", depth)
        self.emit("public void setInternal%s(%s %s) {" % (str(field.name).capitalize(),
            self.javaType(field), field.name), depth)
        self.emit("this.%s = %s;" % (field.name, field.name), depth+1)
        self.emit("}", depth)

        self.emit('@ExposedGet(name = "%s")' % field.name, depth)
        self.emit("public PyObject get%s() {" % self.processFieldName(field.name), depth)
        if field.seq:
            self.emit("return new PyList(%s);" % field.name, depth+1)
        else:
            if str(field.type) == 'identifier':
                self.emit("if (%s == null) return Py.None;" % field.name, depth+1)
                self.emit("return new PyUnicode(%s);" % field.name, depth+1)
            elif str(field.type) == 'bool':
                self.emit("if (%s) return Py.True;" % field.name, depth+1)
                self.emit("return Py.False;", depth+1)
            elif str(field.type) == 'int':
                self.emit("return Py.newInteger(%s);" % field.name, depth+1)
            elif str(field.type) in ('constant', 'object', 'singleton'):
                self.emit("return %s;" % field.name, depth+1)
            elif is_simple(field.typedef) or str(field.type) in self.bltinnames:
                self.emit("return AstAdapters.%s2py(%s);" % (str(field.type), field.name), depth+1)
            else:
                self.emit("return %s;" % field.name, depth+1)
            #self.emit("return Py.None;", depth+1)
        self.emit("}", depth)
        self.emit('@ExposedSet(name = "%s")' % field.name, depth)
        self.emit("public void set%s(PyObject %s) {" % (self.processFieldName(field.name), field.name), depth)
        if field.seq:
            #self.emit("this.%s = new %s(" % (field.name, self.javaType(field)), depth+1)
            self.emit("this.%s = AstAdapters.py2%sList(%s);" % (field.name, str(field.type), field.name), depth+1)
        elif str(field.type) in ('constant', 'object', 'singleton'):
            self.emit("this.%s = %s;" % (field.name, field.name), depth+1)
        else:
            self.emit("this.%s = AstAdapters.py2%s(%s);" % (field.name, str(field.type), field.name), depth+1)
        self.emit("}", depth)
        self.emit("", 0)

    bltinnames = {
        'int' : 'Integer',
        'bool' : 'Boolean',
        'bytes' : 'String',
        'identifier' : 'String',
        'constant' : 'PyObject',
        'singleton' : 'PyObject',
        'string' : 'String',
        'object' : 'PyObject', # was PyObject

        #Below are for enums
        'boolop' : 'boolopType',
        'cmpop' : 'cmpopType',
        'expr_context' : 'expr_contextType',
        'operator' : 'operatorType',
        'unaryop' : 'unaryopType',
    }

    def fieldDef(self, field):
        jtype = self.javaType(field)
        name = field.name
        return "%s %s" % (jtype, name)

    def javaType(self, field, check_seq=True):
        jtype = str(field.type)
        jtype = self.bltinnames.get(jtype, jtype)
        if check_seq and field.seq:
            return "java.util.List<%s>" % jtype
        return jtype

    def extraFields(self, clsname):
        for f in extra_fields[clsname]:
            tmpl = f"""
    private boolean {f};

    public boolean is{f}() {{
        return {f};
    }}

    public void set{f}(boolean {f}) {{
        this.{f} = {f};
    }}
"""
            self.file.write(tmpl)

class VisitorVisitor(EmitVisitor):
    def __init__(self, dir):
        EmitVisitor.__init__(self, dir)
        self.ctors = []

    def visitModule(self, mod):
        for dfn in mod.dfns:
            self.visit(dfn)
        self.open("ast", "VisitorIF", refersToPythonTree=0)
        self.emit('public interface VisitorIF<R> {', 0)
        for ctor in self.ctors:
            self.emit("public boolean enter%s(%s node);" % 
                    (ctor, ctor), 1)
            self.emit("public R visit%s(%s node);" % 
                    (ctor, ctor), 1)
            self.emit("public void leave%s(%s node);" % 
                    (ctor, ctor), 1)

        self.emit('}', 0)
        self.close()

        self.open("ast", "VisitorBase")
        self.emit('public abstract class VisitorBase<R> implements VisitorIF<R> {', 0)
        for ctor in self.ctors:
            self.emit("public R visit%s(%s node) {" % 
                    (ctor, ctor), 1)
            self.emit("R ret = unhandled_node(node);", 2)
            self.emit("traverse(node);", 2)
            self.emit("return ret;", 2)
            self.emit('}', 1)
            self.emit('', 0)

            self.emit("public boolean enter%s(%s node) {" % 
                    (ctor, ctor), 1)
            self.emit("return true;", 2)
            self.emit('}', 1)
            self.emit('', 0)

            self.emit("public void leave%s(%s node) {" % 
                    (ctor, ctor), 1)
            self.emit('}', 1)
            self.emit('', 0)

        self.emit("abstract protected R unhandled_node(PythonTree node);", 1)
        self.emit("abstract public void traverse(PythonTree node);", 1)
        self.emit('}', 0)
        self.close()

    def visitType(self, type, depth=1):
        self.visit(type.value, type.name, depth)

    def visitSum(self, sum, name, depth):
        if not is_simple(sum):
            for t in sum.types:
                self.visit(t, name, depth)

    def visitProduct(self, product, name, depth):
        pass

    def visitConstructor(self, cons, name, depth):
        self.ctors.append(cons.name)

class ChainOfVisitors:
    def __init__(self, *visitors):
        self.visitors = visitors

    def visit(self, object):
        for v in self.visitors:
            v.visit(object)

def main(outdir, grammar="Python.asdl"):
    mod = asdl.parse(grammar)
    if not asdl.check(mod):
        sys.exit(1)
    c = ChainOfVisitors(AnalyzeVisitor(outdir),
                        JavaVisitor(outdir),
                        VisitorVisitor(outdir))
    c.visit(mod)

# Extra fields to add to the AST nodes
extra_fields = {
        "ClassDef": ["NeedsClassClosure"],
        "FunctionDef": ["Split"],
        "Expr": ["Print"],
        'Name': ['Expr'],
        'BinOp': ['Inplace']
        }

if __name__ == "__main__":
    import getopt
    from os.path import dirname, join, abspath

    usage = "Usage: python %s [-o outdir] [grammar]" % sys.argv[0]

    scriptdir = dirname(abspath(__file__))
    OUT_DIR = join(dirname(scriptdir), 'org.python', 'src', 'org', 'python', 'antlr')
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'o:')
    except:
       print(usage)
       sys.exit(1)
    for o, v in opts:
        if o == '-o' and v != '':
            OUT_DIR = v
    if len(opts) > 1 or len(args) > 1:
        print(usage)
        sys.exit(1)
    if len(args) == 1:
        main(OUT_DIR, args[0])
    else:
        main(OUT_DIR)

