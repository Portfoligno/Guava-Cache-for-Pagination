package io.github.portfoligno.guava.cache.sorted

import com.google.common.cache.CacheBuilder
import com.google.common.collect.Range
import com.google.common.util.concurrent.UncheckedExecutionException
import java.util.concurrent.ExecutionException
import kotlin.collections.Map.Entry

internal
class PaginatingCacheImpl<K, V : Any>(
    delegate: LoadingSortedCache<K, V>
) : PaginatingCache<K, V>, LoadingSortedCache<K, V> by delegate
    where K : Any, K : Comparable<K> {
  companion object {
    fun <K, V : Any> create(
        chunkSize: Int,
        loader: PaginatingCacheLoader<K, V>,
        cacheBuilder: CacheBuilder<Any, Any>): PaginatingCacheImpl<K, V>
        where K : Any, K : Comparable<K> {
      val sortedCacheLoader = object : SortedCacheLoader<K, V> {
        override
        fun loadChunk(cut: Cut<K>, size: Int): Iterable<Entry<K, V>> {
          if (cut is Cut.Bounded.Open) {
            // Only left open ranges are supported
            return loader.loadGreaterThan(cut.endpoint, size)
          }
          throw UnsupportedOperationException(cut.javaClass.simpleName)
        }
      }

      return PaginatingCacheImpl(LoadingSortedCacheImpl.create(chunkSize, sortedCacheLoader, cacheBuilder))
    }
  }

  override
  fun getAllGreaterThan(key: K): Iterable<Entry<K, V>> {
    val iterable = get(Range.greaterThan(key))

    return Iterable {
      val delegate = iterable.iterator()

      object : Iterator<Entry<K, V>> {
        // ExecutionException is thrown instead for compatibility
        fun <T> rethrowChecked(block: () -> T): T =
            try {
              block()
            }
            catch (e: UncheckedExecutionException) {
              throw ExecutionException(e.cause)
            }

        override
        fun hasNext() = rethrowChecked {
          delegate.hasNext()
        }

        override
        fun next() = rethrowChecked {
          delegate.next()
        }
      }
    }
  }
}
