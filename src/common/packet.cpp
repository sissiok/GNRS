/*
 * Filename: packet.cpp
 * 
 * This module include a generic Packet class
 * 
 */
#include "packet.h"
using namespace std; 


/**
 * Call constructor with payloadBufferSize = DEFAULT_PAYLOAD_SIZE; 
*/
Packet::Packet()
{
  size_=0; 
  payloadBufferSize_ = DEFAULT_PAYLOAD_SIZE;
  payload_ = new char [DEFAULT_PAYLOAD_SIZE];
}

/**
  * Constructor: 
  * 		+ Allocate memory for the packet by buffer_length
  * 		+ Create new PacketHeader
  * 		+ Init size and buffersize
  */
Packet::Packet(int buffer_length)
{
  size_=0; 
  payloadBufferSize_=buffer_length;
  payload_= new char[buffer_length];
  //header_ = new PacketHeader(); 
}

/**
 * fillPayload with the inputStream 
 * Exception is throw if not success
 * Parameter: size of payload, buffer containing the payload 
 */
int Packet::fillPayload(int payloadSize, char* inputstream)
{
  setPayloadSize(payloadSize);
  //copy inputstream to payload
  //throw exception if fail
  if (memcpy((char *)payload_, (char *)inputstream,  payloadSize) == NULL) {
    throw "Fill payload Failed";
  }
  
  return 0;
}

/**
 * Set size of the payload
 * Increase size of the payloadBuffer if the size is greater than current payloadBufferSize_
 */
void Packet::setPayloadSize(int payloadSize)
{
  size_ = payloadSize;   
  if (payloadSize >payloadBufferSize_ ) 
  {
    if (payload_ != NULL) delete [] payload_;
    payloadBufferSize_ = (int)(1.5 * payloadSize);
    payload_ =  new char[payloadBufferSize_];
  }
}
/**
 * get the  header of this  packet 
 * Parameter: incomming stream buffer
 */
void Packet::extractHeader(char* streamBuffer)
{
  char* p = streamBuffer; 
  //get headerSize from the tream buffer
  unsigned int headerSize = (unsigned char)* (p++);
  //assign headerLength
  //header_->setHeaderLength(headerSize);
  //write to header Content 
  //memcpy(header_->getHeaderPointer(),p,headerSize);
}

/**
 * Creating the buffer containing both header and payload of this packet
 * Note: Header of the packet must be defined before calling
 * this assembling operation
 * Return: size of the data (including header + payload + 1)  AND the buffer    
 */
// int Packet::makePacket(char* streamBuffer)
// {
//   
//   streamBuffer[0]= ( header_->getHeaderLength() ) & 0xff;
//   streamBuffer[1]=  0x00;
//   
//   memcpy(streamBuffer+1, header_->getHeaderPointer(), header_->getHeaderLength());
//   memcpy(streamBuffer+1+ header_->getHeaderLength(), payload_, size_);
//   
//   return 1+size_+ header_->getHeaderLength();
// }




