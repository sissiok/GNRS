#!/usr/bin/perl

# this script is used to generate the client config file
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

my $outFile1;  #to generate client.xml

my $count = 1;
# Read the input file, line-by-line
while (my $line = $inFile->getline) {
	# Skip comments at the start of the line
	if($line =~ m/^#/) {
		next;
	}
	$count++;
	my $myIP = $count + 100;
	chomp $line;
	open($outFile1, ">", "client_".$line.".xml");

	#here assume the client IP is $count + 100, and is connected to the server $count
	printf $outFile1 "<edu.rutgers.winlab.mfirst.client.Configuration>\n";
	printf $outFile1 "  <serverHost>192.168.1.".$count."</serverHost>\n";
	printf $outFile1 "  <serverPort>5001</serverPort>\n";
	printf $outFile1 "  <clientPort>4001</clientPort>\n";
	printf $outFile1 "  <clientHost>192.168.1.".$myIP."</clientHost>\n";
	printf $outFile1 "  <randomSeed>-1</randomSeed>\n";
	printf $outFile1 "</edu.rutgers.winlab.mfirst.client.Configuration>";

	close $outFile1;
}

