package io.github.portfoligno.guava.cache.sorted

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.collections.Map.Entry
import com.google.common.collect.ImmutableSortedMap as Sorted
import kotlin.collections.AbstractIterator as Itr

internal
class LoadingSortedCacheImpl<K, V : Any>(
    private
    val delegate: LoadingCache<Cut<K>, Sorted<K, V>>
) : LoadingSortedCache<K, V>
    where K : Any, K : Comparable<K> {
  companion object {
    fun <K, V : Any> create(
        chunkSize: Int,
        loader: SortedCacheLoader<K, V>,
        cacheBuilder: CacheBuilder<Any, Any>): LoadingSortedCacheImpl<K, V>
        where K : Any, K : Comparable<K> {
      require(chunkSize > 0) { "chunkSize = $chunkSize" }

      return LoadingSortedCacheImpl(cacheBuilder.build(object : CacheLoader<Cut<K>, Sorted<K, V>>() {
        override
        fun load(cut: Cut<K>) =
            Sorted.copyOf(loader.loadChunk(cut, chunkSize))
      }))
    }
  }

  private
  val keys = ConcurrentSkipListSet<Cut<K>>(ForwardCutComparator.instance())

  override
  fun invalidateAll() {
    keys.clear()
    delegate.invalidateAll()
  }

  private
  fun loadSorted(cut: Cut<K>): Sorted<K, V> {
    val m = delegate.getUnchecked(cut)
    keys.add(cut)
    return m
  }

  private
  fun getSortedIfPresent(cut: Cut<K>): Sorted<K, V>? {
    if (cut !is Cut.Bounded) {
      return delegate.getIfPresent(cut)
    }
    fun go(m: Sorted<K, V>): Sorted<K, V>? {
      if (!m.isEmpty()) {
        val inclusive = cut is Cut.Bounded.Closed
        val hasNext =
            if (inclusive)
              m.lastKey() >= cut.endpoint
            else
              m.lastKey() > cut.endpoint

        if (hasNext) {
          return m.tailMap(cut.endpoint, inclusive)
        }
        return null
      }
      return m
    }

    for (k in keys
        .headSet(cut, true) // Inclusive
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
  fun getChunkIfPresent(cut: Cut<K>): Iterable<Entry<K, V>>? =
      getSortedIfPresent(cut)?.entries

  override
  fun getChunk(cut: Cut<K>, loader: Callable<out Iterable<Entry<K, V>>>): Iterable<Entry<K, V>> =
      (getSortedIfPresent(cut) ?: delegate.get(cut) { Sorted.copyOf(loader.call()) }).entries

  private
  fun get(cut: Cut<K>) = Iterable {
    Iterators.concat(object : Itr<Iterator<Entry<K, V>>>() {
      var cursor = cut

      override
      fun computeNext() {
        val m = getSortedIfPresent(cursor) ?: loadSorted(cursor)

        if (!m.isEmpty()) {
          cursor = Cut.open(m.lastKey())
          setNext(m.entries.iterator())
        }
        else {
          done()
        }
      }
    })
  }

  override
  fun get(keys: Range<K>): Iterable<Entry<K, V>> {
    if (keys.isEmpty) {
      return ImmutableSet.of()
    }
    val iterable = get(keys.lowerBound)

    if (!keys.hasUpperBound()) {
      return iterable
    }
    val end = keys.upperEndpoint()

    if (keys.upperBoundType() == BoundType.OPEN) {
      return iterable.takeWhile { it.key < end }
    }
    return iterable.takeWhile { it.key <= end }
  }
}
