#!/bin/ruby
#
# A function that generates configuration files for GNRS server/client nodes.
#
# Author: Robert Moore
# Last Modified: Jan 8, 2013
#

# node: GNRSNode object
def makeServerConfig(node)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.Configuration>
\t<numWorkerThreads>1</numWorkerThreads>
\t<numReplicas>5</numReplicas>
\t<collectStatistics>true</collectStatistics>
\t<networkType>ipv4udp</networkType>
\t<networkConfiguration>/etc/gnrs/net-ipv4_XxX.xml</networkConfiguration>
\t<mappingConfiguration>/etc/gnrs/map-ipv4.xml</mappingConfiguration>
\t<storeType>berkeleydb</storeType>
\t<storeConfiguration>/etc/gnrs/berkeleydb.xml</storeConfiguration>
\t<numAttempts>2</numAttempts>
\t<timeoutMillis>500</timeoutMillis>
\t<cacheEntries>0</cacheEntries>
\t<defaultExpiration>900000</defaultExpiration>
\t<defaultTtl>30000</defaultTtl>
\t<statsDirectory>/var/gnrs/stats/</statsDirectory>
\t<replicaSelector>random</replicaSelector>
</edu.rutgers.winlab.mfirst.Configuration>
ENDSTR

	# Replace placeholder with node-specific info
	return asString.gsub(/XxX/,node.asNumber.to_s)
end # makeServerConfig

def makeServerNetConfig(node)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
\t<bindPort>_PORT_</bindPort>
\t<bindAddress>_IPADDR_</bindAddress>
\t<asynchronousWrite>false</asynchronousWrite>
</edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
ENDSTR

	return asString.gsub(/_PORT_/,node.port.to_s).gsub(/_IPADDR_/,node.ipAddress.to_s)
end # makeServerNetConfig

def makeClientConfig(client,server)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.client.Configuration>
\t<serverHost>_SRV-IPADDR_</serverHost>
\t<serverPort>_SRV-PORT_</serverPort>
\t<clientPort>_CLT-PORT_</clientPort>
\t<clientHost>_CLT-IPADDR_</clientHost>
\t<randomSeed>-1</randomSeed>
</edu.rutgers.winlab.mfirst.client.Configuration>
ENDSTR

	return asString.gsub(/_SRV-IPADDR_/,server.ipAddress.to_s).gsub(/_SRV-PORT_/,server.port.to_s).gsub(/_CLT-IPADDR_/,client.ipAddress.to_s).gsub(/_CLT-PORT_/,client.port.to_s)
end # makeClientConfig

def makeServerInit(server)
	asString = <<-ENDSTR
#! /bin/sh
### BEGIN INIT INFO
# Provides:          gnrsd_#{server.asNumber}
# Required-Start:    
# Required-Stop:     
# Default-Start:     
# Default-Stop:      0 1 2 3 4 5 6
# Short-Description: GNRS Server
# Description:       Name lookup service for GUID
### END INIT INFO

# Author: Robert Moore \\(romoore@rutgers.edu\\)
#

# Do NOT "set -e"

# PATH should only include /usr/* if it runs after the mountnfs.sh script
GNRS_USER=root
PATH=/usr/local/bin/gnrs:/sbin:/usr/sbin:/bin:/usr/bin
DESC="GNRS Server #{server.asNumber}"
NAME=gnrsd_#{server.asNumber}
DAEMON=/usr/local/bin/gnrs/$NAME
DAEMON_ARGS="/etc/gnrs/server_#{server.asNumber}.xml"
PIDFILE=/var/run/$NAME.pid
SCRIPTNAME=/etc/init.d/$NAME

# Exit if the package is not installed
[ -x "$DAEMON" ] || exit 0

# Read configuration variable file if it is present
#[ -r /etc/default/$NAME ] && . /etc/default/$NAME

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base \\(>= 3.2-14\\) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

#
# Function that starts the daemon/service
#
do_start\\(\\)
{
	# Return
	#   0 if daemon has been started
	#   1 if daemon was already running
	#   2 if daemon could not be started
	start-stop-daemon --chuid $GNRS_USER --start --quiet --pidfile $PIDFILE --exec $DAEMON --test > /dev/null \
		|| return 1
	start-stop-daemon --chuid $GNRS_USER -b -m --start --quiet --pidfile $PIDFILE --exec $DAEMON -- \
		$DAEMON_ARGS \
		|| return 2
	# Add code here, if necessary, that waits for the process to be ready
	# to handle requests from services started subsequently which depend
	# on this one.  As a last resort, sleep for some time.
}

#
# Function that stops the daemon/service
#
do_stop\\(\\)
{
	# Return
	#   0 if daemon has been stopped
	#   1 if daemon was already stopped
	#   2 if daemon could not be stopped
	#   other if a failure occurred
	#start-stop-daemon --stop --quiet --retry=QUIT/10/KILL/5 --pidfile $PIDFILE --name $NAME
  if [ -e $PIDFILE ]; then 
    PARENT_PID=`cat $PIDFILE`
    pkill -P$PARENT_PID
    RETVAL="$?"
    rm -f $PIDFILE
  else
    RETVAL=0
  fi
	#[ "$RETVAL" = 2 ] && return 2
	# Wait for children to finish too if this is a daemon that forks
	# and if the daemon is only ever run from this initscript.
	# If the above conditions are not satisfied then add some other code
	# that waits for the process to drop all resources that could be
	# needed by services started subsequently.  A last resort is to
	# sleep for some time.
	#start-stop-daemon --stop --quiet --oknodo --retry=QUIT/10/KILL/5 --exec $DAEMON
	#[ "$?" = 2 ] && return 2
	# Many daemons dont delete their pidfiles when they exit.
	return "$RETVAL"
}

#
# Function that sends a SIGHUP to the daemon/service
#
do_reload\\(\\) {
	#
	# If the daemon can reload its configuration without
	# restarting \\(for example, when it is sent a SIGHUP\\),
	# then implement that here.
	#
	start-stop-daemon --stop --signal 1 --quiet --pidfile $PIDFILE --name $NAME
	return 0
}

case "$1" in
  start\\)
	[ "$VERBOSE" != no ] && log_daemon_msg "Starting $DESC" "$NAME"
	do_start
	case "$?" in
		0|1\\) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2\\) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  stop\\)
	[ "$VERBOSE" != no ] && log_daemon_msg "Stopping $DESC" "$NAME"
	do_stop
	case "$?" in
		0|1\\) [ "$VERBOSE" != no ] && log_end_msg 0 ;;
		2\\) [ "$VERBOSE" != no ] && log_end_msg 1 ;;
	esac
	;;
  status\\)
       status_of_proc "$DAEMON" "$NAME" && exit 0 || exit $?
       ;;
  #reload|force-reload\\)
	#
	# If do_reload\\(\\) is not implemented then leave this commented out
	# and leave "force-reload" as an alias for "restart".
	#
	#log_daemon_msg "Reloading $DESC" "$NAME"
	#do_reload
	#log_end_msg $?
	#;;
  restart|force-reload\\)
	#
	# If the "reload" option is implemented then remove the
	# "force-reload" alias
	#
	log_daemon_msg "Restarting $DESC" "$NAME"
	do_stop
	case "$?" in
	  0|1\\)
		do_start
		case "$?" in
			0\\) log_end_msg 0 ;;
			1\\) log_end_msg 1 ;; # Old process is still running
			*\\) log_end_msg 1 ;; # Failed to start
		esac
		;;
	  *\\)
	  	# Failed to stop
		log_end_msg 1
		;;
	esac
	;;
  *\\)
	#echo "Usage: $SCRIPTNAME {start|stop|restart|reload|force-reload}" >&2
	echo "Usage: $SCRIPTNAME {start|stop|status|restart|force-reload}" >&2
	exit 3
	;;
esac

:
ENDSTR
	return asString
end
