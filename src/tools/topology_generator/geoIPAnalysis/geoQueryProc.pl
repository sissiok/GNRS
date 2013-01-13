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
	chomp $line2; #AS ID
	my @lineSplit = split(/,/,$line1);
	my @elemSplit0 = split(/"/,$lineSplit[0]); #IP prefix
	my @elemSplit1 = split(/"/,$lineSplit[1]); #location
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

# ASArray.LOC1 and ASArray.LOC2: each line contains the AS IDs for an AS
# ASArray.LOC2: fill up 0 for each line to keep the same length, used for matlab input
my $outFile1 = FileHandle->new;
$outFile1->open(">" . "ASArray.LOC1")
        || die "Could not create \"ASArray.LOC1\" for writing.";
my $outFile2 = FileHandle->new;
$outFile2->open(">" . "ASArray.LOC2")
        || die "Could not create \"ASArray.LOC2\" for writing.";


my $locCount = keys %AS2LOC;
print "number of different countries in the database: $locCount\n";

#generate ASArray.LOC1 and count max number of ASes in one location(country)
my $maxLocSize = 0;
for (keys %AS2LOC) {
        printf $outFile1 "@{$AS2LOC{$_}}\n";
	if($maxLocSize < $#{$AS2LOC{$_}}+1)  {
		$maxLocSize = $#{$AS2LOC{$_}}+1;
	}
}

#generate ASArray.LOC2
for (keys %AS2LOC) {
	my $i = 0;
	my $fillNum = $maxLocSize - $#{$AS2LOC{$_}} - 1;

	printf $outFile2 "@{$AS2LOC{$_}}";

	while ($i < $fillNum)  {
		printf $outFile2 " 0";
		$i++;
	}
	printf $outFile2 "\n";
}

close $inFile;
close $outFile1;
close $outFile2;
