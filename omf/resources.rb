#!/bin/ruby
#
# Author: Robert Moore
# Last Modified: Jan 4, 2013
#
# A "resources" file that will be evaluated by the experiment script
# before any other code.  Primarily used to set default property values
# that can be overridden at runtime by commandline arguments.
#

# Basic properties (pushed to nodes)
defProperty('numServers', 1, "Number of nodes to use for servers")
defProperty('numClients', 1, "Number of clients to use for clients")
defProperty('microWait', 2, "Number of seconds to wait for copies, writes, etc. to occur.")
defProperty('miniWait', 5, 'Number of seconds to wait for things like file downloads.')
defProperty('largeWait', 20, 'Number of seconds to wait for "heavy" things.')
defProperty('clientWait', 120, 'Number of seconds to wait for each client to run.')
defProperty('disableDelay', nil, 'Switch to disable the delay module.')
defProperty('serversPerNode', 1, 'Maximum number of server processes per node.')
defProperty('sNodes', 1, 'Number of nodes used for servers.')
defProperty('cNodes', 1, 'Number of nodes used for clients.')

# Primary topology from which to draw nodes
defProperty('topology', 'system:topo:imaged', 'The primary topology from which to draw nodes.')

# Web resources
defProperty('dataUrl', 'http://my.domain.com/path/to/files', 'Base URL for topology/delay files.')
defProperty('scriptUrl', 'https://bitbucket.org/romoore/gnrs/downloads', 'Base URL for script files.')
defProperty('tarUrl', 'https://my.domain.com/path/to/files', 'Base URL for tarballed files.')
#defProperty('bindingFile', 'topology.bind', 'Filename of AS -> IP:Port binding information.')
defProperty('routeFile', 'topology.data.route', 'Filename of delay matrix for topology.')
defProperty('clickModule', 'delayModule.click', 'Filename of delay module Click script.')
# 'XxX' is a placeholder for the AS number in the experiment script
#defProperty('delayConfigServer', 'as_XxX_delay_serv.dat', 'Filename of server delay module configuration.')
#defProperty('delayConfigClient', 'as_XxX_delay_client.dat', 'Filename of client delay module configuration.')
#defProperty('serverBDB', 'berkeleydb.xml', 'Filename of BerkeleyDB configuration.')
defProperty('mapIpv4', 'map-ipv4.xml', 'Filename of IPv4 mapper configuration.')
defProperty('prefixIpv4', 'prefixes.ipv4', 'Filename of the IPv4 prefixes file (BGP table).')
defProperty('tmpDir', '/tmp/gnrs', 'Temporary directory in which to build the archives.')

# Client/Server compiled JAR file
defProperty('jarFile', 'gnrs.jar', 'Filename of the server/client JAR file.')
# Client Scripts
defProperty('ggen', 'ggen', 'Filename of the "ggen" script.')
defProperty('gbench', 'gbench', 'Filename of the "gbench" script.')
# Server launch script
defProperty('gnrsd', 'gnrsd', 'Filename of the "gnrsd" script.')
# Server rc.d (init.d) script
defProperty('gnrsdInit', 'gnrsd.init', 'Filename of the "gnrsd" init.d script.')
# Client trace file
defProperty('clientTrace', 'client_XxX.trace', 'Trace file for client "XxX" (will be replaced with AS number at runtime.')
# Client lookup count
defProperty('numLookups', 10000, 'Number of lookups to perform.')
# Client inter-message delay (microseconds)
defProperty('messageDelay', 2000, 'Number of microseconds between each message.')

# Local applications
# wget - 3 second timeout, quiet output, overwrite if newer
defProperty('wget', '/usr/bin/wget --timeout=3 -qN', 'Location of the wget utility.')
defProperty('clickInstall', '/usr/local/sbin/click-install -u', 'Location of the click-install utility.')
defProperty('clickUninstall', '/usr/local/sbin/click-uninstall', 'Location of the click-uninstall utility.')
defProperty('java', '/usr/bin/java', 'Location of the Java launcher.')
defProperty('updateRc', '/usr/sbin/update-rc.d', 'Location of update-rc.d utility.')
defProperty('scp', 'scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no', 'SCP utility to copy files.')
