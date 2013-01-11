#!/bin/bash

wget https://bitbucket.org/romoore/gnrs/downloads/ASPrefixData.mat
wget https://bitbucket.org/romoore/gnrs/downloads/topologyData.mat
wget https://bitbucket.org/romoore/gnrs/downloads/shellMem.mat
wget https://bitbucket.org/romoore/gnrs/downloads/hangMem.mat
wget https://bitbucket.org/romoore/gnrs/downloads/clique.mat

./foo.sh $1 $2
