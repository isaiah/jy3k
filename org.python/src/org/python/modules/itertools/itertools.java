/* Copyright (c) Jython Developers */
package org.python.modules.itertools;

import org.python.core.BuiltinDocs;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyIterator;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyTuple;
import org.python.core.Visitproc;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;

/**
 * Functional tools for creating and using iterators. Java implementation of the CPython module
 * itertools.
 * 
 * @since 2.5
 */
@ExposedModule(doc = BuiltinDocs.itertools_doc)
public class itertools {
    /**
     * Iterator base class used by most methods.
     */
    static abstract class ItertoolsIterator extends PyIterator {

        /**
         * Returns the next element from an iterator. If it raises/throws StopIteration just store
         * the Exception and return null according to PyIterator practice.
         */
        protected PyObject nextElement(PyObject pyIter) {
            return pyIter.__next__();
        }
    }

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("accumulate", accumulate.TYPE);
        dict.__setitem__("chain", chain.TYPE);
        dict.__setitem__("combinations", combinations.TYPE);
        dict.__setitem__("combinations_with_replacement", combinationsWithReplacement.TYPE);
        dict.__setitem__("compress", compress.TYPE);
        dict.__setitem__("cycle", cycle.TYPE);
        dict.__setitem__("count", count.TYPE);
        dict.__setitem__("dropwhile", dropwhile.TYPE);
        dict.__setitem__("groupby", groupby.TYPE);
        dict.__setitem__("filterfalse", filterfalse.TYPE);
        dict.__setitem__("islice", islice.TYPE);
        dict.__setitem__("zip_longest", zip_longest.TYPE);
        dict.__setitem__("permutations", permutations.TYPE);
        dict.__setitem__("product", product.TYPE);
        dict.__setitem__("repeat", repeat.TYPE);
        dict.__setitem__("starmap", starmap.TYPE);
        dict.__setitem__("takewhile", takewhile.TYPE);
    }

    static int py2int(PyObject obj, int defaultValue, String msg) {
        if (obj instanceof PyNone) {
            return defaultValue;
        } else {
            int value = defaultValue;
            try {
                value = Py.py2int(obj);
            }
            catch (PyException pyEx) {
                if (pyEx.match(Py.TypeError)) {
                    throw Py.ValueError(msg);
                } else {
                    throw pyEx;
                }
            }
            return value;
        }
    }

    /**
     * Iterator base class for iterators returned by <code>ifilter</code> and
     * <code>filterfalse</code>.
     */
    static class FilterIterator extends ItertoolsIterator {
        private PyObject predicate;

        private PyObject iterator;

        private boolean filterTrue;

        FilterIterator(PyObject predicate, PyObject iterable, boolean filterTrue) {
            if (predicate instanceof PyNone) {
                this.predicate = null;
            } else {
                this.predicate = predicate;
            }
            this.iterator = getIter(iterable);
            this.filterTrue = filterTrue;
        }

        public PyObject __next__() {

            while (true) {
                PyObject element = nextElement(iterator);
                if (element != null) {
                    // the boolean value of calling predicate with the element
                    // or if predicate is null/None of the element itself
                    boolean booleanValue = predicate != null ? predicate
                            .__call__(element).isTrue() : element
                            .isTrue();
                    if (booleanValue == filterTrue) {
                        // if the boolean value is the same as filterTrue return
                        // the element
                        // for ifilter filterTrue is always true, for
                        // filterfalse always false
                        return element;
                    }
                } else {
                    return null;
                }
            }
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            int retVal = super.traverse(visit, arg);
            if (retVal != 0) {
                return retVal;
            }
            if (iterator != null) {
                retVal = visit.visit(iterator, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
            return predicate != null ? visit.visit(predicate, arg) : 0;
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            return ob != null && (ob == iterator || ob == predicate ||
                    super.refersDirectlyTo(ob));
        }
    }

    /**
     * Iterator base class used by <code>dropwhile()</code> and <code>takewhile</code>.
     */
    static class WhileIterator extends ItertoolsIterator {
        private PyObject iterator;

        private PyObject predicate;

        // flag that indicates if the iterator shoul drop or return arguments "while" the predicate is true
        private boolean drop;

        // flag that is set once the predicate is satisfied
        private boolean predicateSatisfied;

        WhileIterator(PyObject predicate, PyObject iterable, boolean drop) {
            this.predicate = predicate;
            iterator = getIter(iterable);
            this.drop = drop;
        }

        public PyObject __next__() {

            while (true) {
                PyObject element = nextElement(iterator);
                if (element != null) {
                    if (!predicateSatisfied) {
                        // the predicate is not satisfied yet (or still satisfied in the case of drop beeing 
                        // false), so we need to check it
                        if (predicate.__call__(element).isTrue() != drop) {
                            predicateSatisfied = drop;
                            return element;
                        }
                        predicateSatisfied = !drop;
                    } else {
                        if (drop) {
                            return element;
                        } else {
                            // end iteration if predicate is false and drop is false
                            return null;
                        }
                    }
                } else {
                    // end iteration
                    return null;
                }

            }
        }


        /* Traverseproc implementation */
        @Override
        public int traverse(Visitproc visit, Object arg) {
            int retVal = super.traverse(visit, arg);
            if (retVal != 0) {
                return retVal;
            }
            if (iterator != null) {
                retVal = visit.visit(iterator, arg);
                if (retVal != 0) {
                    return retVal;
                }
            }
            return predicate != null ? visit.visit(predicate, arg) : 0;
        }

        @Override
        public boolean refersDirectlyTo(PyObject ob) {
            return ob != null && (ob == iterator || ob == predicate ||
                    super.refersDirectlyTo(ob));
        }
    }

    /**
     * Create a tuple of iterators, each of which is effectively a copy of iterable.
     */
    @ExposedFunction(defaults = {"2"}, doc = BuiltinDocs.itertools_tee_doc)
    public static PyTuple tee(PyObject iterable, final int n) {
        return new PyTuple(PyTeeIterator.makeTees(iterable, n));
    }

    static PyTuple makeIndexedTuple(PyTuple pool, int indices[]) {
        return makeIndexedTuple(pool, indices, indices.length);
    }
    
    static PyTuple makeIndexedTuple(PyTuple pool, int indices[], int end) {
        PyObject items[] = new PyObject[end];
        for (int i = 0; i < end; i++) {
            items[i] = pool.__getitem__(indices[i]);
        }
        return new PyTuple(items);
    }
}
