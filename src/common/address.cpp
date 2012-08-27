/*  Filename: address.h
 * 
 *  Address Class is to handle addresses of unix/linux sockets.
 *  It is defined by a pair of <Hostname or IP address, portnumber> 
 *  For normal sockets, the address used will be a combination of IP address and port.
 *  In Socket Programming, IP address itself is usually not enough to distinguish an connection, port # is also needed.
 *  For PF_PACKET sockets, the address used is MAC address(HW address). So, we also put macaddr_ as a member variable.
 */

#include "address.h"

Address::Address():port_(-1)
{   
  hostname_[0] = '\0';  
  macAddr_[0] = '\0';
}

Address::Address(const char* hostname, short port)
{ 
   setPort(port); 
   setHostname(hostname);
}


// set the hostname
void Address::setHostname(const char* hostname)
{
  if (hostname == NULL) 
      hostname_[0] = '\0';
  else
      strcpy(hostname_, hostname);
}

// Set macAddress 
void Address::setMacAddr(unsigned char* macAdd)
{
  memcpy(macAddr_, macAdd , MAC_ADDR_LENGTH*sizeof(unsigned char));
}

// clone this address
Address* Address::clone()
{
  Address * ad =  new Address(hostname_, port_);
  ad->setMacAddr(macAddr_);
  return ad;
}

//Compare whether the two normal "name+port" address is same or not
bool Address::isSame(Address* addr)
{
  if (port_ == addr->getPort())
	  return false;
  if (strcmp(hostname_, addr->getHostname()) == 0)
	  return true;
  return false;
}

//Compare whether the two MAC addresses are the same or not  
bool Address::isSameMacAddr(Address* addr)
{
  if ( memcmp(macAddr_, addr->getMacAddr(), MAC_ADDR_LENGTH*sizeof(unsigned char))  == 0 )
           return true;
  return false;
}

/*
 * Function to convert the input MAC address string to bytes.
 * First, check the MAC address is valid
 * - there are at least 12 Hex characters
 * - there are no other charcter except colon
 */
void Address::setMacAddrFromColonFormat(const char* colon_seperated_macaddr)
{
  char HexChar;
  //First verify the address
  int Count  = 0;
  int num_mac_char = 0;
  /* Two ASCII characters per byte of binary data */
  bool error_end = false;
  while (!error_end)
    { /* Scan string for first non-hex character.  Also stop scanning at end
         of string (HexChar == 0), or when out of six binary storage space */
      HexChar = (char)colon_seperated_macaddr[Count++];
      if (HexChar == ':') continue;     
      if (HexChar > 0x39) HexChar = HexChar | 0x20;  /* Convert upper case to lower */
      if ( (HexChar == 0x00) || num_mac_char  >= (MAC_ADDR_LENGTH * 2) ||
           (!(((HexChar >= 0x30) && (HexChar <= 0x39))||  /* 0 - 9 */
             ((HexChar >= 0x61) && (HexChar <= 0x66))) ) ) /* a - f */ 
	{
	  error_end = true;
	} else 
            num_mac_char++;
    }
  if (num_mac_char != MAC_ADDR_LENGTH * 2 )
    throw "Given Wrong MAC address Format.";

  // Conversion
  unsigned char HexValue = 0x00;
  Count = 0;
  num_mac_char = 0;
  int mac_byte_num = 0;
  while (mac_byte_num < MAC_ADDR_LENGTH )
    {
      HexChar = (char)colon_seperated_macaddr[Count++];
      if (HexChar == ':') continue;
      num_mac_char++;  // locate a HEX character
      if (HexChar > 0x39)
        HexChar = HexChar | 0x20;  /* Convert upper case to lower */
      HexChar -= 0x30;
      if (HexChar > 0x09)  /* HexChar is "a" - "f" */
	HexChar -= 0x27;
      HexValue = (HexValue << 4) | HexChar;
      if (num_mac_char % 2  == 0 ) /* If we've converted two ASCII chars... */
        {
          macAddr_[mac_byte_num] = HexValue;
	  HexValue = 0x0;
	  mac_byte_num++;
        }
    }  
  return;  
}

//convert MacAddress to colone-separated format
char* Address::convertMacAddrToColonFormat()
{
   char *colonformat =  new char[17];  
   sprintf(colonformat,"%02X:%02X:%02X:%02X:%02X:%02X",
          macAddr_[0],macAddr_[1],macAddr_[2],macAddr_[3],macAddr_[4],macAddr_[5]);  
  return colonformat;
}





