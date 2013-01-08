#!/bin/ruby
#
# A simple class representing a gnrs client/server node.
#
# Author: Robert Moore
# Last Modified: Jan 7, 2013
#
class GNRSNode
	attr_accessor :ipAddress, :port, :asNumber, :hostname, :group

	def to_s
		"#{@hostname}/#{@ipAddress}:#{@port} (AS ##{@asNumber})"
	end # to_s
end # class GNRSNode

