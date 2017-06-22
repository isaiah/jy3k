Dynpy: python with invokedynamic

# Requirements

JDK9

# Build requirement

Python 3.5+

# Summary

A Python3 compatible language on JVM.

The main goal is to implement Python3 on mordern JVM, utilising invokedynamic.
Most of the architecture aspect are inspired by Nashorn.

This started as an effort to upgrade jython to python3k, and it quickly turn
into a personal experimental project.

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

# Related works

jython: https://github.com/jython/jython3
zippy: https://github.com/securesystemslab/zippy
