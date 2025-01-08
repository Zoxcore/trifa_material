#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

id -a
pwd
ls -al

if [ "$1""x" == "raspix" ]; then
  echo "*** RASPI ***"
  sudo apt-get update && \
          sudo apt-get install -y --no-install-recommends \
          ca-certificates \
          openjdk-17-jdk \
          openjdk-17-jdk-headless \
          coreutils autoconf libtool pkg-config
fi

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
git clone https://github.com/zoff99/ToxAndroidRefImpl

# -- git hash for jni code --
cd ToxAndroidRefImpl/
git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"
cd ..
# -- git hash for jni code --

cp -v ToxAndroidRefImpl/jni-c-toxcore/*.c ./
pwd
ls -al

## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h'
echo "JAVADIR1------------------"

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h'
echo "JAVADIR2------------------"

dirname $(find /usr -name 'jni.h' 2>/dev/null|grep -v 'libavcodec'|grep -v 'android'|head -1) > /tmp/xx1
dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
export JAVADIR1=$(cat /tmp/xx1)
export JAVADIR2=$(cat /tmp/xx2)
echo "JAVADIR1:""$JAVADIR1"
echo "JAVADIR2:""$JAVADIR2"


CFLAGS_ADDON='-O2 -g -fPIC -D_FORTIFY_SOURCE=2'
CFLAGS_MORE="--param=ssp-buffer-size=1 -fstack-protector-all -std=gnu99 -I$_INST_/include/ -L$_INST_/lib"

if [ "$1""x" == "localx" ]; then
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
    pwd
    ls -al /c-toxcore/
    echo "_________"
    ls -al /c-toxcore/jni-c-toxcore.c
    cp -av /c-toxcore/jni-c-toxcore.c ./
fi

GCC_=gcc

if [ "$1""x" == "raspix" ]; then
  echo "*** RASPI ***"
  GCC_="$CC"
fi

if [ "$2""x" == "asanx" ]; then
  echo "***** ASAN *****"
  echo "***** ASAN *****"
  echo "***** ASAN *****"
  CFLAGS_ASAN="-lstdc++ -fno-rtti -fsanitize=address -fno-omit-frame-pointer -fsanitize-recover=address -static-libasan"
else
  CFLAGS_ASAN=""
fi


$GCC_ $CFLAGS \
-Wall \
$CFLAGS_ASAN \
-DGIT_HASH=\"$git_hash_for_jni\" \
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
-Wl,-Bsymbolic \
$_INST_/lib/libavcodec.a \
$_INST_/lib/libavdevice.a \
$_INST_/lib/libavformat.a \
$_INST_/lib/libavutil.a \
$_INST_/lib/libopus.a \
$_INST_/lib/libvpx.a \
$_INST_/lib/libx264.a \
$_INST_/lib/libx265.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxencryptsave.a \
$_INST_/lib/libsodium.a \
-lpthread \
-lm \
-lstdc++ \
-shared \
-Wl,-soname,libjni-c-toxcore.so -o libjni-c-toxcore.so || exit 1

sha256sum /home/runner/work/trifa_material/trifa_material/libjni-c-toxcore.so


ls -al libjni-c-toxcore.so || exit 1
pwd

if [ "$2""x" == "asanx" ]; then
    # check if we actually have ASAN symbols in the library file
    nm libjni-c-toxcore.so | grep -i asan || exit 1
fi

file libjni-c-toxcore.so

