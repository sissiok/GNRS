#!/bin/ruby
#
# A very simple OEDL/OMF script that tests out command issuance.
# Author: Robert Moore
# Last Modified: Dec 21, 2012
#
# Runtime configuration of "domain" property can be effected like this:
#  omf exec simple.rb -- --domain sub.domain.com

# Global properties (pushed to nodes)
defProperty('domain', 'changeme.fakedomain', "Subdomain for nodes (grid, sb, etc.)")

# Add node1-1 to the "server list"
defGroup('server', "node1-1.#{property.domain}") do |node|
  node.net.e0.ip="192.168.1.2"
end
# Add node1-2 to the "client list"
defGroup('client', "node1-2.#{property.domain}") do |node|
  node.net.e0.ip="192.168.1.102"
end

onEvent(:ALL_UP) do |event|
  info "GNRS: All nodes are up."
  wait 2
  Experiment.done
end

