#!/usr/bin/perl

# this script is used to generate the AS binding file.
# input: AS_list.data (output file from the as-uniq.pl)
# output: AS_binding.data ( current format: <AS#> <node IP address>; future format: <AS#> <node IP address, port>
# usage: ./ASbinding.pl <AS list>

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;

my $inFile = FileHandle->new;
$inFile->open("<" . $ARGV[0]) 
  || die "Could not open \"$ARGV[0]\" for reading.";

my $outFile = FileHandle->new;
$outFile->open("> AS_binding.data")
    || die "Could not create \"node_list_perline.data\" for writing.";

#read the AS, and generate the corresponding IP mapping
my $count = 1;
while (my $line = $inFile->getline) {
	# Skip comments at the start of the line
	if($line =~ m/^#/) {
	next;
  	}
	$count++;
	chomp $line;
	if($count != 2) {
		printf $outFile "\n";
	}
	printf $outFile "$line"." 192.168.1.$count";
}
