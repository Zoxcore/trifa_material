#! /bin/bash

ver="v1.0.20"

cd ../circle_scripts/
grep -ril 'SODIUM_VERSION='|xargs -L1 sed -i -e 's#SODIUM_VERSION=".*"#SODIUM_VERSION="'"$ver"'"#'

