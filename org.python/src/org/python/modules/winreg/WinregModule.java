package org.python.modules.winreg;

import org.python.annotations.ExposedConst;
import org.python.annotations.ExposedFunction;
import org.python.annotations.ExposedModule;
import org.python.core.Py;
import org.python.core.PyLong;
import org.python.core.PyObject;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

@ExposedModule
public class WinregModule {

    @ExposedConst
    public static final int HKEY_CURRENT_USER = 0x80000001;

    @ExposedConst
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;

    @ExposedConst
    public static final int HKEY_USERS = 0x80000003;

    @ExposedConst
    public static final int HKEY_PERFORMANCE_DATA = 0x80000004;

    @ExposedConst
    public static final int HKEY_CURRENT_CONFIG = 0x80000005;

    @ExposedConst
    public static final int HKEY_DYN_DATA = 0x80000006;

    public static final int REG_SUCCESS = 0;
    public static final int REG_NOTFOUND = 2;
    public static final int REG_ACCESSDENIED = 5;

    @ExposedConst
    public static final int KEY_ALL_ACCESS = 0xf003f;
    @ExposedConst
    public static final int KEY_READ = 0x20019;
    @ExposedConst
    public static final int KEY_WRITE = 0x20006;
    @ExposedConst
    public static final int KEY_QUERY_VALUE = 0x1;
    @ExposedConst
    public static final int KEY_SET_VALUE = 0x2;
    @ExposedConst
    public static final int KEY_CREATE_SUB_KEY = 0x4;
    @ExposedConst
    public static final int KEY_NOTIFY = 0x10;
    @ExposedConst
    public static final int KEY_CREATE_LINK = 0x20;

    private static final Preferences userRoot = Preferences.userRoot();
    private static final Preferences systemRoot = Preferences.systemRoot();

    private static final Class<? extends Preferences> userClass = userRoot.getClass();
    private static final Method regOpenKey;
    private static final Method regCloseKey;
    private static final Method regQueryValueEx;
    private static final Method regEnumValue;
    private static final Method regQueryInfoKey;
    private static final Method regEnumKeyEx;
    private static final Method regCreateKeyEx;
    private static final Method regSetValueEx;
    private static final Method regDeleteKey;
    private static final Method regDeleteValue;
    private static final Constructor<? extends Preferences> regCtor;
    private static final Constructor<? extends Preferences> regSubCtor;

    static {
        try {
            regCtor = userClass.getDeclaredConstructor(int.class, byte[].class);
            regCtor.setAccessible(true);
            regSubCtor = userClass.getDeclaredConstructor(userClass, String.class);
            regSubCtor.setAccessible(true);
            regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", int.class, byte[].class, int.class);
            regOpenKey.setAccessible(true);
            regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", int.class);
            regCloseKey.setAccessible(true);
            regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class );
            regQueryValueEx.setAccessible(true);
            regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", int.class, int.class, int.class);
            regEnumValue.setAccessible(true);
            regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", int.class);
            regQueryInfoKey.setAccessible(true);
            regEnumKeyEx = userClass.getDeclaredMethod(
                    "WindowsRegEnumKeyEx", int.class, int.class, int.class);
            regEnumKeyEx.setAccessible(true);
            regCreateKeyEx = userClass.getDeclaredMethod(
                    "WindowsRegCreateKeyEx", int.class, byte[].class);
            regCreateKeyEx.setAccessible(true);
            regSetValueEx = userClass.getDeclaredMethod(
                    "WindowsRegSetValueEx", int.class, byte[].class, byte[].class);
            regSetValueEx.setAccessible(true);
            regDeleteValue = userClass.getDeclaredMethod(
                    "WindowsRegDeleteValue",  int.class, byte[].class);
            regDeleteValue.setAccessible(true);
            regDeleteKey = userClass.getDeclaredMethod(
                    "WindowsRegDeleteKey", int.class, byte[].class);
            regDeleteKey.setAccessible(true);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getByteArray(PyObject subKey) {
        byte[] bytes;
        if (subKey == Py.None) {
            bytes = new byte[0];
        } else {
            byte[] keyBytes = subKey.asString().getBytes();
            bytes = new byte[keyBytes.length + 1];
            System.arraycopy(keyBytes, 0, bytes, 0, keyBytes.length);
            bytes[keyBytes.length] = 0;
        }
        return bytes;
    }

    @ExposedFunction
    public static PyObject CreateKey(PyObject key, PyObject subKey) {
        Preferences prefs;
        byte[] bytes = getByteArray(subKey);
        try {
            if (key instanceof PyLong) {
                prefs = regCtor.newInstance(key.asInt(), bytes);
                return new PyHKEY(key.asInt(), prefs, null);
            } else if (key instanceof PyHKEY) {
                int[] handles = (int[]) regCreateKeyEx.invoke(((PyHKEY) key).getPrefs(), bytes);
                return new PyHKEY(handles[0], null, (PyHKEY) key);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw Py.JavaError(e);
        }
        throw Py.TypeError("Require PyHKEY or int");
    }

    @ExposedFunction
    public static PyObject DeleteKey(PyObject key, PyObject subKey) {
        int handle;
        if (key instanceof PyLong) {
            handle = key.asInt();
        } else if (key instanceof PyHKEY) {
            handle = ((PyHKEY) key).getHandle();
        } else {
            throw Py.TypeError("Requires int or PyHKEY");
        }
        try {
            regDeleteKey.invoke(handle, getByteArray(subKey));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw Py.OSError(e.getMessage());
        }
        return Py.None;
    }

    public static void closeKey(Preferences root, PyHKEY key) {
        try {
            regCloseKey.invoke(root, key.getHandle());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw Py.JavaError(e);
        }
    }

    public static String readString(int hkey, String key, String valueName)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readString(systemRoot, hkey, key, valueName);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            return readString(userRoot, hkey, key, valueName);
        }
        else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Read value(s) and value name(s) form given key
     * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @return the value name(s) plus the value(s)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Map<String, String> readStringValues(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readStringValues(systemRoot, hkey, key);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            return readStringValues(userRoot, hkey, key);
        }
        else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Read the value name(s) from a given key
     * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @return the value name(s)
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static List<String> readStringSubKeys(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        if (hkey == HKEY_LOCAL_MACHINE) {
            return readStringSubKeys(systemRoot, hkey, key);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            return readStringSubKeys(userRoot, hkey, key);
        }
        else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Create a key
     * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
     * @param key
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void createKey(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int [] ret;
        if (hkey == HKEY_LOCAL_MACHINE) {
            ret = createKey(systemRoot, hkey, key);
            regCloseKey.invoke(systemRoot, ret[0]);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            ret = createKey(userRoot, hkey, key);
            regCloseKey.invoke(userRoot, ret[0]);
        }
        else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
        if (ret[1] != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
        }
    }

    /**
     * Write a value in a given key/value name
     * @param hkey
     * @param key
     * @param valueName
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void writeStringValue
    (int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        if (hkey == HKEY_LOCAL_MACHINE) {
            writeStringValue(systemRoot, hkey, key, valueName, value);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            writeStringValue(userRoot, hkey, key, valueName, value);
        }
        else {
            throw new IllegalArgumentException("hkey=" + hkey);
        }
    }

    /**
     * Delete a given key
     * @param hkey
     * @param key
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void deleteKey(int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteKey(systemRoot, hkey, key);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteKey(userRoot, hkey, key);
        }
        if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
        }
    }

    /**
     * delete a value from a given key/value name
     * @param hkey
     * @param key
     * @param value
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void deleteValue(int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int rc = -1;
        if (hkey == HKEY_LOCAL_MACHINE) {
            rc = deleteValue(systemRoot, hkey, key, value);
        }
        else if (hkey == HKEY_CURRENT_USER) {
            rc = deleteValue(userRoot, hkey, key, value);
        }
        if (rc != REG_SUCCESS) {
            throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
        }
    }

    // =====================

    private static int deleteValue
            (Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_ALL_ACCESS);
        if (handles[1] != REG_SUCCESS) {
            return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
        }
        int rc =(int) regDeleteValue.invoke(root, handles[0], toCstr(value));
        regCloseKey.invoke(root, handles[0]);
        return rc;
    }

    private static int deleteKey(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        return (int) regDeleteKey.invoke(root, hkey, toCstr(key));  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
    }

    private static String readString(Preferences root, int hkey, String key, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        byte[] valb = (byte[]) regQueryValueEx.invoke(root, handles[0], toCstr(value));
        regCloseKey.invoke(root, handles[0]);
        return (valb != null ? new String(valb).trim() : null);
    }

    private static Map<String,String> readStringValues
            (Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        HashMap<String, String> results = new HashMap<String,String>();
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        int[] info = (int[]) regQueryInfoKey.invoke(root, handles[0]);

        int count = info[0]; // count
        int maxlen = info[3]; // value length max
        for(int index=0; index<count; index++)  {
            byte[] name = (byte[]) regEnumValue.invoke(root,
                    handles[0], index, maxlen + 1);
            String value = readString(hkey, key, new String(name));
            results.put(new String(name).trim(), value);
        }
        regCloseKey.invoke(root, handles[0]);
        return results;
    }

    private static List<String> readStringSubKeys
            (Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        List<String> results = new ArrayList<String>();
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
        if (handles[1] != REG_SUCCESS) {
            return null;
        }
        int[] info = (int[]) regQueryInfoKey.invoke(root,
                new Object[] { handles[0] });

        int count  = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
        int maxlen = info[3]; // value length max
        for(int index=0; index<count; index++)  {
            byte[] name = (byte[]) regEnumKeyEx.invoke(root, handles[0], index, maxlen + 1);
            results.add(new String(name).trim());
        }
        regCloseKey.invoke(root, handles[0]);
        return results;
    }

    private static int [] createKey(Preferences root, int hkey, String key)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        return  (int[]) regCreateKeyEx.invoke(root,
                new Object[] { hkey, toCstr(key) });
    }

    private static void writeStringValue
            (Preferences root, int hkey, String key, String valueName, String value)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_ALL_ACCESS);

        regSetValueEx.invoke(root, handles[0], toCstr(valueName), toCstr(value));
        regCloseKey.invoke(root, handles[0]);
    }

    // utility
    private static byte[] toCstr(String str) {
        byte[] result = new byte[str.length() + 1];

        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }
}