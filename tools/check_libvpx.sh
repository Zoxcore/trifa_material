#! /bin/bash

r1='https://github.com/webmproject/libvpx'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1)

echo "__VERSIONUPDATE__:""$ver"

cd ./circle_scripts/
grep -ril 'VPX_VERSION='|xargs -L1 sed -i -e 's#VPX_VERSION=".*"#VPX_VERSION="'"$ver"'"#'

