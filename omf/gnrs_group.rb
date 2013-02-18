#!/bin/ruby
#
# A simple class representing a physical host/OMF group for GNRS.
#
# Author: Robert Moore
# Last Modified: Feb 15, 2013
#

class GNRSGroup 
	attr_accessor :hostname, :group, :nodelist, :ipAddress
	def to_s
		"#{@hostname}/#{@ipAddress}: #{@nodelist}"
	end
end
