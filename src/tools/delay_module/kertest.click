//FromDevice(eth0)
//->Print(ok,60)
        //->Queue(100)
       // ->DelayShaper(5)
//        -> net_delay
        //->Unqueue(100)
//->ToHost(eth0)




cla :: Classifier( 12/0800,   //IP packets
		   -);

ip_cla :: IPClassifier( dst udp port 5000 or dst udp port 5001 or dst udp port 9000,
			-);

FromDevice(eth0)
//->Print(OK0) 
->cla
//-> Print(OK1)
-> CheckIPHeader(14, CHECKSUM false) 
-> ip_cla 
//-> Print(OK2)
-> net_delay -> ToHost;

cla[1] -> ToHost;

ip_cla[1] -> ToHost;

