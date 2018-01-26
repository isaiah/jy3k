package org.python.tools.codegen;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.python.annotations.ExposedModule;
import org.python.core.PyModule;
import org.python.core.PyType;
import org.python.expose.BaseModuleBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Expose a class with the {@link ExposedModule} annotation as a builtin Python module.
 */
public class ModuleExposer extends Exposer {
    private String doc;

    private Type onType;

    private String name;

    private String init; // initializer method name for the module, annotated by @ModuleInit

    private Collection<MethodExposer> methods;

    private Map<String, FieldNode> constants;

    private int numNames;

    public ModuleExposer(Type onType,
                         String doc,
                         String name,
                         String init,
                         Collection<MethodExposer> methods,
                         Map<String, FieldNode> constants) {
        super(BaseModuleBuilder.class, makeGeneratedName(onType));
        this.doc = doc;
        this.onType = onType;
        this.name = name;
        this.init = init;
        this.methods = methods;
        Set<String> names = new HashSet<>();

        for(MethodExposer method : methods) {
            String[] methNames = method.getNames();
            for(String methName : methNames) {
                if(!names.add(methName)) {
                    throwDupe(methName);
                }
            }
            numNames += methNames.length;
        }
        this.constants = constants;
    }

    @Override
    protected void generate() {
        startStaticMethod("clinic", PYOBJ, PYMODULE);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, PYMODULE.getInternalName(), "__dict__", PYOBJ.getDescriptor());
        mv.visitInsn(DUP);
        mv.visitLdcInsn("__name__");
        mv.visitLdcInsn(name);
        callStatic(PY, "newUnicode", PYSTR, STRING);
        call(PYOBJ, "__setitem__", VOID, STRING, PYOBJ);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ASTORE, 1);
        for(MethodExposer exposer : methods) {
            for(final String methodName : exposer.getNames()) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(methodName);
                mv.visitTypeInsn(NEW, BUILTIN_METHOD.getInternalName());
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(NEW, BUILTIN_METHOD_DATA.getInternalName());
                mv.visitInsn(DUP);
                mv.visitLdcInsn(methodName);
                exposer.generateNamedConstructor(mv);
                mv.visitLdcInsn(name);
                callConstructor(BUILTIN_METHOD_DATA, STRING, STRING, METHOD_HANDLE, STRING, BOOLEAN, BOOLEAN, BOOLEAN, STRING);
                callConstructor(BUILTIN_METHOD, PYOBJ, BUILTIN_METHOD_DATA);
                call(PYOBJ, "__setitem__", VOID, STRING, PYOBJ);
            }
        }
        for (String key: constants.keySet()) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(key);
            mv.visitLdcInsn(constants.get(key).value);
            toPy(Type.getType(constants.get(key).desc));
            call(PYOBJ, "__setitem__", VOID, STRING, PYOBJ);
        }
        // customer initializer
        if (init != null) {
            mv.visitInsn(DUP);
            callStatic(onType, init, VOID, PYOBJ);
        }
        endMethod(ARETURN);
    }

    private void throwDupe(String exposedName) {
        throw new InvalidExposingException("Only one item may be exposed on a type with a given name[name="
                + exposedName + ", class=" + onType.getClassName() + "]");
    }

    public static String makeGeneratedName(Type onType) {
        return onType.getClassName() + "$PyExposer";
    }

    public String getName() {
        return name;
    }
}
