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
defProperty('numServers', 1, "Number of nodes to use for servers")
defProperty('numClients', 1, "Number of clients to use for clients")

# Defines successful topology as "topology.topo-success"
#require("/tmp/#{property.topology}-topo-success.rb")
#eval(File.new("/tmp/#{property.topology}-topo-success.rb").read)
#baseTopo = Topology['/tmp/pxe_slice-2013-01-04t01.00.54-05.00-topo-success']
# nodelist = Topology("#{property.topology}-topo-success").nodes
successTopology = Topology['system:topo:imaged']
nodelist = successTopology.nodes
totalNodes = nodelist.size

if totalNodes < (property.numServers + property.numClients)
		experiment.done
end

serversList = []
clientsList = []

for serverCount in 1..property.numServers
	serversList.push(nodelist.pop())
end

puts "Servers: #{serversList.to_s}"

for clientCount in 1..property.numClients
	clientsList.push(nodelist.pop())
end

puts "Clients: #{clientsList.to_s}"

# Split the nodes into servers and clients

i = 0
defGroup('server', serversList.join(",")) do |node|
  node.net.e0.ip="192.168.1.#{i + 2}"
end

i = 0
# Add node1-2 to the "client list"
defGroup('client', clientsList.join(",")) do |node|
  node.net.e0.ip="192.168.1.#{i + 102}"
end

onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
  Experiment.done
end

