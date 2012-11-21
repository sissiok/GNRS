package edu.rutgers.winlab.mfirst.storage.cache;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple implementation of an LRU cache based on LinkedHashMap.  Idea provided by Hank Gay on StackOverflow.com
 * 
 * Sourced from http://stackoverflow.com/questions/221525/how-would-you-implement-an-lru-cache-in-java-6
 * 
 * @author <a href="http://stackoverflow.com/users/4203/hank-gay">Hank Gay</a>
 * @author Robert Moore II
 *
 * @param <K> 
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
  
  /**
   * To be updated when the class members change.
   */
  private static final long serialVersionUID = 5148706907508646895L;
 
  /**
   * Maximum capacity of the cache.
   */
  private final int capacity;
  
  /**
   * Creates a new LRU cache with the specified capacity.
   * @param capacity the maximum capacity for this cache.
   */
  public LRUCache(int capacity)
  {
    super(capacity+1, 1.0f, true);
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(final Map.Entry<K, V> entry)
  {
    return super.size() > this.capacity;
  }
}
