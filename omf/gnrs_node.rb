#!/bin/ruby
#
# A simple class representing a gnrs client/server.
#
# Author: Robert Moore
# Last Modified: Feb 15, 2013
#
class GNRSNode
	attr_accessor :ipAddress, :port, :asNumber, :node

	def to_s
		"#{@node}/#{@ipAddress}:#{@port} (AS ##{@asNumber})"
	end # to_s
end # class GNRSNode

