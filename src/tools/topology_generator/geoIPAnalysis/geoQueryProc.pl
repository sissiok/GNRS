#!/usr/bin/perl

#perl script to process the geoQueryResult
#
# input files: geoQueryResult, AS.data
# output file: AS2LOC.data

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

while(my $line = $geoInFile->getline)  {

}


close $geoInFile;
close $asInFile;
close $outFile;
