#!/usr/bin/perl

# perl script to select certain nodes from the node_list_ID generated from nodeFilter.sh
#
# input: 
# option: 0: select the first N nodes; 1: select N nodes from offset n
# node_list_ID.data (generated from nodeFilter.sh. one node ID per line.)
# N: number of nodes that will be selected
# n: if option=1, choose N nodes after the {n}the node
#
# output: 
# selected_node_ID.data
#
# usage: ./node_list_converter.pl <option> <node list src> <selected node list output> <N> ( <offset n> )

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;

#open the node list src file
my $inFile = FileHandle->new;
$inFile->open("<" . $ARGV[1]) 
  || die "Could not open \"$ARGV[1]\" for reading.";

my $outNodeFile = FileHandle->new;
$outNodeFile->open(">" . $ARGV[2])
    || die "Could not create \"select_node_list.data\" for writing.";

my @nodeList = ();

#read file content, line-by-line
while (my $line = $inFile->getline) {
	push @nodeList, @line;
}

#select certain nodes
if ($ARGV[0] = 0)  {  #select the first N nodes
	my @Snodes = splice(@nodeList,0,$ARGV[3])
}
else {  #select N nodes after {n}th node
	my @Snodes = splice(@nodeList,$ARGV[4],$ARGV[4]+$ARGV[3])
}
for my $i (@Snodes) {
        print "$i \n";
        print $outNodeFile "$i\n";
}

close $inFile;
close $outNodeFile;
