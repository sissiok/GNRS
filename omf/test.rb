maxServers = 4;
maxClients = 2*maxServers;

totalNodes = 10;

numServers = 5;
numClients = 4;

# Want to assign servers/clients as "evenly" as possible
# 1. Can't put server and client on same host.

# The number of clients on each "client node"
clientsPerNode = [];
# The number of servers on each "server node"
serversPerNode = [];

quotient = (numServers + numClients) / totalNodes;
remainder = (numServers + numClients) % totalNodes;

if(quotient >= 1 && remainder > 0) # Need to have multiple client/server per node
	puts "Need to multi-allocate.\n";

else # One client/server per node
	puts "Only one per node. :)\n":

end # Should be allocated now
