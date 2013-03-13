#!/bin/ruby
#
# A simple class representing a gnrs client/server.
#
# Author: Robert Moore
# Last Modified: Feb 15, 2013
#
class GNRSNode
	attr_accessor :port, :asNumber, :group, :server, :delayConfig

	def to_s
		"#{@group.ipAddress}:#{@port} (AS ##{@asNumber})"
	end # to_s
end # class GNRSNode

