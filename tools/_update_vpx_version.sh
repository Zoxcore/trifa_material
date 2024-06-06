#! /bin/bash

ver="v1.14.1"

cd ../circle_scripts/
grep -ril 'VPX_VERSION='|xargs -L1 sed -i -e 's#VPX_VERSION=".*"#VPX_VERSION="'"$ver"'"#'

