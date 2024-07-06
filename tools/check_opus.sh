#! /bin/bash

r1='https://github.com/xiph/opus'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep -v '\-' | tail --lines=1)

echo "__VERSIONUPDATE__:""$ver"

cd ./circle_scripts/
grep -ril 'OPUS_VERSION='|xargs -L1 sed -i -e 's#OPUS_VERSION=".*"#OPUS_VERSION="'"$ver"'"#'

