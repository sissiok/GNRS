#!/bin/bash
#this script runs on console. It's hard coded yet for simple testing. node1-1 works as the gnrs server while node1-2 works as client.
#it will directly calls script on each node. (omf exec sometimes doesn't work well)

ssh root@node1-1 killall -9 mpstat
ssh root@node1-1 "rm /var/log/timing*.log /var/log/*.data"
ssh root@node1-2 "rm /var/log/timing*.log /var/log/*.data"

#configure network address and disable cpu frequency scaling
ssh root@node1-1 "ifconfig eth0 192.168.1.110"
ssh root@node1-2 "ifconfig eth0 192.168.1.120"

ssh root@node1-1 "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"
ssh root@node1-2 "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"

ssh root@node1-1 "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/cleanup_gnrsd.sh"
ssh root@node1-2 "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/cleanup_gnrsd.sh"

ssh root@node1-1 "mpstat -P ALL 1 > /var/log/mpstat.data 2>&1 &"

#call gnrs server script and client script
ssh root@node1-1 "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_gnrsd.sh gnrsd.conf 192.168.1.110 eth0 servers.lst 1 45000"
sleep 20
ssh root@node1-2 "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_gnrs_client.sh client.conf 192.168.1.120 192.168.1.110 eth0 request.data.1 10000 350"


flag=1

while [ $flag -eq 1 ]; do
    if  ssh root@node1-2 ps -A | grep gbench > /dev/null || ssh root@node1-1 ps -A | grep gnrsd > /dev/null
    then
        echo "service is still running"
        sleep 10
    else
        flag=0
    fi
done

ssh root@node1-1 killall -9 mpstat

#copy files from each node to console
rm -rf ./server_stat ./client_stat
mkdir ./server_stat ./client_stat
scp root@node1-1:/var/log/*.data ./server_stat/
scp root@node1-1:/var/log/gnrsd.log ./server_stat/
scp root@node1-1:/var/log/timings-0.log ./server_stat/

scp root@node1-2:/var/log/*.data ./client_stat/
scp root@node1-2:/var/log/gnrsd-client.log-* ./client_stat/
scp root@node1-2:/var/log/timing*.log ./client_stat/
