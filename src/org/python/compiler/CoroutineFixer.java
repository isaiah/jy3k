package org.python.compiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import static org.objectweb.asm.Opcodes.ASM5;

/**
 * Insert saveStack and restoreStack instructions when there is a yield expr
 *
 * The reason why this is done in the bytecode visitor is that we got the frame information, otherwise we have
 * to keep track of the operand stack and local variables table
 */
public class CoroutineFixer extends ClassVisitor {
    private String owner;

    public static byte[] transform(byte[] b) {
        final ClassReader classReader = new ClassReader(b);
        final ClassWriter cw = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new CoroutineFixer(cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private CoroutineFixer(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        owner = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        OperandStackSaver instrumentor =  new OperandStackSaver(mv);
        AnalyzerAdapter analyzerAdapter = new AnalyzerAdapter(owner, access, name, desc, instrumentor);
        instrumentor.setAnalyzer(analyzerAdapter);
        return analyzerAdapter;
    }
}
