#!/bin/bash
# Data files

export PATH="$PATH":.

BASE_FILE="topology"
TOPO_FILE="$BASE_FILE.data"
PREFIX_FILE="prefix.data"
ROUTE_FILE="$TOPO_FILE.route"
AS_FILE="$BASE_FILE.aslist"

WGET='wget --timeout=10 -N'
ARCH=`uname -m`
SCRIPT_URL="https://bitbucket.org/romoore/gnrs/raw/master/script/"
DWNLD_URL="https://bitbucket.org/romoore/gnrs/downloads/"

AS_UNIQ='as-uniq.pl'
DEL_GEN_CLT='click_delay_gen_client.pl'
DEL_GEN_SRV='click_delay_gen_serv.pl'
SPG='spg.i386'
if [ ${ARCH} == 'x86_64' ]; then
	SPG='spg.amd64';
fi

echo "Downloading required files"
$WGET $DWNLD_URL$SPG
chmod +x $SPG
$WGET $SCRIPT_URL$AS_UNIQ
chmod +x $AS_UNIQ
$WGET $SCRIPT_URL$DEL_GEN_CLT
chmod +x $DEL_GEN_CLT
$WGET $SCRIPT_URL$DEL_GEN_SRV
chmod +x $DEL_GEN_SRV

echo "Generating files"
$SPG $TOPO_FILE
rm AS_arr.data
$AS_UNIQ $TOPO_FILE $AS_FILE
$DEL_GEN_SRV $AS_FILE $ROUTE_FILE
$DEL_GEN_CLT $AS_FILE $ROUTE_FILE

