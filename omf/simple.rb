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
require ('./gnrs_config.rb')
require ('./statscollect.rb')

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

	if (property.disableDelay.to_s == "") 
		info "## Preparing the delay modules ##"
		success = prepareDelayModule(serversMap, clientsMap, property.dataUrl, property.clickModule)
		if success == 0
			info "\tSuccessfully installed and configured delay module on all nodes."
		else
			error "\tUnable to configure delay module on one or more nodes. Exiting."
			return;
		end
	end

	info "## Installing configuration files ##"
	success = installConfigs(serversMap, clientsMap)
	if success == 0
		info "\tSuccessfully installed configuration files."
	else
		error "\tUnable to install configuration files."
		return;
	end

	info "## Launching servers ##"
	success = launchServers(serversMap)
	if success == 0
		info "\tSuccessfully launched servers."
	else
		error "\tUnable to launch servers."
		return;
	end

	info "## Waiting 5 seconds for servers to start ##"
	wait property.miniWait

	info "## Loading GUIDs ##"
	success = loadGUIDs(clientsMap)
	if success == 0
		info "\tSuccessfully launched clients."
	else
		error "\tUnable to launch clients."
		stopServers(serversMap)
		return;
	end

	info "Waiting #{property.clientWait} for trace to execute."
	wait property.clientWait

	info "## Shutting down servers ##"
	success = stopServers(serversMap)
	if success == 0
		info "\tTerminated servers successfully."
	else
		error "\tUnable to terminate servers."
		return
	end

	info "Collecting statistsics from nodes."
	# clients
	success = collectClientStats(clientsMap, '')
	if success != 0
		error "\tUnable to collect client statistics."
		return
	end

	# servers
	success = collectServerStats(serversMap,'')
	if success != 0
		error "\tUnable to collect server statistics."
		return
	end

	# remove experimental files
	info "Removing experiment-related files from nodes."
	success = removeExperimentFiles(serversMap)
	success |= removeExperimentFiles(clientsMap)

	if success != 0
		error "\tUnable to remove files from one or more nodes. You should reimage the testbed."
		return
	end

end # main

def defineGroups(serversMap, clientsMap)
	successTopology = Topology[property.topology.to_s]
	nodelist = successTopology.nodes
	totalNodes = nodelist.size

	if totalNodes < (property.numServers + property.numClients)
		puts "Not enough nodes available. Need #{property.numServers + property.numClients}, but only have #{totalNodes}"
		puts "Consider using a smaller number of servers (numServers) or clients (numClients)."
		return -1
	end

	asMap = Hash.new

	for serverCount in 1..property.numServers
		node = GNRSNode.new
		node.hostname = nodelist.pop().to_s
		node.asNumber = serverCount
		node.ipAddress = "192.168.1.#{serverCount + 1}"
		node.port = "5001"
		serversMap[node.hostname] = node
		asMap[node.asNumber] = node
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
		node.server = asMap[node.asNumber]
		# Randomly pick a server for this client
		if node.server.nil?
			node.asNumber = rand(property.numServers.to_s.to_i)+1
			node.server = asMap[node.asNumber]
		end
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
	cmd = "#{property.wget} #{property.scriptUrl}/#{property.clickModule}"

	serversMap.each_value { |node|
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		node.group.exec(cmd)
	}

	wait property.miniWait

	# Install the delay module click script
	info "Installing Click delay module"
	cmd = "#{property.clickInstall} -u #{property.clickModule}"

	serversMap.each_value { |node|
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		node.group.exec(cmd)
	}

	wait property.miniWait

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

	wait property.miniWait
	info "Installing node delay configurations"
	client = "cp #{property.delayConfigClient} /click/delayMod/config"
	server  = "cp #{property.delayConfigServer} /click/delayMod/config"
	serversMap.each_value { |node|
		node.group.exec(server.gsub(/XxX/,node.asNumber.to_s))
	}
	clientsMap.each_value { |node|
		node.group.exec(client.gsub(/XxX/,node.asNumber.to_s))
	}

	wait property.miniWait

	# Delete any files we downloaded and no longer need
	info "Cleaning up temporary files"

	serversMap.each_value { |node|
		cmd = "rm #{property.clickModule}"
		node.group.exec(cmd)
		cmd = "rm #{property.delayConfigServer}".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}
	clientsMap.each_value { |node|
		cmd = "rm #{property.clickModule}"
		node.group.exec(cmd)
		cmd = "rm #{property.delayConfigClient}".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}

	return 0
end # prepareDelayModule

def installConfigs(serversMap, clientsMap)

	info "Creating required directories"

	mkVar = "mkdir -p /var/gnrs/stats"
	mkEtc = "mkdir -p /etc/gnrs"
	mkBin = "mkdir -p /usr/local/bin/gnrs/"
	
	serversMap.each_value { |node|
		node.group.exec(mkVar)
		node.group.exec(mkEtc)
		node.group.exec(mkBin)
	}

	clientsMap.each_value { |node|
		node.group.exec(mkVar)
		node.group.exec(mkEtc)
		node.group.exec(mkBin)
	}

	wait property.microWait

	info "Creating server configuration files."
	serversMap.each_value { |node|
		# Main server config
		configContents = makeServerConfig(node)
		cmd = "echo '#{configContents}' >/etc/gnrs/server.xml"
		node.group.exec(cmd)

		# Networking config
		configContents = makeServerNetConfig(node)
		cmd = "echo '#{configContents}' >/etc/gnrs/net-ipv4_#{node.asNumber}.xml"
		node.group.exec(cmd)

		# Download static files

		# Binding file
		cmd = "#{property.wget} #{property.dataUrl}/#{property.bindingFile}"
		node.group.exec(cmd)
		# IPv4 Prefix File (BGP Table)
		cmd = "#{property.wget} #{property.dataUrl}/#{property.prefixIpv4}"
		node.group.exec(cmd)
		# BerkeleyDB Config
		cmd = "#{property.wget} #{property.dataUrl}/#{property.serverBDB}"
		node.group.exec(cmd)
		# IPv4 Mapper Configuration
		cmd = "#{property.wget} #{property.dataUrl}/#{property.mapIpv4}"
		node.group.exec(cmd)
		# Jar file
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.jarFile}"
		node.group.exec(cmd)
		# GNRSD script
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.gnrsd}"
		node.group.exec(cmd)
		# GNRSD Init script
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.gnrsdInit}"
		node.group.exec(cmd)

	}
	
	info "Creating client configuration files."
	clientsMap.each_value { |node|
		# Main client config
		configContents = makeClientConfig(node,node.server)
		cmd = "echo '#{configContents}' >/etc/gnrs/client.xml"
		node.group.exec(cmd)
		# Download static files

		# Jar file
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.jarFile}"
		node.group.exec(cmd)
		# GGen script
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.ggen}"
		node.group.exec(cmd)
		# GBench script
		cmd = "#{property.wget} #{property.scriptUrl}/#{property.gbench}"
		node.group.exec(cmd)
		# Client trace file
		cmd = "#{property.wget} #{property.dataUrl}/#{property.clientTrace}".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}


	wait property.miniWait

	info "Installing server files"

	# Install static files
	serversMap.each_value { |node|
		# Binding file
		cmd = "mv #{property.bindingFile} /etc/gnrs/"
		node.group.exec(cmd)
		# BerkeleyDB Config
		cmd = "mv #{property.serverBDB} /etc/gnrs/"
		node.group.exec(cmd)
		# IPv4 Mapper Configuration
		cmd = "mv #{property.mapIpv4} /etc/gnrs/"
		node.group.exec(cmd)
		# IPv4 Prefix File (BGP Table)
		cmd = "mv #{property.prefixIpv4} /etc/gnrs/"
		node.group.exec(cmd)
		# Jar file
		cmd = "mv #{property.jarFile} /usr/local/bin/gnrs/"
		node.group.exec(cmd)
		# GNRSD Script
		cmd = "chmod +x #{property.gnrsd}"
		node.group.exec(cmd)
		cmd = "mv #{property.gnrsd} /usr/local/bin/gnrs/"
		node.group.exec(cmd)
		# GNRSD Init script
		cmd = "chmod +x #{property.gnrsdInit}"
		node.group.exec(cmd)
		cmd = "mv #{property.gnrsdInit} /etc/init.d/gnrsd"
		node.group.exec(cmd)
		# Update rc.d scripts
		cmd = "#{property.updateRc} gnrsd stop 2 0 1 2 3 4 5 6 ."
		node.group.exec(cmd)
	}

	info "Installing client files"

	clientsMap.each_value { |node|
	 	# JAR file
		cmd = "mv #{property.jarFile} /usr/local/bin/gnrs/"
		node.group.exec(cmd)
		# GBench script
		cmd = "chmod +x #{property.gbench}"
		node.group.exec(cmd)
		cmd = "mv #{property.gbench} /usr/local/bin/gnrs/"
		node.group.exec(cmd)
		# GGen script
		cmd = "chmod +x #{property.ggen}"
		node.group.exec(cmd)
		cmd = "mv #{property.ggen} /usr/local/bin/gnrs/"
		node.group.exec(cmd)
		# Trace file
		cmd = "mv #{property.clientTrace} /etc/gnrs/".gsub(/XxX/,node.asNumber.to_s)
		node.group.exec(cmd)
	}

	return 0
end # installConfigs

def launchServers(serversMap)

	cmd = "service gnrsd start"

	serversMap.each_value { |node|
		info "Launching server on #{node.to_s}"
		node.group.exec(cmd)
	}

	return 0
end #launchServers

def loadGUIDs(clientsMap)

	# 3 parameters to gbench: client config, trace file, inter-message send time in microseconds
	baseCmd = "/usr/local/bin/gnrs/#{property.gbench} /etc/gnrs/client.xml /etc/gnrs/#{property.clientTrace} #{property.messageDelay}"

	clientsMap.each_value { |node|
		cmd = baseCmd.gsub(/XxX/,node.asNumber.to_s)
		info "#{node.to_s}: Execing '#{cmd}'"
		node.group.exec(cmd)
	}

	return 0
end # loadGUIDs

def genLookups(clientsMap)
	baseCmd = "/usr/local/bin/gnrs/#{property.ggen} /etc/gnrs/client.xml #{property.numLookups} #{property.messageDelay} 1"

	clientsMap.each_value { |node|
		info "#{node.to_s}: Execing '#{baseCmd}'"
		node.group.exec(baseCmd)
	}

	return 0
end # genLookups

def stopServers(serversMap)
	info "Stopping GNRS servers"

	cmd = "service gnrsd stop"

	serversMap.each_value { |node|
		node.group.exec(cmd)
	}

	return 0
end # stopServers

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

