#!/bin/ruby
#
# Slightly-more complex example that uses the output of OMF load to generate
# node groups.
# Author: Robert Moore
# Last Modified: Jan 4, 2013
#
# Runtime configuration of propertes can be effected like this:
#  omf exec simple.rb -- --prop1 value1 --prop2 value2

# Resources file location can be configured with:
#   --resourceFile /path/to/file.rb
resourceFile = './resources.rb'

if defined? property and not property.resourceFile.nil?
	resourceFile = property.resourceFile
end

# Read the resources file and execute its code
eval(File.new(resourceFile).read)

# Global constants
CLIENT_GRP_NAME = 'client'
SERVER_GRP_NAME = 'server'

# Main function, used so we can "return" from the experiment early when
# errors are detected.
def main 
	
	serversList = []
	clientsList = []
	
	# Grab the imaged topology (successful nodes) and break them into groups
	info "## Preparing node groups ##"
	success = defineGroups(serversList, clientsList)
	if success == 0
		info "\t#{serversList.count} servers and #{clientsList.count} clients available."
	else 
		error "Unable to prepare nodes. Exiting."
		return
	end
	
	info "## Performing node pre-configuration ##"
	success = prepareNodes(serversList, clientsList)
	if success == 0
		info "\tNode configuration complete."
	else 
		error "Unable to configure nodes. Exiting."
		return;
	end

	info "## Preparing the delay modules ##"
	success = prepareDelayModule(property.dataUrl, property.clickModule)
	if success == 0
		info "Successfully installed and configured delay module on all nodes."
	else
		error "Unable to configure delay module on one or more nodes. Exiting."
		return;
	end
end # main

def defineGroups(serversList, clientsList)
	successTopology = Topology['system:topo:imaged']
	nodelist = successTopology.nodes
	totalNodes = nodelist.size

	if totalNodes < (property.numServers + property.numClients)
		puts "Not enough nodes available. Need #{property.numServers + property.numClients}, but only have #{totalNodes}"
		puts "Consider using a smaller number of servers (numServers) or clients (numClients)."
		return -1
	end

	for serverCount in 1..property.numServers
		serversList.push(nodelist.pop())
	end

	for clientCount in 1..property.numClients
		clientsList.push(nodelist.pop())
	end

	return 0
end # defineGroups

def prepareNodes(serversList, clientsList) 
	# Split the nodes into servers and clients

	i = 0
	defGroup(SERVER_GRP_NAME, serversList.join(",")) do |node|
		node.net.e0.ip="192.168.1.#{i + 2}"
	end

	i = 0
	# Add node1-2 to the "client list"
	defGroup(CLIENT_GRP_NAME, clientsList.join(",")) do |node|
		node.net.e0.ip="192.168.1.#{i + 102}"
	end

	return 0
end # prepareNodes

def prepareDelayModule(baseUrl, clickScript)
	group(SERVER_GRP_NAME).exec("#{property.wget} --timeout=10 -q #{property.dataUrl}/#{property.clickModule}")
end # prepareDelayModule

# Called when all nodes are available for use.
onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
  Experiment.done
end # onEvent

# Invoke the main method to get started
main
