
/**
 * GNRS record storage classes and interfaces for the GNRS server.
 * 
 * <p>The storage engine for GNRS GUID bindings is abstracted from the rest
 * of the GNRS server code.  This package provides a combination of storage-agnostic
 * interface (for specific storage types to implement), as well as solutions
 * based solely on Java SE technology.  That is, this package defines interfaces
 * for specific implementations to provide, as well as implementations that require
 * no external dependencies.</p>
 */
package edu.rutgers.winlab.mfirst.storage;