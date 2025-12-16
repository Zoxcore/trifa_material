#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

cd "$_HOME_"

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

CXXFLAGS_ADDON='-O2 -g -fPIC -D_FORTIFY_SOURCE=2 -fstack-protector-strong'
CFLAGS_ADDON='-O2 -g -fPIC -D_FORTIFY_SOURCE=2 -fstack-protector-strong'
CFLAGS_MORE='--param=ssp-buffer-size=1 -fstack-protector-all'
# ----------- config ------------


# ------- deps verisions ---------
NASM_VERSION="nasm-2.15.05" # "nasm-2.16.01"
FFMPEG_VERSION="n8.0.1"
OPUS_VERSION="v1.6"
SODIUM_VERSION="1.0.20"
VPX_VERSION="v1.15.2"
_X264_VERSION_="b35605ace3ddf7c1a5d67a2eb553f034aef41d55"
# ------- deps verisions ---------



# ---------- ffmpeg ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

FFMPEG_FILENAME="$FFMPEG_VERSION.tar.gz"
rm -f "ffmpeg"*.tar.*
wget $WGET_OPTIONS "https://github.com/FFmpeg/FFmpeg/archive/refs/tags/$FFMPEG_FILENAME" -O "ffmpeg_""$FFMPEG_FILENAME"
tar -xf "ffmpeg_""$FFMPEG_FILENAME"
rm -f "ffmpeg"*.tar.*
cd *mpeg*/

export LDFLAGS=" "

#
# sadly need "--disable-asm" or you will get:
#
# libavcodec.a(h264_qpel_10bit.o): relocation R_X86_64_PC32 against symbol `ff_pw_1023' can not be used when making a shared object; recompile with -fPIC
#

  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}
  ./configure \
              --enable-gpl \
              --prefix="$_INST_" \
              --disable-asm \
              --enable-pic \
              --disable-swscale \
              --disable-network \
              --disable-everything \
              --disable-debug \
              --disable-shared \
              --disable-programs \
              --disable-protocols \
              --disable-doc \
              --disable-sdl2 \
              --disable-avfilter \
              --disable-filters \
              --disable-iconv \
              --disable-network \
              --disable-muxers \
                \
              --disable-swresample \
              --disable-swscale-alpha \
              --disable-dwt \
              --disable-lsp \
              --disable-faan \
              --disable-vaapi \
              --disable-vdpau \
              --disable-zlib \
              --disable-xlib \
              --disable-bzlib \
              --disable-lzma \
              --disable-encoders \
              --disable-decoders \
              --disable-demuxers \
              --disable-parsers \
              --disable-bsfs \
              --disable-libxcb \
              --disable-libxcb-shm \
              --enable-parser=h264 \
              --enable-decoder=h264 || exit 1

#              --disable-lzo \
#              --disable-avresample \


  make -j || exit 1
  make install

  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- ffmpeg ---------


# ---------- opus ---------
if [ 1 == 1 ]; then

cd "$_SRC_"


OPUS_FILENAME="$OPUS_VERSION.tar.gz"
rm -f "opus"*.tar.gz
wget $WGET_OPTIONS "https://github.com/xiph/opus/archive/refs/tags/$OPUS_FILENAME" -O "opus_""$OPUS_FILENAME"
tar -xf "opus_""$OPUS_FILENAME"
rm -f "opus"*.tar.gz
cd opus*/

echo '#!/bin/bash
export PACKAGE_VERSION="'"$OPUS_VERSION"'"' > package_version
chmod a+rx package_version

  ./autogen.sh
  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}
  ./configure \
                               --prefix="$_INST_" \
                               --disable-shared \
                               --enable-static \
                               --disable-soname-versions \
                               --disable-extra-programs \
                               --disable-doc || exit 1
  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- opus ---------



# ---------- sodium ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

SODIUM_FILENAME="libsodium-$SODIUM_VERSION.tar.gz"
rm -f libsodium-*.tar.gz
wget $WGET_OPTIONS "https://github.com/jedisct1/libsodium/releases/download/""$SODIUM_VERSION""-RELEASE/""$SODIUM_FILENAME" -O "$SODIUM_FILENAME"
tar -xf "$SODIUM_FILENAME"
cd libsodium*/

  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}
  ./configure \
              --prefix="$_INST_" \
              --disable-shared \
              --enable-static \
              --with-pic || exit 1

  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- sodium ---------


# ---------- vpx ---------
if [ 1 == 1 ]; then

cd "$_SRC_"


VPX_FILENAME="$VPX_VERSION.tar.gz"

rm -f *.tar.gz
wget $WGET_OPTIONS "https://github.com/webmproject/libvpx/archive/refs/tags/""$VPX_VERSION"".tar.gz" -O "$VPX_FILENAME"
tar -xf "$VPX_FILENAME"
cd libvpx*/

  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}
  ./configure \
                                         --prefix="$_INST_" \
                                         --disable-shared \
                                         --size-limit=16384x16384 \
                                         --enable-onthefly-bitpacking \
                                         --enable-runtime-cpu-detect \
                                         --enable-realtime-only \
                                         --enable-multi-res-encoding \
                                         --enable-temporal-denoising \
                                         --enable-static \
                                         --disable-examples \
                                         --disable-tools \
                                         --disable-docs \
                                         --disable-unit-tests || exit 1

  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- vpx ---------


# --- NASM ---
if [ 1 == 1 ]; then

cd "$_SRC_"

    export PATH=$ORIGPATH

    rm -Rf nasm
    git clone https://github.com/netwide-assembler/nasm
    cd nasm/
    git checkout "$NASM_VERSION"

    ./autogen.sh
    ./configure --prefix=/

    make || exit 1

    # seems man pages are not always built. but who needs those
    touch nasm.1
    touch ndisasm.1
    sudo make install

    type -a nasm

    nasm --version || exit 1
    
    export PATH=$NEWPATH
cd "$_HOME_"

fi
# --- NASM ---


# ---------- x264 ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

git clone https://code.videolan.org/videolan/x264.git
cd x264/

  git checkout "$_X264_VERSION_"

  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}
  ./configure \
                                         --prefix="$_INST_" \
                                         --disable-opencl \
                                         --enable-static \
                                         --disable-avs \
                                         --disable-cli \
                                         --enable-pic || exit 

  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- x264 ---------


# ---------- c-toxcore ---------

cd "$_SRC_"

if [ "$1""x" == "localx" ]; then
    cp -av /c-toxcore ./
    LOGG=" -DMIN_LOGGER_LEVEL=LOGGER_LEVEL_DEBUG "
else
    unset LOGG
    git clone https://github.com/zoff99/c-toxcore c-toxcore
fi
cd c-toxcore/
git checkout "zoff99/zoxcore_local_fork"

# ------ set c-toxcore git commit hash ------
git_hash_for_toxcore=$(git rev-parse --verify --short=8 HEAD 2>/dev/null|tr -dc '[A-Fa-f0-9]' 2>/dev/null)
echo "XX:""$git_hash_for_toxcore"":YY"
cat toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
cd toxcore/ ; sed -i -e 's;^.*TOX_GIT_COMMIT_HASH.*$;#define TOX_GIT_COMMIT_HASH "'$git_hash_for_toxcore'";' tox.h
cd ../
cat toxcore/tox.h | grep 'TOX_GIT_COMMIT_HASH'
# ------ set c-toxcore git commit hash ------

ls -al toxcore/crypto_core.c toxcore/crypto_core.h toxcore/net_crypto.c toxcore/net_crypto.h
wget https://raw.githubusercontent.com/zoff99/c-toxcore/noise/toxcore/crypto_core.c -O toxcore/crypto_core.c
wget https://raw.githubusercontent.com/zoff99/c-toxcore/noise/toxcore/crypto_core.h -O toxcore/crypto_core.h
wget https://raw.githubusercontent.com/zoff99/c-toxcore/noise/toxcore/net_crypto.c -O toxcore/net_crypto.c
wget https://raw.githubusercontent.com/zoff99/c-toxcore/noise/toxcore/net_crypto.h -O toxcore/net_crypto.h
ls -al toxcore/crypto_core.c toxcore/crypto_core.h toxcore/net_crypto.c toxcore/net_crypto.h

autoreconf -fi

./configure \
     CXXFLAGS="$CXXFLAGS_ADDON" \
     CFLAGS="-fPIC $CFLAGS_ADDON $CFLAGS_MORE -DTOX_CAPABILITIES_ACTIVE $LOGG" \
    --prefix="$_INST_" \
    --disable-soname-versions \
    --disable-shared \
    --disable-testing \
    --disable-rt || exit 1

    make || exit 1
    make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

# ---------- c-toxcore ---------



