#! /bin/bash

# # -> for docker run
# docker run -ti --rm ubuntu:20.04 /bin/bash
#
# # -> install dependencies
# apt-get update
# apt-get install default-jdk ca-certificates libconfig-dev wget unzip zip automake autotools-dev build-essential check checkinstall libtool pkg-config rsync git ffmpeg libavcodec-dev libavdevice-dev libsodium-dev libvpx-dev libopus-dev libx264-dev clang
#
# # -> add local user and change to that user
# useradd -m -s /bin/bash user2
# su - user2
# # -> then run this script as "user2"


_HOME_="$(pwd)"
export _HOME_

id -a
pwd
ls -al

_CTC_SRC_DIR_="$_HOME_/work/c-toxcore"
export _CTC_SRC_DIR_

export _SRC_=$_HOME_/src/
export _INST_=$_HOME_/inst/


export CF2=" -O2 -g3"
export CF3=" "
export VV1=" " # VERBOSE=1 V=1 "


mkdir -p $_SRC_
mkdir -p $_INST_
mkdir -p $_CTC_SRC_DIR_

export LD_LIBRARY_PATH=$_INST_/lib/
export PKG_CONFIG_PATH=$_INST_/lib/pkgconfig

mkdir -p $_HOME_/workspace/data/

mkdir -p $_HOME_/work/
cd $_HOME_/work/
git clone https://github.com/zoff99/c-toxcore "$_CTC_SRC_DIR_"/

cd "$_CTC_SRC_DIR_"/
pwd
ls -al

git checkout zoff99/zoxcore_local_fork

./autogen.sh

make clean
export CFLAGS_=" $CF2 -D_GNU_SOURCE -DTOX_CAPABILITIES_ACTIVE -I$_INST_/include/ -O2 -g3 -fno-omit-frame-pointer -fstack-protector-all -fPIC "
export CFLAGS="$CFLAGS_"
# export CFLAGS=" $CFLAGS -Werror=div-by-zero -Werror=format=2 -Werror=implicit-function-declaration "
export LDFLAGS="-L$_INST_/lib -fPIC "

./configure \
--prefix=$_INST_ \
--disable-soname-versions --disable-testing --disable-shared
make -j$(nproc) || exit 1
make install

export CFLAGS=" $CFLAGS_ -fPIC "
export CXXFLAGS=" $CFLAGS_ -fPIC "
export LDFLAGS=" $LDFLAGS_ -fPIC "
# timeout -k 242 240 make V=1 -j20 check || exit 0 # tests fail too often on CI -> don't error out on test failures



# -------------- now compile the JNI lib ----------------------

cd $_HOME_/work/
pwd
ls -al


echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


add_config_flag() { CONFIG_FLAGS="$CONFIG_FLAGS $@";    }
add_c_flag()      { C_FLAGS="$C_FLAGS $@";              }
add_cxx_flag()    { CXX_FLAGS="$CXX_FLAGS $@";          }
add_ld_flag()     { LD_FLAGS="$LD_FLAGS $@";            }
add_flag()        { add_c_flag "$@"; add_cxx_flag "$@"; }

CONFIG_FLAGS=""
C_FLAGS=""
CXX_FLAGS=""
LD_FLAGS=""

unset CFLAGS
unset CXXFLAGS
unset CPPFLAGS
unset LDFLAGS

# Optimisation flags.
add_flag -O2 -march=native
add_flag -g3


## ---------------------------
cd $_HOME_/work/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd $_HOME_/work/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al
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

# export ASAN_CLANG_FLAGS=" -fsanitize=address -fno-omit-frame-pointer -fno-optimize-sibling-calls -lasan "
export ASAN_CLANG_FLAGS=" "
export CFLAGS=" -fPIC -std=gnu99 -I$_INST_/include/ -L$_INST_/lib -fstack-protector-all "


clang_="clang"


$clang_ $CFLAGS \
-DJAVA_LINUX \
$ASAN_CLANG_FLAGS \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
-I$JAVADIR1/ \
-I$JAVADIR2/ \
-L/usr/local/lib \
-I/usr/local/include/ \
jni-c-toxcore.c \
-Wl,-whole-archive \
$_INST_/lib/libtoxcore.a \
$_INST_/lib/libtoxav.a \
$_INST_/lib/libtoxencryptsave.a \
-Wl,-no-whole-archive \
-lopus \
-lvpx \
-lx264 \
-lavcodec \
-lavutil \
-lsodium \
-shared \
-lpthread \
-lm \
-ldl \
-Wl,-soname,libjni-c-toxcore.so -o libjni-c-toxcore.so || exit 1


ls -al libjni-c-toxcore.so || exit 1
pwd
file libjni-c-toxcore.so


# git clone https://github.com/zoff99/java_toxclient_example

