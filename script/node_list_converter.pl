#!/usr/bin/perl

# perl script to convert the node_list file generated from omf load output to more friendly format
#
# input: node_list.data (generated from omf load output. only one line in the file with all those nodes that are successfully imaged.)
#
# output: 
# node_list_perline.data: node list: one line for one node
# ID_list.data: node ID

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

my $outNodeFile = FileHandle->new;
my $outIDFile = FileHandle->new;
$outNodeFile->open("> node_list_perline.data")
    || die "Could not create \"node_list_perline.data\" for writing.";
$outIDFile->open("> ID_list.data")
    || die "Could not create \"ID_list.data\" for writing.";

#read file content
my $line = $inFile->getline;

#convert to several line: one line for one node
chomp $line;
my @nodes = split(/,/,$line);
for my $i (@nodes) {
	print "$i \n";
	print $outNodeFile "$i\n";
}

#extract node ID
for my $i (@nodes) {
	my @nodeSplit = split(/\./,$i);
	my $nodeID = $nodeSplit[0];
	#print "$nodeID \n";

	my @IDSplit = split(/node/,$nodeID);
	my $ID = $IDSplit[1];
	print "$ID \n";
	printf $outIDFile "$ID\n";
}

close $inFile;
close $outNodeFile;
close $outIDFile;
