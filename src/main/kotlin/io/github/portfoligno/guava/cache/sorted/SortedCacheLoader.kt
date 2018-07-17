package io.github.portfoligno.guava.cache.sorted

import kotlin.collections.Map.Entry

interface SortedCacheLoader<K, out V : Any>
    where K : Any, K : Comparable<K> {
  @Throws(Exception::class)
  fun loadChunk(cut: Cut<K>, size: Int): Iterable<Entry<K, V>>
}
