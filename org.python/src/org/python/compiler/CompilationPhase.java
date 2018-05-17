package org.python.compiler;

import org.python.antlr.base.mod;
import org.python.core.CompilerFlags;
import org.python.core.Py;

public abstract class CompilationPhase {
    private long startTime, endTime;

    final mod begin(mod node) {
        startTime = System.nanoTime();
        return node;
    }

    final mod end(mod node) {
        endTime = System.nanoTime();
        Py.getThreadState()._timing.accumulateTime(toString(), endTime - startTime);
        return node;
    }

    final mod apply(Module compiler, mod node, String filename) {
        return end(transform(compiler, begin(node), filename));
    }

    abstract mod transform(Module compiler, mod node, String filename);

    private static final class NameManglePhase extends CompilationPhase {

        @Override
        mod transform(Module compiler, mod node, String filename) {
            new NameMangler().visit(node);
            return node;
        }

        @Override
        public String toString() {
            return "Mangle Name";
        }
    }

    public static final CompilationPhase NAME_MANGLING_PHASE = new NameManglePhase();

    private static final class ClassClosureMarkPhase extends CompilationPhase {

        @Override
        mod transform(Module compiler, mod node, String filename) {
            new ClassClosureGenerator().visit(node);
            return node;
        }

        @Override
        public String toString() {
            return "Mark Class Closure";
        }
    }

    public static final CompilationPhase CLASS_CLOSURE_MARK_PHASE = new ClassClosureMarkPhase();

    private static final class LoweringPhase extends CompilationPhase {

        @Override
        mod transform(Module compiler, mod node, String filename) {
            new Lower(filename).visit(node);
            return node;
        }

        public String toString() {
            return "Lower Control Flow";
        }
    }

    public static final CompilationPhase LOWERING_PHASE = new LoweringPhase();

    private static final class AnnotationPhase extends CompilationPhase {
        @Override
        mod transform(Module compiler, mod node, String filename) {
            new AnnotationsCreator().visit(node);
            return node;
        }

        @Override
        public String toString() {
            return "Create Annotation";
        }
    }

    public static final CompilationPhase ANNOTATION_PHASE = new AnnotationPhase();

    private static final class SymbolAssignmentPhase extends CompilationPhase {
        @Override
        mod transform(Module compiler, mod node, String filename) {
            compiler.st = Symtable.buildObject(node, filename, 0);
            return node;
        }

        @Override
        public String toString() {
            return "Assign Symbols";
        }
    }

    public static final CompilationPhase SYMBOL_ASSIGNMENT_PHASE = new SymbolAssignmentPhase();

    private static final class BytecodeGenerationPhase extends CompilationPhase {

        @Override
        mod transform(Module module, mod node, String filename) {
            CodeCompiler compiler = new CodeCompiler(module);
            CompileUnit code = compiler.enterScope("<module>", CompilerScope.MODULE, node, 0);
            compiler.parse(node, null, new CompilerFlags(), false);
            compiler.exitScope();
            module.mainCode = code;
            return node;
        }

        @Override
        public String toString() {
            return "Generate Bytecode";
        }
    }

    public static final CompilationPhase BYTECODE_GENERATION_PHASE = new BytecodeGenerationPhase();
}
