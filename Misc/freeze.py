"""
This file is to generate frozen version of the importlib bootstrap files,
has to be run with jylang, after jylang Misc/freeze.py, move the Frozen.java
file into correct source folder
"""
import _imp
import sys
import io
import zlib
import os

scriptdir = os.path.dirname(__file__)
base_dir = os.path.dirname(os.path.abspath(scriptdir))
resource_dir = os.path.join(base_dir, 'src', 'resources')

packages = {"_frozen_importlib": "_bootstrap.py",
        "_frozen_importlib_external": "_bootstrap_external.py"
        }
if hasattr(_imp, '_compile_source'):
    for fullname in packages:
        source_name = packages[fullname]
        with open(f"dist/Lib/importlib/{source_name}", "r") as f:
            source_bytes = f.read()
            data = _imp._compile_source(fullname, source_bytes, source_name)
            with open(os.path.join(resource_dir, fullname), 'wb') as res:
                res.write(data)
else:
    print('frezze.py only works with jylang')
