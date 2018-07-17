package io.github.portfoligno.guava.cache.sorted

import com.google.common.collect.Range

interface LoadingSortedCache<K, V : Any> : SortedCache<K, V>
    where K : Any, K : Comparable<K> {
  fun get(keys: Range<K>): Iterable<Map.Entry<K, V>>
}
