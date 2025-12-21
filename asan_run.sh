#! /bin/bash

#####################################
#
# config:
#
java_17_or_newer_dir=/usr/lib/jvm/temurin-22-jdk-amd64/
asan_runtime_lib=$(ls -1 /usr/lib/x86_64-linux-gnu/libasan.s*0.0)
#
#
#
#####################################


# runReleaseDistributable

p=$(pwd)
h=$(echo $HOME)

if [ "$1""x" == "buildx" ]; then
  if [ "$2""x" != "1x" ]; then
    cp -av resources/common/libffmpeg_av_jni.so__ASAN resources/common/libffmpeg_av_jni.so
    cp -av resources/common/libjni_notifications.so__ASAN resources/common/libjni_notifications.so
    cp -av resources/common/libjni-c-toxcore.so__ASAN resources/common/libjni-c-toxcore.so
  fi
  ./gradlew -Dorg.gradle.java.home="$java_17_or_newer_dir" packageDistributionForCurrentOS
  ./gradlew -Dorg.gradle.java.home="$java_17_or_newer_dir" packageUberJarForCurrentOS
fi

# options to log GC stats, and limit memusage a lot
# -XX:+UseG1GC -XX:MinHeapFreeRatio=1 -XX:MaxHeapFreeRatio=1

if [ "$2""x" != "1x" ]; then
  export ASAN_OPTIONS="halt_on_error=false,detect_leaks=0"
  LD_PRELOAD="$asan_runtime_lib" \
    "$java_17_or_newer_dir"/bin/java -Dapple.awt.application.name=TRIfA \
    -Dcom.apple.mrj.application.apple.menu.about.name=TRIfA \
    -Dcompose.application.configure.swing.globals=true \
    -Dcompose.application.resources.dir="$p"/build/compose/tmp/prepareAppResources \
    -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant \
    -cp "$p"/build/compose/jars/trifa_material-linux-x64-1.0.59.jar \
    TrifaMainKt
else
  "$java_17_or_newer_dir"/bin/java -Dapple.awt.application.name=TRIfA \
    -Dcom.apple.mrj.application.apple.menu.about.name=TRIfA \
    -Dcompose.application.configure.swing.globals=true \
    -Dcompose.application.resources.dir="$p"/build/compose/tmp/prepareAppResources \
    -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant \
    -cp "$p"/build/compose/jars/trifa_material-linux-x64-1.0.59.jar \
    TrifaMainKt
fi
