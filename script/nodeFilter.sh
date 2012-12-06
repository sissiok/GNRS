#!/bin/bash

# shell script to filter nodes that don't work even if they are imaged

nodeID_input=$1
nodeID_output=$2

rm $nodeID_output
count=0
for line in $(cat $nodeID_input); do
    echo "come to $line"
    ssh root@node$line  "echo hello"
    if [ $? -eq 0 ] ;
    then
	((count++));
	echo $line >> $nodeID_output
    fi
done
echo $count
