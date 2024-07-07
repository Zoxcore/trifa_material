#! /bin/bash

r1='https://github.com/JetBrains/compose-multiplatform'
ver=$(git ls-remote --refs --sort='v:refname' --tags "$r1" 2>/dev/null \
    | cut --delimiter='/' --fields=3 2>/dev/null| tail -1 2>/dev/null|sed -e 's#^v##' 2>/dev/null)

ver_base=$(echo "$ver"|cut -d'-' -f1)
echo "$ver_base"
ver_other=$(git ls-remote --refs --sort='v:refname' --tags "$r1" 2>/dev/null | cut --delimiter='/' --fields=3 2>/dev/null| \
 grep -e "${ver_base}"'-alph' -e "${ver_base}"'-beta' -e "${ver_base}"'-rc'| tail -1 2>/dev/null|sed -e 's#^v##' 2>/dev/null)
echo "$ver_other"

if [ "$ver_other""x" != "x" ]; then
    ver="$ver_other"
fi

echo "__VERSIONUPDATE__:""$ver"

sed -i -e 's#^compose.version=.*$#compose.version='"$ver"'#' gradle.properties

