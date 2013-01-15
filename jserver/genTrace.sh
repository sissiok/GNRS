#!/bin/bash

numInsert=$1
numLookup=$2
numTotal=$(($numInsert + $numLookup))

I=0
for ((; I < $numInsert; I=$I+1)); do
	echo "$I I $I $I,999,1"
done

for ((; I < $numTotal; I=$I+1)); do
	echo "$I Q $(($I-$numInsert))"
done

