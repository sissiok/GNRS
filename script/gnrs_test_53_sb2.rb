#
# This OMF script defines and runs GNRS
#

        defApplication('GNRS-server', 'Gserver') {|app|
          app.shortDescription = "GNRS server app"
          app.path = "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_gnrsd.sh" #script for running gnrsd configuration
          app.defProperty('config_file', 'GNRS configuration file', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 1})
          app.defProperty('self_ip', 'my own ip', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 2})
          app.defProperty('interface', 'interface: eth0 or wlan0', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 3})
          app.defProperty('srvrs_list_name', 'server list file', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 4})
          app.defProperty('pool_size', 'server thread pool size', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 5})
          app.defProperty('serv_req_num', 'server request number threshold', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 6})
        }

        defApplication('LNRS-server', 'Lserver') {|app|
          app.shortDescription = "LNRS server app"
          app.path = "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_lnrsd.sh" #script for running lnrsd configuration
          app.defProperty('config_file', 'GNRS configuration file', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 1})
          app.defProperty('self_ip', 'my own ip', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 2})
          app.defProperty('interface', 'interface: eth0 or wlan0', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 3})
          app.defProperty('srvrs_list_name', 'server list file', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 4})
        }

        defApplication('GNRS-client', 'client') {|app|
            app.shortDescription = "GNRS client app"
            app.path = "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_gnrs_client.sh" #script for running client configuration
            app.defProperty('config_file', 'GNRS configuration file', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 1})
            app.defProperty('self_ip', 'my own ip', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 2})
            app.defProperty('server_ip', 'server IP', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 3})
            app.defProperty('interface', 'interface: eth0 or wlan0', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 4})
            app.defProperty('request_file', 'request data trace', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 5})
            app.defProperty('listen_port', 'client listen_port', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 6})
            app.defProperty('request_interval', 'client request interval', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 7})
        }

        defApplication('interface-initial', 'interface') {|app|
            app.shortDescription = "interface initializing app"
            app.path = "/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/init_interface.sh" #script for initialize the interface
            app.defProperty('interface', 'interface: eth0 or wlan0', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 1})
            app.defProperty('self_ip', 'my own ip', nil,{:type => :string,:dynamic => false, :use_name => false, :order => 2})
        }

	#defTopology('Gserver_universe', [[4,11],[4,8],[3,19],[3,18],[2,12],[2,6],[1,12],[3,17],[2,11],[1,13],[4,15],[3,16],[2,9],[3,14],[3,10],[3,7],[2,18],[2,15],[2,7],[1,9],[1,6],[4,13],[4,3],[3,8],[2,13],[2,5],[1,17],[1,14],[3,11],[3,5],[2,8],[1,3],[4,12],[4,5],[3,13],[3,1],[2,19],[2,17],[1,10],[1,7],[2,20],[1,16],[4,9],[3,9],[3,3],[2,10],[2,2],[1,11],[1,2],[4,1],[3,2],[1,4],[4,2],[1,20],[1,18],[1,15]])
	#defTopology('Gserver_universe', [[3,18],[2,12],[2,6],[1,12],[3,17],[2,11],[1,13],[4,15],[3,16],[2,9],[3,14],[3,10],[3,7],[2,18],[2,15],[2,7],[1,9],[1,6],[4,13],[4,3],[3,8],[2,13],[2,5],[1,17],[1,14],[3,11],[3,5],[1,3],[4,12],[4,5],[3,13],[3,1],[2,19],[2,17],[1,7],[2,20],[1,16],[3,9],[3,3],[2,10],[2,2],[1,11],[4,1],[3,2],[1,4],[4,2],[1,20],[1,18],[1,15]])
	#defTopology('Gserver_universe', [[3,18],[2,12],[2,6],[1,12],[3,17],[2,11],[1,13],[4,15],[3,16],[2,9],[3,14],[3,10],[3,7],[2,18],[2,15],[2,7],[1,6],[4,13],[4,3],[2,13],[2,5],[1,17],[1,14],[3,11],[3,5],[1,3],[4,12],[4,5],[3,13],[3,1],[2,19],[2,17],[1,7],[2,20],[1,16],[3,9],[3,3],[2,10],[2,2],[1,11],[3,2],[1,4],[1,20],[1,18],[1,15]])
	defTopology('Gserver_universe','node1-1.sb2.orbit-lab.org')
	GserverTopo = Topology['Gserver_universe']
	
#	defTopology('Lserver_universe', [[16,16],[8,8],[5,5],[3,13],[17,5],[14,11],[13,8],[1,20],[20,20],[20,1],[13,13],[6,16],[1,1],[20,2],[18,13],[17,16],[8,13],[5,16],[3,3],[20,19],[18,3],[8,18],[15,5],[4,5],[16,5],[11,11],[8,3],[3,18]])
#	LserverTopo = Topology['Lserver_universe']
	
	defTopology('client_universe', 'node1-2.sb2.orbit-lab.org')
	ClientTopo = Topology['client_universe']
	
	num_Gserver = 1
#	num_Lserver = 0
	num_client = 1
	num_client_node = 1
	num_client_per_node = 1
	
	for i in 1..num_Gserver
		defTopology("mf:topo:Gserver_#{i}") { |t|
			aNode = GserverTopo.getNodeByIndex(i-1)
#			aNode = GserverTopo.getNodeByIndex(i-1)
			t.addNode(aNode)
			print "Adding node: ", aNode, " to Gserver topo\n"
		}

	        defGroup("Gserver_#{i}", "mf:topo:Gserver_#{i}") {|node|
	                node.addApplication('GNRS-server') {|app|
	                    app.setProperty('config_file', 'gnrsd.conf')
			    app.setProperty('self_ip', "192.168.1.110")
	                    #app.setProperty('self_ip', "192.168.1.#{99+i}")
	                    app.setProperty('interface', 'eth0')
	                    app.setProperty('srvrs_list_name', 'servers.lst')
	                    app.setProperty('pool_size', '1')
	                    app.setProperty('serv_req_num', '45000')  #server request number threshold
	                }
	        }
	end
	
#	for i in 1..num_Lserver
#		defTopology("mf:topo:Lserver_#{i}") { |t|
#			aNode = LserverTopo.getNode(i-1)
#			t.addNode(aNode)
#			print "Adding node: ", aNode, " to Lserver topo\n"
#		}

#	        defGroup("Lserver_#{i}", "mf:topo:Lserver_#{i}") {|node|
#	                node.addApplication('LNRS-server') {|app|
#	                    app.setProperty('config_file', 'gnrsd.conf')
#	                    app.setProperty('self_ip', "192.168.1.#{99+i}")
#	                    app.setProperty('interface', 'eth0')
#	                    app.setProperty('srvrs_list_name', 'server.lst-30')
#	                }
#	        }
#	end
	
        for i in 1..num_client_node
                defTopology("mf:topo:client_#{i}") { |t|
                        #aNode = ClientTopo.getNodebyIndex(i-1)
                        aNode = ClientTopo.getNodeByIndex(i-1)
                        t.addNode(aNode)
                        print "Adding node: ", aNode, " to client topo\n"
                }
		
		defGroup("ini_client_#{i}","mf:topo:client_#{i}") {|node|
			node.addApplication('interface-initial') {|app|
                            app.setProperty('interface', 'eth0')
                            app.setProperty('self_ip', "192.168.1.#{199+i}")
                        }
		  }

           for j in 1..num_client_per_node
                defGroup("client_#{num_client_per_node*(i-1)+j}", "mf:topo:client_#{i}") {|node|
                        node.addApplication('GNRS-client') {|app|
                            app.setProperty('config_file', 'client.conf')
			    app.setProperty('self_ip', "192.168.1.120")
                            #app.setProperty('self_ip', "192.168.1.#{199+i}")
			    app.setProperty('server_ip', "192.168.1.110")
                            #app.setProperty('server_ip', "192.168.1.#{99+i}")
                            app.setProperty('interface', 'eth0')
                            app.setProperty('request_file', 'request.data.1')
                            app.setProperty('listen_port', "#{9999+num_client_per_node*(i-1)+j}")
			    app.setProperty('request_interval','350')  #unit: us
                        }
                }
	   end
        end


        onEvent(:ALL_UP) do |event|
	    wait 2
	    #bring up interface on which click operates
	    allGroups.exec("ifconfig eth0 up")
	    wait 5
	    allGroups.exec("/usr/local/mobilityfirst/code/prototype/gnrsd/scripts/cleanup_gnrsd.sh 1>&2")
	    wait 5
	    for i in 1..num_Gserver
		group("Gserver_#{i}").exec("mpstat -P ALL 1 > /var/log/mpstat.data 2>&1 &")
	        group("Gserver_#{i}").startApplications
	    end
	    wait 20
	    #for i in 1..num_Lserver
	    #    group("Lserver_#{i}").startApplications
	    #end
	    #wait 5
#	    for i in 1..num_client_node
#	        group("ini_client_#{i}").startApplications
#	    end
#	    wait 2
	    for i in 1..num_client_node
		for j in 1..num_client_per_node
		        #group("client_#{num_client_per_node*(i-1)+j}").exec("touch /test.data")
		        group("client_#{num_client_per_node*(i-1)+j}").startApplications
		        group("client_#{num_client_per_node*(i-1)+j}").exec("touch test.data")
		end
	    end
	
	    #wait for experiment duration
	    wait 160
	    Experiment.done
	end
