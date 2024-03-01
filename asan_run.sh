#! /bin/bash

#####################################
#
# config:
#
java_17_or_newer_dir=/usr/lib/jvm/java-17-openjdk-amd64/
asan_runtime_lib=/usr/lib/x86_64-linux-gnu/libasan.so.6.0.0
#
#
#
#####################################



p=$(pwd)
h=$(echo $HOME)

if [ "$1""x" == "buildx" ]; then
  cp -av resources/common/libffmpeg_av_jni.so__ASAN resources/common/libffmpeg_av_jni.so
  cp -av resources/common/libjni_notifications.so__ASAN resources/common/libjni_notifications.so
  cp -av resources/common/libjni-c-toxcore.so__ASAN resources/common/libjni-c-toxcore.so
  ./gradlew -Dorg.gradle.java.home="$java_17_or_newer_dir" packageDistributionForCurrentOS
  ./gradlew -Dorg.gradle.java.home="$java_17_or_newer_dir" packageUberJarForCurrentOS
fi

# options to log GC stats, and limit memusage a lot
# -XX:+UseG1GC -XX:MinHeapFreeRatio=1 -XX:MaxHeapFreeRatio=1

LD_PRELOAD="$asan_runtime_lib" \
"$java_17_or_newer_dir"/bin/java -Dapple.awt.application.name=TRIfA \
-Dcom.apple.mrj.application.apple.menu.about.name=TRIfA \
-Dcompose.application.configure.swing.globals=true \
-Dcompose.application.resources.dir="$p"/build/compose/tmp/prepareAppResources \
-Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant \
-cp "$p"/build/compose/jars/trifa_material-linux-x64-1.0.22.jar \
MainKt

