Jylang: A fork of jython

# Summary

A python compatible language on JVM.

This started as an effort to upgrade jython to python3k, and it quickly turn
into a personal experimental project.

# Goals

1. Be compatible with cpython 3, at the time being 3.6.0, but as new versions of
cpython come out, this project will always target the lastest version of
cpython, depends on when it can be in a state for release.

2. JVM language interoperation


# Non-Goals

1. Python 2.
2. C extension support. The approach JRuby took makes more sense, if a
   library is popular and necessary, implement it with Java native extension.

# May work on windows

No promise

# Roadmap

These are the nasty things when trying to port a language hosted on C to Java.

[] Rewrite IO layer, since PEP-3116 is inspired by java io, why not just wrap
the io layer of the host platform.
[] Rewrite socket stdlib in java, might use jnr-unixsocket?

[] `invokedynamic`: JDK9 have `jdk.dynalink` builtin, maybe the most fun
project.

# References

nashorn: http://openjdk.java.net/projects/nashorn/
JRuby: https://github.com/jruby/jruby/


# Original Readme

Jython: Python for the Java Platform

Welcome to Jython 3.5!

This repo is in the very early stages of development of a release of
Jython 3.5. Planned goals are language and runtime compatibility with
CPython 3.5, along with continued substantial support of the Python
ecosystem.

Please see ACKNOWLEDGMENTS for details about Jython's copyright,
license, contributors, and mailing lists; and NEWS for detailed
release notes, including bugs fixed, backwards breaking changes, and
new features.
