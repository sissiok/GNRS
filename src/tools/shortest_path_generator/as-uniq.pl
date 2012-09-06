#!/usr/bin/perl

#
# Unique Autononous System (AS) sorter for the GNRS toolchain.
# Author: Robert Moore
# Last Modified: 2012/09/06
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
sub usage {
  print 'Usage: ',  $progName, " <INPUT> [<OUTPUT>]\n";
  print "  If the OUTPUT file name is not provided, then output will be\n";
  print "  directed to stdout.\n";
}

# Subroutine for extracting unique elements from a parameter array.
# Author: Greg Hewgill <http://stackoverflow.com/users/893/greg-hewgill>
# Source: http://stackoverflow.com/questions/7651/how-do-i-remove-duplicate-items-from-an-array-in-perl
# Date: 2012/09/05
sub uniq {
  return keys %{{ map { $_ => 1 } @_ }} ;
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

# AS array
my @asList = ();

# Read the input file, line-by-line
while (my $line = $inFile->getline) {
  # Skip comments at the start of the line
  if($line =~ m/^#/) {
    next;
  }
  my @columns = split(/\s/,$line);
  push @asList, $columns[0];
  push @asList, $columns[1];
}

# Extract unique elements
@asList = uniq(@asList);

# Sort the AS elements
@asList = sort { $a <=> $b } @asList;

# Write to output file
foreach my $as (@asList) {
  print $outFile $as . "\n";
}

$outFile->close;
$inFile->close;
exit;
