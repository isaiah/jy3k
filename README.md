jylang: python with invokedynamic

# Requirements

JDK9 + ant

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

# Related works

jython: https://github.com/jython/jython3
zippy: https://github.com/securesystemslab/zippy
