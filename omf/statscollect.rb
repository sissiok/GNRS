# Collection of functions that retrieve and package statistics ands logs
# from nodes after an GNRS experiment.
#

# Retrieves client statistics files and places them in a set of subdirectories
# of the 'prefixDir' directory.
def collectClientStats(clientsMap, prefixDir)

	# Create the prefix dir if it is provided
	unless ( prefixDir.nil? or prefixDir.empty? )
		system("mkdir -p #{prefixDir}") or 
			begin
				info "Unable to create directory '#{prefixDir}'";
				return 1;
			end
	end

	clientsMap.each_value { |client|
		path = prefixDir.empty? ? "client_#{client.asNumber}/" : "#{prefixDir}/client_#{client.asNumber}/";
		system("mkdir -p #{path}");
		system("#{property.scp} root@#{client.hostname}:\"/trace-client/*\" #{path}");
		system("#{property.scp} root@#{client.hostname}:/var/gnrs/gnrsd.log #{path}");

		# exitStatus = $?.exitstatus
		#pid = $?.pid
	}
	return 0;
end # collectClientStats

def collectServerStats(serversMap, prefixDir)
	
	# Create the prefix directory if it s provied
	unless ( prefixDir.nil? or prefixDir.empty? )
		system("mkdir -p #{prefixDir}") or
		begin
			info "Unable to create directory '#{prefixDir}'";
			return 1;
		end
	end

	serversMap.each_value { |server|
		path = prefixDir.empty? ? "server_#{server.asNumber}/" : "#{prefixDir}/server_#{server.asNumber}/";
		system("mkdir -p #{path}");
		system("#{property.scp} root@#{server.hostname}:\"/var/gnrs/stats/*\" #{path}");
		system("#{property.scp} root@#{server.hostname}:/var/gnrs/gnrsd.log #{path}");
	}
	
	return 0;
end # collectServerStats

def removeExperimentFiles(nodeMap)
	nodeMap.each_value { |node|
		node.group.exec("rm -rf /var/gnrs /etc/gnrs /usr/local/bin/gnrs /trace-client")
	}
	return 0
end # removeExperimentFiles
