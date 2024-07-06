#! /bin/bash

r1='https://github.com/FFmpeg/FFmpeg'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '^v' | grep -v '\-dev'|tail --lines=1)

echo "__VERSIONUPDATE__:""$ver"

cd ./circle_scripts/

grep -ril 'FFMPEG_VERSION='|xargs -L1 sed -i -e 's#FFMPEG_VERSION=".*"#FFMPEG_VERSION="'"$ver"'"#'

