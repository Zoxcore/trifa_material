#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"

./gradlew packageReleaseDeb || exit 1

out_pkg=$(ls -1tr ./build/compose/binaries/main-release/deb/trifa-material*_amd64.deb 2>/dev/null | tail -1 2> /dev/null)
echo "found $out_pkg"
if [ "$out_pkg""x" == "x" ]; then
    echo "pkg not found"
    exit 1
else
    cp -v "$out_pkg" aa.deb || exit 1
    tools/fix_debian_pkg.sh aa.deb || exit 1
    rm -f aa.deb
fi
