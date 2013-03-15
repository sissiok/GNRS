#!/bin/ruby
#
# Module containing GNRS experiment-related functionality.
# See <https://bitbucket.org/romoore/gnrs/wiki/Running%20an%20Experiment%20on%20Orbit>
# for more details.
#
# Author: Robert Moore
# Last Modified: Jan 23, 2013
#
# Runtime configuration of propertes can be effected like this:
#  omf exec simple.rb -- --prop1 value1 --prop2 value2

# Import the GNRSNode class
require ('./gnrs_node.rb')
require ('./gnrs_group.rb')
require ('./gnrs_config.rb')
require ('./statscollect.rb')

# Resources file location can be configured with:
#   --resourceFile /path/to/file.rb
defProperty('resourceFile', './resources.rb', 'Experiment resources configuration')

# Read the resources file and execute its code
eval(File.new(property.resourceFile).read)


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

	allNodes = Topology[property.topology.to_s]

	# Grab the imaged topology (successful nodes) and break them into groups
	success = defineGroups(allNodes, serversMap, clientsMap)
	if success == 0
	else 
		return success
	end

	return success, serversMap, clientsMap
end # doInitSetup

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

def defineGroups(topology, serversMap, clientsMap)
	nodelist = topology.nodes
	totalNodes = nodelist.size

	sNodes = property.sNodes.to_s.to_i
	cNodes = property.cNodes.to_s.to_i
	numServers = property.numServers.to_s.to_i
	numClients = property.numClients.to_s.to_i

	serversPerNode = []
	clientsPerNode = []


	quotient, remainder = numServers.divmod(sNodes)

	
	if(quotient >= 1 || remainder > 0)
		for i in 1..sNodes
			num = quotient
			if(remainder > 0)
				num += 1
				remainder -= 1
			end
			serversPerNode << num
		end
	else
		for i in 1..sNodes
			serversPerNode << 1
		end
	end

	quotient, remainder = numClients.divmod(cNodes)

	
	if(quotient >= 1 || remainder > 0)
		for i in 1..cNodes
			num = quotient
			if(remainder > 0)
				num += 1
				remainder -= 1
			end
			clientsPerNode << num
		end
	else
		for i in 1..cNodes
			clientsPerNode << 1
		end
	end

	###############################

	# Let's set the defGroup/nodes
	asNumber = 1
	ipOffset = 0
	
	asMap = Hash.new
	
	serversPerNode.each { |numServers|
		remainingServers = numServers
		hostname = nodelist.pop().to_s
		group = defGroup(hostname, hostname)
		gnrsGroup = GNRSGroup.new
		gnrsGroup.hostname = hostname
		gnrsGroup.group = group
		gnrsGroup.nodelist = []
		gnrsGroup.ipAddress = "192.168.1.#{ipOffset + 1}"
		serversMap[hostname] = gnrsGroup

		for i in 1..remainingServers
			node = GNRSNode.new
			node.asNumber = asNumber
			node.port = "500#{i}"
			node.group = gnrsGroup
			gnrsGroup.nodelist << node
			asMap[node.asNumber] = node
			asNumber += 1
		end
		ipOffset += 1
	}
	maxAsNumber = asNumber - 1

	ipOffset = 0
	asNumber = 0
	
	clientsPerNode.each { |numClients|
		remainingClients = numClients
		hostname = nodelist.pop().to_s
		group = defGroup(hostname, hostname)
		gnrsGroup = GNRSGroup.new
		gnrsGroup.hostname = hostname
		gnrsGroup.group = group
		gnrsGroup.nodelist = []
		gnrsGroup.ipAddress = "192.168.1.#{ipOffset + 101}"
		clientsMap[hostname] = gnrsGroup

		for i in 1..remainingClients
			node = GNRSNode.new
			node.asNumber = (asNumber % maxAsNumber) + 1;
			node.server = asMap[node.asNumber];
			node.port = "400#{i}"
			node.group = gnrsGroup
			gnrsGroup.nodelist << node
			asNumber += 1
		end

		ipOffset += 1
		
	}

	return 0
end # defineGroups

def prepareNodes(serversMap, clientsMap) 

	serversMap.each_value { | group | 
		group.group.net.e0.ip = group.ipAddress
	}

	clientsMap.each_value { | group | 
		group.group.net.e0.ip = group.ipAddress
	}

	return 0
end # prepareNodes

def prepareDelayModule(serversMap, clientsMap, baseUrl, clickScript)

	info "Building delay module Click script"
	# Download delay module click script
	#cmd = "#{property.wget} #{property.scriptUrl}/#{property.clickModule}"

	serversMap.each_value { |group|
		clickScript = makeDelayScript(group,false)
		cmd = "echo '#{clickScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >/#{property.clickModule}"
		group.group.exec(cmd)
	}
	clientsMap.each_value { |group|
		clickScript = makeDelayScript(group,true)
		cmd = "echo '#{clickScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >/#{property.clickModule}"
		group.group.exec(cmd)
	}

	wait property.miniWait

	info "Installing delay module click script"

	# Install the delay module click script

	serversMap.each_value { |group|
		cmd = "#{property.clickInstall} -u #{property.clickModule}"
		group.group.exec(cmd)
	}
	clientsMap.each_value { |group|
		cmd = "#{property.clickInstall} -u #{property.clickModule}"
		group.group.exec(cmd)
	}

	wait property.miniWait


	info "Downloading topology route file"
	system("#{property.wget} #{property.dataUrl}/#{property.routeFile}")

	info "Building delay configurations"
	# This function will set the "delayConfig" member of each server/client
	makeDelayConfig(serversMap,clientsMap,property.routeFile.to_s)

	info "Installing the delay module configurations"

	asCount = Hash.new(0)

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "echo '#{node.delayConfig}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >/delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat"
			node.group.group.exec(cmd)
			cmd = "cp /delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat /click/delayMod#{node.asNumber}R#{asCount[node.asNumber]}/config"
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}

	asCount = Hash.new(0)

	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "echo '#{node.delayConfig}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >/delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat"
			node.group.group.exec(cmd)
			cmd = "cp /delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat /click/delayMod#{node.asNumber}R#{asCount[node.asNumber]}/config"
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}


	wait property.miniWait

	# Delete any files we downloaded and no longer need
	info "Deleting temporary files"

	system("rm #{property.routeFile}");

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "rm #{property.clickModule}"
			node.group.group.exec(cmd)
			cmd = "rm #{property.delayConfigServer}".gsub(/XxX/,node.asNumber.to_s)
			node.group.group.exec(cmd)
		}
	}
	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "rm #{property.clickModule}"
			node.group.group.exec(cmd)
			cmd = "rm #{property.delayConfigClient}".gsub(/XxX/,node.asNumber.to_s)
			node.group.group.exec(cmd)
		}
	}

	return 0
end # prepareDelayModule

def installConfigs(serversMap, clientsMap)

	info "Creating required directories"
	mkVar = "mkdir -p /var/gnrs/stats"
	mkEtc = "mkdir -p /etc/gnrs"
	mkBin = "mkdir -p /usr/local/bin/gnrs/"
	
	serversMap.each_value { |group|
		group.nodelist.each { |server|
			group.group.exec("#{mkVar}#{server.asNumber}")
		}
		group.group.exec(mkEtc)
		group.group.exec(mkBin)
	}

	clientsMap.each_value { |group|
		group.nodelist.each { |server|
			group.group.exec("#{mkVar}#{server.asNumber}")
		}
		group.group.exec(mkEtc)
		group.group.exec(mkBin)
	}

	wait property.microWait

	info "Downloading server configuration files."

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			# Main server config
			configContents = makeServerConfig(node)
			cmd = "echo '#{configContents}' >/etc/gnrs/server_#{node.asNumber}.xml"
			node.group.group.exec(cmd)

			# Networking config
			configContents = makeServerNetConfig(node)
			cmd = "echo '#{configContents}' >/etc/gnrs/net-ipv4_#{node.asNumber}.xml"
			node.group.group.exec(cmd)

			# Download static files

			# Binding file
			#cmd = "#{property.wget} #{property.dataUrl}/#{property.bindingFile}"
			#node.group.group.exec(cmd)
			# IPv4 Prefix File (BGP Table)
			cmd = "#{property.wget} #{property.dataUrl}/#{property.prefixIpv4}"
			node.group.group.exec(cmd)
			# IPv4 Mapper Configuration
			cmd = "#{property.wget} #{property.dataUrl}/#{property.mapIpv4}"
			node.group.group.exec(cmd)
			# Jar file
			cmd = "#{property.wget} #{property.scriptUrl}/#{property.jarFile}"
			node.group.group.exec(cmd)
			# GNRSD script
			cmd = "#{property.wget} #{property.scriptUrl}/#{property.gnrsd}"
			node.group.group.exec(cmd)
			# GNRSD Init script
			#cmd = "#{property.wget} #{property.scriptUrl}/#{property.gnrsdInit}"
			#node.group.group.exec(cmd)
		}
	}

	info "Downloading client configuration files"
	asCount = Hash.new(0)
	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			# Main client config
			configContents = makeClientConfig(node,node.server,asCount[node.asNumber])
			cmd = "echo '#{configContents}' >/etc/gnrs/client#{node.asNumber}.xml"
			node.group.group.exec(cmd)
			# Download static files

			# Jar file
			cmd = "#{property.wget} #{property.scriptUrl}/#{property.jarFile}"
			node.group.group.exec(cmd)
			# GGen script
			cmd = "#{property.wget} #{property.scriptUrl}/#{property.ggen}"
			node.group.group.exec(cmd)
			# GBench script
			cmd = "#{property.wget} #{property.scriptUrl}/#{property.gbench}"
			node.group.group.exec(cmd)
			# Client trace file
			cmd = "#{property.wget} #{property.dataUrl}/#{property.clientTrace}".gsub(/XxX/,node.asNumber.to_s)
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}


	wait property.miniWait

	info "Installing server configuration files"

	# Build AS binding file
	asBinding = makeBindingFile(serversMap)

	# Install static files
	serversMap.each_value { |node|
		# Binding file
		cmd = "echo '#{asBinding}' >/etc/gnrs/topology.bind"
		node.group.exec(cmd)
		# BerkeleyDB Config
		node.nodelist.each { |server|
			bdb = makeBerkeleyDBConfig(server)
			cmd = "echo '#{bdb}' >/etc/gnrs/berkeleydb_#{server.asNumber}.xml"
			node.group.exec(cmd)
		}
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
	}

	info "Installing client configuration files"

	clientsMap.each_value { |group|
		# JAR file
		cmd = "mv #{property.jarFile} /usr/local/bin/gnrs/"
		group.group.exec(cmd)
		# GBench script
		cmd = "chmod +x #{property.gbench}"
		group.group.exec(cmd)
		cmd = "mv #{property.gbench} /usr/local/bin/gnrs/"
		group.group.exec(cmd)
		# GGen script
		cmd = "chmod +x #{property.ggen}"
		group.group.exec(cmd)
		cmd = "mv #{property.ggen} /usr/local/bin/gnrs/"
		group.group.exec(cmd)
		group.nodelist.each { |node|
			# Trace file
			cmd = "mv #{property.clientTrace} /etc/gnrs/".gsub(/XxX/,node.asNumber.to_s)
			group.group.exec(cmd)
		}
	}

	info "Installing init scripts"
	installInit(serversMap)

	return 0
end # installConfigs

def installInit(servers)
	info "Creating server init scripts"
	servers.each_value { |group|
		group.nodelist.each { |node|

			initScript = makeServerInit(node)

			# The sed command below will replace any escaped ( or ) characters on the
			# node side.  This is because the ResourceController (RC) in OMF will
			# crash if the string contains unbalanced ( or ) characters.
			# Big thanks to Ben Firner for the regular expression
			cmd = "echo '#{initScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >/etc/init.d/gnrsd_#{node.asNumber}"
			node.group.group.exec(cmd)
		}
	}

	wait property.microWait
	info "Updating permissions for server init scripts"

	servers.each_value { |group|
		group.nodelist.each { |node|
			cmd = "chmod +x /etc/init.d/gnrsd_#{node.asNumber}"
			node.group.group.exec(cmd)
		}
	}

	wait property.microWait
	info "Installing server init scripts."

	servers.each_value { |group|
		group.nodelist.each { |node|
			# Update rc.d scripts
			cmd = "#{property.updateRc} gnrsd_#{node.asNumber} stop 2 0 1 2 3 4 5 6 ."
			node.group.group.exec(cmd)
		}
	}
end

def launchServers(serversMap)

	info "Launching server processes"
	serversMap.each_value { |group|
		info "\t#{group}"
	}


	serversMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "service gnrsd_#{node.asNumber} start"
			node.group.group.exec(cmd)
		}
	}

	return 0
end #launchServers

def loadGUIDs(clientsMap)

	info "Launching trace clients"
	clientsMap.each_value { |group|
		info "\t#{group}"
	}	


	# 3 parameters to gbench: client config, trace file, inter-message send time in microseconds
	baseCmd = "/usr/local/bin/gnrs/#{property.gbench} /etc/gnrs/clientXxX.xml /etc/gnrs/#{property.clientTrace} #{property.messageDelay} >/var/gnrs/clientXxX.log"

	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = baseCmd.gsub(/XxX/,node.asNumber.to_s)
			node.group.group.exec(cmd)
		}
	}

	return 0
end # loadGUIDs

def genLookups(clientsMap)

	info "Launching generation clients"

	clientsMap.each_value { |node|
		baseCmd = "/usr/local/bin/gnrs/#{property.ggen} /etc/gnrs/client#{node.asNumber}.xml #{property.numLookups} #{property.messageDelay} 1"
		node.group.exec(baseCmd)
	}

	return 0
end # genLookups

def stopServers(serversMap)

	info "Stopping gnrs servers"

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "service gnrsd_#{node.asNumber} stop"
			node.group.group.exec(cmd)
		}
	}

	return 0
end # stopServers

def removeExperimentFiles(nodeMap)
	nodeMap.each_value { |group|
		group.group.exec("rm -rf /var/gnrs /etc/gnrs /usr/local/bin/gnrs /trace-client")
		group.nodelist.each { |node|
			group.group.exec("#{property.updateRc} -f gnrsd_#{node.asNumber} remove")
			group.group.exec("rm /etc/init.d/gnrsd_#{node.asNumber}")
		}
	}
	return 0
end # removeExperimentFiles
