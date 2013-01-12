#!/bin/bash
# this script is used to process the result after getting the geoQueryResult from the script ./geoQuery.sh

wget https://bitbucket.org/romoore/gnrs/downloads/geoQueryResult
wget https://bitbucket.org/romoore/gnrs/downloads/AS.data

./geoQueryProc.pl
