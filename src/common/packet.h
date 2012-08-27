/*
 * Filename: packet.h
 * 
 * This module include a generic Packet class
 * 
 */
#ifndef PACKET_H
#define PACKET_H

#include "packetheader.h"
#include "../common/Messages.h"

class Packet
{
public: 
  //Default payload size is 512 bytes
  static const int DEFAULT_PAYLOAD_SIZE = 512;
  
  //message to be sent
  //GNRSMessages message; 
  
  Packet();
  /**
  * Constructor: 
  * 		+ Allocate memory for the packet by DEFAULT_PAYLOAD_SIZE
  * 		+ Create new PacketHeader
  * 		+ Init size and buffersize
  */
  Packet(int buffer_length);

  ~Packet(){delete[] payload_;}
  
  /**
  * fillPayload with the inputStream 
  * Exception is throw if not success
  * Parameter: size of payload, buffer containing the payload 
  */
  int fillPayload(int payloadSize, char* inputstream);
  
  /**
  * Creating the buffer containing both header and payload of this packet
  * Note: Header of the packet must be defined before calling
  * this assembling operation
  * Return: size of the data (including header + payload + 1)  AND the buffer    
  */
  //int makePacket(char* streamBuffer); 
  
  /**
  * Set size of the payload
  * Increase size of the payloadBuffer if the size is greater than current payloadBufferSize_
  */
  void setPayloadSize(int payloadSize); 
  
  /**
   * Extract header from a buffer
   */
  void extractHeader(char *streamBuffer);
  
  /**
   * Get a  pointer to the payload 
   */
  inline char* getPayloadPointer(){return payload_;};
  
  /**
   * Get payload buffer size 
   */
  inline int getPayloadBufferSize(){return payloadBufferSize_;};
  
  /**
   *  Get payload size
   */
  inline int getPayloadSize(){ return size_; }; 
  
  /**
   * Get header length
   */
  //inline int getHeaderSize(){ return header_->getHeaderLength() ;}
  
  /**
   * Get PacketHeader pointer 
   */
  //inline PacketHeader* getPacketHeader() { return header_; }; 

   	void setPayload(char* buffer, unsigned size){
		if (payload_ != NULL) delete [] payload_;
		payload_ = buffer;
		size_ = size;
		
	}
  
protected:
  // Packet length in Bytes (including the header)
  unsigned int size_; 
  // Maximum allocated Size of Payload buffer. Note: it must be greater than size_
  int payloadBufferSize_; 
  //Pointer to payload 
  char* payload_; 
  // Header 
  //PacketHeader * header_;
};

#endif // PACKET_H
