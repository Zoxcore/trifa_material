#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd ./sorma2/

java \
-classpath ".:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen"

echo "#############"
echo "#############"
echo ""
echo "have a look in ${basedir}/sorma2/gen/com/zoffcc/applications/sorm/ for the generated java source files"
echo ""
echo "#############"
echo "#############"
