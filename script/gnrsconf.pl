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

my $outFile;

my $count = 1;
# Read the input file, line-by-line
while (my $line = $inFile->getline) {
	# Skip comments at the start of the line
	if($line =~ m/^#/) {
		next;
	}
	$count++;
	chomp $line;
	open($outFile, ">", "gnrsd_".$line.".conf");

	printf $outFile "#daemon config\n";
	printf $outFile "DAEMON_LISTEN_PORT=5000;\n";
	printf $outFile "SERVER_ADDR=\"192.168.1.$count\";\n";
	printf $outFile "#for HASH_FUNC: 0 is hash128, 1 is BGP_hash\n";
	printf $outFile "HASH_FUNC=0;\n";
	printf $outFile "THREAD_POOL_SIZE=1;\n";
	printf $outFile "#FOR SERVICE_REQ_NUM: If it's a negative number, it will keep running. If positive, it will shutdown after receiving SERVICE_REQ_NUM requests.\n";
	printf $outFile "SERVICE_REQ_NUM=-1;\n\n";
	printf $outFile "#static partipation set of gnrs servers\n";
	printf $outFile "SERVERS_LIST_FILE=\"./servers.lst\";\n\n";
	printf $outFile "#database details for persistence engine\n";
	printf $outFile "DB_HOSTNAME=\"127.0.0.1\";\n";
	printf $outFile "DB_PORT=3306;\n";
	printf $outFile "DB_NAME=\"mf_gnrs\";\n";
	printf $outFile "DB_USER=\"mf-user\";\n";
	printf $outFile "DB_PASSWD=\"mf-user\";\n\n";
	printf $outFile "#client config\n";
	printf $outFile "CLIENT_LISTEN_PORT=9000;\n";
	printf $outFile "CLIENT_ADDR=\"\";\n";

	close $outFile;
}
	
