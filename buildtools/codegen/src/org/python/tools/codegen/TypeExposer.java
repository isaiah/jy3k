package org.python.tools.codegen;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.python.annotations.ExposedType;
import org.python.core.BytecodeLoader;
import org.python.expose.BaseTypeBuilder;
import org.python.expose.TypeBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Generates a subclass of TypeBuilder to expose a class with the {@link ExposedType} annotation as
 * a builtin Python type.
 */
public class TypeExposer extends Exposer {

    private Type baseType;

    private boolean isBaseType;

    private boolean isModule;

    private String doc;

    private Type onType;

    private String name;

    private Collection<MethodExposer> methods;

    private Collection<DescriptorExposer> descriptors;

    private int numNames;

    private Exposer ne;

    private Collection<FunctionExposer> typeSlots;

    public TypeExposer(Type onType,
                       Type baseType,
                       boolean isBaseType,
                       String doc,
                       String name,
                       Collection<MethodExposer> methods,
                       Collection<DescriptorExposer> descriptors,
                       Collection<FunctionExposer> slots,
                       Exposer ne) {
        super(BaseTypeBuilder.class, makeGeneratedName(onType));
        this.baseType = baseType;
        this.isBaseType = isBaseType;
        this.doc = doc;
        this.onType = onType;
        this.name = name;
        this.methods = methods;
        this.descriptors = descriptors;
        this.typeSlots = slots;
        Set<String> names = new HashSet<>();
        for (DescriptorExposer exposer : descriptors) {
            if (!names.add(exposer.getName())) {
                throwDupe(exposer.getName());
            }
        }
        for (MethodExposer method : methods) {
            String[] methNames = method.getNames();
            for (String methName : methNames) {
                if (!names.add(methName)) {
                    throwDupe(methName);
                }
            }
            numNames += methNames.length;
        }
        this.ne = ne;
    }

    public static String makeGeneratedName(Type onType) {
        return onType.getClassName() + "$PyExposer";
    }

    private void throwDupe(String exposedName) {
        throw new InvalidExposingException("Only one item may be exposed on a type with a given name[name="
                + exposedName + ", class=" + onType.getClassName() + "]");
    }

    public TypeBuilder makeBuilder() {
        BytecodeLoader.Loader l = new BytecodeLoader.Loader();
        if (ne != null) {
            ne.load(l);
        }
        for (DescriptorExposer de : descriptors) {
            de.load(l);
        }
//        for(MethodExposer me : methods) {
//            me.load(l);
//        }
        Class descriptor = load(l);
        try {
            return (TypeBuilder) descriptor.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // If we're unable to create the generated class, the process is
            // definitely ill, but that shouldn't be the case most of the time
            // so make this a runtime exception
            throw new RuntimeException("Unable to create generated builder", e);
        }
    }

    public String getName() {
        return name;
    }

    protected void generate() {
        startConstructor();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(getName());
        mv.visitLdcInsn(onType);
        mv.visitLdcInsn(baseType);
        mv.visitLdcInsn(isBaseType);
        if (doc == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            mv.visitLdcInsn(doc);
        }
        mv.visitLdcInsn(numNames);
        mv.visitTypeInsn(ANEWARRAY, BUILTIN_METHOD.getInternalName());
        mv.visitVarInsn(ASTORE, 1);
        int i = 0;

        for (MethodExposer exposer : methods) {
            if (exposer instanceof ClassMethodExposer) {
                for (final String name : exposer.getNames()) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(i++);
                    mv.visitTypeInsn(NEW, BUILTIN_CLASS_METHOD.getInternalName());
                    mv.visitInsn(DUP);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitTypeInsn(NEW, BUILTIN_METHOD_DATA.getInternalName());
                    mv.visitInsn(DUP);
                    mv.visitLdcInsn(name);
                    exposer.generateNamedConstructor(mv);
                    callConstructor(BUILTIN_METHOD_DATA, STRING, STRING, METHOD_HANDLE, STRING, BOOLEAN, BOOLEAN);
                    callConstructor(BUILTIN_CLASS_METHOD, PYOBJ, BUILTIN_METHOD_DATA);
                    mv.visitInsn(AASTORE);
                }
            } else {
                for (final String name : exposer.getNames()) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitLdcInsn(i++);
                    mv.visitTypeInsn(NEW, BUILTIN_METHOD.getInternalName());
                    mv.visitInsn(DUP);
                    mv.visitLdcInsn(name);
                    exposer.generateNamedConstructor(mv);
                    callConstructor(BUILTIN_METHOD, STRING, STRING, METHOD_HANDLE, STRING, BOOLEAN, BOOLEAN);
                    mv.visitInsn(AASTORE);
                }
            }
        }

        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn(descriptors.size());
        mv.visitTypeInsn(ANEWARRAY, DATA_DESCR.getInternalName());
        mv.visitVarInsn(ASTORE, 2);
        i = 0;
        for (DescriptorExposer desc : descriptors) {
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(i++);
            instantiate(desc.getGeneratedType());
            mv.visitInsn(AASTORE);
        }
        mv.visitVarInsn(ALOAD, 2);
        if (ne != null) {
            instantiate(ne.getGeneratedType());
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitLdcInsn(typeSlots.size());
        mv.visitTypeInsn(ANEWARRAY, TYPE_SLOT.getInternalName());
        mv.visitVarInsn(ASTORE, 3);
        i = 0;
        for (FunctionExposer exposer : typeSlots) {
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn(i++);
            mv.visitTypeInsn(NEW, TYPE_SLOT.getInternalName());
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, SLOT_FUNC.getInternalName(), exposer.slot.name(), SLOT_FUNC.getDescriptor());
            String desc = Type.getMethodDescriptor(exposer.returnType, exposer.args);
            mv.visitLdcInsn(new Handle(H_INVOKESTATIC, onType.getInternalName(), exposer.methodName, desc, false));
            callConstructor(TYPE_SLOT, SLOT_FUNC, METHOD_HANDLE);
            mv.visitInsn(AASTORE);
        }
        mv.visitVarInsn(ALOAD, 3);
        superConstructor(STRING, CLASS, CLASS, BOOLEAN, STRING, ABUILTIN_METHOD, ADATA_DESCR,
                PYNEWWRAPPER, ATYPE_SLOT);
        endConstructor();
    }
}
