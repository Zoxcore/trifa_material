#! /bin/bash

ver="v1.5.2"

cd ../circle_scripts/
grep -ril 'OPUS_VERSION='|xargs -L1 sed -i -e 's#OPUS_VERSION=".*"#OPUS_VERSION="'"$ver"'"#'

