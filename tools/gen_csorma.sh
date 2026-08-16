#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

cd "$basedir"
cd ./sorma2/

java \
-classpath ".:sorma2.jar" \
com/zoffcc/applications/sorm/Generator "gen"

echo "#############"
echo "#############"
echo ""
echo "have a look in ${basedir}/sorma2/gen/com/zoffcc/applications/sorm/ for the generated java source files"
echo "and ${basedir}/src/main/java/com/zoffcc/applications/sorm/ for the files used in the application"
echo "#############"
echo "#############"

files="
BootstrapNodeEntryDB.java
Column.java
ConferenceDB.java
ConferenceMessage.java
FileDB.java
Filetransfer.java
FriendList.java
GroupDB.java
GroupMessage.java
Index.java
Log.java
Message.java
Nullable.java
OnConflict.java
PrimaryKey.java
RelayListDB.java
Table.java
TRIFADatabaseGlobalsNew.java
"

for i in $files ; do
    cp -v "$basedir"/sorma2/gen/com/zoffcc/applications/sorm/"$i" "$basedir"/src/main/java/com/zoffcc/applications/sorm/
done

