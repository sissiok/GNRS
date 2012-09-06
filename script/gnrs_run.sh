#!/bin/bash

ssh root@node1-1 killall -9 mpstat
ssh root@node1-1 rm /var/log/timing*.log /var/log/*.data
ssh root@node1-2 rm /var/log/timing*.log /var/log/*.data
omf-5.3 exec ./gnrs_test_53_sb2.rb

flag=1

while [ $flag -eq 1 ]; do
    if  ssh root@node1-2 ps -A | grep gbench > /dev/null || ssh root@node1-1 ps -A | grep gnrsd > /dev/null
    then
        echo "service is still running"
        sleep 5
    else
        flag=0
    fi
done

ssh root@node1-1 killall -9 mpstat
rm -rf ./server_stat ./client_stat
mkdir ./server_stat ./client_stat
scp root@node1-1:/var/log/*.data ./server_stat/
scp root@node1-1:/var/log/gnrsd.log ./server_stat/
scp root@node1-1:/var/log/timings-0.log ./server_stat/

scp root@node1-2:/var/log/*.data ./client_stat/
scp root@node1-2:/var/log/gnrsd-client.log-* ./client_stat/
scp root@node1-2:/var/log/timing*.log ./client_stat/
