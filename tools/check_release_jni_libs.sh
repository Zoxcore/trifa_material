#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd resources/common/

count_expect=15

# check count libs
count=$(ls -1 *.dll *.jnilib *.so 2>/dev/null | wc -l 2>/dev/null)
if [ "$count""x" != "$count_expect""x" ]; then
    ls -1 *.dll *.jnilib *.so
    echo "jni libs count does not match: $count != $count_expect"
    exit 1
fi

# checks lib files not empty
err_count=0
for i in $(ls -1 *.dll *.jnilib *.so 2>/dev/null); do
    if [ ! -s "$i" ]; then
        echo "jni lib has zero byte size: $i"
        err_count=$[ $err_count + 1 ]
    fi
done
if [ $err_count -gt 0 ]; then
    exit 1
fi

# checks that release libs do NOT contain ASAN symbols
err_count=0
for i in $(ls -1 *.dll *.so 2>/dev/null|grep -v libjni_notifications_rs.so 2>/dev/null); do
    nm "$i"|grep -v 'global_version_asan_string'|grep -i asan
    res=$?
    if [ $res -eq 0 ]; then
        echo "jni lib contains ASAN symbols: $i"
        err_count=$[ $err_count + 1 ]
    fi
done
if [ $err_count -gt 0 ]; then
    exit 1
fi

echo "all checks OK"

