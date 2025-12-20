#! /bin/bash

debfile="$1"
workdir="./tmp_work/"


postinst_cmd1='xdg-desktop-menu install /opt/trifa-material/lib/trifa-material-trifa_material.desktop'
# postinst_cmd2='xdg-mime install --novendor /opt/trifa-material/lib/app/resources/trifa_material.xml'

prerm_cmd1='xdg-desktop-menu uninstall /opt/undereat-material/lib/undereat-material-undereat_material.desktop'
# prerm_cmd2='xdg-mime uninstall /opt/trifa-material/lib/app/resources/trifa_material.xml'

mkdir -p "$workdir"
cp -av "$debfile" "$workdir"

cd "$workdir" || exit 1

ls -al
ar -x "$debfile"
ls -al

use_xz=0
if [ -e 'control.tar.xz' ] ; then
    use_xz=1
    echo "using compression: XZ"
else
    echo "using compression: ZSTD"
fi

if [ "$use_xz" == "1" ] ; then
    tar -xvf control.tar.xz || exit 1
else
    tar --use-compress-program=unzstd -xvf control.tar.zst || exit 1
fi

# postinst
## sed -i -e 's#^xdg-desktop-menu.*$#'"$postinst_cmd1"'\n'"$postinst_cmd2"'#g' postinst
# prerm
## sed -i -e 's#^xdg-desktop-menu.*$#'"$prerm_cmd1"'\n'"$prerm_cmd2"'#g' prerm
# control
sed -i -e 's#^Depends: .*$#Depends: libc6, libfontconfig1, libfreetype6, xdg-utils, zlib1g#g' control

if [ "$use_xz" == "1" ] ; then
    rm -f control.tar.xz
    tar --owner 0 --group 0 -cJvf control.tar.xz control postinst postrm preinst prerm || exit 1
else
    rm -f control.tar.zst
    tar --use-compress-program=zstd --owner 0 --group 0 -cvf control.tar.zst control postinst postrm preinst prerm || exit 1
fi

rm -f control postinst postrm preinst prerm

mkdir -p d_/
cd d_/ || exit 1

if [ "$use_xz" == "1" ] ; then
    tar -xvf ../data.tar.xz || exit 1
else
    tar --use-compress-program=unzstd -xvf ../data.tar.zst || exit 1
fi

desktop_file="./opt/trifa-material/lib/trifa-material-trifa_material.desktop"

cat "$desktop_file"

# sed -i -e 's#Exec=/opt/trifa-material/bin/trifa_material#Exec=/opt/trifa-material/bin/trifa_material %U#' "$desktop_file"
sed -i -e 's#Comment=.*$#Comment=TRIfA Material Tox Client#' "$desktop_file"
sed -i -e 's#Name=.*$#Name=TRIfA Material#' "$desktop_file"
sed -i -e 's#MimeType=.*$#MimeType=x-scheme-handler/tox;#' "$desktop_file"
sed -i -e 's#Categories=.*$#Categories=InstantMessaging;Network;#' "$desktop_file"

echo 'StartupWMClass=TrifaMainKt' >> "$desktop_file"

cat "$desktop_file"

if [ "$use_xz" == "1" ] ; then
    xz --decompress ../data.tar.xz || exit 1
else
    unzstd ../data.tar.zst || exit 1
fi

tar --delete -vf ../data.tar "$desktop_file" || exit 1

echo "checking ..."
tar -tvf ../data.tar | grep '\.desktop'
echo "checking ... DONE"

tar --owner 0 --group 0 -rvf ../data.tar "$desktop_file" || exit 1

echo "checking ..."
tar -tvf ../data.tar | grep '\.desktop'
echo "checking ... DONE"

if [ "$use_xz" == "1" ] ; then
    rm -f ../data.tar.xz
    xz --compress ../data.tar || exit 1
else
    rm -f ../data.tar.zst
    zstd ../data.tar || exit 1
fi

echo "checking ..."
ls -al ../
echo "checking ... DONE"

cd ../ && rm -Rf d_/

if [ "$use_xz" == "1" ] ; then
    ar rc final_pkg.deb debian-binary control.tar.xz data.tar.xz || exit 1
else
    ar rc final_pkg.deb debian-binary control.tar.zst data.tar.zst || exit 1
fi

cp -av final_pkg.deb ../ || exit 1

cd ../ && rm -Rf "$workdir"


