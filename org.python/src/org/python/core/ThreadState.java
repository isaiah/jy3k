// Copyright (c) Corporation for National Research Initiatives
package org.python.core;

import org.python.internal.runtime.Timing;
import org.python.io.util.FilenoUtil;
import org.python.io.util.SelectorPool;

import java.util.Deque;
import java.util.LinkedList;

public class ThreadState {

    public PySystemState systemState;

    public PyFrame frame;

    public Deque<PyException> exceptions = new LinkedList<>();

    public int call_depth;

    public boolean tracing;

    public PyList reprStack;

    public int compareStateNesting;

    public TraceFunction tracefunc;

    public TraceFunction profilefunc;

    private PyDictionary compareStateDict;

    public Runnable onDelete;

    public Timing _timing;

    public ThreadState(PySystemState systemState) {
        this.systemState = systemState;
        _timing = new Timing();
    }

    /**
     * Get the last exception thrown, used by exc_info fake NameConstant, generated by Lower
     * @return
     */
    public PyException getexc() {
        return peekexc();
    }

    public PyException peekexc() {
        if (frame == null || frame.exceptions == null) {
            return null;
        }
        return frame.exceptions.peek();
    }

    public PyException popexc() {
        return frame.exceptions.pollFirst();
    }

    public void pushexc(PyException exc) {
        if (frame.exceptions == null) {
            if (frame.f_back == null || frame.f_back.exceptions == null) {
                frame.exceptions = new LinkedList<>();
            } else {
                frame.exceptions = new LinkedList<>(frame.f_back.exceptions);
            }
        }
        frame.exceptions.offerFirst(exc);
    }

    public boolean enterRepr(PyObject obj) {
        if (reprStack == null) {
            reprStack = new PyList(new PyObject[] {obj});
            return true;
        }
        for (int i = reprStack.size() - 1; i >= 0; i--) {
            if (obj == reprStack.pyget(i)) {
                return false;
            }
        }
        reprStack.append(obj);
        return true;
    }

    public void exitRepr(PyObject obj) {
        if (reprStack == null) {
            return;
        }
        for (int i = reprStack.size() - 1; i >= 0; i--) {
            if (reprStack.pyget(i) == obj) {
                reprStack.delRange(i, reprStack.size());
            }
        }
    }

    public PyDictionary getCompareStateDict() {
        if (compareStateDict == null) {
            compareStateDict = new PyDictionary();
        }
        return compareStateDict;
    }

    public SelectorPool selectorPool() {
        return systemState.selectorPool();
    }

    public FilenoUtil filenoUtil() {
        return systemState.filenoUtil();
    }
}
