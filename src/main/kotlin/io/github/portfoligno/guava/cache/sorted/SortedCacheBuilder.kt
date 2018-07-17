package io.github.portfoligno.guava.cache.sorted

import com.google.common.cache.CacheBuilder
import java.util.concurrent.TimeUnit

class SortedCacheBuilder private constructor (
    private val delegate: CacheBuilder<Any, Any>
) {
  companion object {
    private
    fun create(delegate: CacheBuilder<Any, Any>) = SortedCacheBuilder(delegate)

    @JvmStatic
    fun newBuilder() = create(CacheBuilder.newBuilder())
  }

  fun weakValues() = create(delegate.weakValues())

  fun softValues() = create(delegate.softValues())

  fun expireAfterWrite(duration: Long, unit: TimeUnit) =
      create(delegate.expireAfterWrite(duration, unit))

  fun expireAfterAccess(duration: Long, unit: TimeUnit) =
      create(delegate.expireAfterAccess(duration, unit))

  @Deprecated("Use the SortedCacheLoader variant instead")
  fun <K, V : Any> build(chunkSize: Int, loader: PaginatingCacheLoader<K, V>): PaginatingCache<K, V>
      where K : Any, K : Comparable<K> =
      PaginatingCacheImpl.create(chunkSize, loader, delegate)

  fun <K, V : Any> build(chunkSize: Int, loader: SortedCacheLoader<K, V>): LoadingSortedCache<K, V>
      where K : Any, K : Comparable<K> =
      LoadingSortedCacheImpl.create(chunkSize, loader, delegate)
}
