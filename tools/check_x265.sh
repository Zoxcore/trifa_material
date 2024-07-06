#! /bin/bash

r1='https://bitbucket.org/multicoreware/x265_git.git'
ver=$(git ls-remote --sort=v:refname "$r1" refs/heads/stable \
    |awk '{print $1}' | tail --lines=1)

echo "__VERSIONUPDATE__:""$ver"

cd ./circle_scripts/
grep -ril '_X265_VERSION_='|xargs -L1 sed -i -e 's#_X265_VERSION_=".*"#_X265_VERSION_="'"$ver"'"#'

