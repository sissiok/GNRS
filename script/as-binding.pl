#!/usr/bin/perl

#
# Node->IP binding generation script
# Author: Feixiong Zhang, Robert Moore
# Last Modified: 2012/12/17
# Args: <Input File> [<Output File>]
#
# The input file is expected to be formatted as a list of AS numbers, each on
# a separate line.  The output format is a comma-separated value (CSV) file
# containing three columns: AS number, IP address, UDP Port number
# 
# If the output file is not provided, STDOUT is used.
#

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;

# Store the script name
my $progName = $0; 

my $numArgs = $#ARGV+1;

# Simple subroutine for printing usage info to the terminal
sub usage {
  print 'Usage: ',  $progName, " <INPUT> [<OUTPUT>]\n";
  print "  If the OUTPUT file name is not provided, then output will be\n";
  print "  directed to stdout.\n";
}

# Ensure there is at least the input file, else die
if($numArgs < 1) {
  usage && exit 1;
}

my $outFile = FileHandle->new;
my $inFile = FileHandle->new;

# Open the input and output files according to arguments
if($numArgs > 1) {
  $outFile->open(">" . $ARGV[1])
    || die "Could not open \"$ARGV[1]\" for writing.";
}else {
  $outFile->open("> -" )
    || die "Could not open STDOUT for writing.";
}

$inFile->open("<" . $ARGV[0])
  || die "Could not open \"$ARGV[0]\" for reading.";

#read the AS, and generate the corresponding IP mapping
my $count = 1;
#port number is hard coded as 5001
my $port = 5001;
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
	printf $outFile "$line"." 192.168.1.$count $port";
}
