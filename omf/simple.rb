#!/bin/ruby
#
# Simple GNRS experiment that performs the tasks outlined in the project Wiki.
# See <https://bitbucket.org/romoore/gnrs/wiki/Running%20an%20Experiment%20on%20Orbit>
# for more details.
#
# Author: Robert Moore
# Last Modified: Jan 4, 2013
#
# Runtime configuration of propertes can be effected like this:
#  omf exec simple.rb -- --prop1 value1 --prop2 value2

# Import the GNRSNode class
require ('./gnrs_node.rb')

# Resources file location can be configured with:
#   --resourceFile /path/to/file.rb
defProperty('resourceFile', './resources.rb', 'Experiment resources configuration')

# Read the resources file and execute its code
eval(File.new(property.resourceFile).read)

# Global constants
CLIENT_GRP_NAME = 'client'
SERVER_GRP_NAME = 'server'

# Main function, used so we can "return" from the experiment early when
# errors are detected.
def main 
	
	serversList = Hash.new 
	clientsList = Hash.new
	
	# Grab the imaged topology (successful nodes) and break them into groups
	info "## Preparing node groups ##"
	success = defineGroups(serversList, clientsList)
	if success == 0
		info "\t#{serversList.length} servers and #{clientsList.length} clients available."
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
		node = GNRSNode.new
		node.hostname = nodelist.pop().to_s
		node.asNumber = serverCount
		node.ipAddress = "192.168.1.#{serverCount + 1}"
		node.port = "5001"
		serversList[node.hostname] = node
	end
	
	info "Servers:"
	serversList.each do |key, value|
		info "\t#{key} => #{value}"
	end

	for clientCount in 1..property.numClients
		node = GNRSNode.new
		node.hostname = nodelist.pop().to_s
		node.asNumber = clientCount
		node.ipAddress = "192.168.1.#{clientCount + 101}"
		node.port = "4001"
		clientsList[node.hostname] = node
	end
	
	info "Clients:"
	clientsList.each do |key, value|
		info "\t#{key} => #{value}"
	end

	return 0
end # defineGroups

def prepareNodes(serversList, clientsList) 
	# Split the nodes into servers and clients

	i = 0
	serverString = ""
	serversList.each_value do |node|
		serverString += node.hostname
		if i != serversList.length-1
			serverString += ","
		end
		i += 1
	end

	info serverString

	i = 0
	defGroup(SERVER_GRP_NAME, serverString) do |node|
#	info node.to_yaml
	#info node.nodeID
#		gnrsNode = serversList[node.to_s]
#		node.net.e0.ip=gnrsNode.ipAddress
	end

	serverNs = NodeSet[SERVER_GRP_NAME].each { |node|
		gnrsNode = serversList[node.name.to_s]
		info "Setting IP of #{node.name.to_s} to #{gnrsNode.ipAddress}"
		node.net.e0.ip=gnrsNode.ipAddress
	}

	i = 0
	clientString = ""
	clientsList.each_value do |node|
		clientString += node.hostname
		if i != clientsList.length-1
			clientString += ","
		end
		i += 1
	end

	i = 0
	# Add node1-2 to the "client list"
	defGroup(CLIENT_GRP_NAME, clientString) do | node |
	end

	clientNs = NodeSet[CLIENT_GRP_NAME].each { | node |
		gnrsNode = clientsList[node.name.to_s]
		info "Setting IP of #{node.name.to_s} to #{gnrsNode.ipAddress}"
		node.net.e0.ip=gnrsNode.ipAddress
	}

	return 0
end # prepareNodes

def prepareDelayModule(baseUrl, clickScript)

	# Download delay module click script
	info "Downloading delay module script"
	cmd = "#{property.wget} --timeout=3 -q #{property.dataUrl}/#{property.clickModule}"
	info "Executing '#{cmd}'"
	group(SERVER_GRP_NAME).exec(cmd)
	group(CLIENT_GRP_NAME).exec(cmd)

	wait 5

	# Download the appropriate delay module configuration file
	info "Installing Click delay module"
	cmd = "#{property.clickInstall} -u #{property.clickModule}"
	info "Executing '#{cmd}'"
	group(SERVER_GRP_NAME).exec(cmd)
	group(CLIENT_GRP_NAME).exec(cmd)

	wait 5

	# Delete any files we downloaded and no longer need
	info "Cleaning up temporary files"
	cmd = "rm #{property.clickModule}"
	info "Executing '#{cmd}'"
	group(SERVER_GRP_NAME).exec(cmd)
	group(CLIENT_GRP_NAME).exec(cmd)

	return 0
end # prepareDelayModule

# Called when all nodes are available for use.
onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
end # onEvent

# Invoke the main method to get started
main
Experiment.done
