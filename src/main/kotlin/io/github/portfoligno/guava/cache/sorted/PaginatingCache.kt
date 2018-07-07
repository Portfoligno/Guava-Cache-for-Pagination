package io.github.portfoligno.guava.cache.sorted

import kotlin.collections.Map.Entry

interface PaginatingCache<K, V : Any> : SortedCache<K, V>
    where K : Any, K : Comparable<K> {
  fun getAllGreaterThan(key: K): Iterable<Entry<K, V>>
}
