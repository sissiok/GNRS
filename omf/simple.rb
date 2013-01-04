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

# Resources file location can be configured with:
#   --resourceFile /path/to/file.rb
defProperty('resourceFile', './resource.rb', 'Experiment resources configuration')

# Read the resources file and execute its code
eval(File.new(property.resourceFile).read)

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
	
	info "Servers:"
	serversList.each do |item|
		info "\t#{item}"
	end

	for clientCount in 1..property.numClients
		clientsList.push(nodelist.pop())
	end
	
	info "Clients:"
	clientsList.each do |item|
		info "\t#{item}"
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
	cmd = "rm #{property.bindingFile} #{property.clickModule}"
	info "Executing '#{cmd}'"
	group(SERVER_GRP_NAME).exec(cmd)
	group(CLIENT_GRP_NAME).exec(cmd)

	return 0
end # prepareDelayModule

# Called when all nodes are available for use.
onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
  Experiment.done
end # onEvent

# Invoke the main method to get started
main
