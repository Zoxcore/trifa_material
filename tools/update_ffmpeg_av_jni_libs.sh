#! /bin/bash
base_url='https://github.com/zoff99/ffmpeg_av_jni/releases/download/nightly/'

file_mac='libffmpeg_av_jni.jnilib'
file_arm_mac='libffmpeg_av_jni_arm64.jnilib'
file_win='ffmpeg_av_jni.dll'
file_lix='libffmpeg_av_jni.so'
file_asan_lix='libffmpeg_av_jni.so__ASAN'
file_rpi='libffmpeg_av_jni_raspi.so'

java_source_url_01='https://raw.githubusercontent.com/zoff99/ffmpeg_av_jni/refs/heads/master/com/zoffcc/applications/ffmpegav/AVActivity.java'
java_source_url_02='https://raw.githubusercontent.com/zoff99/ffmpeg_av_jni/refs/heads/master/com/zoffcc/applications/ffmpegav/Log.java'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../resources/common/"

cd "$basedir"
wget "$base_url""$file_mac" -O "$file_mac"
wget "$base_url""$file_arm_mac" -O "$file_arm_mac"
wget "$base_url""$file_win" -O "$file_win"
wget "$base_url""$file_lix" -O "$file_lix"
wget "$base_url""$file_asan_lix" -O "$file_asan_lix"
wget "$base_url""$file_rpi" -O "$file_rpi"

cd "$_HOME_"/../
wget "$java_source_url_01" -O ./src/main/java/com/zoffcc/applications/ffmpegav/AVActivity.java
wget "$java_source_url_02" -O ./src/main/java/com/zoffcc/applications/ffmpegav/Log.java

