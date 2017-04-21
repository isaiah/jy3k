package org.python.compiler;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.Assert;
import org.junit.Test;
import org.python.antlr.BuildAstVisitor;
import org.python.antlr.PythonLexer;
import org.python.antlr.PythonParser;
import org.python.antlr.PythonTree;

/**
 * Created by isaiah on 4/7/17.
 */
public class ClassClosureGeneratorTest {
    @Test
    public void testCreatingClassClosure() throws Exception {
        String expected = "Module(body=[FunctionDef(name=foo,args=arguments(args=[],vararg=arg(arg=__args__,annotation=null,),kwonlyargs=[],kw_defaults=[],kwarg=arg(arg=__keywords__,annotation=null,),defaults=[],),body=[ClassDef(name=Foo,bases=[Starred(value=Name(id=__args__,ctx=Load,),ctx=Load,)],keywords=[keyword(arg=null,value=Name(id=__keywords__,ctx=Load,),)],body=[FunctionDef(name=__init__,args=arguments(args=[arg(arg=self,annotation=null,)],vararg=null,kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=[Assign(targets=[Attribute(value=Name(id=self,ctx=Load,),attr=a,ctx=Store,)],value=Name(id=__class__,ctx=Load,),)],decorator_list=[],returns=null,)],decorator_list=[],),Assign(targets=[Name(id=__class__,ctx=Store,)],value=Name(id=Foo,ctx=Load,),),Return(value=Name(id=Foo,ctx=Load,),)],decorator_list=[],returns=null,),Assign(targets=[Name(id=Foo,ctx=Store,)],value=Call(func=Name(id=foo,ctx=Load,),args=[Name(id=object,ctx=Load,)],keywords=[keyword(arg=metaclass,value=Attribute(value=Name(id=abc,ctx=Load,),attr=Meta,ctx=Load,),)],),)],)";
        String param = "class Foo(object, metaclass=abc.Meta):\n  def __init__(self):\n    self.a = __class__\n";
        PythonTree tree = CompilerUtil.parse(param, "file");
        new ClassClosureGenerator().visit(tree);
        Assert.assertEquals(expected, tree.toStringTree());
    }
}
