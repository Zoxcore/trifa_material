#! /bin/bash

_HOME2_=$(dirname "$0")
export _HOME2_
_HOME_=$(cd "$_HOME2_" || exit;pwd)
export _HOME_

echo "$_HOME_"
cd "$_HOME_" || exit


build_for='ubuntu:20.04
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


pkgs_Ubuntu_16_04="
    :u:
    ca-certificates
    libconfig-dev
    wget
    unzip
    zip
    automake
    autotools-dev
    build-essential
    check
    checkinstall
    libtool
    pkg-config
    rsync
    git
    gdb
    nano
    yasm
    nasm
    sudo
    cmake
    openjdk-8-jdk-headless
"

pkgs_Ubuntu_18_04="
    :u:
    ca-certificates
    libconfig-dev
    wget
    unzip
    zip
    automake
    autotools-dev
    build-essential
    check
    checkinstall
    libtool
    pkg-config
    rsync
    git
    gdb
    nano
    yasm
    nasm
    sudo
    cmake
    openjdk-17-jdk-headless
"

pkgs_Ubuntu_20_04="$pkgs_Ubuntu_18_04"
pkgs_DebianGNU_Linux_9="$pkgs_Ubuntu_18_04"
pkgs_DebianGNU_Linux_10="$pkgs_Ubuntu_18_04"

export DEBIAN_FRONTEND=noninteractive


os_release=$(cat /etc/os-release 2>/dev/null|grep "PRETTY_NAME=" 2>/dev/null|cut -d"=" -f2)
echo "using /etc/os-release"
system__=$(cat /etc/os-release 2>/dev/null|grep "^NAME=" 2>/dev/null|cut -d"=" -f2|tr -d "\""|sed -e "s#\s##g")
version__=$(cat /etc/os-release 2>/dev/null|grep "^VERSION_ID=" 2>/dev/null|cut -d"=" -f2|tr -d "\""|sed -e "s#\s##g")

echo "compiling on: $system__ $version__"

pkgs_name="pkgs_"$(echo "$system__"|tr "." "_"|tr "/" "_")"_"$(echo $version__|tr "." "_"|tr "/" "_")
echo "PKG:-->""$pkgs_name""<--"

for i in ${!pkgs_name} ; do
    if [[ ${i:0:3} == ":u:" ]]; then
        echo "apt-get update"
        apt-get update > /dev/null 2>&1
    elif [[ ${i:0:3} == ":c:" ]]; then
        cmd=$(echo "${i:3}"|sed -e "s#\\\s# #g")
        echo "$cmd"
        $cmd > /dev/null 2>&1
    else
        echo "apt-get install -y --force-yes ""$i"
        apt-get install -qq -y --force-yes $i > /dev/null 2>&1
    fi
done

#------------------------

pwd
ls -al
id -a

mkdir -p /workspace/data/jni-c-toxcore/
cd /workspace/data/jni-c-toxcore/ || exit 1

ls -al

set -x

if [ "$1""x" == "localx" ]; then
    ../circle_scripts/deps_linux.sh local || exit 1
    ../circle_scripts/java_jni_lib_linux.sh local || exit 1
else
    ../circle_scripts/deps_linux.sh || exit 1
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
     "$system_to_build_for_orig" \
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
     "$system_to_build_for_orig" \
     /bin/sh -c "apk add bash >/dev/null 2>/dev/null; /bin/bash /script/run.sh"
     if [ $? -ne 0 ]; then
        echo "** ERROR **:$system_to_build_for_orig"
        exit 1
     else
        echo "--SUCCESS--:$system_to_build_for_orig"
     fi
fi
done

