#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

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

MAKEFLAGS=j$(nproc)
export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

# ----------- config ------------

echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


## ---------------------------
mkdir -p /root/work/
cd /root/work/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd /root/work/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al


if [ "$1""x" == "localx" ]; then
    cp -av /c-toxcore/jni-c-toxcore.c jni-c-toxcore.c
fi

## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h'
echo "JAVADIR1------------------"

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h'
echo "JAVADIR2------------------"

dirname $(find /usr -name 'jni.h' 2>/dev/null|grep -v 'libavcodec'|head -1) > /tmp/xx1
dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
export JAVADIR1=$(cat /tmp/xx1)
export JAVADIR2=$(cat /tmp/xx2)
echo "JAVADIR1:""$JAVADIR1"
echo "JAVADIR2:""$JAVADIR2"


FLAGS_ASAN='-fsanitize=address -fno-omit-frame-pointer' # -static-libasan'
CFLAGS_ADDON='-O2 -g -fPIC'
CFLAGS_MORE="--param=ssp-buffer-size=1 -fstack-protector-all -std=gnu99 -I$_INST_/include/ -L$_INST_/lib"

# if [ "$1""x" == "localx" ]; then
#     export CFLAGS=" $CFLAGS -pg "
# fi

gcc $CFLAGS \
-Wall \
-DJAVA_LINUX \
-DNOGLOBALVARS \
$CFLAGS_ADDON $CFLAGS_MORE \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
-I$JAVADIR1/ \
-I$JAVADIR2/ \
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
-Wl,-soname,libjni-c-toxcore.so -o libjni-c-toxcore.so || exit 1


ls -al libjni-c-toxcore.so || exit 1
pwd
file libjni-c-toxcore.so
cp -a libjni-c-toxcore.so /artefacts/ || exit 1

