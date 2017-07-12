package org.python.compiler;

import org.junit.Assert;
import org.junit.Test;
import org.python.antlr.PythonTree;

public class NameManglerTest {
    @Test
    public void testManglingFluentAttribute() throws Exception {
        String expected = "Module(body=[ClassDef(name=Foo,bases=[],keywords=[],body=[Assign(targets=[Name(id=_Foo__foo,ctx=Store,)],value=Str(s=1,2,),),FunctionDef(name=_Foo__parse,args=arguments(args=[arg(arg=self,annotation=null,)],vararg=null,kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=[Expr(value=Call(func=Attribute(value=Attribute(value=Name(id=self,ctx=Load,),attr=_Foo__foo,ctx=Load,),attr=split,ctx=Load,),args=[Str(s=,,)],keywords=[],),)],decorator_list=[],returns=null,)],decorator_list=[],)],)";
        String param = "class Foo():\n  __foo = '1,2'\n  def __parse(self):\n    self.__foo.split(',')\n";
        PythonTree tree = CompilerUtil.parse(param, "file");
        new NameMangler().visit(tree);
        Assert.assertEquals(expected, tree.toStringTree());
    }
}
