// Copyright (c) Corporation for National Research Initiatives
// Copyright 2000 Samuele Pedroni

package org.python.core;

import org.python.annotations.ExposedType;
import org.python.core.packagecache.PackageManager;
import org.python.importlib.JavaImporter;

import java.util.StringTokenizer;

/**
 * A representation of java package.
 */
@ExposedType(name = "java_package")
public class PyJavaPackage extends PyObject implements Traverseproc {
    private static final PyObject JAVAPKG_PATH = new PyList(new PyObject[] {new PyUnicode(JavaImporter.JAVA_IMPORT_PATH_ENTRY)});
    public static final PyType TYPE = PyType.fromClass(PyJavaPackage.class);
    public String __name__;

    public PyStringMap __dict__;

    /** Its keys are the names of statically known classes.
     * E.g. from jars pre-scan.
     */
    public PyStringMap clsSet;
    public String __file__;

    /** (Control) package manager whose hierarchy contains this java pkg.
     */
    public PackageManager __mgr__;

    public ClassLoader classLoader;

    public PyJavaPackage(String name) {
        this(name, null, null);
    }

    public PyJavaPackage(String name,String jarfile) {
        this(name, null, jarfile);
    }

    public PyJavaPackage(String name,PackageManager mgr) {
        this(name, mgr, null);
    }

    public PyJavaPackage(String name,PackageManager mgr,String jarfile) {
        __file__ = jarfile;
        __name__ = name;

        if( mgr == null )
           __mgr__ = PySystemState.packageManager; // default
        else
           __mgr__ = mgr;

        clsSet= new PyStringMap();

        __dict__ = new PyStringMap();
        __dict__.__setitem__("__name__", new PyUnicode(__name__));
    }

    public PyJavaPackage addPackage(String name) {
        return addPackage(name, null, null);
    }

    public PyJavaPackage addPackage(String name, String jarfile, ClassLoader cl) {
        int dot = name.indexOf('.');
        String firstName=name;
        String lastName=null;
        if (dot != -1) {
            firstName = name.substring(0,dot);
            lastName = name.substring(dot+1, name.length());
        }
        firstName = firstName.intern();
        PyJavaPackage p = (PyJavaPackage)__dict__.__finditem__(firstName);
        if (p == null) {
            String pname = __name__.length() == 0 ?
                           firstName : __name__+'.'+firstName;
            p = new PyJavaPackage(pname, __mgr__, jarfile);
            __dict__.__setitem__(firstName, p);
            p.classLoader = cl;
        } else {
            // this code is ok here, because this is not needed for
            // a top level package
            if (jarfile == null || !jarfile.equals(p.__file__))
                p.__file__ = null;
        }
        if (lastName != null) return p.addPackage(lastName, jarfile, cl);
        else return p;
    }

    public PyObject addClass(String name, Class<?> c) {
        PyObject ret = Py.java2py(c);
        __dict__.__setitem__(name.intern(), ret);
        return ret;
    }

    /**
     * Add statically known classes.
     *
     * @param classes
     *            their names as comma-separated string
     */
    public void addPlaceholders(String classes) {
        StringTokenizer tok = new StringTokenizer(classes, ",@");
        while  (tok.hasMoreTokens())  {
            String p = tok.nextToken();
            String name = p.trim().intern();
            if (clsSet.__finditem__(name) == null)
                clsSet.__setitem__(name, Py.One);
        }
    }

    public PyObject __dir__() {
        return __mgr__.doDir(this,false,false);
    }

    /**
     * Used for 'from xyz import *', dynamically dir pkg filling up __dict__. It uses
     * {@link PackageManager#doDir} implementation furnished by the control package manager with
     * instantiate true. The package manager should load classes with {@link #addClass} in the
     * package.
     *
     * @return list of member names
     */
    public PyObject fillDir() {
        return __mgr__.doDir(this,true,false);
    }


    public PyObject __findattr_ex__(String name) {

        PyObject ret = __dict__.__finditem__(name);
        if (ret != null) return ret;

        if (__mgr__.packageExists(__name__,name)) {
            __mgr__.notifyPackageImport(__name__,name);
            return addPackage(name);
        }

        Class<?> c = __mgr__.findClass(__name__,name, "java class", classLoader);
        if (c != null) return addClass(name,c);

        if (name == "__name__") return new PyUnicode(__name__);
        if (name == "__dict__") return __dict__;
        if (name == "__mgr__") return Py.java2py(__mgr__);
        if (name == "__file__") {
            if (__file__ != null) return new PyUnicode(__file__);

//            return Py.None;
            throw Py.AttributeError("__file__");
        }

        if (name.equals("__path__")) {
            // java modules are always package, the path is ignored by the JavaImporter
            return JAVAPKG_PATH;
        }

        return null;
    }

    public void __setattr__(String attr, PyObject value) {
        if (attr == "__mgr__") {
            PackageManager newMgr = Py.tojava(value,
                                                       PackageManager.class);
            if (newMgr == null) {
                throw Py.TypeError("cannot set java package __mgr__ to None");
            }
            __mgr__ = newMgr;
            return;
        }
        if (attr == "__file__") {
            __file__ = value.__str__().toString();
            return;
        }

        super.__setattr__(attr,value);
    }

    public String toString()  {
        return "<java package "+__name__+" "+Py.idstr(this)+">";
    }


    /* Traverseproc implementation */
    @Override
    public int traverse(Visitproc visit, Object arg) {
        //__dict__ cannot be null
        int retVal = visit.visit(__dict__, arg);
        if (retVal != 0) {
            return retVal;
        }

        //clsSet cannot be null
        retVal = visit.visit(clsSet, arg);
        if (retVal != 0) {
            return retVal;
        }
        //__mgr__ and __mgr__.topLevelPackage cannot be null
        return visit.visit(__mgr__.topLevelPackage, arg);
    }

    @Override
    public boolean refersDirectlyTo(PyObject ob) {
        return ob != null && (ob == __dict__ || ob == clsSet || ob == __mgr__.topLevelPackage);
    }

    @Override
    public PyObject fastGetDict() {
        return __dict__;
    }
}
