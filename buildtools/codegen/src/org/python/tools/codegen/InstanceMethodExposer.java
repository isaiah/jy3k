package org.python.tools.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.python.core.PyBuiltinMethod;
import org.python.annotations.ExposedMethod;
import org.python.expose.MethodType;

/**
 * Generates a class to call a given method with the {@link ExposedMethod} annotation as a method on
 * a builtin Python type.
 */
public class InstanceMethodExposer extends MethodExposer {

    MethodType type;

    public InstanceMethodExposer(Type onType,
                                 int access,
                                 String methodName,
                                 String desc,
                                 String typeName) {
        this(onType,
             access,
             methodName,
             desc,
             typeName,
             new String[0],
             new String[0],
             MethodType.DEFAULT,
             "");
    }

    public InstanceMethodExposer(Type onType,
                                 int access,
                                 String methodName,
                                 String desc,
                                 String typeName,
                                 String[] asNames,
                                 String[] defaults,
                                 MethodType type,
                                 String doc) {
        super(onType,
              methodName,
              Type.getArgumentTypes(desc),
              Type.getReturnType(desc),
              typeName,
              asNames,
              defaults,
              PyBuiltinMethod.class,
              doc, isWide(desc));
        if ((access & ACC_STATIC) != 0) {
            throwInvalid("@ExposedMethod can't be applied to static methods");
        }
        if (isWide(args) && defaults.length > 0) {
            throwInvalid("Can't have defaults on a method that takes PyObject[], String[]");
        }
        this.type = type;
    }
}
