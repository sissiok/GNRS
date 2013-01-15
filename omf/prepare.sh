#!/bin/bash
# Data files

export PATH="$PATH":.

BASE_FILE="topology"
TOPO_FILE="$BASE_FILE.data"
PREFIX_FILE="prefix.data"
ROUTE_FILE="$TOPO_FILE.route"
AS_FILE="$BASE_FILE.aslist"
BIND_FILE="$BASE_FILE.bind"
BDB_FILE="berkeleydb.xml"
MAP_FILE="map-ipv4.xml"
UPLOAD_DIR="$BASE_FILE/"

WGET='wget --timeout=10 -N'
ARCH=`uname -m`
SCRIPT_URL="https://bitbucket.org/romoore/gnrs/raw/master/script/"
DWNLD_URL="https://bitbucket.org/romoore/gnrs/downloads/"

AS_UNIQ='as-uniq.pl'
DEL_GEN_CLT='click_delay_gen_client.pl'
DEL_GEN_SRV='click_delay_gen_serv.pl'
AS_BIND='as-binding.pl'
SPG='spg.i386'
if [ ${ARCH} == 'x86_64' ]; then
	SPG='spg.amd64';
fi

echo "Downloading required files"
$WGET $DWNLD_URL$MAP_FILE
$WGET $DWNLD_URL$BDB_FILE
$WGET $DWNLD_URL$SPG
chmod +x $SPG
$WGET $SCRIPT_URL$AS_UNIQ
chmod +x $AS_UNIQ
$WGET $SCRIPT_URL$DEL_GEN_CLT
chmod +x $DEL_GEN_CLT
$WGET $SCRIPT_URL$DEL_GEN_SRV
chmod +x $DEL_GEN_SRV
$WGET $SCRIPT_URL$AS_BIND
chmod +x $AS_BIND

echo "Generating files"
$SPG $TOPO_FILE
$AS_UNIQ $TOPO_FILE $AS_FILE
$AS_BIND $AS_FILE $BIND_FILE
$DEL_GEN_SRV $AS_FILE $ROUTE_FILE
$DEL_GEN_CLT $AS_FILE $ROUTE_FILE

echo "Creating upload directory"
mkdir -p $UPLOAD_DIR
mv as_*.dat $UPLOAD_DIR
cp $PREFIX_FILE $UPLOAD_DIR/prefixes.ipv4
mv $BDB_FILE $UPLOAD_DIR
mv $MAP_FILE $UPLOAD_DIR
mv $BIND_FILE $UPLOAD_DIR
cp "*.trace" $UPLOAD_DIR

tar -czvf $BASE_FILE.tgz $UPLOAD_DIR

echo "Removing temporary files"
rm $AS_UNIQ
rm $DEL_GEN_CLT
rm $DEL_GEN_SRV
rm $SPG
rm AS_arr.data
rm $AS_FILE
rm $ROUTE_FILE
rm -rf $UPLOAD_DIR

echo "Prepared files are located in $BASE_FILE.tgz"
