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
	uniqueId = Hash.new(0)
	asCount = Hash.new(0)
	clientsMap.each_value { |group|
		group.nodelist.each { |client|
			path = prefixDir.empty? ? "client_#{client.asNumber}R#{asCount[client.asNumber]}/" : "#{prefixDir}/client_#{client.asNumber}R#{asCount[client.asNumber]}/";
			
			#if File.exists?(path)  and File.directory?(path)
			#	nextId = uniqueId[path]
			#	uniqueId[path] = nextId+1
			#	path = path[0..-2]
			#	path = "#{path}_#{nextId}/"
			#end
			system("mkdir -p #{path}");
			system("#{property.scp} root@#{group.hostname}:\"/var/gnrs/stats#{client.asNumber}R#{asCount[client.asNumber]}/*\" #{path}");
			system("#{property.scp} root@#{group.hostname}:/var/log/gbench_#{client.asNumber}R#{asCount[client.asNumber]}.log #{path}");
			asCount[client.asNumber] = asCount[client.asNumber] + 1

			# exitStatus = $?.exitstatus
			#pid = $?.pid
		}
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

	serversMap.each_value { |group|
		group.nodelist.each { |server|
			path = prefixDir.empty? ? "server_#{server.asNumber}/" : "#{prefixDir}/server_#{server.asNumber}/";
			info "mkdir -p #{path}"
			system("mkdir -p #{path}");
			info "#{property.scp} root@#{group.hostname}:\"/var/gnrs/stats#{server.asNumber}/*\" #{path}"
			system("#{property.scp} root@#{group.hostname}:\"/var/gnrs/stats#{server.asNumber}/*\" #{path}");
			system("#{property.scp} root@#{group.hostname}:/var/log/gnrsd_#{server.asNumber}.log #{path}");
		}
	}
	
	return 0;
end # collectServerStats


