#!/bin/bash

# do experiment preparation from the console for server 
# it will generate config files, send config files to corresponding nodes, and config node IP address and install click delay module
# input:
# AS_topo_file: AS topology as a 3xN matrix
# node_list_file: nodes that are successfully imaged and are allocated as servers
# prefixes.ipv4: IP prefix file used at the server
# example: ./GNRSServPrep.sh AS_topo.data server_node_list.data prefixes.ipv4


AS_topo_file=$1
node_list_file=$2
prefix_file=$3

script_path=~/GNRS/script
data_path=~/GNRS/data

#generate configuration files
#generate delay matrix
$script_path/run.now $data_path/$AS_topo_file
#generate node ID list: ID_list.data
$script_path/node_list_converter.pl $data_path/$node_list_file serv_node_list_perline.data serv_nodeID_list.data
#generate AS list
$script_path/as-uniq.pl $data_path/$AS_topo_file $data_path/AS_list.data
#generate AS to <ip,port> tuple
$script_path/ASbinding.pl $data_path/AS_list.data
#AS_no=$(wc -l < $data_path/AS_list.data)
$script_path/click_delay_gen_serv.pl $data_path/AS_list.data $data_path/$AS_topo_file.route
#$script_path/serverlst.pl $AS_no
#generate .xml config file
$script_path/gnrsconf.pl serv_nodeID_list.data

mv $script_path/*.dat* $data_path/
mv $script_path/*.xml $data_path/
mv $script_path/*.ipv4 $data_path/

#send config files to correpsonding nodes
MF_path=/usr/local/mobilityfirst
MF_server=$MF_path/code/prototype/gnrs/jserver

#TODO: assume as# increases from 1.
count=1
for line in $(cat $data_path/serv_nodeID_list.data); do
    echo "come to $line"
    scp $data_path/as-binding.ipv4 $data_path/$prefix_file root@node$line:$MF_path/conf/
    scp $data_path/server_${line}.xml $data_path/net-ipv4_${line}.xml $data_path/log4j.xml $data_path/berkeleydb.xml $data_path/map-ipv4.xml root@node$line:$MF_path/conf/
    scp $data_path/as_${count}_delay_serv.dat root@node$line:$MF_path/conf/
    scp $data_path/gnrsd.sh root@node$line:$MF_path/scripts/
    ((count++))
    ssh root@node$line "cp /etc/gnrs/delayModule.click $MF_path/conf/; cp $MF_server/target/gnrs-server-1.0.0-SNAPSHOT-jar-with-dependencies.jar $MF_path/bin/"
    ssh root@node$line "ifconfig eth0 192.168.1.$count; click-install -u $MF_path/conf/delayModule.click &"
done
