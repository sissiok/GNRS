#!/bin/bash

# script to config the delay module and start GNRS server from the console

script_path=~/GNRS/script
data_path=~/GNRS/data
MF_path=/usr/local/mobilityfirst

#TODO: assume as# increases from 1.
count=1;
for line in $(cat $data_path/serv_nodeID_list.data); do
	echo "come to $line"
	ssh root@node$line "cp $MF_path/conf/as_${count}_delay_serv.dat /click/delayMod/config"
	ssh root@node$line "$MF_path/scripts/gnrsd.sh $MF_path/conf/server_${line}.xml > /var/log/gnrsd.log 2>&1 &"
	((count++))
done
