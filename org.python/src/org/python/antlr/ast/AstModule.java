package org.python.antlr.ast;

import org.python.annotations.ExposedModule;
import org.python.annotations.ModuleInit;
import org.python.antlr.AST;
import org.python.antlr.base.boolop;
import org.python.antlr.base.cmpop;
import org.python.antlr.base.excepthandler;
import org.python.antlr.base.expr;
import org.python.antlr.base.expr_context;
import org.python.antlr.base.mod;
import org.python.antlr.base.operator;
import org.python.antlr.base.slice;
import org.python.antlr.base.stmt;
import org.python.antlr.base.unaryop;
import org.python.antlr.op.Add;
import org.python.antlr.op.And;
import org.python.antlr.op.AugLoad;
import org.python.antlr.op.AugStore;
import org.python.antlr.op.BitAnd;
import org.python.antlr.op.BitOr;
import org.python.antlr.op.BitXor;
import org.python.antlr.op.Del;
import org.python.antlr.op.Div;
import org.python.antlr.op.Eq;
import org.python.antlr.op.FloorDiv;
import org.python.antlr.op.Gt;
import org.python.antlr.op.GtE;
import org.python.antlr.op.In;
import org.python.antlr.op.Invert;
import org.python.antlr.op.Is;
import org.python.antlr.op.IsNot;
import org.python.antlr.op.LShift;
import org.python.antlr.op.Load;
import org.python.antlr.op.Lt;
import org.python.antlr.op.LtE;
import org.python.antlr.op.MatMult;
import org.python.antlr.op.Mod;
import org.python.antlr.op.Mult;
import org.python.antlr.op.Not;
import org.python.antlr.op.NotEq;
import org.python.antlr.op.NotIn;
import org.python.antlr.op.Or;
import org.python.antlr.op.Param;
import org.python.antlr.op.Pow;
import org.python.antlr.op.RShift;
import org.python.antlr.op.Store;
import org.python.antlr.op.Sub;
import org.python.antlr.op.UAdd;
import org.python.antlr.op.USub;
import org.python.core.CompilerFlags;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyUnicode;

@ExposedModule(name = "_ast")
public class AstModule {

    private AstModule() {}

    @ModuleInit
    public static void classDictInit(PyObject dict) {
        dict.__setitem__("__version__", new PyUnicode("62047"));
        dict.__setitem__("PyCF_ONLY_AST", new PyLong(CompilerFlags.PyCF_ONLY_AST));

        dict.__setitem__("AST", AST.TYPE);
        dict.__setitem__("Module", Module.TYPE);
        dict.__setitem__("Assert", Assert.TYPE);
        dict.__setitem__("Assign", Assign.TYPE);
        dict.__setitem__("Attribute", Attribute.TYPE);
        dict.__setitem__("AsyncFor", AsyncFor.TYPE);
        dict.__setitem__("AsyncFunctionDef", AsyncFunctionDef.TYPE);
        dict.__setitem__("AsyncWith", AsyncWith.TYPE);
        dict.__setitem__("AugAssign", AugAssign.TYPE);
        dict.__setitem__("BinOp", BinOp.TYPE);
        dict.__setitem__("BoolOp", BoolOp.TYPE);
        dict.__setitem__("Break", Break.TYPE);
        dict.__setitem__("Bytes", Bytes.TYPE);
        dict.__setitem__("Call", Call.TYPE);
        dict.__setitem__("ClassDef", ClassDef.TYPE);
        dict.__setitem__("Compare", Compare.TYPE);
        dict.__setitem__("Continue", Continue.TYPE);
        dict.__setitem__("Constant", Constant.TYPE);
        dict.__setitem__("Delete", Delete.TYPE);
        dict.__setitem__("Dict", Dict.TYPE);
        dict.__setitem__("Ellipsis", Ellipsis.TYPE);

        dict.__setitem__("ExceptHandler", ExceptHandler.TYPE);
        dict.__setitem__("Expr", Expr.TYPE);
        dict.__setitem__("Expression", Expression.TYPE);
        dict.__setitem__("ExtSlice", ExtSlice.TYPE);
        dict.__setitem__("For", For.TYPE);
        dict.__setitem__("FormattedValue", FormattedValue.TYPE);
        dict.__setitem__("FunctionDef", FunctionDef.TYPE);
        dict.__setitem__("GeneratorExp", GeneratorExp.TYPE);
        dict.__setitem__("Global", Global.TYPE);
        dict.__setitem__("If", If.TYPE);
        dict.__setitem__("IfExp", IfExp.TYPE);
        dict.__setitem__("Import", Import.TYPE);
        dict.__setitem__("ImportFrom", ImportFrom.TYPE);
        dict.__setitem__("Index", Index.TYPE);
        dict.__setitem__("Interactive", Interactive.TYPE);
        dict.__setitem__("JoinedStr", JoinedStr.TYPE);
        dict.__setitem__("Lambda", Lambda.TYPE);
        dict.__setitem__("List", List.TYPE);
        dict.__setitem__("ListComp", ListComp.TYPE);
        dict.__setitem__("Module", Module.TYPE);
        dict.__setitem__("Name", Name.TYPE);
        dict.__setitem__("NameConstant", NameConstant.TYPE);
        dict.__setitem__("Num", Num.TYPE);
        dict.__setitem__("Pass", Pass.TYPE);
        dict.__setitem__("Raise", Raise.TYPE);
        dict.__setitem__("Return", Return.TYPE);
        dict.__setitem__("Set", Set.TYPE);
        dict.__setitem__("SetComp", SetComp.TYPE);
        dict.__setitem__("Slice", Slice.TYPE);
        dict.__setitem__("Str", Str.TYPE);
        dict.__setitem__("Subscript", Subscript.TYPE);
        dict.__setitem__("Suite", Suite.TYPE);
        dict.__setitem__("Try", Try.TYPE);
        dict.__setitem__("Tuple", Tuple.TYPE);
        dict.__setitem__("UnaryOp", UnaryOp.TYPE);
        dict.__setitem__("While", While.TYPE);
        dict.__setitem__("With", With.TYPE);
        dict.__setitem__("Yield", Yield.TYPE);
        dict.__setitem__("alias", alias.TYPE);
        dict.__setitem__("arg", arg.TYPE);
        dict.__setitem__("arguments", arguments.TYPE);
        dict.__setitem__("comprehension", comprehension.TYPE);
        dict.__setitem__("excepthandler", excepthandler.TYPE);
        dict.__setitem__("expr", expr.TYPE);
        dict.__setitem__("keyword", keyword.TYPE);
        dict.__setitem__("mod", mod.TYPE);
        dict.__setitem__("slice", slice.TYPE);
        dict.__setitem__("stmt", stmt.TYPE);
        
        dict.__setitem__("operator", operator.TYPE);
        dict.__setitem__("Add", Add.TYPE);
        dict.__setitem__("Sub", Sub.TYPE);
        dict.__setitem__("Mult", Mult.TYPE);
        dict.__setitem__("MatMult", MatMult.TYPE);
        dict.__setitem__("Div", Div.TYPE);
        dict.__setitem__("FloorDiv", FloorDiv.TYPE);
        dict.__setitem__("Mod", Mod.TYPE);
        dict.__setitem__("LShift", LShift.TYPE);
        dict.__setitem__("RShift", RShift.TYPE);
        dict.__setitem__("BitOr", BitOr.TYPE);
        dict.__setitem__("BitAnd", BitAnd.TYPE);
        dict.__setitem__("BitXor", BitXor.TYPE);
        dict.__setitem__("Pow", Pow.TYPE);
       
        dict.__setitem__("boolop", boolop.TYPE);
        dict.__setitem__("And", And.TYPE);
        dict.__setitem__("Or", Or.TYPE);
      
        dict.__setitem__("cmpop", cmpop.TYPE);
        dict.__setitem__("Eq", Eq.TYPE);
        dict.__setitem__("Gt", Gt.TYPE);
        dict.__setitem__("GtE", GtE.TYPE);
        dict.__setitem__("In", In.TYPE);
        dict.__setitem__("Is", Is.TYPE);
        dict.__setitem__("IsNot", IsNot.TYPE);
        dict.__setitem__("Lt", Lt.TYPE);
        dict.__setitem__("LtE", LtE.TYPE);
        dict.__setitem__("NotEq", NotEq.TYPE);
        dict.__setitem__("NotIn", NotIn.TYPE);
       
        dict.__setitem__("expr_context", expr_context.TYPE);
        dict.__setitem__("Load", Load.TYPE);
        dict.__setitem__("Store", Store.TYPE);
        dict.__setitem__("Del", Del.TYPE);
        dict.__setitem__("AugLoad", AugLoad.TYPE);
        dict.__setitem__("AugStore", AugStore.TYPE);
        dict.__setitem__("Param", Param.TYPE);
       
        dict.__setitem__("unaryop", unaryop.TYPE);
        dict.__setitem__("Invert", Invert.TYPE);
        dict.__setitem__("Not", Not.TYPE);
        dict.__setitem__("UAdd", UAdd.TYPE);
        dict.__setitem__("USub", USub.TYPE);
    }
}
