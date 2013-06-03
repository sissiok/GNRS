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

	`rm -rf #{property.tmpDir}`
	`mkdir -p #{property.tmpDir}`
	unless $?.success?
		puts "Unable to use temporary directory #{property.tmpDir}."
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
		`mkdir -p #{property.tmpDir}/#{group.hostname}`
	}

	clientsMap.each_value { | group | 
		group.group.net.e0.ip = group.ipAddress
		`mkdir -p #{property.tmpDir}/#{group.hostname}`
	}

	return 0
end # prepareNodes

def prepareDelayModule(serversMap, clientsMap, baseUrl, clickScript)

	info "Building delay module Click script"
	# Download delay module click script
	#cmd = "#{property.wget} #{property.scriptUrl}/#{property.clickModule}"
	info "Downloading topology route file"
	system("#{property.wget} #{property.dataUrl}/#{property.routeFile}")
	info "Building delay configurations"
	# This function will set the "delayConfig" member of each server/client
	makeDelayConfig(serversMap,clientsMap,property.routeFile.to_s)

	asCount = Hash.new(0)

	serversMap.each_value { |group|
		clickScript = makeDelayScript(group,false)
		`echo '#{clickScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >#{property.tmpDir}/#{group.hostname}/#{property.clickModule}`
		unless $?.success?
			puts "Unable to generate click script for #{group.hostname}."
			return -1
		end
		group.nodelist.each { |node|
			`echo '#{node.delayConfig}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >#{property.tmpDir}/#{group.hostname}/delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat`
			unless $?.success?
				puts "Unable to copy click script for server #{node.asNumber}."
				return -1
			end
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}
	clientsMap.each_value { |group|
		clickScript = makeDelayScript(group,true)
		`echo '#{clickScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >#{property.tmpDir}/#{group.hostname}/#{property.clickModule}`
		unless $?.success?
			puts "Unable to generate click script for #{group.hostname}."
			return -1
		end
		group.nodelist.each { |node|
			`echo '#{node.delayConfig}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >#{property.tmpDir}/#{group.hostname}/delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat`
			unless $?.success?
				puts "Unable to copy click script for client #{node.asNumber}."
				return -1
			end
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}
	

	return 0
end

def installDelayModule(serversMap, clientsMap, baseUrl, clickScript)

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




	info "Installing the delay module configurations"

	asCount = Hash.new(0)

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "cp /delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat /click/delayMod#{node.asNumber}R#{asCount[node.asNumber]}/config"
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}

	asCount = Hash.new(0)

	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "cp /delayMod#{node.asNumber}R#{asCount[node.asNumber]}.dat /click/delayMod#{node.asNumber}R#{asCount[node.asNumber]}/config"
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
	}


	wait property.miniWait

	# Delete any files we downloaded and no longer need
#	info "Deleting temporary files"

#	system("rm #{property.routeFile}");

#	serversMap.each_value { |group|
#		group.nodelist.each { |node|
#			cmd = "rm #{property.clickModule}"
#			node.group.group.exec(cmd)
#			cmd = "rm #{property.delayConfigServer}".gsub(/XxX/,node.asNumber.to_s)
#			node.group.group.exec(cmd)
#		}
#	}
#	clientsMap.each_value { |group|
#		group.nodelist.each { |node|
#			cmd = "rm #{property.clickModule}"
#			node.group.group.exec(cmd)
#			cmd = "rm #{property.delayConfigClient}".gsub(/XxX/,node.asNumber.to_s)
#			node.group.group.exec(cmd)
#		}
#	}

	return 0
end # prepareDelayModule

def installConfigs(serversMap, clientsMap)

	info "Creating required directories"
	mkVar = "mkdir -p /var/gnrs/stats"
	mkEtc = "mkdir -p /etc/gnrs"
	mkBin = "mkdir -p /usr/local/bin/gnrs/"

	asCount = Hash.new(0)
	
	serversMap.each_value { |group|
		group.nodelist.each { |server|
			`mkdir -p #{tmpDir}/#{group.hostname}/var/gnrs/stats#{server.asNumber}$#{asCount[server.asNumber]}`
			asCount[server.asNumber] = asCount[server.asNumber] + 1
		}
		`mkdir -p #{tmpDir}/#{group.hostname}/etc/gnrs`
		`mkdir -p #{tmpDir}/#{group.hostname}/usr/local/bin/gnrs`
	}

	clientsMap.each_value { |group|
		group.nodelist.each { |server|
			`mkdir -p #{tmpDir}/#{group.hostname}/var/gnrs/stats#{server.asNumber}$#{asCount[server.asNumber]}`
			asCount[server.asNumber] = asCount[server.asNumber] + 1
		}
		`mkdir -p #{tmpDir}/#{group.hostname}/etc/gnrs`
		`mkdir -p #{tmpDir}/#{group.hostname}/usr/local/bin/gnrs`
	}

	wait property.microWait

	info "Downloading server configuration files."
	`#{property.wget} #{property.dataUrl}/#{property.prefixIpv4}`
	`#{property.wget} #{property.dataUrl}/#{property.mapIpv4}`
	`#{property.wget} #{property.scriptUrl}/#{property.jarFile}`
	`#{property.wget} #{property.scriptUrl}/#{property.gnrsd}`
	# GGen script
	`#{property.wget} -P #{property.scriptUrl}/#{property.ggen}`
	# GBench script
	`#{property.wget} #{property.scriptUrl}/#{property.gbench}`

	# Build AS binding file
	asBinding = makeBindingFile(serversMap)

	serversMap.each_value { |group|
		group.nodelist.each { |node|
			# Main server config
			configContents = makeServerConfig(node)
			`echo '#{configContents}' >#{property.tmpDir}/#{group.hostname}/etc/gnrs/server_#{node.asNumber}.xml`

			# Networking config
			configContents = makeServerNetConfig(node)
			`echo '#{configContents}' >#{property.tmpDir}/#{group.hostname}/etc/gnrs/net-ipv4_#{node.asNumber}.xml`

			bdb = makeBerkeleyDBConfig(server)
			`echo '#{bdb}' >#{property.tmpDir}/#{group.hostname}/etc/gnrs/berkeleydb_#{server.asNumber}.xml`
		
			initScript = makeServerInit(node)

			# The sed command below will replace any escaped ( or ) characters on the
			# node side.  This is because the ResourceController (RC) in OMF will
			# crash if the string contains unbalanced ( or ) characters.
			# Big thanks to Ben Firner for the regular expression
			`echo '#{initScript}' | sed -e 's/\\\\\\([()]\\)/\\1/g' >#{property.tmpDir}/#{group.hostname}/etc/init.d/gnrsd_#{node.asNumber}`

		}
		# Download static files

		# Binding file
		# IPv4 Prefix File (BGP Table)
		`cp #{property.prefixIpv4} #{property.tmpDir}/#{group.hostname}/etc/gnrs/`
		# IPv4 Mapper Configuration
		`cp #{property.mapIpv4} #{property.tmpDir}/#{group.hostname}/etc/gnrs/`
		# Jar file
		`cp #{property.jarFile} #{property.tmpDir}/#{group.hostname}/usr/local/bin/gnrs/`
		# GNRSD script
		`cp #{property.gnrsd} #{property.tmpDir}/#{group.hostname}/usr/local/bin/gnrs/`
		# Binding file
		`echo '#{asBinding}' >#{property.tmpDir}/#{group.hostname}/etc/gnrs/topology.bind`

	}

	info "Downloading client configuration files"
	asCount = Hash.new(0)
	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			# Client trace file
			cmd = "#{property.wget} -P #{property.tmpDir}/#{group.hostname}/etc/gnrs/ #{property.dataUrl}/#{property.clientTrace}".gsub(/XxX/,node.asNumber.to_s)
			`#{cmd}`
			
			# Main client config
			configContents = makeClientConfig(node,node.server,asCount[node.asNumber])
			`echo '#{configContents}' >#{property.tmpDir}/#{group.hostname}/etc/gnrs/client#{node.asNumber}R#{asCount[node.asNumber]}.xml`
			asCount[node.asNumber] = asCount[node.asNumber]+1
		}
		`cp #{property.jarFile} #{property.tmpDir}/#{group.hostname}/usr/local/bin/gnrs/`
		`cp #{property.ggen} #{property.tmpDir}/#{group.hostname}/usr/local/bin/gnrs/`
		`cp #{property.gbench} #{property.tmpDir}/#{group.hostname}/usr/local/bin/gnrs/`
	
	}

	return 0
end # installConfigs

def getHostTarballs(serversMap, clientsMap)
	
	serversMap.each_value { |group|
		cmd = "#{property.wget} #{property.tarUrl}/#{group.hostname}.tgz && tar -zxf #{group.hostname}.tgz"
		group.group.exec(cmd)
	}

	clientsMap.each_value { |group|
		cmd = "#{property.wget} #{property.tarUrl}/#{group.hostname}.tgz && tar -zxf #{group.hostname}.tgz"
		group.group.exec(cmd)
	}

end # getHostTarballs

def installInit(servers)
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
	asCount = Hash.new(0)
	clientsMap.each_value { |group|
		group.nodelist.each { |node|
			cmd = "export gnrsLogfile=/var/log/gbench_#{node.asNumber}R#{asCount[node.asNumber]}.log; /usr/local/bin/gnrs/#{property.gbench} /etc/gnrs/client#{node.asNumber}R#{asCount[node.asNumber]}.xml /etc/gnrs/client_#{node.asNumber}.trace #{property.messageDelay}"
			node.group.group.exec(cmd)
			asCount[node.asNumber] = asCount[node.asNumber] + 1
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
		group.group.exec("rm -rf /var/log/gbench*.log")
		group.group.exec("rm -rf /var/log/gnrsd*.log")
		group.nodelist.each { |node|
			group.group.exec("#{property.updateRc} -f gnrsd_#{node.asNumber} remove")
			group.group.exec("rm /etc/init.d/gnrsd_#{node.asNumber}")
			group.group.exec("#{property.clickUninstall}")
			group.group.exec("rm /delayMod*.dat")
		}
	}
	return 0
end # removeExperimentFiles
