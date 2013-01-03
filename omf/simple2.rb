#!/bin/ruby
#
# Slightly-more complex example that uses the output of OMF load to generate
# node groups.
# Author: Robert Moore
# Last Modified: Dec 21, 2012
#
# Runtime configuration of propertes can be effected like this:
#  omf exec simple.rb -- --prop1 value1 --prop2 value2

# Global properties (pushed to nodes)
defProperty('topology', 'pxe_slice-2013-01-03t00.00.00-00.00', "Topology basename from 'omf load' command.")
defProperty('numServers', 1, "Number of nodes to use for servers")
defProperty('numClients', 1, "Number of clients to use for clients")

# Defines successful topology as "topology.topo-success"
require("/tmp/#{property.topology}-topo-success.rb")

nodelist = topology.topo-success.nodes
totalNodes = nodelist.count

if totalNodes < (property.numServers + property.numClients)
		experiment.done
end


nodelist.each do |node|

end

# Split the nodes into servers and clients

defGroup('server', "node1-1.#{property.domain}") do |node|
  node.net.e0.ip="192.168.1.2"
end

# Add node1-2 to the "client list"
defGroup('client', "node1-2.#{property.domain}") do |node|
  node.net.e0.ip="192.168.1.102"
end

onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
  Experiment.done
end

