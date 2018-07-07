package io.github.portfoligno.guava.cache.sorted

import kotlin.collections.Map.Entry

interface PaginatingCacheLoader<K, out V : Any>
    where K : Any, K : Comparable<K> {
  /**
   * @param key the exclusive starting point of entries
   * @param amount amount of entries to get
   */
  @Throws(Exception::class)
  fun loadGreaterThan(key: K, amount: Int): Iterable<Entry<K, V>>
}
