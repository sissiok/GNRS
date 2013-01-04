#!/bin/ruby
#
# Author: Robert Moore
# Last Modified: Jan 4, 2013
#
# A "resources" file that will be evaluated by the experiment script
# before any other code.  Primarily used to set default property values
# that can be overridden at runtime by commandline arguments.
#

# Global properties (pushed to nodes)
defProperty('numServers', 1, "Number of nodes to use for servers")
defProperty('numClients', 1, "Number of clients to use for clients")
defProperty('data-url', 'http://my.domain.com/path/to/files', 'Base URL for topology/delay files.')
defProperty('binding-file', 'jellyfish_topo.bind', 'Filename of AS -> IP:Port binding information.')

