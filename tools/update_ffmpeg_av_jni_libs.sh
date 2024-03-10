#! /bin/sh
base_url='https://github.com/zoff99/ffmpeg_av_jni/releases/download/nightly/'

file_mac='libffmpeg_av_jni.jnilib'
file_win='ffmpeg_av_jni.dll'
file_lix='libffmpeg_av_jni.so'
file_rpi='libffmpeg_av_jni_raspi.so'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../resources/common/"

cd "$basedir"
wget "$base_url""$file_mac" -O "$file_mac"
wget "$base_url""$file_win" -O "$file_win"
wget "$base_url""$file_lix" -O "$file_lix"
wget "$base_url""$file_rpi" -O "$file_rpi"
