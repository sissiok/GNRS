defProperty('source', 'node1-1.sb9.orbit-lab.org', 'ID of source node')
defProperty('sink', 'node1-2.sb9.orbit-lab.org', 'ID of sink node')
defProperty('sinkaddr', '10.0.0.38', 'Ping destination address') # How do I get the IP address? Which interface?

defApplication('ping_app', 'pingmonitor') do |a| # What do the two strings mean?
	a.path = "/root/pingWrap.rb"
	a.version(1, 2, 0)
	a.shortDescription = "Wrapper around ping" # Why is sohrtDescription longer than description?
	a.description = "ping application"
	a.defProperty('dest_addr', 'Address to ping', '-a', { :type => :string, :dynamic => false })
	a.defProperty('count', 'Number of times to ping', '-c', { :type => :integer, :dynamic => false})
	a.defProperty('interval', 'Interval between pings', '-i', { :type => :integer, :dynamic => false})

	a.defMeasurement('myping') do |m|
		m.defMetric('dest_addr', :string)
		m.defMetric('ttl', :int)
		m.defMetric('rtt', :float) # Why is this float and not double?
		m.defMetric('rtt_unit', :string)
	end # defMeasurement
end # defApplication

defGroup('Source', property.source) do |node| # Isn't this a RootNodeSet? Why called node?
	node.addApplication("ping_app") do |app| # Why is only this double-quoted?
		app.setProperty('dest_addr', property.sinkaddr)
		app.setProperty('count', 5)
		app.setProperty('interval', 1)
		app.measure('myping', :samples => 1) # What is samples? Why does it have a value of 1?
	end # addApplication
end # defGroup('Source')

defGroup('Sink', property.sink) do |node| # Why even have a block here if nothing happens?
end # defGroup('Sink')

onEvent(:ALL_UP_AND_INSTALLED) do |event| # Why do we need an event if it's not used?
	info "Starting the ping"
	group('Source').startApplications  # Why not start the 'ping_app' explicitly?
	wait 6 # Why 6? What if we want to parameterize the number of pings? Timeouts may take longer than 1 second
	info "Stopping the ping"
	group('Source').stopApplications # Again, why not stop the ping app?
	Experiment.done
end
