#! /bin/bash

r1='https://code.videolan.org/videolan/x264.git'
ver=$(git ls-remote --sort=v:refname "$r1" refs/heads/stable \
    |awk '{print $1}' | tail --lines=1)

echo "$ver"

cd ./circle_scripts/
grep -ril '_X264_VERSION_='|xargs -L1 sed -i -e 's#_X264_VERSION_=".*"#_X264_VERSION_="'"$ver"'"#'

