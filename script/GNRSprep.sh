#!/bin/bash

# do experiment preparation from the console 
# it will generate config files, send config files to corresponding nodes, and config node IP address

AS_topo_file=$1
node_list_file=$2

script_path=~/GNRS/script
data_path=~/GNRS/data

#generate configuration files
$script_path/run.now $data_path/$AS_topo_file
$script_path/node_list_converter.pl $data_path/$node_list_file
$script_path/as-uniq.pl $data_path/$AS_topo_file $data_path/AS_list.data
$script_path/ASbinding.pl $data_path/AS_list.data
#AS_no=$(wc -l < $data_path/AS_list.data)
$script_path/delay_gen.pl $data_path/AS_list.data $data_path/$AS_topo_file.route
#$script_path/serverlst.pl $AS_no
$script_path/gnrsconf.pl ID_list.data

mv $script_path/*.dat* $data_path/
mv $script_path/*.xml $data_path/
mv $script_path/*.ipv4 $data_path/

#send config files to correpsonding nodes
MF_path=/usr/local/mobilityfirst

count=1
for line in $(cat $data_path/ID_list.data); do
    echo "come to $line"
    scp $data_path/as-binding.ipv4 $data_path/prefixes.ipv4 root@node$line:$MF_path/conf/
    scp $data_path/server_${line}.xml $data_path/net-ipv4_${line}.xml root@node$line:$MF_path/conf/
    scp $data_path/as_${count}_delay.dat root@node$line:$MF_path/conf/
    ((count++))
    ssh root@node$line "ifconfig eth0 192.168.1.$count"
done
