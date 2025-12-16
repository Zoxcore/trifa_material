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
_X265_VERSION_="1d117bed4747758b51bd2c124d738527e30392cb"
# ------- deps verisions ---------


if [ "$1""x" == "raspix" ]; then
  echo "*** RASPI ***"
  sudo apt-get update && \
          sudo apt-get install -y --no-install-recommends \
          cmake
fi



# ---------- x265 ---------
if [ 1 == 1 ]; then

cd "$_SRC_"

git clone https://bitbucket.org/multicoreware/x265_git.git
cd x265_git/

  git checkout "$_X265_VERSION_"
  cd source/

  export CXXFLAGS=${CXXFLAGS_ADDON}
  export CFLAGS=${CFLAGS_ADDON}

  # fix ratecontrol while streaming --------------------
  # echo "#####ratecontrolfix#####"
  # sed -i -e \
  # 's#m_param->rc.bitrate = m_param->rc.vbvMaxBitrate#m_param->rc.vbvMaxBitrate = m_param->rc.bitrate#g' \
  # ./encoder/ratecontrol.cpp
  # git diff ./encoder/ratecontrol.cpp
  # echo "#####ratecontrolfix#####"
  # fix ratecontrol while streaming --------------------

  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
    cmake . -DCMAKE_INSTALL_PREFIX="$_INST_" -DENABLE_PIC=ON -DENABLE_ASSEMBLY=OFF # -DCMAKE_VERBOSE_MAKEFILE=ON
  else
    # /usr/bin/ld: /home/runner/work/trifa_material/trifa_material/inst//lib/libx265.a(cpu-a.asm.o): relocation R_X86_64_PC32 against symbol `x265_intel_cpu_indicator_init' can not be used when making a shared object; recompile with -fPIC
    cmake . -DCMAKE_INSTALL_PREFIX="$_INST_" -DENABLE_PIC=ON -DENABLE_ASSEMBLY=ON # -DCMAKE_VERBOSE_MAKEFILE=ON
  fi
  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

ls -hal $_INST_/lib/libx265.a || exit 1

fi
# ---------- x265 ---------


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

  export CXXFLAGS="${CXXFLAGS_ADDON}"
  export CFLAGS="${CFLAGS_ADDON}"

  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
  ./configure --arch="aarch64" \
              --enable-gpl \
              --prefix="$_INST_" \
              --target-os="linux" \
              --cross-prefix="$CROSS_COMPILE" \
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
              --enable-parser=hevc \
              --enable-decoder=hevc \
              --enable-parser=h264 \
              --enable-decoder=h264 || exit 1

  else

  ./configure \
              --enable-gpl \
              --prefix="$_INST_" \
              --enable-asm \
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
              --enable-parser=hevc \
              --enable-decoder=hevc \
              --enable-parser=h264 \
              --enable-decoder=h264 || exit 1

#              --disable-lzo \
#              --disable-avresample \

  fi

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

  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
    ./configure --host="$CROSS_TRIPLE" \
                               --prefix="$_INST_" \
                               --disable-shared \
                               --enable-static \
                               --disable-soname-versions \
                               --disable-extra-programs \
                               --disable-doc || exit 1
  else
    ./configure \
                               --prefix="$_INST_" \
                               --disable-shared \
                               --enable-static \
                               --disable-soname-versions \
                               --disable-extra-programs \
                               --disable-doc || exit 1
  fi
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
  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
    ./configure --host="$CROSS_TRIPLE" \
              --prefix="$_INST_" \
              --disable-shared \
              --enable-static \
              --with-pic || exit 1
  else
    ./configure \
              --prefix="$_INST_" \
              --disable-shared \
              --enable-static \
              --with-pic || exit 1
  fi

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

  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"

    ./configure --target=arm64-linux-gcc \
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
  else
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
  fi
  make || exit 1
  make install
  unset CXXFLAGS
  unset CFLAGS

cd "$_HOME_"

fi
# ---------- vpx ---------


# --- NASM ---

if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
else
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

  if [ "$1""x" == "raspix" ]; then
    echo "*** RASPI ***"
    ./configure --host="$CROSS_TRIPLE" \
                                         --disable-asm \
                                         --prefix="$_INST_" \
                                         --disable-opencl \
                                         --enable-static \
                                         --disable-avs \
                                         --disable-cli \
                                         --enable-pic || exit 
  else
    ./configure \
                                         --enable-asm \
                                         --prefix="$_INST_" \
                                         --disable-opencl \
                                         --enable-static \
                                         --disable-avs \
                                         --disable-cli \
                                         --enable-pic || exit 
  fi
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
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
    echo "**** LOCAL BUILD ****"
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

autoreconf -fi

if [ "$1""x" == "raspix" ]; then
  echo "*** RASPI ***"
  ./configure --host="$CROSS_TRIPLE" \
     CXXFLAGS="$CXXFLAGS_ADDON" \
     CFLAGS="-fPIC $CFLAGS_ADDON $CFLAGS_MORE -DTOX_CAPABILITIES_ACTIVE $LOGG" \
    --prefix="$_INST_" \
    --disable-soname-versions \
    --disable-shared \
    --disable-testing \
    --disable-rt || exit 1
else
  if [ "$2""x" == "asanx" ]; then
    echo "***** ASAN *****"
    echo "***** ASAN *****"
    echo "***** ASAN *****"
    # enable H265 encoder
    h265_feature="yes"
    CFLAGS_ASAN="-fsanitize=address -fno-omit-frame-pointer -fsanitize-recover=address -static-libasan"
  else
    # enable H265 encoder also on the regular builds
    h265_feature="yes"
    CFLAGS_ASAN=""
  fi
  ./configure \
     CXXFLAGS="$CXXFLAGS_ADDON" \
     CFLAGS="-fPIC $CFLAGS_ADDON $CFLAGS_MORE -DTOX_CAPABILITIES_ACTIVE $LOGG $CFLAGS_ASAN" \
    --prefix="$_INST_" \
    --enable-feature-h265="$h265_feature" \
    --disable-soname-versions \
    --disable-shared \
    --disable-testing \
    --disable-rt || exit 1
fi

    make || exit 1
    make install
  unset CXXFLAGS
  unset CFLAGS

if [ "$2""x" == "asanx" ]; then
    # check if we actually have ASAN symbols in the library files
    nm $_INST_/lib/libtoxcore.a | grep -i asan || exit 1
    nm $_INST_/lib/libtoxav.a | grep -i asan || exit 1
    nm $_INST_/lib/libtoxencryptsave.a | grep -i asan || exit 1
fi

cd "$_HOME_"

# ---------- c-toxcore ---------



