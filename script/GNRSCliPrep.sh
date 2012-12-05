#!/bin/bash

# do experiment preparation from the console for client
# it will generate config files, send config files to corresponding nodes, and config node IP address and install click delay module
# input:
# client_node_list_file: nodes that are successfully imaged and are allocated as client
# request.data: client workload
# example: ./GNRSCliPrep.sh AS_topo.data client_node_list.data request.data

AS_topo_file=$1
node_list_file=$2
client_workload=$3

script_path=~/GNRS/script
data_path=~/GNRS/data

$script_path/node_list_converter.pl $data_path/$node_list_file client_node_list_perline.data client_nodeID_list.data
$script_path/click_delay_gen_client.pl $data_path/AS_list.data $data_path/$AS_topo_file.route
$script_path/clientconf.pl client_nodeID_list.data

mv $script_path/*.dat* $data_path/
mv $script_path/*.xml $data_path/

#send config files to correpsonding nodes
MF_path=/usr/local/mobilityfirst
MF_server=$MF_path/code/prototype/gnrs/jserver

#TODO: assume as# increases from 1 and client IP starts from 102
count=1
for line in $(cat $data_path/client_nodeID_list.data); do
    echo "come to $line"
    scp $data_path/$client_workload root@node$line:$MF_path/conf/
    scp $data_path/client_${line}.xml root@node$line:$MF_path/conf/
    scp $data_path/as_${count}_delay_client.dat root@node$line:$MF_path/conf/
    scp $data_path/gbench.sh root@node$line:$MF_path/scripts/
    scp $data_path/ggen.sh root@node$line:$MF_path/scripts/
    ((count++))
    myIP=$(( $count + 100 ))
    ssh root@node$line "cp /etc/gnrs/delayModule.click $MF_path/conf/; cp $MF_server/target/gnrs-server-1.0.0-SNAPSHOT-jar-with-dependencies.jar $MF_path/bin/"
    ssh root@node$line "ifconfig eth0 192.168.1.$myIP; click-install -u $MF_path/conf/delayModule.click &"
done

