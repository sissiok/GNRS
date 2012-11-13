/**
 * Classes and utilities for GUID&rarr;Network Address (NA) for IPv4/UDP network
 * implementations.
 * 
 * <p>These classes are specific to the GUID&rarr;NA mapping functionality for IPv4/UDP
 * networking interfaces.  Authoritative GNRS replica servers will be mapped from IPv4 
 * address values based on network prefix announcements (via BGP routing tables).</p>
 * 
 * <p>GUID&rarr;NA mapping occurs using the following steps:
 *  <ol>
 *    <li>GUID is hashed using one of several hash functions (MD5, SHA-1, SHA-256) to generate
 *    a sequence of random byte values.</li>
 *    <li>The byte values are then mapped into <em>K</em> different IP addresses.  If the hash
 *    function does not generate enough bytes for the desired number of addresses, then the output
 *    of the previous hash value is appended to the GUID value, and this combined value is hashed.</li>
 *    <li>The random IP address vales are matched against the BGP routing information and
 *    AS responsible for the random IP address is determined. In the case of allocation "holes",
 *    then the IP address bytes are concatenated to the GUID value and rehashed.  This continues until
 *    a matching prefix is determined, or sufficient attempts have failed and a "nearest" prefix
 *    is used instead.</li>
 *    <li>The <em>k</em> authoritative replica servers are forwarded the LOOKUP, APPEND, or REPLACE 
 *    message from the client.  If one of the <em>k</em> replicas is the local server, then
 *    the LOOKUP, APPEND, or REPLACE is performed locally BEFORE forwarding to remote servers.</li>
 *    <li>The client receives a response message to indicate SUCCESS or FAILURE, depending on
 *    the options requested and result of the request.</li>
 *  </ol>
 * </p>
 */

package edu.rutgers.winlab.mfirst.mapping.ipv4udp;