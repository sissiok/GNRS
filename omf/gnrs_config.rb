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
\t<networkConfiguration>/etc/gnrs/net-ipv4_#{node.asNumber}.xml</networkConfiguration>
\t<mappingConfiguration>/etc/gnrs/map-ipv4.xml</mappingConfiguration>
\t<storeType>berkeleydb</storeType>
\t<storeConfiguration>/etc/gnrs/berkeleydb_#{node.asNumber}.xml</storeConfiguration>
\t<numAttempts>2</numAttempts>
\t<timeoutMillis>500</timeoutMillis>
\t<cacheEntries>0</cacheEntries>
\t<defaultExpiration>900000</defaultExpiration>
\t<defaultTtl>30000</defaultTtl>
\t<statsDirectory>/var/gnrs/stats#{node.asNumber}/</statsDirectory>
\t<replicaSelector>random</replicaSelector>
</edu.rutgers.winlab.mfirst.Configuration>
ENDSTR

	return asString
end # makeServerConfig

def makeBerkeleyDBConfig(node)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.storage.bdb.Configuration>
\t<pathToFiles>/var/gnrs/bdb#{node.asNumber}/</pathToFiles>
\t<cacheSizeMiB>16</cacheSizeMiB>
</edu.rutgers.winlab.mfirst.storage.bdb.Configuration>
ENDSTR
	return asString
end # makeBerkeleyDBConfig

def makeServerNetConfig(node)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
\t<bindPort>#{node.port}</bindPort>
\t<bindAddress>#{node.group.ipAddress}</bindAddress>
\t<asynchronousWrite>false</asynchronousWrite>
</edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
ENDSTR

	return asString
end # makeServerNetConfig

def makeClientConfig(client,server)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.client.Configuration>
\t<serverHost>#{server.group.ipAddress}</serverHost>
\t<serverPort>#{server.port}</serverPort>
\t<clientPort>#{client.port}</clientPort>
\t<clientHost>#{client.group.ipAddress}</clientHost>
\t<randomSeed>-1</randomSeed>
\t<statsDirectory>/var/gnrs/stats#{client.asNumber}/</statsDirectory>
</edu.rutgers.winlab.mfirst.client.Configuration>
ENDSTR

	return asString
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
DAEMON=/usr/local/bin/gnrs/gnrsd
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

# Creates a click delay module script for the set
# of servers or clients assigned to an ORBIT node
def makeDelayScript(group,isClient)
	asString = "cla :: Classifier\\(12/0800, -\\);\n"
	asString << "ip_cla :: IPClassifier\\("
	group.nodelist.each { |node|
		asString << " dst udp port #{node.port},"
	}
	asString << "-\\);\n"
	group.nodelist.each { |node|
		asString << "delayMod#{node.asNumber} :: NetDelay\\(\\);\n"
	}
	asString << "FromDevice\\(eth0\\)\n"
	asString << " -> cla\n"
	asString << " -> CheckIPHeader\\(14, CHECKSUM false\\)\n"
	asString << " -> ip_cla;\n"
	asString << "cla[1] -> ToHost;\n"


	group.nodelist.each_with_index { |node, index|
		asString << "ip_cla[#{index}] -> delayMod#{node.asNumber} -> ToHost;\n"
	}
	asString << "ip_cla[#{group.nodelist.length}] -> ToHost;\n"
	return asString
end # makeDelayScript

def makeDelayConfig(serversMap, clientsMap, delayFileName)

	file = File.open(delayFileName)
	delArr = file.readlines
	file.close
	delArr.slice!(0)

	delMat = Array.new(delArr.length) { Array.new(delArr.length) }

	row = 0
	delArr.each { |line|
		col = 0
		elems = line.split
		elems.each { |delAsStr|
			delMat[row][col] = delAsStr.to_i
			col += 1
		}
		row += 1
	}

	info "Finished parsing delay file"

	# Build a delayMod config for each server, include delays
	# from all other servers (other AS), and matching clients (same AS)
	serversMap.each_value { | host |
		host.nodelist.each { | server | 
			delayString = "";
			# Go through all other servers
			serversMap.each_value { |otherHost|
				otherHost.nodelist.each { |otherServer|
					# Skip the same AS (same server)
					next if (server.asNumber == otherServer.asNumber)
					delayString << "#{otherHost.ipAddress},#{otherServer.port},#{delMat[server.asNumber-1][otherServer.asNumber-1]},\n"
				}
			}
			clientsMap.each_value { |clientHost|
				clientHost.nodelist.each { |client|
					# You shouldn't be talking to me unless we're in the same AS
					next if (server.asNumber != client.asNumber)
					delayString << "#{clientHost.ipAddress},#{client.port},5,\n"
				}
			}
			server.delayConfig = delayString
		}
	}

	# Now do all the clients, but thankfully they're simpler
	clientsMap.each_value { |host|
		host.nodelist.each { |client|
			delayString = "";
			# Go through all servers and look for our "match"
			serversMap.each_value {|serverHost|
				serverHost.nodelist.each {|server|
					next if(server.asNumber != client.asNumber)
					delayString << "#{serverHost.ipAddress},#{server.port},5,\n"
				}
			}
			client.delayConfig = delayString
		}
	}
	
end # makeDelayConfig

def makeBindingFile(serversMap)
	asString = ""
	serversMap.each_value { |group|
		group.nodelist.each { |server|
			asString << "#{server.asNumber} #{group.ipAddress} #{server.port}\n"
		}
	}
	return asString
end # makeBindingFile
