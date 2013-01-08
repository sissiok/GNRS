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
	success = prepareDelayModule(serversMap, clientsMap, property.dataUrl, property.clickModule)
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
	serversMap.each_value do |node|
		grp = defGroup(node.hostname, node.hostname)
		node.group = grp
	end

	# Now the clients
	clientsMap.each_value do |node|
		grp = defGroup(node.hostname, node.hostname)
		node.group = grp
	end

	return 0
end

def prepareNodes(serversMap, clientsMap) 

	serversMap.each_value { | node | 
		info "Setting IP of #{node.hostname} to #{node.ipAddress}"
		node.group.net.e0.ip = node.ipAddress
	}

	clientsMap.each_value { | node | 
		info "Setting IP of #{node.hostname} to #{node.ipAddress}"
		node.group.net.e0.ip = node.ipAddress
	}

	return 0
end # prepareNodes

def prepareDelayModule(serversMap, clientsMap, baseUrl, clickScript)

	# Download delay module click script
	info "Downloading delay module script"
	cmd = "#{property.wget} #{property.dataUrl}/#{property.clickModule}"
	info "Executing '#{cmd}'"

	serversMap.each_value { |node|
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		node.group.exec(cmd)
	}

	wait 5

	# Install the delay module click script
	info "Installing Click delay module"
	cmd = "#{property.clickInstall} -u #{property.clickModule}"
	info "Executing '#{cmd}'"

	serversMap.each_value { |node|
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		node.group.exec(cmd)
	}

	wait 5

	# Download and install the delay module configuration file
	info "Retrieving node delay configurations"
	server = "#{property.wget} #{property.dataUrl}/#{property.delayConfigServer}"
	client = "#{property.wget} #{property.dataUrl}/#{property.delayConfigClient}"

	serversMap.each_value { |node|
		node.group.exec(server.gsub(/XxX/,node.asNumber.to_s))
	}
	clientsMap.each_value { |node|
		node.group.exec(client.gsub(/XxX/,node.asNumber.to_s))
	}

	wait 5

	info "Installing node delay configurations"
	server = "cp #{property.delayConfigClient} /click/delayMod/config"
	client  = "cp #{property.delayConfigServer} /click/delayMod/config"
	serversMap.each_value { |node|
		node.group.exec(server.gsub(/XxX/,node.asNumber.to_s))
	}
	clientsMap.each_value { |node|
		node.group.exec(client.gsub(/XxX/,node.asNumber.to_s))
	}

	wait 5

	# Delete any files we downloaded and no longer need
	info "Cleaning up temporary files"
	cmd = "rm #{property.clickModule}"
	info "Executing '#{cmd}'"

	serversMap.each_value { |node|
		node.group.exec(cmd)
		cmd = "rm #{property.delayConfigServer}".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		node.group.exec(cmd)
		cmd = "rm #{property.delayConfigClient}".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}

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

