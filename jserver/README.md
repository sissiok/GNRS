# GNRS Java Server #
A Java-based implementation of the GUID Name Resolution Service server for the
Mobility First project.  It is intended as a prototype implementation and is
not suitable for commercial or enterprise use.

## Building ##
Be sure you have Java 1.6 and Maven 2 or higher installed.  On Ubuntu systems,
you can do this with apt-get:

    sudo apt-get install openjdk-6-jdk maven

Once that's installed, you can compile and build the .jar file with:

    mvn clean package

The first time you compile, Maven will automatically resolve and download all
dependencies (be sure you have internet access).  It will then compile the
code and assemble the JAR.  The output will be in the `target/` subdirectory.

## Running the Server ##
The server requires a main configuration file as the only command-line
argument.  A sample is provided in `src/main/resources/conf.xml`.  This
XML-based configuration file has comments describing each component and will
also define the location of any other files used by the server.

To run the server directly from the JAR with the provided configuration file,
I will assume you are in the `jserver/` directory:

    java -jar target/gnrs-server-1.0.0-SNAPSHOT-jar-with-dependencies.jar \\
      src/main/resources/conf.xml

Since this is a bit cumbersome to remember, a launch script (Bash), `gnrsd` has been
provided in the base directory and will pass any arguments directly to the
server.  Using this launch script, the above startup command becomes:

    ./gnrsd src/main/resources/conf.xml

## Logging Configuration ##
The server uses logging through the Log4J logging library (indirectly through
SLF4J).  By default, it will send INFO, WARNING, and ERROR message directly to
the console.  It will also log everything, including DEBUG and TRACE messages,
to a set of 10 files called _gnrsd-debug.log_, _gnrsd-debug.log.1_, etc.

To change the logging behavior, you must edit the log configuration file in
`src/main/resources/log4j.xml` and recompile/rebuild the JAR file.  The most
common change would be disabling console output, for example if you were not
monitoring the server directly.
