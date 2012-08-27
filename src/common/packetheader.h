/*
    Filename: packetheader.h
    
    This module contains generic PacketHeader class    
    A Header is defined by a pointer pointing to header content and 
    the length of the header.    
*/

#ifndef PACKETHEADER_H
#define PACKETHEADER_H

#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <cstdlib>
#include <vector>
#include <ctime>

#define MAX_HEADER_SIZE 256

class PacketHeader
{

public:
    /* Constructor: 
    *		+ Allocates memory for the header and initialize with 0s
    *		+ init header length to 0
    */
    PacketHeader();

    ~PacketHeader() {delete[] content_;};
    
    /* 
     * Read 1 bytes from the header at specified position    
     * Return: a char number
     * Parameter: start position
     */
    unsigned char read1byte(int position); 
    /* 
     * Read 2 bytes from the header at specified position    
     * Return: a short number
     * Parameter: start position
     */
    short read2bytes(int position);    
    /* 
     * Read 4 bytes from the header at specified position    
     * Return: an int number 
     * Parameter: start position
     */
    int read4bytes(int position); 
    
    /* 
     * Get the content pointer
     * Return: a pointer to header's content 
     */
    inline unsigned char* getHeaderPointer() { return content_; };
    
    /* Get the length of the header
     * Return: length of the header in int
     */
    inline int getHeaderLength() { return length_; };
    
    /* Set an octet to the header
     * Parameter: Value to be assigned, position on the header
     */
    void set1byte(unsigned char value, int position);
    
    /* Set 2 bytes to the header
     * Parameter: Value to be assigned, position on the header
     */
    void set2bytes(short value, int position);
    
    /* Set 4 bytes to the header
     * Parameter: Value to be assigned, position on the header
     */
    void set4bytes(int value, int position); 
    
    /* Set HeaderLength
     * Parameter: HeaderLength
     */
    inline void setHeaderLength(int value) {length_ = value;}; 
    
protected:
	//pointer to the header's content
	unsigned char* content_; 
	//length of the header 
	int length_;
    
};

#endif // PACKETHEADER_H
