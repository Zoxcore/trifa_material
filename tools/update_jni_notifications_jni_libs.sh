#! /bin/bash
base_url='https://github.com/zoff99/jni_notifications/releases/download/nightly/'

file_mac='libjni_notifications.jnilib'
file_lix='libjni_notifications.so'
file_asan_lix='libjni_notifications.so__ASAN'
file_lix_rust='libjni_notifications_rs.so'
file_rpi='libjni_notifications_raspi.so'

java_source_url_01='https://raw.githubusercontent.com/zoff99/jni_notifications/refs/heads/master/com/zoffcc/applications/jninotifications/NTFYActivity.java'
java_source_url_02='https://raw.githubusercontent.com/zoff99/jni_notifications/refs/heads/master/com/zoffcc/applications/jninotifications/Log.java'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../resources/common/"

cd "$basedir"
wget "$base_url""$file_mac" -O "$file_mac"
wget "$base_url""$file_lix" -O "$file_lix"
wget "$base_url""$file_asan_lix" -O "$file_asan_lix"
wget "$base_url""$file_lix_rust" -O "$file_lix_rust"
wget "$base_url""$file_rpi" -O "$file_rpi"

cd "$_HOME_"/../
wget "$java_source_url_01" -O ./src/main/java/com/zoffcc/applications/jninotifications/NTFYActivity.java
wget "$java_source_url_02" -O ./src/main/java/com/zoffcc/applications/jninotifications/Log.java

