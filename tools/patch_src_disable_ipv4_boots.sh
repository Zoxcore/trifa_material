#! /bin/bash

patch_file="src/main/java/com/zoffcc/applications/sorm/BootstrapNodeEntryDB.java"

ips=$(cat src/main/java/com/zoffcc/applications/sorm/BootstrapNodeEntryDB.java |grep BootstrapNodeEntryDB_|\
grep -v '"[a-z]' |grep -v ':'|sed -e 's#^.*num_, "##g'|grep -v 'boolean udp_node_,'|awk '{print $1}'|\
sed -e 's#".*$##'|sort|uniq |sort)

hostnames=$(cat src/main/java/com/zoffcc/applications/sorm/BootstrapNodeEntryDB.java |grep BootstrapNodeEntryDB_|\
grep '"[a-z]'|sed -e 's#^.*num_, "##g'|grep -v 'boolean udp_node_,'|awk '{print $1}'|\
sed -e 's#".*$##'|sort|uniq |sort)

for i in $(echo $ips) ; do
    cat "$patch_file" | grep -v "$i" > a.txt
    cp a.txt "$patch_file"
    rm -f a.txt
done

for i in $(echo $hostnames) ; do
    cat "$patch_file" | grep -v "$i" > a.txt
    cp a.txt "$patch_file"
    rm -f a.txt
done

