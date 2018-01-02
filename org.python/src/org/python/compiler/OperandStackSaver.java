package org.python.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.python.compiler.ClassConstants.*;
import static org.python.compiler.CompilerConstants.*;

/**
 * It tracks the stack and replace placeholder with real instructions
 */
public class OperandStackSaver extends InstructionAdapter {
    private AnalyzerAdapter analyzer;
    private List stack;
    private int tmpIndex;
    private List<Label> yields;

    public OperandStackSaver(MethodVisitor visitor) {
        super(Opcodes.ASM6, visitor);
        this.yields = new ArrayList<>();
    }

    public void setAnalyzer(AnalyzerAdapter analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        if (yields.isEmpty()) {
            mv.visitTableSwitchInsn(min, max, dflt, labels);
            return;
        }
        Label[] y = new Label[yields.size() + 1];
        y[0] = dflt;
        for (int i = 0; i < yields.size(); i++) {
            y[i+1] = yields.get(i);
        }
        tableswitch(0, y.length - 1, dflt, y);
    }



    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == INVOKESTATIC) {
            if (name.equals(SAVE_OPRANDS.symbolName())) {
                 if (analyzer.stack == null || analyzer.stack.isEmpty()) {
                     return;
                 }
                tmpIndex = analyzer.locals.size();
                int stackSize = analyzer.stack.size();
                stack = new ArrayList(analyzer.stack);
                iconst(stackSize);
                newarray(OBJ);
                mv.visitVarInsn(ASTORE, tmpIndex);
                for(int i = stackSize - 1; i >= 0; i--) {
                    mv.visitVarInsn(ALOAD, tmpIndex);
                    mv.visitInsn(SWAP);
                    iconst(i);
                    mv.visitInsn(SWAP);
                    Object value = stack.get(i);
                    if (value instanceof Integer) {
                        invokestatic(INTEGER_TYPE.getInternalName(), "valueOf",
                                Type.getMethodDescriptor(Type.getType(Integer.class),
                                        Type.getType(Integer.TYPE)), false);
                    }
                    mv.visitInsn(AASTORE);
                }
                mv.visitVarInsn(ALOAD, 2); // load frame
                mv.visitVarInsn(ALOAD, tmpIndex);
                mv.visitFieldInsn(PUTFIELD, PYFRAME.getInternalName(), "f_savedStack", OBJARR.getDescriptor());
                return;
            } else if (name.equals(RESTORE_OPRANDS.symbolName())) {
                if (stack == null) {
                    return;
                }
                mv.visitVarInsn(ALOAD, 2); // load frame
                mv.visitFieldInsn(GETFIELD, PYFRAME.getInternalName(), "f_savedStack", OBJARR.getDescriptor());
                mv.visitVarInsn(ASTORE, tmpIndex);
                int stackSize = stack.size();
                for(int i = 0; i < stackSize; i++) {
                    Object value = stack.get(i);
                    if (value instanceof Integer) {
                        iconst((Integer) value);
                    } else {
                        mv.visitVarInsn(ALOAD, tmpIndex);
                        iconst(i);
                        mv.visitInsn(AALOAD);
                        mv.visitTypeInsn(CHECKCAST, String.valueOf(value));
    //                    mv.visitTypeInsn(CHECKCAST, ClassConstants.INTEGER.getInternalName());
    //                    invokevirtual(INTEGER_TYPE.getInternalName(), "intValue", Type.getMethodDescriptor(Type.getType(Integer.TYPE)), false);
                    }
                }
                stack = null;
                return;
            } else if (name.equals(YIELD.symbolName())) {
                mv.visitInsn(ARETURN);
                return;
            } else if (name.equals(MARK.symbolName())) {
                Label label = new Label();
                yields.add(label);
                mv.visitLabel(label);
                return;
            }
        }
        mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}