package io.github.portfoligno.guava.cache.sorted

interface SortedCache<K, V : Any>
    where K : Any, K : Comparable<K> {
  fun invalidateAll()
}
