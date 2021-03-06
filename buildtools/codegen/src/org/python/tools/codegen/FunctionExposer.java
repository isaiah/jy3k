package org.python.tools.codegen;

import org.objectweb.asm.Type;
import org.python.annotations.SlotFunc;
import org.python.core.PyBuiltinMethod;

public class FunctionExposer extends MethodExposer {
    SlotFunc slot;
    public FunctionExposer(Type onType,
                           int access,
                           String methodName,
                           String desc,
                           String typeName,
                           String[] asNames,
                           String[] defaults,
                           String doc) {
        super(onType,
                methodName,
                Type.getArgumentTypes(desc),
                Type.getReturnType(desc),
                typeName,
                asNames,
                defaults,
                PyBuiltinMethod.class,
                doc,
                true,
                isWide(desc));
        if ((access & ACC_STATIC) == 0) {
            throwInvalid("@ExposedFunction can't be applied to non-static methods");
        }
        if (defaults.length > 0 && isWide(args)) {
            throwInvalid("Can't have defaults on a method that takes PyObject[], String[]");
        }
    }

    public FunctionExposer(Type onType,
                           int access,
                           String methodName,
                           String desc,
                           String typeName,
                           String[] asNames,
                           String[] defaults,
                           String doc,
                           SlotFunc slot) {

        this(onType, access, methodName, desc, typeName, asNames, defaults, doc);
        this.slot = slot;
    }

    @Override
    public String[] getNames() {
        if (slot == null) {
            if(asNames.length == 0) {
                return new String[] {methodName};
            }
            return asNames;

        }
        if (slot.hasName()) {
            return new String[]{slot.getName()};
        }
        return new String[0];
    }

    @Override
    public boolean needsSelf() {
        return slot != null;
    }
}
