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

# Import the experiment utilities
eval(File.new('./utils.rb').read)

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

	info "Servers:"
	serversMap.each_value { |node|
		info "\t#{node}"
	}

	info "Clients:"
	clientsMap.each_value { |node|
		info "\t#{node}"
	}

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

	wait property.microWait

	info "## Compressing configuration files ##"
	success = buildTarballs()
	if success == 0
		info "\tSuccessfully compressed configuration files."
	else
		error "\tUnable to compress configuration files."
		return;
	end

	wait property.microWait

	# WGET everything onto the nodes
	
	info "## Downloading to the nodes ##"
	success = getHostTarballs(serversMap,clientsMap)
	if success == 0
		info "\tSuccessfully downloaded configuration files."
	else
		error "\tUnable to download configuration files."
		return
	end

	wait property.miniWait
	
	# Install delay modules
	if (property.disableDelay.to_s == "") 
		info "## Install the delay modules ##"
		success = installDelayModule(serversMap, clientsMap, property.dataUrl, property.clickModule)
		if success == 0
			info "\tSuccessfully installed and configured delay module on all nodes."
		else
			error "\tUnable to configure delay module on one or more nodes. Exiting."
			return;
		end
	end

	wait property.largeWait

	# Now update the permissions on the nodes
	info "Installing init scripts"
	installInit(serversMap)

	wait property.miniWait

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
		GNRSUtils.stopServers(serversMap)
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
	success = 0
	success = removeExperimentFiles(serversMap)
	success |= removeExperimentFiles(clientsMap)
	if success != 0
		error "\tUnable to remove files from one or more nodes. You should reimage the testbed."
		return
	end

end # main

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

