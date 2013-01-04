#!/bin/ruby
#
# A simple class representing a gnrs client/server node.
#
# Author: Robert Moore
# Last Modified: Jan 4, 2013
#
class GNRSNode
	attr_accessor :ipAddress, :port, :asNumber, :hostname

	def to_s
		@hostname
	end # to_s
end # class GNRSNode

