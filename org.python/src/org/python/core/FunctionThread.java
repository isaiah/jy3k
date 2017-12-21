package org.python.core;

import java.lang.invoke.SwitchPoint;
import java.util.concurrent.atomic.AtomicInteger;

import org.python.modules._systemrestart;

public class FunctionThread extends Thread
{
    private final PyObject func;
    private final PyObject[] args;
    private final PyObject kws;
    private static AtomicInteger counter = new AtomicInteger();

    public FunctionThread(PyObject func, PyObject[] args, PyObject kws, long stack_size, ThreadGroup group) {
        super(group, null, "Thread", stack_size);
        this.func = func;
        this.args = args;
        this.kws = kws;
        this.setName("Thread-"+Integer.toString(counter.incrementAndGet()));
    }

    public void run() {
        // new thread state will be created for the current thread
        final ThreadState tstate = Py.getThreadState();
        String[] keywords = Py.NoKeywords;
        PyObject[] newArgs = args;
        if (kws != null && kws != Py.None) {
            int len = kws.__len__();
            keywords = new String[len];
            newArgs = new PyObject[args.length + len];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            int i = 0;
            for (PyObject key: (kws.asIterable())) {
                keywords[i] = key.asString();
                newArgs[args.length + i++] = kws.__getitem__(key);
            }
        }
        try {
            func.__call__(newArgs, keywords);
        } catch (PyException exc) {
            if (exc.match(Py.SystemExit) || exc.match(_systemrestart.SystemRestart)) {
                return;
            }
            Py.stderr.println("Unhandled exception in thread started by " + func);
            Py.printException(exc);
        } finally {
            if (tstate.onDelete != null) {
                tstate.onDelete.run();
                tstate.onDelete = null;
            }
//            SwitchPoint.invalidateAll(new SwitchPoint[] {tstate.switchPoint});
        }
    }

    @Override
    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return String.format("FunctionThread[%s,%s,%s]", getName(), getPriority(),
                                 group.getName());
        } else {
            return String.format("FunctionThread[%s,%s]", getName(), getPriority());
        }
    }
}
