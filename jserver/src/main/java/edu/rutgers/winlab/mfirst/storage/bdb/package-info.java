/**
 * BerkeleyDB implementation of the GNRS GUID storage engine.
 * 
 * <p>Provides GNRS record storage by using a BerkeleyDB persistent storage
 * solution.  This Java-specific implementation is incompatible with other BerkeleyDB
 * APIs since it uses the Java-specific Data Persistence Interface (DPI) rather than
 * the JNI-based C-style API (Base API). In-memory caching is used to provide faster
 * access to frequently-used records.</p>
 */
package edu.rutgers.winlab.mfirst.storage.bdb;