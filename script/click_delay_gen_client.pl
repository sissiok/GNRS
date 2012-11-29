#!/usr/bin/perl

#
# Delay Combiner script for GNRS toolchain.
# Author: Robert Moore
# Last Modified: 2012/09/06
# Args: <AS List> <Delay Matrix>
#
# The AS List input file should be a list of AS numbers, identical to those in
# the Delay Matrix, sorted in non-decreasing order.
#
# The Delay Matrix input file is expected to be the complete shortest-path
# delay matrix where each row is some destination AS, and each column is the
# source AS.  The first line, however is the number of rows/columns in the
# matrix.  The values in this matrix are the inter-AS network delay, stored
# in milliseconds. Both rows and columns should have the AS values stored in
# non-decreasing order, sorted on AS #.
#
# The output will be a set of files, one for each AS listed, and each file
# will have the following format:
#   AS#1 DELAY2\r\n
#   AS#2 DELAY2\r\n
#   ...
# So each line will be a soure AS and the delay associated with packets
# arriving from that AS.  One file will be generated for each AS, and named
# with the template "as_xxx_delay.dat", where "xxx" is the AS #.

######################################################################################
# modified by Feixiong Zhang to adapt to the click delay config file format requirement for client.
# also modify the output file name "as_xxx_delay.dat", "xxx" starts from 1 and increases.


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
  print 'Usage: ', $progName, " <AS List> <Delay Matrix>\n";
}

# Simple subroutine for reading a line from a file
# Author: Terence <http://stackoverflow.com/users/227767/terence>
# Source: http://stackoverflow.com/questions/2498937/how-can-i-walk-through-two-files-simultaneously-in-perl
# Accessed: 2012/09/06
sub readFileLine {
  my $fh = shift;

  while($fh and my $line = <$fh>) {
    chomp $line;
    if($line =~ m/^#/) {
      next;
    }
    return $line;
  }
  return;
}

if($numArgs < 2) {
  usage && exit 1;
}

# File containing AS list
my $asFile;
# File containing delay matrix
my $delayFile;

open($asFile, "<", $ARGV[0])
  or die "Could not open \"$ARGV[0]\" for reading.";

open($delayFile, "<", $ARGV[1])
  or die "Could not open \"$ARGV[1]\" for reading.";

# List of AS numbers from file
my @asList = ();
my $asNum = readFileLine($asFile);

# Build the AS list from the file
while($asNum) {
  push @asList, $asNum;
  $asNum = readFileLine($asFile);
}

close $asFile;
my $outFile;

# Skip the first line of the delay matrix file
readFileLine($delayFile);

my $Count = 1;
#port number is hardcoded as 5001 amd 4001
my $serverPort = 5001;
my $clientPort = 4001;
my $intraDelay = 5;  #assume intradomain delay is 5ms
# Go through each AS and generate a file
GEN_OUTPUT: {
  foreach $asNum (@asList) {
    # The output file, named by AS number
    open($outFile, ">", "as_".$Count."_delay_client.dat");
    # Get the delays for each source AS from the delay matrix
    my $delayLine = readFileLine($delayFile);

    # If the line is empty, then break the loop.
    if($delayLine) {
      my $count = 1;
      # Extract each of the delay values (separated by whitespace)
      my @delayList = split(/\s/,$delayLine);
      # Go through each delay and print it on a separate line
      foreach my $srcAS (@asList) {
	$count++;
        # Get the next delay, remove it from the array
        my $delay = shift(@delayList);
        # For the local AS, add the intradomain delay
        if($srcAS == $asNum) {
          print $outFile "192.168.1.$count, $serverPort, $intraDelay,\n";
          next;
        }
        # Write the line to the output file.
        print $outFile "192.168.1.$count, $serverPort, $delay,\n";
      }
      # Tidy up
      close $outFile;
    } else {
      # Tidy up
      close $outFile;
      last GEN_OUTPUT;
    }
    $Count++;
  }
}


# Close file handles
close $delayFile;
