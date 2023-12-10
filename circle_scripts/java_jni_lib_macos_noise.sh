#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

_HOME2_=$(dirname $0)
export _HOME2_
_HOME3_=$(cd $_HOME2_;pwd)
export _HOME3_

id -a
pwd
ls -al

export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/


mkdir -p $_SRC_
mkdir -p $_INST_

export LD_LIBRARY_PATH=$_INST_/lib/
export PKG_CONFIG_PATH=$_INST_/lib/pkgconfig


# ----------- config ------------
ORIGPATH=$PATH
export ORIGPATH
NEWPATH=$PATH # /usr/x86_64-w64-mingw32/bin:$PATH
export NEWPATH
export PATH=$NEWPATH

# MAKEFLAGS=j$(nproc)
# export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

# ----------- config ------------

echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


## ---------------------------
sudo mkdir -p /Users/runner/
sudo chmod a+rwx /Users/runner/
cd /Users/runner/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd /Users/runner/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al
## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h'
echo "JAVADIR1------------------"

# /usr/local/Cellar/openjdk/15.0.2/include/jni.h
# /usr/local/Cellar/openjdk/15.0.2/include/jni_md.h

# /usr/local/Cellar/openjdk/15.0.2/libexec/openjdk.jdk/Contents/Home/include/jni.h
# /usr/local/Cellar/openjdk/15.0.2/libexec/openjdk.jdk/Contents/Home/include/darwin/jni_md.h

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h'
echo "JAVADIR2------------------"

#dirname $(find /usr -name 'jni.h' 2>/dev/null|grep -v 'libavcodec'|head -1) > /tmp/xx1
#dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
#export JAVADIR1=$(cat /tmp/xx1)
#export JAVADIR2=$(cat /tmp/xx2)

#export JAVADIR1="/usr/local/Cellar/openjdk/16.0.1/include"
#export JAVADIR2="/usr/local/Cellar/openjdk/16.0.1/include"

#if [ ! -e "$JAVADIR1" ]; then
#    mkdir -p "$_INST_/jinclude/"
#    cp -av /Users/travis/build/zoff99/java_toxclient_example/circle_scripts/jni_md.h "$_INST_/jinclude/"
#    cp -av /Users/travis/build/zoff99/java_toxclient_example/circle_scripts/jni.h "$_INST_/jinclude/"
#    export JAVADIR1="$_INST_/jinclude"
#    export JAVADIR2="$_INST_/jinclude"
#fi

#echo "JAVADIR1:""$JAVADIR1"
#echo "JAVADIR2:""$JAVADIR2"

mkdir -p "$_INST_/jinclude/"
cp -av "$_HOME3_"/jni_md.h "$_INST_/jinclude/"
cp -av "$_HOME3_"/jni.h "$_INST_/jinclude/"

export CFLAGS=" -fPIC -std=gnu99 -I$_INST_/include/ -I$_INST_/jinclude/ -L$_INST_/lib -fstack-protector-all "

git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"

gcc $CFLAGS \
-Wall \
-DGIT_HASH=\"$git_hash_for_jni\" \
-DJAVA_LINUX \
-DNOGLOBALVARS \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
jni-c-toxcore.c \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libavcodec.a \
$_INST_/lib/libavdevice.a \
$_INST_/lib/libavformat.a \
$_INST_/lib/libavutil.a \
$_INST_/lib/libopus.a \
$_INST_/lib/libvpx.a \
$_INST_/lib/libx264.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libsodium.a \
-lpthread \
-lm \
-shared \
-o libjni-c-toxcore_noise.jnilib || exit 1

ls -al libjni-c-toxcore_noise.jnilib || exit 1

otool -L libjni-c-toxcore_noise.jnilib
pwd
file libjni-c-toxcore_noise.jnilib

pwd
find . -name libjni-c-toxcore_noise.jnilib


if [ -e /usr/bin/nasm ]; then
    pwd
    ls -hal libjni-c-toxcore_noise.jnilib
    echo "-------- DONE --------"
else
    :
fi

