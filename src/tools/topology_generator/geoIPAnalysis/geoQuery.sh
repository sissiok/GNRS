#!/bin/bash
# this script is used to query the geoip service to get the location info for IP in the input file
# usage: ./geoQuery.sh <prefix file>

prefix_file=$1

count=1
for line in $(cat $prefix_file); do
        echo "come to $line"
        echo "count: $count"
        curl http://freegeoip.net/csv/$line >> geoQueryResult
        ((count++))
        sleep 4
done