#!/bin/bash

# script to config the delay module and start client from the console

script_path=~/GNRS/script
data_path=~/GNRS/data
MF_path=/usr/local/mobilityfirst

#TODO: assume as# increases from 1.
count=1;
for line in $(cat $data_path/client_nodeID_list.data); do
        echo "come to $line"
        ssh root@node$line "cp $MF_path/conf/as_${count}_delay_client.dat /click/delayMod/config"
        #ssh root@node$line "java -Xmx256m -cp $MF_path/bin/gnrs-server-1.0.0-SNAPSHOT-jar-with-dependencies.jar $MF_path/conf/client_${line}.xml $MF_path/conf/request.data 1 &"
	ssh root@node$line "$MF_path/scripts/gbench.sh $MF_path/conf/client_${line}.xml $MF_path/conf/request.data 1 > /var/log/gbench.log 2<&1 &"
	((count++))
done
