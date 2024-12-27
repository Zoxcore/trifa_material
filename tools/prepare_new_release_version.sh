#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"

if [[ $(git status --porcelain --untracked-files=no) ]]; then
	echo "ERROR: git repo has changes."
	echo "please commit or cleanup the git repo."
	exit 1
else
	echo "git repo clean."
fi


./tools/check_release_jni_libs.sh
res_1=$?
if [ $res_1 -ne 0 ]; then
	echo "ERROR: JNI libs have some problem."
	exit 1
else
	echo "JNI lib are ok."
fi

f1="build.gradle.kts"
f2="asan_run.sh"

cur_m_version=$(cat "$f1" | grep 'version = "' | head -1 | \
	sed -e 's#^.*version = "##' | \
	sed -e 's#"$##')

echo "$cur_m_version"
next_m_version=$(echo "$cur_m_version"|awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')

echo "$next_m_version"


sed -i -e 's#version = ".*#version = "'"$next_m_version"'"#' "$f1"
sed -i -e 's#trifa_material-linux-x64-'"$cur_m_version"'.jar#trifa_material-linux-x64-'"$next_m_version"'.jar#' "$f2"


commit_message="new version ""$next_m_version"
tag_name='v'"$next_m_version"

git commit -m "$commit_message" "$f1" "$f2"
git tag -a "$tag_name" -m "$tag_name"




