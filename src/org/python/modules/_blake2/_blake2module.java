package org.python.modules._blake2;

import org.python.core.PyObject;
import org.python.expose.ExposedConst;
import org.python.expose.ExposedModule;
import org.python.expose.ModuleInit;
import org.python.internal.blake2.Blake2b;
import org.python.internal.blake2.Blake2s;

@ExposedModule(name = "_blake2")
public class _blake2module implements Blake2b.Spec {

    @ExposedConst
    public static final int BLAKE2B_MAX_DIGEST_SIZE = max_digest_bytes;
    @ExposedConst
    public static final int BLAKE2B_MAX_KEY_SIZE = max_key_bytes;
    @ExposedConst
    public static final int BLAKE2B_MAX_PERSON_SIZE = max_personalization_bytes;
    @ExposedConst
    public static final int BLAKE2B_SALT_SIZE = max_salt_bytes;

    @ExposedConst
    public static final int BLAKE2S_MAX_DIGEST_SIZE = Blake2s.MAX_DIGEST_LENGTH;
    @ExposedConst
    public static final int BLAKE2S_MAX_KEY_SIZE = Blake2s.MAX_KEY_LENGTH;
    @ExposedConst
    public static final int BLAKE2S_MAX_PERSON_SIZE = 8;
    @ExposedConst
    public static final int BLAKE2S_SALT_SIZE = 8;

    @ModuleInit
    public static void init(PyObject dict) {
        dict.__setitem__("blake2b", PyBlake2b.TYPE);
        dict.__setitem__("blake2s", PyBlake2s.TYPE);
    }
}
