#!/usr/bin/perl

# this script is used to generate the server config file
# input: ID_list.data (might be AS_list.data if multiple servers are located at one node)
# output: a bunch of files, one for each ID.
# usage: ./gnrsconf.pl <ID_list.data>

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;


#open the node list src file
my $inFile = FileHandle->new;
$inFile->open("<" . $ARGV[0])
  || die "Could not open \"$ARGV[0]\" for reading.";

my $outFile1;  #to generate server.xml
my $outFile2;  #to generate net-ipv4.xml

my $count = 1;
# Read the input file, line-by-line
while (my $line = $inFile->getline) {
	# Skip comments at the start of the line
	if($line =~ m/^#/) {
		next;
	}
	$count++;
	chomp $line;
	open($outFile1, ">", "server_".$line.".xml");

	printf $outFile1 "<edu.rutgers.winlab.mfirst.Configuration>\n";
	printf $outFile1 "  <numWorkerThreads>1</numWorkerThreads>\n";
	printf $outFile1 "  <numReplicas>5</numReplicas>\n";
	printf $outFile1 "  <collectStatistics>true</collectStatistics>\n";
	printf $outFile1 "  <networkType>ipv4udp</networkType>\n";
	printf $outFile1 "  <networkConfiguration>/etc/gnrs/net-ipv4_".$line.".xml</networkConfiguration>\n";
	printf $outFile1 "  <mappingConfiguration>/etc/gnrs/map-ipv4.xml</mappingConfiguration>\n";
	printf $outFile1 "  <storeType>berkeleydb</storeType>\n";
	printf $outFile1 "  <storeConfiguration>/etc/gnrs/berkeleydb.xml</storeConfiguration>\n";
	printf $outFile1 "  <numAttempts>2</numAttempts>\n";
	printf $outFile1 "  <timeoutMillis>500</timeoutMillis>\n";
	printf $outFile1 "  <cacheEntries>0</cacheEntries>\n";
	printf $outFile1 "  <defaultExpiration>900000</defaultExpiration>\n";
	printf $outFile1 "  <defaultTtl>30000</defaultTtl>\n";
	printf $outFile1 "  <statsDirectory>/var/gnrs/stats/</statsDirectory>\n";
	printf $outFile1 "  <replicaSelector>random</replicaSelector>\n";
	printf $outFile1 "</edu.rutgers.winlab.mfirst.Configuration>";

	open($outFile2, ">", "net-ipv4_".$line.".xml");

	printf $outFile2 "<edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>\n";
	printf $outFile2 "  <bindPort>5001</bindPort>\n";
	printf $outFile2 "  <bindAddress>192.168.1.".$count."</bindAddress>\n";
	printf $outFile2 "  <asynchronousWrite>false</asynchronousWrite>\n";
	printf $outFile2 "</edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>";

	close $outFile1;
	close $outFile2;
}

