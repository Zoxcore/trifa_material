#! /bin/bash

ver="n7.0.1"

cd ../circle_scripts/

grep -ril 'FFMPEG_VERSION='|xargs -L1 sed -i -e 's#FFMPEG_VERSION=".*"#FFMPEG_VERSION="'"$ver"'"#'

