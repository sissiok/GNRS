/**
 * General classes and interfaces related to networking for the GNRS
 * server.  Protocol-specific implementations should be kept in subpackages.
 * 
 * <p>This package contains only those interfaces and classes which are networking-agnostic
 * or shared among several different networking layers. The central interface in this
 * package is the NetworkAccessObject, which provides methods for networking implementations
 * to enable the GNRS server to interact with different networks in a modular way.</p>
 */
package edu.rutgers.winlab.mfirst.net;