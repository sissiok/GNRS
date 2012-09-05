# Shortest Path Generator #
**IMPORTANT**: This application produces 3 rather large matrices in-memory.
As a result, it requires a non-trivial amount of memory in order to run.  As
an example, an input file consisting of ~24,000 AS's (~90,000 links) used
nearly 24GB of RAM at its peak, and produced an output file of 4GB.  The size
of the matrices are effectively the square of the number of AS's, so be sure
to allocate sufficient resources when running on large data sets.

The shortest-path generator takes as input a topology file for different
Autonomous Systems (AS's) and the associated cost of that inter-AS link. It
then uses Djikstra's algorithm to compute the shortest-path between every pair
of AS's and output it into a matrix file.

## Building the Program ##
To build the shortest path generator, run GMake with the makefile in the
current directory.  If you use the default invocation, it should produce a
binary output file named 'run.now'.

    make clean all

Alternatively, you can enable some debugging statements by executing either
the debug1 or debug2 directives in the makefile.

    make clean debug1
    # Or for more verbose output
    make clean debug2

The output of either of the debug directives is named 'run.debug'.

## Running the Program ##
The program takes a single input, the name of a topology file, and produces an
output file in the same location as the input, and with the same name that
only appends the '.route' suffix to the name.  For example:

    ./run.now topology.txt
    # Produces the output file 'topology.txt.route'

## Input File Format ##
The input file (topology file) is expected to have be an ASCII-encoded (text)
file with inter-AS link weights on individual lines.  For example, the simple
network illustrated below:

    AS6
     |  \
     |   7
     |    \
     5     AS3
     |    /
     |   4
     |  /
    AS2

Could be represented by a topology file like this:
    
    6 3 7
    6 2 5
    2 3 4

Equally, you could use a different arrangement of the lines:

    2 3 4
    6 2 5
    3 6 7

The output of the program is a '.route' file that has the computed
shortest-path cost from each AS to every other AS, arranged in a 2-dimensional
matrix.  There are no headers present in the output file, but each row or
column represents some single AS, ordered in non-decreasing order, based on
the values found in the input file.  For the example above, the output would
be this:

    0 4 5
    4 0 7
    5 7 0

Note the lack of any column/row header information.  Also check so that you
understand how the AS number values are used to order the rows and columns.
