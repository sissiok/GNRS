#!/bin/bash
gnuplot << EOF
set datafile separator ','

set key right bottom
set xlabel "Time in microseconds"
set ylabel "CDF"
set yrange [0:1]
set logscale x

set term postscript eps enhanced color 
set output "$2"
f(x)=0.95
set xrange [100:20000000]
plot "$1" using 2:1  title columnhead(2) with linespoints, \
	"$1" using 3:1  title columnhead(3) with linespoints, \
	"$1" using 4:1  title columnhead(4) with linespoints, \
	"$1" using 5:1  title columnhead(5) with linespoints, \
	"$1" using 6:1  title columnhead(6) with linespoints, \
 f(x) w l lt 1 lc 8 title "95 pct"
EOF
