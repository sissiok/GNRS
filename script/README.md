# GNRS Experimental Scripts #
The scripts in this directory are provided to make configuring and running
GNRS-related experiments simpler and easier.

## Orbit Usage ##
You can take a look at the following website on the basic usage of orbit:
	http://orbit-lab.org/wiki/HowToGetStarted
Steps to load an image:

1. omf-5.3 tell -a offh -t TOPOLOGY
1. omf-5.3 load -t TOPOLOGY -i IMAGE
1. omf-5.3 tell -a offh -t TOPOLOGY
1. omf-5.3 tell -a on -t TOPOLOGY

You can ssh into each node after the node is up if the loading image succeeds.


## Experimental Setup ##
Below are the general steps used to configure and run an experiment on the
Oribt Grid testbed.

1. Select a topology
1. Build the delay matrix
1. Build the AS list.
1. Build the delay module configuration files.
1. Build the gnrs server and client configuration files.

### delay module configuration ###


### gnrs server configuration ###
the gnrs server need the following configuration file:
gnrsd.conf: contains information about server IP and port, mysql database info, etc.
servers.lst: contains all gnrs servers IP address
AS2node.data: the mapping from an AS num to a node ip and port

### client configuration ###
client.conf: contains info about server IP and port, client IP and port, etc.
request.data: request datatrace.
