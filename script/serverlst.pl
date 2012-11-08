#!/usr/bin/perl

# perl script to generate serverlist used by the GNRS server. the serverlist file contains all the server IP that are used in the experiment
# input: number of servers that are used
# output: servers.lst
# usage: ./serverlst.pl <num of servers>

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;

my $outFile = FileHandle->new;
$outFile->open("> servers.lst")
    || die "Could not create \"node_list_perline.data\" for writing.";

#generate the servers.lst: start from 192.168.1.10, and increases
for my $i (1 .. $ARGV[0])  {
	my $j = 1 + $i;
	print $outFile "192.168.1.".$j;
	if($i != $ARGV[0])  {
		printf $outFile "\n";
	}
}
