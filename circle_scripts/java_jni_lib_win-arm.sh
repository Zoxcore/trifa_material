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

# --- Setup llvm-mingw for aarch64 ---
LLVM_MINGW_VERSION="20240619"
LLVM_MINGW_DIR="/opt/llvm-mingw-${LLVM_MINGW_VERSION}-ucrt-ubuntu-20.04-x86_64"
export PATH="${LLVM_MINGW_DIR}/bin:$PATH"
# ------------------------------------

# ----------- config ------------
ORIGPATH=$PATH
export ORIGPATH

MAKEFLAGS=j$(nproc)
export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

# Target architecture is now aarch64
ARCH="aarch64"
export ARCH
# ----------- config ------------

echo "--------------"
ls -al "$_INST_/lib/libtoxcore.a" || echo "WARNING: libtoxcore.a not found in $_INST_/lib/"
echo "--------------"

## ---------------------------
if [ ! -d "ToxAndroidRefImpl" ]; then
    git clone https://github.com/zoff99/ToxAndroidRefImpl
fi

# -- git hash for jni code --
cd ToxAndroidRefImpl/
git_hash_for_jni=$(git rev-parse --verify --short=8 HEAD 2>/dev/null | tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_jni"":YY"
cd ..
# -- git hash for jni code --

cp -v ToxAndroidRefImpl/jni-c-toxcore/*.c ./
pwd
ls -al

## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h' 2>/dev/null | grep -v 'libavcodec' | grep -v 'android' | head -1
echo "JAVADIR1------------------"

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h' 2>/dev/null | head -1
echo "JAVADIR2------------------"

# Find Java include directories
JAVADIR1_RAW=$(find /usr -name 'jni.h' 2>/dev/null | grep -v 'libavcodec' | grep -v 'android' | head -1)
JAVADIR2_RAW=$(find /usr -name 'jni_md.h' 2>/dev/null | head -1)

export JAVADIR1=$(dirname "$JAVADIR1_RAW")
export JAVADIR2=$(dirname "$JAVADIR2_RAW")

echo "JAVADIR1: $JAVADIR1"
echo "JAVADIR2: $JAVADIR2"

# Note: Removed empty $C_FLAGS, $CXX_FLAGS, $LD_FLAGS variables to prevent shell expansion issues.
export CFLAGS="-fPIC -D_FORTIFY_SOURCE=2 -std=gnu99 -I$_INST_/include/ -L$_INST_/lib -fstack-protector-all"

# Compile and link for Windows ARM64
aarch64-w64-mingw32-gcc $CFLAGS \
-Wall -D_JNI_IMPLEMENTATION_ \
-DTOX_HAVE_NGCMID \
-DGIT_HASH=\"$git_hash_for_jni\" \
-DJAVA_LINUX \
-DNOGLOBALVARS \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
-I"$JAVADIR1/" \
-I"$JAVADIR2/" \
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
$_INST_/lib/libsodium.a \
-Wl,-Bstatic -lws2_32 \
-l:libiphlpapi.a \
-Wl,-Bstatic -lbcrypt \
-shared \
-lpthread \
-lm \
-o jni-c-toxcore_arm.dll || exit 1

sha256sum /home/runner/work/trifa_material/trifa_material/jni-c-toxcore_arm.dll

ls -al jni-c-toxcore_arm.dll || exit 1
pwd
file jni-c-toxcore_arm.dll



