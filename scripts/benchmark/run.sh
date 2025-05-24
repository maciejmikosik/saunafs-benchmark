#!/bin/bash -e

HERE=`dirname ${0}`
set -x
cd "${HERE}/../.."

java \
  -classpath "bin:jar/gson-2.13.1.jar" \
  com.saunafs.bm.Main
