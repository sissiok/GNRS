cla :: Classifier( 12/0800,   //IP packets
		   -);

ip_cla :: IPClassifier( dst udp port 5000 or dst udp port 5001 or dst udp port 9000,
			-);

delayMod :: NetDelay(1, 2, 3 4 5, abc def);

FromDevice(eth0)
  ->cla
  -> CheckIPHeader(14, CHECKSUM false) 
  -> ip_cla 
  -> delayMod
  -> ToHost;

cla[1] -> ToHost;

ip_cla[1] -> ToHost;

