#! /bin/bash
base_url='https://github.com/zoff99/trifa_material/releases/download/nightly/'

file_mac_arm64='libjni-c-toxcore_arm64.jnilib'
file_mac='libjni-c-toxcore.jnilib'
file_mac_noise_arm64='libjni-c-toxcore_arm64_noise.jnilib'
file_mac_noise='libjni-c-toxcore_noise.jnilib'
file_win='jni-c-toxcore.dll'
file_win_noise='jni-c-toxcore_noise.dll'
file_lix='libjni-c-toxcore.so'
file_asan_lix='libjni-c-toxcore.so__ASAN'
file_lix_noise='libjni-c-toxcore_noise.so'
file_rpi='libjni-c-toxcore_raspi.so'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../resources/common/"

cd "$basedir"
wget "$base_url""$file_mac_arm64" -O "$file_mac_arm64"
# wget "$base_url""$file_mac_noise_arm64" -O "$file_mac_noise_arm64"
wget "$base_url""$file_mac" -O "$file_mac"
# wget "$base_url""$file_mac_noise" -O "$file_mac_noise"
wget "$base_url""$file_win" -O "$file_win"
# wget "$base_url""$file_mac_noise" -O "$file_mac_noise"
wget "$base_url""$file_lix" -O "$file_lix"
wget "$base_url""$file_asan_lix" -O "$file_asan_lix"
# wget "$base_url""$file_lix_noise" -O "$file_lix_noise"
wget "$base_url""$file_rpi" -O "$file_rpi"
