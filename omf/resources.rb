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

# Web resources
defProperty('dataUrl', 'http://my.domain.com/path/to/files', 'Base URL for topology/delay files.')
defProperty('bindingFile', 'jellyfish_topo.bind', 'Filename of AS -> IP:Port binding information.')
defProperty('clickModule', 'delayModule.click', 'Filename of delay module Click script.')
# 'XxX' is a placeholder for the AS number in the experiment script
defProperty('delayConfigServer', 'as_XxX_delay_serv.dat', 'Filename of server delay module configuration.')
defProperty('delayConfigClient', 'as_XxX_delay_client.dat', 'Filename of client delay module configuration.')
defProperty('serverBDB', 'berkeleydb.xml', 'Filename of BerkeleyDB configuration.')
defProperty('mapIpv4', 'map-ipv4.xml', 'Filename of IPv4 mapper configuration.')
defProperty('prefixIpv4', 'prefixes.ipv4', 'Filename of the IPv4 prefixes file (BGP table).')
defProperty('jarFile', 'gnrs.jar', 'Filename of the server/client JAR file.')
defProperty('ggen', 'ggen', 'Filename of the "ggen" script.')
defProperty('gbench', 'gbench', 'Filename of the "gbench" script.')
defProperty('gnrsd', 'gnrsd', 'Filename of the "gnrsd" script.')

# Local applications
# wget - 3 second timeout, quiet output, overwrite if newer
defProperty('wget', '/usr/bin/wget --timeout=3 -qN', 'Location of the wget utility.')
defProperty('clickInstall', '/usr/local/sbin/click-install', 'Location of the click-install utility.')
defProperty('java', '/usr/bin/java', 'Location of the Java launcher.')
