#! /bin/bash

_HOME2_=$(dirname "$0")
export _HOME2_
_HOME_=$(cd "$_HOME2_" || exit;pwd)
export _HOME_

echo "$_HOME_"
cd "$_HOME_" || exit

if [ "$1""x" == "buildx" ]; then
    docker build -f Dockerfile_debx -t trifa_materia_004a_deb12 .
    exit 0
fi


build_for='debian:12
'


for system_to_build_for in $build_for ; do

    system_to_build_for_orig="$system_to_build_for"
    system_to_build_for=$(echo "$system_to_build_for_orig" 2>/dev/null|tr ':' '_' 2>/dev/null)"_linux"

    cd "$_HOME_"/ || exit
    mkdir -p "$_HOME_"/"$system_to_build_for"/

    # rm -Rf $_HOME_/"$system_to_build_for"/script 2>/dev/null
    # rm -Rf $_HOME_/"$system_to_build_for"/workspace 2>/dev/null

    mkdir -p "$_HOME_"/"$system_to_build_for"/artefacts
    mkdir -p "$_HOME_"/"$system_to_build_for"/script
    mkdir -p "$_HOME_"/"$system_to_build_for"/workspace

    ls -al "$_HOME_"/"$system_to_build_for"/

    rsync -a ../ --exclude=.localrun "$_HOME_"/"$system_to_build_for"/workspace/data
    chmod a+rwx -R "$_HOME_"/"$system_to_build_for"/workspace/data

    echo '#! /bin/bash


#------------------------

pwd
ls -al
id -a

mkdir -p /workspace/data/jni-c-toxcore/
cd /workspace/data/jni-c-toxcore/ || exit 1

ls -al

ls -al /workdocker/

if [ "$1""x" == "localx" ]; then
    cd /workdocker/ ; /workdocker/deps_linux.sh local "" "" "" phase2 || exit 1
else
    cd /workdocker/ ; /workdocker/deps_linux.sh "" "" "" "" phase2 || exit 1
fi


mkdir -p /workspace/data/jni-c-toxcore/inst/
cp -a /workdocker/inst/* /workspace/data/jni-c-toxcore/inst/

mkdir -p /workspace/data/jni-c-toxcore/lib/
cp -av /workdocker/lib/* /workspace/data/jni-c-toxcore/lib/

cd /workspace/data/jni-c-toxcore/ || exit 1

if [ "$1""x" == "localx" ]; then
    ../circle_scripts/java_jni_lib_linux.sh local || exit 1
else
    ../circle_scripts/java_jni_lib_linux.sh || exit 1
fi


#------------------------


' > "$_HOME_"/"$system_to_build_for"/script/run.sh

if [ "$1""x" == "localx" ]; then
    echo " ******* LOCAL *******"
    echo " ******* LOCAL *******"
    echo " ******* LOCAL *******"
    docker run -ti --rm \
      -v "$_HOME_"/"$system_to_build_for"/artefacts:/artefacts \
      -v "$_HOME_"/"$system_to_build_for"/script:/script \
      -v "$_HOME_"/"$system_to_build_for"/workspace:/workspace \
      -v "$_HOME_"/"$system_to_build_for"/c-toxcore:/c-toxcore \
      --net=host \
     "trifa_materia_004a_deb12" \
     /bin/sh -c "apk add bash >/dev/null 2>/dev/null; /bin/bash /script/run.sh local"
     if [ $? -ne 0 ]; then
        echo "** ERROR **:$system_to_build_for_orig"
        exit 1
     else
        echo "--SUCCESS--:$system_to_build_for_orig"
     fi
else
    docker run -ti --rm \
      -v "$_HOME_"/"$system_to_build_for"/artefacts:/artefacts \
      -v "$_HOME_"/"$system_to_build_for"/script:/script \
      -v "$_HOME_"/"$system_to_build_for"/workspace:/workspace \
      --net=host \
     "trifa_materia_004a_deb12" \
     /bin/sh -c "apk add bash >/dev/null 2>/dev/null; /bin/bash /script/run.sh"
     if [ $? -ne 0 ]; then
        echo "** ERROR **:$system_to_build_for_orig"
        exit 1
     else
        echo "--SUCCESS--:$system_to_build_for_orig"
     fi
fi
done

