#!/bin/bash -e

HERE=`dirname ${0}`
cd "${HERE}/../.."
set -x
javac \
  -sourcepath java \
  -classpath jar/gson-2.13.1.jar \
  -d bin \
  java/com/saunafs/bm/Main.java
