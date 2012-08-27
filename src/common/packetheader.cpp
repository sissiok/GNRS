/*
    Filename: packetheader.cpp
    
    This module contains generic PacketHeader class    
    A Header is defined by a pointer pointing to header content and 
    the length of the header.    
*/
#include "packetheader.h"

using namespace std; 

/* Constructor: 
*		+ Allocates memory for the header and initialize with 0s
*		+ init header length to 0
*/
PacketHeader::PacketHeader()
{
  content_=new unsigned char[MAX_HEADER_SIZE];  
  memset(content_,0, MAX_HEADER_SIZE); 
  length_=0; 
}
/* 
* Read 1 bytes from the header at specified position    
* Return: a char number
* Parameter: start position
*/
unsigned char PacketHeader::read1byte(int position)
{
  return content_[position];
}

/* 
* Read 2 bytes from the header at specified position    
* Return: a short number
* Parameter: start position
*/
short PacketHeader::read2bytes(int position)
{
  short val;
  unsigned char *p = content_ + position;
  val = *(p++);
  val = val << 8 | *(p++);

  return val;
}

/* 
* Read 4 bytes from the header at specified position    
* Return: an int number 
* Parameter: start position
*/
int PacketHeader::read4bytes(int position)
{
  int val; 
  unsigned char *p = content_ + position; 
  
  val = *(p++);
  val = val << 8 | *(p++);
  val = val << 8 | *(p++);
  val = val << 8 | *(p);
  
  return val;
}

/* Set an octet to the header
* Parameter: Value to be assigned, position on the header
*/
void PacketHeader::set1byte(unsigned char value, int position)
{
  *(content_+position)=value; 
  length_++; 
}

/* Set 2 bytes to the header
* Parameter: Value to be assigned, position on the header
*/
void PacketHeader::set2bytes(short value, int position)
{
  unsigned char *p=content_ + position; 
  *(p++) = (value >> 8);
  *(p++) =  value & 0xFF;
  length_+=2; 
}

/* Set 4 bytes to the header
* Parameter: Value to be assigned, position on the header
*/
void PacketHeader::set4bytes(int value, int position)
{
  unsigned char *p=content_ + position; 
  *(p++) =  value >> 24;
  *(p++) = (value >> 16) & 0xFF;
  *(p++) = (value >> 8) & 0xFF;
  *(p++) =  value & 0xFF;
  length_+=4; 
}


