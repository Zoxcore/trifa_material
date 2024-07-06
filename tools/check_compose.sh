#! /bin/bash

r1='https://github.com/JetBrains/compose-multiplatform'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" 2>/dev/null \
    | cut --delimiter='/' --fields=3 2>/dev/null| tail -1 2>/dev/null|sed -e 's#^v##' 2>/dev/null)

echo "__VERSIONUPDATE__:""$ver"

sed -i -e 's#^compose.version=.*$#compose.version='"$ver"'#' gradle.properties

