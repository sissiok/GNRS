#!/usr/bin/perl

#
# Unique Autononous System (AS) sorter for the GNRS toolchain.
# Author: Robert Moore
# Last Modified: 2012/09/05
# Args: <Input File> <Output File>
#
# The input file is expected to be formatted as a 3xN matrix of numbers. The
# first and second columns are AS numbers and the third column is the cost of
# the link between them.
#
# This utility will use the first 2 columns to generate an output file
# containing unique AS numbers, in non-decreasing order, on individual lines.
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
sub usage
{
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

if($numArgs > 1) {
  $outFile->open(">" . $ARGV[1])
    || die "Could not open \"$ARGV[1]\" for writing.";
}else {
  $outFile->open("> -" )
    || die "Could not open STDOUT for writing.";
}

$inFile->open("<" . $ARGV[0]) 
  || die "Could not open \"$ARGV[0]\" for reading.";

print $outFile "test\n";

$outFile->close;
$inFile->close;
exit;
