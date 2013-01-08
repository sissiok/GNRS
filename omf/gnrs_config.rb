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
end

def makeServerNetConfig(node)
	asString = <<-ENDSTR
<edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
\t<bindPort>_PORT_</bindPort>
\t<bindAddress>_IPADDR_</bindAddress>
\t<asynchronousWrite>false</asynchronousWrite>
</edu.rutgers.winlab.mfirst.net.ipv4udp.Configuration>
ENDSTR

	return asString.gsub(/_PORT_/,node.port.to_s).gsub(/_IPADDR_/,node.ipAddress.to_s)
end
