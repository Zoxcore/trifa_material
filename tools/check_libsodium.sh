#! /bin/bash

r1='https://github.com/jedisct1/libsodium'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | grep '\-RELEASE' | tail --lines=1|sed -e 's#-RELEASE$##')

echo "__VERSIONUPDATE__:""$ver"

cd ./circle_scripts/
grep -ril 'SODIUM_VERSION='|xargs -L1 sed -i -e 's#SODIUM_VERSION=".*"#SODIUM_VERSION="'"$ver"'"#'

