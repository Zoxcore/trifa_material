#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd ./sorma2/

java \
-classpath ".:sqlite-jdbc-3.46.1.2.jar:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen"

