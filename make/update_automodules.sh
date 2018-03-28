#!/usr/bin/env bash
set -e

ant deps
for fjar in ../lib/*.jar; do
  jar=$(basename $fjar)
  [[ $jar =~ ^(asm-.*\.jar)$ ]] && continue
  jdeps --generate-module-info tmp  --module-path ../lib --add-modules=ALL-MODULE-PATH $fjar

  for filename in tmp/**/module-info.java; do
    mkdir -p tmp/classes
    cd tmp/classes
    jar xf ../../$fjar
    cd -
    javac -d tmp/classes -p ../lib $filename
    rm $filename
    jar --update --file $fjar --module-version=1.0 -C tmp/classes/ module-info.class
    rm tmp/classes -r
  done
done
