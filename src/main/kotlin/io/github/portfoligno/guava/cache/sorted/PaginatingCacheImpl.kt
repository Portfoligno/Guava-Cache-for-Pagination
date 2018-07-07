package io.github.portfoligno.guava.cache.sorted

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.ImmutableSortedMap
import com.google.common.collect.Iterators
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.collections.Map.Entry

private
typealias Sorted<K, V> = ImmutableSortedMap<K, V>

internal
class PaginatingCacheImpl<K, V : Any>(
    private
    val chunkSize: Int,
    private
    val loader: PaginatingCacheLoader<K, V>,
    private
    val delegate: Cache<K, Sorted<K, V>>
) : PaginatingCache<K, V>
    where K : Any, K : Comparable<K> {

  companion object {
    fun <K, V : Any> create(
        chunkSize: Int,
        loader: PaginatingCacheLoader<K, V>,
        cacheBuilder: CacheBuilder<Any, Any>): PaginatingCacheImpl<K, V>
        where K : Any, K : Comparable<K> {
      require(chunkSize > 0) { "chunkSize = $chunkSize" }
      return PaginatingCacheImpl(chunkSize, loader, cacheBuilder.build())
    }
  }

  private
  val keys = ConcurrentSkipListSet<K>()

  override
  fun invalidateAll() {
    keys.clear()
    delegate.invalidateAll()
  }

  private
  fun loadGreaterThan(key: K): Sorted<K, V> {
    val m = delegate.get(key) {
      ImmutableSortedMap.copyOf(loader.loadGreaterThan(key, chunkSize))
    }
    keys.add(key)
    return m
  }

  private
  fun getGreaterThan(key: K): Sorted<K, V>? {
    fun go(m: Sorted<K, V>): Sorted<K, V>? {
      if (!m.isEmpty()) {
        if (m.lastKey() > key) {
          return m.tailMap(key, false) // Exclusive
        }
        return null
      }
      return m
    }

    for (k in keys
        .headSet(key, true) // Inclusive
        .descendingSet()) {
      val m = delegate.getIfPresent(k)

      if (m != null) {
        return go(m)
      }
      if (keys.remove(k)) {
        val m1 = delegate.getIfPresent(k)

        if (m1 != null) {
          // Put it back if it becomes available again
          keys.add(k)
          return go(m1)
        }
      }
    }
    return null
  }

  override
  fun getAllGreaterThan(key: K): Iterable<Entry<K, V>> = Iterable {
    Iterators.concat(object : AbstractIterator<Iterator<Entry<K, V>>>() {
      var cursor = key

      override
      fun computeNext() {
        val m = getGreaterThan(cursor) ?: loadGreaterThan(cursor)

        if (!m.isEmpty()) {
          cursor = m.lastKey()
          setNext(m.entries.iterator())
        }
        else {
          done()
        }
      }
    })
  }
}
