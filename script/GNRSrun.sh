#!/bin/bash

# script to start GNRS server from the console

script_path=~/GNRS/script
data_path=~/GNRS/data
MF_path=/usr/local/mobilityfirst

for line in $(cat $data_path/ID_list.data); do
	echo "come to $line"
	ssh root@node$line "$MF_path/scripts/gnrsd /usr/local/mobilityfirst/conf/server_${line}.xml"
done
