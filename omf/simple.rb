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


# Initial work of configuring topology, groups, and readying for node "up"
def doInitSetup

	# Check initial parameters, make sure everything is sane.
	success = checkParams
	if success != 0
		error "*** One or more parameters was invalid. See messages above for more details. ***"
		return success
	end

	serversMap = Hash.new
	clientsMap = Hash.new

	# Grab the imaged topology (successful nodes) and break them into groups
	info "## Selecting servers and clients from available nodes ##"
	success = defineGroups(serversMap, clientsMap)
	if success == 0
		info "\t#{serversMap.length} servers and #{clientsMap.length} clients available."
	else 
		error "\tUnable to select nodes. Exiting."
		return success
	end

	
	info "## Defining OMF node groups ##"
	success = buildGroups(serversMap, clientsMap)
	if success == 0
		info "\tNode groups defined."
	else 
		error "\tUnable to define groups. Exiting."
		return success
	end

	return success, serversMap, clientsMap
end

# Verify that the input parameters are sensible and won't cause any problems
# later on in the experiment.
def checkParams
	if property.numServers.to_s.to_i < 1
		error "Must define at least 1 server."
		return -1
	elsif property.numClients.to_s.to_i < 1
		error "Must define at least 1 client."
		return -1
	end

	return 0
end # checkParams


# Main function, used so we can "return" from the experiment early when
# errors are detected.
def doMainExperiment(serversMap, clientsMap)
	info "## Configuring node network interfaces ##"
	success = prepareNodes(serversMap, clientsMap)
	if success == 0
		info "\tNetwork configuration complete."
	else 
		error "\tUnable to configure networking. Exiting."
		return;
	end

	info "## Preparing the delay modules ##"
	success = prepareDelayModule(property.dataUrl, property.clickModule)
	if success == 0
		info "\tSuccessfully installed and configured delay module on all nodes."
	else
		error "\tUnable to configure delay module on one or more nodes. Exiting."
		return;
	end
end # main

def defineGroups(serversMap, clientsMap)
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
		serversMap[node.hostname] = node
	end
	
	info "Servers:"
	serversMap.each do |key, value|
		info "\t#{key} => #{value}"
	end

	for clientCount in 1..property.numClients
		node = GNRSNode.new
		node.hostname = nodelist.pop().to_s
		node.asNumber = clientCount
		node.ipAddress = "192.168.1.#{clientCount + 101}"
		node.port = "4001"
		clientsMap[node.hostname] = node
	end
	
	info "Clients:"
	clientsMap.each do |key, value|
		info "\t#{key} => #{value}"
	end

	return 0
end # defineGroups

def buildGroups(serversMap, clientsMap)
	# Split the nodes into servers and clients
	i = 0
	serverString = ""
	serversMap.each_value do |node|
		serverString += node.hostname
		if i != serversMap.length-1
			serverString += ","
		end
		i += 1
	end


	defGroup(SERVER_GRP_NAME, serverString) do |node|
		# Nothing to do, wait until nodes are up in prepareNodes
	end

	
	# Now the clients
	i = 0
	clientString = ""
	clientsMap.each_value do |node|
		clientString += node.hostname
		if i != clientsMap.length-1
			clientString += ","
		end
		i += 1
	end

	defGroup(CLIENT_GRP_NAME, clientString) do | node |
		# Again, nothing to do yet
	end

	return 0
end

def prepareNodes(serversMap, clientsMap) 
	
	serverNs = NodeSet[SERVER_GRP_NAME].each { |node|
		gnrsNode = serversMap[node.name.to_s]
		info "Setting IP of #{node.name.to_s} to #{gnrsNode.ipAddress}"
		node.net.e0.ip=gnrsNode.ipAddress
	}


	clientNs = NodeSet[CLIENT_GRP_NAME].each { | node |
		gnrsNode = clientsMap[node.name.to_s]
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

# Load resources, get topology, define groups
success, serversMap, clientsMap = doInitSetup

# Only register callback if we were able to prepare the experiment.
if success == 0
	info "Awaiting node readiness"
	# Called when all nodes are available for use.
	onEvent(:ALL_UP) do |event|
		if success 
			info "GNRS: All nodes are up."
			doMainExperiment(serversMap, clientsMap)
		end
		Experiment.done
	end # onEvent
else
	Experiment.done
end

