package io.github.portfoligno.guava.cache.sorted

import kotlin.collections.Map.Entry

@Deprecated("Use LoadingSortedCache instead")
interface PaginatingCache<K, V : Any> : LoadingSortedCache<K, V>
    where K : Any, K : Comparable<K> {
  fun getAllGreaterThan(key: K): Iterable<Entry<K, V>>
}
