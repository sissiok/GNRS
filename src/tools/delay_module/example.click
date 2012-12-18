// Example GNRS Delay Module Click script
// Author: Feixiong Zhang, Robert Moore
// Last modified: August 30, 2012

// Classifier to select IP packets
// The first line (12/0800) identifiers the IP flag, sends to output 0
// The second line sends other packets (e.g., ARP) to output 1
cla :: Classifier( 12/0800,  
		   -);

// Classifier to extract UDP packets destined for ports 4000, 5001
// Matching packets go to output 0
// The remaining (non-UDP or alternate ports) go to output 1
ip_cla :: IPClassifier( dst udp port 4001 or dst udp port 5001,
			-);

// Create an instance of the delay module.
// The variable name "delayMod" will appear in the click proc filesystem at
// "/click/delayMod".  The configuration file "/click/delayMod/config" is used
// to reconfigure the delay module at runtime.
delayMod :: NetDelay();

// Start classifying packets arriving on /dev/eth1
FromDevice(eth0)
// Extract IP datagrams
  ->cla
// Validate the IP headers, don't validate checksum
  -> CheckIPHeader(14, CHECKSUM false) 
// Extract UDP datagrams destined for desired ports
  -> ip_cla 
// Send those packets to the delay module
  -> delayMod
// The send onward to the host
  -> ToHost;

// Non-IP go straight to the host
cla[1] -> ToHost;

// Non-UDP (or unselected ports) go straight to the host
ip_cla[1] -> ToHost;

