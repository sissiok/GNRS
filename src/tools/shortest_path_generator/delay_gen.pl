#!/usr/bin/perl

#
# Delay Combiner script for GNRS toolchain.
# Author: Robert Moore
# Last Modified: 2012/09/06
# Args: <Delay Matrix> <AS List> 
#
# The Delay Matrix input file is expected to be the complete shortest-path
# delay matrix where each row is some destination AS, and each column is the
# source AS.  The values in this matrix are the inter-AS network delay, stored
# in milliseconds. Both rows and columns should have the AS values stored in
# non-decreasing order, sorted on AS #.
#
# The AS List input file should be a list of AS numbers, identical to those in
# the Delay Matrix, sorted in non-decreasing order.
#
# The output will be a set of files, one for each AS listed, and each file
# will have the following format:
#   AS#1 DELAY2\r\n
#   AS#2 DELAY2\r\n
#   ...
# So each line will be a soure AS and the delay associated with packets
# arriving from that AS.  One file will be generated for each AS, and named
# with the template "as_xxx_delay.dat", where "xxx" is the AS #.

# Standard imports.
use v5.10;
use strict;
use warnings;
use autodie;
# Script-specific imports.
use FileHandle;

autoflush STDOUT 1;

# Store the script name
my $progName = $0;
my $numArgs = $#ARGV+1;

# Simple subroutine for printing usage to the terminal
sub usage {
  print 'Usage: ', $progName, " <Delay Matrix> <AS List> [<OUTPUT>]\n";
  print "  If the OUTPUT file name is not provided, then output will be\n";
  print "  directed to stdout.\n";
}

if($numArgs < 2) {
  usage && exit 1;
}


my $outFile = FileHandle->new;
my $asList = FileHandle->new;
my $delayFile = FileHandle->new;

# Open the input and output files based on the arguments provided
if($numArgs > 2) {
  $outFile->open(">" . $ARGV[2])
    || die "Could not open \"$ARGV[2]\" for writing.";
} else {
  $outFile->("> -")
    || die "Could not open stdout for writing.";
}

$asList->open("<".$ARGV[1])
  || die "Could not open \"$ARGV[1]\" for reading.";

$delayFile->open("<".$ARGV[0])
  || die "Could not open \"$ARGV[0]\" for reading.";

close $outFile;
close $asList;
close $delayFile;
