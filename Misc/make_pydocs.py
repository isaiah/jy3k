import io
import types
import re
import itertools
import builtins
import _functools
import _hashlib
import _io
import _json
import _multiprocessing
import posix
import errno
import array
import _struct
import sys
import time
import csv
import zipimport

def print_doc(out, obj, meth):
    if meth == '__doc__':
        doc = getattr(obj, meth)
        bdname = '%s_doc' % obj.__name__
    else:
        if meth == '__abstractmethods__':
            # getattr(type,'__abstractmethods__') would fail
            doc = ""
        else:
            doc = getattr(obj, meth).__doc__
        bdname = '%s_%s_doc' % (obj.__name__, meth)

    if doc is None:
        doc = ""
    if not isinstance(doc, str):
        print(obj, meth)
    lines = doc.split("\n")
    outstring = '\\n" + \n        "'.join(format(line) for line in lines)
    print('    public final static String %s = ' % bdname, file=out)
    print('        "%s";\n' % outstring, file=outfile)

format = lambda line: line.replace('\\', '\\\\').replace('"', r'\"')

async def foo(): pass

coro = foo()
coro.close()

types_list = [
builtins,
object,
type,
bytes,
dict,
list,
slice,
super,
staticmethod,
float,
enumerate,
int,
tuple,
str,
property,
range,
complex,
bool,
classmethod,
set,
frozenset,
BaseException,
bytearray,
memoryview,
types.GeneratorType,
types.CoroutineType,
type(coro.__await__()),
types.FunctionType,
#types.MemberDescriptorType,
types.CodeType,
types.SimpleNamespace,
types.FrameType,
types.TracebackType,
type(re.compile("f")),
type(re.compile("f").match("f")),
type(re.compile("f").scanner("f")),
type(range(1).__iter__()),
type(list().__iter__()),
type(itertools.chain(map(lambda x:x, list()))),
type(None),
type(NotImplemented),
type(Ellipsis),
_multiprocessing.SemLock,
io.TextIOBase,
io.BufferedReader,
io.BufferedWriter,
# modules
array,
csv,
errno,
itertools,
posix,
sys,
time,
zipimport,
_functools,
_hashlib,
_io,
_json,
_struct,
]

outfile = open("BuiltinDocs.java", "w")
print('// generated by make_pydocs.py\n', file=outfile)
print('package org.python.core;\n', file=outfile)
print('public class BuiltinDocs {\n', file=outfile)


for obj in types_list: 
    print('    // Docs for %s' % obj, file=outfile)
    for meth in dir(obj):
        print_doc(outfile, obj, meth)

print('}', file=outfile)
