/* Copyright (c) Jython Developers */
package org.python.core.generator;

import org.python.core.BuiltinDocs;
import org.python.core.CodeFlag;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyFrame;
import org.python.core.PyIterator;
import org.python.core.PyObject;
import org.python.core.PyTableCode;
import org.python.core.PyTraceback;
import org.python.core.PyTuple;
import org.python.core.PyType;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.python.core.Visitproc;
import org.python.core.finalization.FinalizableBuiltin;
import org.python.core.finalization.FinalizeTrigger;
import org.python.annotations.ExposedGet;
import org.python.annotations.ExposedMethod;
import org.python.annotations.ExposedType;

@ExposedType(name = "generator", base = PyObject.class, isBaseType = false, doc = BuiltinDocs.generator_doc)
public class PyGenerator extends PyIterator implements FinalizableBuiltin {

    public static final PyType TYPE = PyType.fromClass(PyGenerator.class);

    @ExposedGet
    protected PyFrame gi_frame;

    @ExposedGet
    protected PyCode gi_code = null;

    @ExposedGet
    protected boolean gi_running;

    protected PyObject closure;

    public PyGenerator(PyFrame frame, PyObject closure) {
        this(TYPE, frame, closure);
    }

    public PyGenerator(PyType subType, PyFrame frame, PyObject closure) {
        super(subType);
        gi_frame = frame;
        if (gi_frame != null) {
            gi_code = gi_frame.f_code;
        }
        this.closure = closure;
        FinalizeTrigger.ensureFinalizer(this);
    }

    public PyObject send(PyObject value) {
        return generator_send(value);
    }

    @ExposedMethod(doc = BuiltinDocs.generator_send_doc)
    public final PyObject generator_send(PyObject value) {
        ThreadState state = Py.getThreadState();
        if (gi_frame == null) {
            if (this instanceof PyAsyncGenerator) {
                throw Py.StopAsyncIteration();
            }
            PyException exc = Py.StopIteration();
            exc.tracebackHere(state.frame);
            throw exc;
        }

        if (gi_frame.f_lasti == 0 && value != Py.None && value != null) {
            throw Py.TypeError("can't send non-None value to a just-started " + tp());
        }
        try {
            return gen_send_ex(state, value, false, false);
        } catch (PyException e) {
            e.tracebackHere(state.frame);
            throw e;
        }
    }

    public PyObject throw$(PyObject type, PyObject value, PyObject tb) {
        return generator_throw$(type, value, tb);
    }

    @ExposedMethod(names="throw", defaults={"null", "null"}, doc = BuiltinDocs.generator_throw_doc)
    public final PyObject generator_throw$(PyObject type, PyObject value, PyObject tb) {
        if (tb == Py.None) {
            tb = null;
        } else if (tb != null && !(tb instanceof PyTraceback)) {
            throw Py.TypeError("throw() third argument must be a traceback object");
        }

        if (gi_frame != null && gi_frame.f_yieldfrom != null) {
            PyObject ret = null;
            PyException err = null;
            if (type == Py.GeneratorExit) {
                gi_running = true;
                try {
                    gen_close_iter(gi_frame.f_yieldfrom);
                } catch (PyException e) {
                    throw e;
                } finally {
                    gi_running = false;
                }
                gi_frame.f_yieldfrom = null;
                return raiseException(type, value, tb);
            }
            if (gi_frame.f_yieldfrom instanceof PyGenerator) {
                gi_running = true;
                try {
                    ret = ((PyGenerator) gi_frame.f_yieldfrom).throw$(type, value, tb);
                } catch (PyException e) {
                    if (!e.match(Py.StopIteration)) {
                        err = e;
                    }
                    gi_frame.f_stacktop = e.value.__findattr__("value");
                } finally {
                    gi_running = false;
                }
            } else {
                PyObject meth = gi_frame.f_yieldfrom.__findattr__("throw");
                if (meth == null) {
                    return raiseException(type, value, tb);
                }
                gi_running = true;
                try {
                    ret = meth.__call__(type, value, tb);
                } finally {
                    gi_running = false;
                }
            }
            if (ret == null) {
                gi_frame.f_yieldfrom = null;
                if (err == null) {
                    gi_frame.f_lasti++;
                } else {
                    gi_frame.previousException = err;
                }
                ret = gen_send_ex(Py.getThreadState(), Py.None, err != null, false);
            }
            return ret;
        }
        return raiseException(type, value, tb);
    }

    public PyObject close() {
        return generator_close();
    }

    @ExposedMethod(doc = BuiltinDocs.generator_close_doc)
    public final PyObject generator_close() {
        PyException pye = null;
        PyObject retval;
        if (gi_frame == null) {
            return Py.None;
        }
        PyObject yf = gi_frame.f_yieldfrom;
        if (yf != null) {
            gi_running = true;
            try {
                gi_frame.f_yieldfrom = null;
                gen_close_iter(yf);
            } catch (PyException e) {
                pye = e;
            } finally {
                gi_running = false;
            }
        }
        if (pye == null) {
            pye = Py.GeneratorExit();
        }

        // if generator closed before call to next, advance anyway
        if (gi_frame.f_lasti == 0) {
            __next__();
        }
        try {
            // clean up
            gi_frame.previousException = pye;
            retval = gen_send_ex(Py.getThreadState(), Py.None, true, true);
        } catch (PyException e) {
            if (e.match(Py.StopIteration) || e.match(Py.GeneratorExit)) {
                return Py.None;
            }
            throw e;
        }
        if (retval != null) {
            throw Py.RuntimeError(tp() + " ignored GeneratorExit");
        }
        // not reachable
        return null;
    }

    @ExposedMethod(doc = BuiltinDocs.generator___next___doc)
    public final PyObject generator___next__() {
        ThreadState state = Py.getThreadState();
        try {
            return gen_send_ex(state, Py.None, false, false);
        } catch (PyException e) {
            e.tracebackHere(state.frame);
            throw e;
        }
    }

    @Override
    public PyObject __iter__() {
        return generator___iter__();
    }

    @ExposedMethod(doc = BuiltinDocs.generator___iter___doc)
    public final PyObject generator___iter__() {
        return this;
    }

    private PyObject raiseException(PyObject type, PyObject value, PyObject tb) {
        PyException pye;
        if (value == null) {
            pye = PyException.doRaise(type);
        } else {
            pye = new PyException(type, value);
        }
        pye.traceback = (PyTraceback) tb;
        if (gi_frame != null) {
            gi_frame.previousException = pye;
        }
        return gen_send_ex(Py.getThreadState(), Py.None, true, false);
    }
    
    @Override
    public void __del_builtin__() {
        if (gi_frame  == null || gi_frame.f_lasti == -1) {
            return;
        }

        // If `gen` is a coroutine, and if it was never awaited on,
        // issue a RuntimeWarning.
        if (this instanceof PyCoroutine && gi_frame.previousException == null && gi_frame.f_lasti == 0) {
            Py.RuntimeWarning(String.format("coroutine '%.50s' was never awaited", getQualname()));
        }
        try {
            close();
        } catch (PyException pye) {
            // PEP 342 specifies that if an exception is raised by close,
            // we output to stderr and then forget about it;
            String className =  PyException.exceptionClassName(pye.type);
            int lastDot = className.lastIndexOf('.');
            if (lastDot != -1) {
                className = className.substring(lastDot + 1);
            }
            String msg = String.format("Exception %s: %s in %s", className, pye.value.__repr__(),
                                       __repr__());
            Py.println(new PyUnicode(msg));
        } catch (Throwable t) {
            // but we currently ignore any Java exception completely. perhaps we
            // can also output something meaningful too?
        }
    }

    @Override
    public PyObject __next__() {
        try {
            return gen_send_ex(Py.getThreadState(), Py.None, false, false);
        } catch (PyException e) {
            if (e.match(Py.StopIteration)) {
                return null;
            }
            throw e;
        }
    }

    @ExposedGet(name = "__name__")
    public final String getName() {
        return gi_code.co_name;
    }

    @ExposedGet(name = "__qualname__")
    public final String getQualname() {
        return gi_code.co_name;
    }

    @ExposedGet(name = "gi_yieldfrom")
    public final PyObject getgi_yieldfrom() {
        return gi_frame.f_yieldfrom;
    }

    private PyObject gen_send_ex(ThreadState state, Object value, boolean exc, boolean closing) {
        if (gi_running) {
            throw Py.ValueError(tp() + " already executing");
        }
        if (gi_frame == null) {
            throw Py.StopIteration();
        }
        if (gi_frame.previousException != null) {
            state.exceptions.offerFirst(gi_frame.previousException);
        }
        if (gi_frame.f_lasti == -1) {
            gi_frame = null;
            throw Py.StopIteration();
        }
        // if value is null, means the input is passed implicitly by frame, don't reset to None
        if (value != null && value != Py.None) {
            gi_frame.setGeneratorInput(value);
        }
        gi_running = true;
        PyObject result = null;
        try {
            gi_frame.f_back = state.frame;
            result = Py.runCode(state, gi_frame.f_code, gi_frame, (PyTuple) closure);
            gi_frame.f_back = null;
            if (exc) {
                assert gi_frame.previousException != null: "exception not provided in exc mode";
                throw gi_frame.previousException;
            }
        } catch (PyException pye) {
            gi_frame = null;
            if (this instanceof PyCoroutine && pye.match(Py.StopIteration)) {
                pye = Py.RuntimeError("coroutine raised StopIteration"); // PEP-479
                pye.normalize();
            }
            throw pye;
        } finally {
            gi_running = false;
        }

        if (result == null && gi_frame.f_yieldfrom != null) {
            gi_frame.f_yieldfrom = null;
            gi_frame.f_lasti++;
            return gen_send_ex(state, value, false, false);
        }

        if (gi_frame.f_lasti == -1) {
            gi_frame = null;
            if (result != Py.None) {
                throw Py.StopIteration(result);
            } else {
                throw Py.StopIteration();
            }
        }
        return result;
    }

    private PyObject gen_close_iter(PyObject iter) {
        if (iter instanceof PyGenerator) {
            return ((PyGenerator) iter).close();
        }
        try {
            PyObject closeMeth = iter.__findattr__("close");
            if (closeMeth != null) {
                return closeMeth.__call__();
            }
        } catch (PyException e) {
            Py.writeUnraisable(e, iter);
        }
        return null;
    }

    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        int retValue = super.traverse(visit, arg);
        if (retValue != 0) {
            return retValue;
        }
        if (gi_frame != null) {
            retValue = visit.visit(gi_frame, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        if (gi_code != null) {
            retValue = visit.visit(gi_code, arg);
            if (retValue != 0) {
                return retValue;
            }
        }
        return closure == null ? 0 : visit.visit(closure, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == gi_frame || ob == gi_code
            || ob == closure || super.refersDirectlyTo(ob));
    }

    private String tp() {
        return this instanceof PyCoroutine ? "coroutine" : "generator";
    }

    public boolean isFlagSet(CodeFlag flag) {
        return ((PyTableCode) gi_code).co_flags.isFlagSet(flag);
    }
}
