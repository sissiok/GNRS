#!/bin/bash

config_file=$1
self_ip=$2
server_ip=$3
interface=$4 # e.g, eth0 or wlan0
request_file_=$5
listen_port=$6
request_interval=$7

GNRSD_BIN="/usr/local/mobilityfirst/code/prototype/gnrsd/src/client-new/gbench"
GNRSD_LOG="/var/log/gnrsd-client.log-$listen_port"

#monitor defaults
#GNRSD_MON_LOG=/var/log/mf-gnrs-mon.log
#GNRSD_MON_OML_CONFIG=/usr/local/mobilityfirst/conf/gnrs-oml-config.xml
#GNRSD_MON_OML_CONFIG=/usr/local/mobilityfirst/conf/gnrs-oml-config-local.xml

#wireless defaults
ESSID="mf-proto"
mode="Ad-Hoc"
channel=11

#bring up the interface
#if [[ $interface = eth* ]]; then
#	echo "Bringing up ethernet interface $interface"
#	/sbin/ifconfig $interface $self_ip up
#elif [[ $interface = wlan* ]]; then
#	echo "Bringing up wifi interface $interface"
#	/usr/local/mobilityfirst/scripts/wlan-up.sh $interface $ESSID $mode $channel
#	/sbin/ifconfig $interface $self_ip up
#else
#	echo "Unsupported interface $interface! Aborting"
#	exit
#fi

client_file="/usr/local/mobilityfirst/code/prototype/gnrsd/src/client-new/$config_file"
request_file="/usr/local/mobilityfirst/code/prototype/gnrsd/src/client-new/$request_file_"
$GNRSD_BIN $client_file $request_file $request_interval $self_ip $server_ip $listen_port > $GNRSD_LOG 2>&1 &
