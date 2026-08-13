#! /bin/sh

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd src/

final_rc=0
for i in $(find . -type f \( -name "*.kt" -o -name "*.java" \) 2>/dev/null | sort 2>/dev/null) ; do
    "$basedir"/tools/check_nonascii.sh -a \
      0939,093F,0928,094D,0926,0940,1F44D,1F44E,2764,FE0F,263A,00F6,00E4,00FC,00DF,00B0 \
      "$i"
    rc=$?
    if [ "$rc""x" != "0x" ]; then
        final_rc=$rc
    fi
done

exit "$final_rc"

