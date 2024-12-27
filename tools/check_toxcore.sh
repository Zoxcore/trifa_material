#! /bin/bash
url='https://github.com/zoff99/c-toxcore'

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
ver=$(git ls-remote "$url" HEAD 2>/dev/null | awk '{print $1}' 2>/dev/null)

echo "latest toxcore commit hash:""$ver"

if [ "y""${ver}""x" != "yx" ]; then
 echo "$ver" > used_toxcore_commit.sha.txt
 echo "__VERSIONUPDATE__:""$ver"
else
 :
 # no version info, there must have been some error
fi

