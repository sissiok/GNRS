#!/usr/bin/perl

#perl script to process the geoQueryResult
#
# input files: geoQueryResult, AS.data
# output file: AS2LOC.data, ASArray.LOC

use v5.10;
use strict;
use warnings;
use autodie;

use FileHandle;

autoflush STDOUT 1;

#open the file geoQueryResult and AS.data
my $geoInFile = FileHandle->new;
my $asInFile = FileHandle->new;
$geoInFile->open("<" . "geoQueryResult")
	|| die "Could not open \"geoQueryResult\" for reading.";
$asInFile->open("<" . "AS.data")
        || die "Could not open \"AS.data\" for reading.";

my $outFile = FileHandle->new;
$outFile->open(">" . "AS2LOC.data")
        || die "Could not create \"AS2LOC.data\" for writing.";

my $line1 = $geoInFile->getline;
my $line2 = $asInFile->getline;

#extract AS and location info
while($line1 and $line2)  {
	chomp $line2;
	my @lineSplit = split(/,/,$line1);
	my @elemSplit0 = split(/"/,$lineSplit[0]);
	my @elemSplit1 = split(/"/,$lineSplit[1]);
	printf $outFile "$line2 $elemSplit0[1] $elemSplit1[1]\n";
	$line1 = $geoInFile->getline;
	$line2 = $asInFile->getline;
}


close $geoInFile;
close $asInFile;
close $outFile;


#process AS2LOC.data, count ASes for each country
my $inFile = FileHandle->new;
$inFile->open("<" . "AS2LOC.data")
        || die "Could not open \"AS2LOC.data\" for reading.";

#key: country, value: AS IDs
my %AS2LOC;
while (my $line = $inFile->getline) {
        chomp $line;
        my @lineSplit = split(/\ /,$line);
        push @{$AS2LOC{$lineSplit[2]}}, $lineSplit[0];
}

$outFile = FileHandle->new;
$outFile->open(">" . "ASArray.LOC")
        || die "Could not create \"ASArray.LOC\" for writing.";

my $size = keys %AS2LOC;
print "number of different countries in the database: $size\n";

for (keys %AS2LOC) {
        printf $outFile "@{$AS2LOC{$_}}\n";
}

close $inFile;
close $outFile;
