#! /bin/bash

_HOME_="$(pwd)"
export _HOME_

_HOME2_=$(dirname $0)
export _HOME2_
_HOME3_=$(cd $_HOME2_;pwd)
export _HOME3_

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

# MAKEFLAGS=j$(nproc)
# export MAKEFLAGS

WGET_OPTIONS="--timeout=10"
export WGET_OPTIONS

# ----------- config ------------

echo "--------------"
ls -al $_INST_/lib/libtoxcore.a
echo "--------------"


## ---------------------------
sudo mkdir -p /Users/runner/
sudo chmod a+rwx /Users/runner/
cd /Users/runner/
git clone https://github.com/zoff99/ToxAndroidRefImpl
cd /Users/runner/ToxAndroidRefImpl/jni-c-toxcore/
pwd
ls -al
## ---------------------------

echo "JAVADIR1------------------"
find /usr -name 'jni.h'
echo "JAVADIR1------------------"

# /usr/local/Cellar/openjdk/15.0.2/include/jni.h
# /usr/local/Cellar/openjdk/15.0.2/include/jni_md.h

# /usr/local/Cellar/openjdk/15.0.2/libexec/openjdk.jdk/Contents/Home/include/jni.h
# /usr/local/Cellar/openjdk/15.0.2/libexec/openjdk.jdk/Contents/Home/include/darwin/jni_md.h

echo "JAVADIR2------------------"
find /usr -name 'jni_md.h'
echo "JAVADIR2------------------"

#dirname $(find /usr -name 'jni.h' 2>/dev/null|grep -v 'libavcodec'|head -1) > /tmp/xx1
#dirname $(find /usr -name 'jni_md.h' 2>/dev/null|head -1) > /tmp/xx2
#export JAVADIR1=$(cat /tmp/xx1)
#export JAVADIR2=$(cat /tmp/xx2)

#export JAVADIR1="/usr/local/Cellar/openjdk/16.0.1/include"
#export JAVADIR2="/usr/local/Cellar/openjdk/16.0.1/include"

#if [ ! -e "$JAVADIR1" ]; then
#    mkdir -p "$_INST_/jinclude/"
#    cp -av /Users/travis/build/zoff99/java_toxclient_example/circle_scripts/jni_md.h "$_INST_/jinclude/"
#    cp -av /Users/travis/build/zoff99/java_toxclient_example/circle_scripts/jni.h "$_INST_/jinclude/"
#    export JAVADIR1="$_INST_/jinclude"
#    export JAVADIR2="$_INST_/jinclude"
#fi

#echo "JAVADIR1:""$JAVADIR1"
#echo "JAVADIR2:""$JAVADIR2"

mkdir -p "$_INST_/jinclude/"
cp -av "$_HOME3_"/jni_md.h "$_INST_/jinclude/"
cp -av "$_HOME3_"/jni.h "$_INST_/jinclude/"

export CFLAGS=" -fPIC -std=gnu99 -I$_INST_/include/ -I$_INST_/jinclude/ -L$_INST_/lib -fstack-protector-all "

gcc $CFLAGS \
-Wall \
-DJAVA_LINUX \
$C_FLAGS $CXX_FLAGS $LD_FLAGS \
-D_FILE_OFFSET_BITS=64 -D__USE_GNU=1 \
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
-o libjni-c-toxcore.jnilib || exit 1

ls -al libjni-c-toxcore.jnilib || exit 1

otool -L libjni-c-toxcore.jnilib
pwd
file libjni-c-toxcore.jnilib

pwd
find . -name libjni-c-toxcore.jnilib


if [ -e /usr/bin/nasm ]; then
    pwd
    ls -hal libjni-c-toxcore.jnilib
    echo "-------- DONE --------"
else

if [ "$1""x" == "norunx" ]; then
    exit 0
fi

cp -av libjni-c-toxcore.jnilib /Users/runner/work/java_toxclient_example/java_toxclient_example/java_ref_client/app/src/main/java/ || exit 1
cp -av libjni-c-toxcore.jnilib /Users/runner/work/java_toxclient_example/java_toxclient_example/trifa_desktop/app/src/main/java/ || exit 1

# -------------- now compile the JNI lib ----------------------

export JAVA_HOME=$(/usr/libexec/java_home -v11)
java -version
$JAVA_HOME/bin/java -version
$JAVA_HOME/bin/javac -version

# --------- compile java example ---------
#cd /Users/runner/work/java_toxclient_example/java_toxclient_example/java_ref_client/app/src/main/java/
#$JAVA_HOME/bin/javac com/zoffcc/applications/trifa/ToxVars.java
#$JAVA_HOME/bin/javac com/zoffcc/applications/trifa/TRIFAGlobals.java
#$JAVA_HOME/bin/javac com/zoffcc/applications/trifa/MainActivity.java
#$JAVA_HOME/bin/javac com/zoffcc/applications/trifa/TrifaToxService.java
# --------- package java example ---------
#cd /Users/runner/work/java_toxclient_example/java_toxclient_example/java_ref_client/app/src/main/java/


# --------- compile java example ---------
cd /Users/runner/work/java_toxclient_example/java_toxclient_example/trifa_desktop/app/src/main/java/
$JAVA_HOME/bin/javac \
-classpath ".:json-20210307.jar:emoji-java-5.1.1.jar:sqlite-jdbc-3.34.0.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
./com/zoffcc/applications/trifa/EmojiFrame.java ./com/kevinnovate/jemojitable/EmojiTable.java ./com/zoffcc/applications/trifa/OperatingSystem.java ./com/zoffcc/applications/trifa/TRIFAGlobals.java ./com/zoffcc/applications/trifa/Table.java ./com/zoffcc/applications/trifa/OnConflict.java ./com/zoffcc/applications/trifa/CombinedFriendsAndConferences.java ./com/zoffcc/applications/trifa/OrmaDatabase.java ./com/zoffcc/applications/trifa/HelperGeneric.java ./com/zoffcc/applications/trifa/ConferencePeerCacheDB.java ./com/zoffcc/applications/trifa/PopupToxIDQrcode.java ./com/zoffcc/applications/trifa/Index.java ./com/zoffcc/applications/trifa/AudioFrame.java ./com/zoffcc/applications/trifa/Filetransfer.java ./com/zoffcc/applications/trifa/FullscreenToggleAction.java ./com/zoffcc/applications/trifa/Nullable.java ./com/zoffcc/applications/trifa/Column.java ./com/zoffcc/applications/trifa/FriendList.java ./com/zoffcc/applications/trifa/ConferenceMessage.java ./com/zoffcc/applications/trifa/SingleComponentAspectRatioKeeperLayout.java ./com/zoffcc/applications/trifa/HelperFriend.java ./com/zoffcc/applications/trifa/JPictureBox.java ./com/zoffcc/applications/trifa/FileDB.java ./com/zoffcc/applications/trifa/MainActivity.java ./com/zoffcc/applications/trifa/TrifaToxService.java ./com/zoffcc/applications/trifa/SettingsActivity.java ./com/zoffcc/applications/trifa/MessageListFragmentJ.java ./com/zoffcc/applications/trifa/AudioSelectOutBox.java ./com/zoffcc/applications/trifa/ByteBufferCompat.java ./com/zoffcc/applications/trifa/MessageListFragmentJInfo.java ./com/zoffcc/applications/trifa/AudioBar.java ./com/zoffcc/applications/trifa/HelperMessage.java ./com/zoffcc/applications/trifa/VideoOutFrame.java ./com/zoffcc/applications/trifa/ConferenceDB.java ./com/zoffcc/applications/trifa/Renderer_MessageList.java ./com/zoffcc/applications/trifa/FriendListFragmentJ.java ./com/zoffcc/applications/trifa/TrifaSetPatternActivity.java ./com/zoffcc/applications/trifa/AudioSelectionRenderer.java ./com/zoffcc/applications/trifa/Callstate.java ./com/zoffcc/applications/trifa/Toast.java ./com/zoffcc/applications/trifa/Log.java ./com/zoffcc/applications/trifa/HelperFiletransfer.java ./com/zoffcc/applications/trifa/PrimaryKey.java ./com/zoffcc/applications/trifa/RelayListDB.java ./com/zoffcc/applications/trifa/Renderer_FriendsAndConfsList.java ./com/zoffcc/applications/trifa/Message.java ./com/zoffcc/applications/trifa/ToxVars.java ./com/zoffcc/applications/trifa/VideoInFrame.java ./com/zoffcc/applications/trifa/ConferenceMessageListFragmentJ.java ./com/zoffcc/applications/trifa/HelperConference.java ./com/zoffcc/applications/trifa/AudioSelectInBox.java ./com/zoffcc/applications/trifa/ChatColors.java ./com/zoffcc/applications/trifa/Renderer_ConfMessageList.java ./com/zoffcc/applications/trifa/TRIFADatabaseGlobalsNew.java ./com/zoffcc/applications/trifa/HelperOSFile.java ./com/zoffcc/applications/trifa/Screenshot.java ./com/zoffcc/applications/trifa/BootstrapNodeEntryDB.java ./com/zoffcc/applications/trifa/HelperRelay.java ./com/github/sarxos/webcam/ds/ffmpegcli/FFmpegScreenDevice.java ./com/github/sarxos/webcam/ds/ffmpegcli/impl/VideoDeviceFilenameFilter.java ./com/github/sarxos/webcam/ds/ffmpegcli/FFmpegScreenDriver.java ./org/imgscalr/Scalr.java ./io/nayuki/qrcodegen/package-info.java ./io/nayuki/qrcodegen/DataTooLongException.java ./io/nayuki/qrcodegen/QrSegmentAdvanced.java ./io/nayuki/qrcodegen/BitBuffer.java ./io/nayuki/qrcodegen/QrCodeGeneratorWorker.java ./io/nayuki/qrcodegen/QrCode.java ./io/nayuki/qrcodegen/QrSegment.java ./io/nayuki/qrcodegen/QrCodeGeneratorDemo.java
# --------- package java example ---------

mkdir -p /Users/runner/work/artefacts/
tar -cvf /Users/runner/work/artefacts/install_macos.tar com assets i18n *.sh *.jnilib *.jar
# --------- run test java application ---------
$JAVA_HOME/bin/java  -Djava.library.path="." \
-classpath ".:json-20210307.jar:emoji-java-5.1.1.jar:sqlite-jdbc-3.34.0.jar:webcam-capture-0.3.12.jar:bridj-0.7.0.jar:slf4j-api-1.7.2.jar:flatlaf-1.0.jar" \
com.zoffcc.applications.trifa.MainActivity > trifa.log 2>&1 &
# --------- run test java application ---------
sleep 40
cat ./trifa.log|head -20
echo
echo
cat ./trifa.log | grep 'MyToxID:' | cut -d':' -f 8
echo
echo

screencapture -T 1 -x -t png /Users/runner/screen101.png &
sudo killall NotificationCenter &
screencapture -x -t png /Users/runner/screen102.png
screencapture -x -t png /Users/runner/screen103.png
sleep 5
screencapture -x -t png /Users/runner/screen104.png

fi
