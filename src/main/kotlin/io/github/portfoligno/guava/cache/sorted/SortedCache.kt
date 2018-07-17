package io.github.portfoligno.guava.cache.sorted

import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import kotlin.collections.Map.Entry

interface SortedCache<K, V : Any>
    where K : Any, K : Comparable<K> {
  fun getChunkIfPresent(cut: Cut<K>): Iterable<Entry<K, V>>?

  @Throws(ExecutionException::class)
  fun getChunk(cut: Cut<K>, loader: Callable<out Iterable<Entry<K, V>>>): Iterable<Entry<K, V>>

  fun invalidateAll()
}
