package org.python.tools.codegen;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class MethodExposer extends Exposer {

    protected String[] defaults;

    protected final String[] asNames;

    protected final String prefix, typeName;

    protected final Type[] args;

    protected final String methodName;

    protected final Type onType, returnType;

    protected final String doc;

    // whether it's a function or method
    protected boolean isStatic;
    protected boolean isWide;
    public MethodExposer(Type onType,
                         String methodName,
                         Type[] args,
                         Type returnType,
                         String typeName,
                         String[] asNames,
                         String[] defaults,
                         Class superClass,
                         String doc,
                         boolean isStatic,
                         boolean isWide
    ) {
        this(onType, methodName, args, returnType, typeName, asNames, defaults, superClass, doc, isWide);
        this.isStatic = isStatic;
    }

    public MethodExposer(Type onType,
                         String methodName,
                         Type[] args,
                         Type returnType,
                         String typeName,
                         String[] asNames,
                         String[] defaults,
                         Class superClass,
                         String doc,
                         boolean isWide) {
        super(superClass, onType.getClassName() + "$" + methodName + "_exposer");
        this.onType = onType;
        this.methodName = methodName;
        this.args = args;
        this.typeName = typeName;
        this.doc = doc;
        String prefix = typeName;
        if (prefix == null) {
            System.out.println(methodName);
        }
        int lastDot = prefix.lastIndexOf('.');
        if (lastDot != -1) {
            prefix = prefix.substring(lastDot + 1);
        }
        this.prefix = prefix;
        this.asNames = asNames;
        this.returnType = returnType;
        this.defaults = defaults;
        this.isStatic = false;
        this.isWide = isWide;
        for(String name : getNames()) {
            if(name.equals("__new__")) {
                throwInvalid("@ExposedNew must be used to create __new__, not @ExposedMethod");
            }
        }
    }

    protected void throwInvalid(String msg) {
        throw new InvalidExposingException(msg + "[method=" + onType.getClassName() + "."
                + methodName + "]");
    }

    /**
     * @return the names this method will be exposed as. Must be at least length 1.
     */
    public String[] getNames() {
        if(asNames.length == 0) {
            String name = methodName;
            if(name.startsWith(prefix + "_")) {
                name = methodName.substring((prefix + "_").length());
            }
            return new String[] {name};
        }
        return asNames;
    }

    public boolean needsSelf() {
        return !isStatic;
    }

    public void generateNamedConstructor(MethodVisitor mv) {
        // defaultVals
        mv.visitLdcInsn(String.join(",", defaults));
        // target
        int tag = isStatic ? H_INVOKESTATIC : H_INVOKEVIRTUAL;
        String desc = Type.getMethodDescriptor(returnType, args);
        if (this instanceof ClassMethodExposer) {
            desc = Type.getMethodDescriptor(returnType, ((ClassMethodExposer) this).actualArgs);
        }
        mv.visitLdcInsn(new Handle(tag, onType.getInternalName(), methodName, desc, false));
        // doc
        mv.visitLdcInsn(doc);
        // static
        mv.visitLdcInsn(isStatic);
        // wide
        mv.visitLdcInsn(isWide(args));
        // self
        mv.visitLdcInsn(needsSelf());
    }

    protected static boolean needsThreadState(Type[] args) {
        return args.length > 0 && args[0].equals(THREAD_STATE);
    }

    protected static boolean isWide(Type[] args) {
        int offset = needsThreadState(args) ? 1 : 0;
        return args.length == 2 + offset
                && args[offset].equals(APYOBJ) && args[offset + 1].equals(ASTRING);
    }

    protected static boolean isWide(String methDescriptor) {
        return isWide(Type.getArgumentTypes(methDescriptor));
    }
}
