#!/bin/bash -e

HERE=`dirname ${0}`
cd "${HERE}/../.."
set -x

java \
  -classpath bin:jar/gson-2.13.1.jar \
  com.saunafs.bm.Present
