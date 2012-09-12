#!/bin/bash

config_file=$1
self_ip=$2
interface=$3 # e.g, eth0 or wlan0
srvrs_file=$4

GNRSD_BIN="/usr/local/mobilityfirst/bin/gnrsd"
GNRSD_LOG="/var/log/gnrsd.log"

export LD_LIBRARY_PATH=/usr/local/lib

#monitor defaults
#GNRSD_MON_LOG=/var/log/mf-gnrs-mon.log
#GNRSD_MON_OML_CONFIG=/usr/local/mobilityfirst/conf/gnrs-oml-config.xml
#GNRSD_MON_OML_CONFIG=/usr/local/mobilityfirst/conf/gnrs-oml-config-local.xml

#wireless defaults
ESSID="mf-proto"
mode="Ad-Hoc"
channel=11

#bring up the interface
if [[ $interface = eth1 ]]; then
	echo "$interface up"
	#/sbin/ifconfig $interface $self_ip up
elif [[ $interface = wlan* ]]; then
	echo "Bringing up wifi interface $interface"
	/usr/local/mobilityfirst/scripts/wlan-up.sh $interface $ESSID $mode $channel
	/sbin/ifconfig $interface $self_ip up
else
	echo "Unsupported interface $interface! Aborting"
	exit
fi

$GNRSD_BIN $config_file $self_ip $srvrs_file > $GNRSD_LOG 2>&1 &
