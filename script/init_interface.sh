#!/bin/bash

interface=$1
self_ip=$2

#wireless defaults
ESSID="mf-proto"
mode="Ad-Hoc"
channel=11

#bring up the interface
if [[ $interface = eth* ]]; then
        echo "Bringing up ethernet interface $interface"
        /sbin/ifconfig $interface $self_ip up
elif [[ $interface = wlan* ]]; then
        echo "Bringing up wifi interface $interface"
        /usr/local/mobilityfirst/scripts/wlan-up.sh $interface $ESSID $mode $channel
        /sbin/ifconfig $interface $self_ip up
else
        echo "Unsupported interface $interface! Aborting"
        exit
fi

