package org.python.antlr;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.compiler.CompilerUtil;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by isaiah on 3/11/17.
 */
@RunWith(JUnitParamsRunner.class)
public class BuildAstVisitorTest {

    @Test
    @Parameters(method = "data")
    public void testBuildAst(String program, String expectedAst) {
        Assert.assertEquals(expectedAst, CompilerUtil.parse(program, "single").toStringTree());
    }

    public Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"rb'\\s+'", "Bytes(s=\\s+,)"},
                {"1", "Num(n=1,)"},
                {"0x11", "Num(n=17,)"},
                {"1.1", "Num(n=1.1,)"},
                {"2.1j", "Num(n=2.1j,)"},
                {"...", "Ellipsis()"},
//                {"None", "NameConstant"},
//                {"True", "NameConstant"},
//                {"False", "NameConstant"},
                {"a", "Name(id=a,ctx=Load,)"},
                {"'str'", "Str(s=str,)"},
                {"yield a", "Yield(value=Name(id=a,ctx=Load,),)"},
                {"yield from a", "YieldFrom(value=Name(id=a,ctx=Load,),)"},
                {"return", "Return(value=null,)"},
                {"return a", "Return(value=Name(id=a,ctx=Load,),)"},
                {"continue", "Continue()"},
                {"break", "Break()"},
                {"del a", "Delete(targets=[Name],)"},
                {"pass", "Pass()"},
                {"import foo.bar as xxx", "Import(names=[alias],)"},
                {"global a", "Global(names=[a],)"},
                {"nonlocal a", "Nonlocal(names=[a],)"},
                {"assert True, 'not true'", "Assert(test=Name(id=True,ctx=Load,),msg=Str(s=not true,),)"},
                {"while 1:\n  pass\n\n", "While(test=Num(n=1,),body=[Pass],orelse=[],)"},
                {"for x in a:\n   pass\n\n", "For(target=Name(id=x,ctx=Store,),iter=Name(id=a,ctx=Load,),body=[Pass],orelse=[],)"},
                {"2 + 3", "BinOp(left=Num(n=2,),op=Add,right=Num(n=3,),)"},
                {"2 - 3", "BinOp(left=Num(n=2,),op=Sub,right=Num(n=3,),)"},
                {"2 * 3", "BinOp(left=Num(n=2,),op=Mult,right=Num(n=3,),)"},
                {"2 / 3", "BinOp(left=Num(n=2,),op=Div,right=Num(n=3,),)"},
                {"2 % 3", "BinOp(left=Num(n=2,),op=Mod,right=Num(n=3,),)"},
                {"2 @ 3", "BinOp(left=Num(n=2,),op=MatMult,right=Num(n=3,),)"},
                {"2 + 3 * 4", "BinOp(left=Num(n=2,),op=Add,right=BinOp(left=Num(n=3,),op=Mult,right=Num(n=4,),),)"},
                {"2 ** 4", "BinOp(left=Num(n=2,),op=Pow,right=Num(n=4,),)"},
                {"foo(1)", "Call(func=Name(id=foo,ctx=Load,),args=[Num],keywords=[],)"},
                {"foo[1]", "Subscript(value=Name(id=foo,ctx=Load,),slice=Index(value=Num(n=1,),),ctx=Load,)"},
                {"foo.bar", "Attribute(value=Name(id=foo,ctx=Load,),attr=bar,ctx=Load,)"},
                {"[x, y]", "List(elts=[Name, Name],ctx=Load,)"},
                {"(x, y)", "Tuple(elts=[Name, Name],ctx=Load,)"},
                {"[x for x in y]", "ListComp(elt=Name(id=x,ctx=Load,),generators=[comprehension],)"},
                {"(x for x in y)", "GeneratorExp(elt=Name(id=x,ctx=Load,),generators=[comprehension],)"},
                {"{1,2}", "Set(elts=[Num, Num],)"},
                {"{1, *a}", "Set(elts=[Num, Starred],)"},
                {"{x for x in y}", "SetComp(elt=Name(id=x,ctx=Load,),generators=[comprehension],)"},
                {"{foo: bar}", "Dict(keys=[Name],values=[Name],)"},
                {"{foo: bar, **quzz}", "Dict(keys=[Name],values=[Name, Name],)"},
                {"{foo: bar for foo, bar in a}", "DictComp(key=Name(id=foo,ctx=Load,),value=Name(id=bar,ctx=Load,),generators=[comprehension],)"},
                {"True and False", "BoolOp(op=And,values=[Name, Name],)"},
                {"a not in b", "Compare(left=Name(id=a,ctx=Load,),ops=[NotIn],comparators=[Name],)"},
                {"a > b", "Compare(left=Name(id=a,ctx=Load,),ops=[Gt],comparators=[Name],)"},
                {"lambda a: a.split", "Lambda(args=arguments(args=[arg],vararg=null,kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=Attribute(value=Name(id=a,ctx=Load,),attr=split,ctx=Load,),)"},
                {"lambda a,: a.split", "Lambda(args=arguments(args=[arg],vararg=null,kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=Attribute(value=Name(id=a,ctx=Load,),attr=split,ctx=Load,),)"},
                {"lambda *arg,: 0", "Lambda(args=arguments(args=[],vararg=arg(arg=arg,annotation=null,),kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=Num(n=0,),)"},
                {"lambda a, *arg,: 0", "Lambda(args=arguments(args=[arg],vararg=arg(arg=arg,annotation=null,),kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=Num(n=0,),)"},
                {"1 if 1 else 0", "IfExp(test=Num(n=1,),body=Num(n=1,),orelse=Num(n=0,),)"},
                {"1if 1else 0", "IfExp(test=Num(n=1,),body=Num(n=1,),orelse=Num(n=0,),)"},
                {"if 1:\n  x\nelse:\n  y\n\n", "If(test=Num(n=1,),body=[Expr],orelse=[Expr],)"},
                {"from a import b", "ImportFrom(module=a,names=[alias],level=0,)"},
                {"from a import b as c", "ImportFrom(module=a,names=[alias],level=0,)"},
                {"from .. import b", "ImportFrom(module=,names=[alias],level=2,)"},
                {"raise a from b", "Raise(exc=Name(id=a,ctx=Load,),cause=Name(id=b,ctx=Load,),)"},
                {"def foo():\n  pass\n\n", "FunctionDef(name=foo,args=null,body=[Pass],decorator_list=[],returns=null,)"},
                {"def foo(a,):\n  pass\n\n", "FunctionDef(name=foo,args=arguments(args=[arg],vararg=null,kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=[Pass],decorator_list=[],returns=null,)"},
                {"def foo(*a,):\n  pass\n\n", "FunctionDef(name=foo,args=arguments(args=[],vararg=arg(arg=a,annotation=null,),kwonlyargs=[],kw_defaults=[],kwarg=null,defaults=[],),body=[Pass],decorator_list=[],returns=null,)"},
                {"a: str = 1", "AnnAssign(target=Name(id=a,ctx=Store,),annotation=Name(id=str,ctx=Load,),value=Num(n=1,),simple=1,)"},
                {"class Foo(a=1,b=2):\n  pass\n", "ClassDef(name=Foo,bases=[],keywords=[keyword, keyword],body=[Pass],decorator_list=[],)"},
        });
    }
}
