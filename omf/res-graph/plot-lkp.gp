#!/usr/bin/gnuplot

basefile = "clt-lkp-all"
set title "Client RTT for Lookups\nTop-200 by Degree/100K GUIDs/1 Client"

set term postscript eps enhanced color solid
set datafile separator ','
set key right bottom title "Interarrival Delay" box
set ylabel "Cumulative Distribution Function (CDF)"
set xlabel "RTT in Milliseconds"
set output basefile.".eps"
plot basefile.".csv" every::0::101 using 2:1 title columnheader(2) with lines, \
	basefile.".csv" every::0::101 using 3:1 title columnheader(3) with lines, \
	basefile.".csv" every::0::101 using 4:1 title columnheader(4) with lines, \
	basefile.".csv" every::0::101 using 5:1 title columnheader(5) with lines, \
	basefile.".csv" every::0::101 using 6:1 title columnheader(6) with lines, \
	basefile.".csv" every::0::101 using 7:1 title columnheader(7) with lines, \
	basefile.".csv" every::0::101 using 8:1 title columnheader(8) with lines, \
	basefile.".csv" every::0::101 using 9:1 title columnheader(9) with lines, \
	basefile.".csv" every::0::101 using 10:1 title columnheader(10) with lines, \
	basefile.".csv" every::0::101 using 11:1 title columnheader(11) with lines

