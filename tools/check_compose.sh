#! /bin/bash

r1='https://github.com/JetBrains/compose-multiplatform'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" \
    | cut --delimiter='/' --fields=3 | tail -1|sed -e 's#^v##')

echo "$ver"

sed -i -e 's#^compose.version=.*$#compose.version='"$ver"'#' gradle.properties

