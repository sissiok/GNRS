/**
 * Classes for GUID&rarr;NetworkAddress mapping functionality.
 * 
 * <p>When a GUID value is received by a GNRS server, either for insertion or retrieval
 * of value bindings, it is mapped to a Network Address.  The mapped address is considered
 * one of several replicas which are responsible for maintaining the current mappings for
 * that GUID value.</p>
 * 
 * <p>This package contains interfaces and classes which are for general GUID&rarr;Network Address
 * mapping, and subpackages are for specific network type implementations.</p>
 */
package edu.rutgers.winlab.mfirst.mapping;