package io.github.portfoligno.guava.cache.sorted

import com.google.common.collect.BoundType

internal
object ForwardCutComparator : Comparator<Cut<Comparable<Comparable<*>>>> {
  @Suppress("UNCHECKED_CAST")
  fun <C> instance(): Comparator<Cut<C>> where C : Any, C : Comparable<C> =
      this as Comparator<Cut<C>>

  override
  fun compare(a: Cut<Comparable<Comparable<*>>>, b: Cut<Comparable<Comparable<*>>>): Int {
    if (a === b) {
      return 0
    }
    if (a !is Cut.Bounded) {
      // Unbounded cuts go first
      return -1
    }
    if (b !is Cut.Bounded) {
      // Unbounded cuts go first
      return 1
    }
    // a and b is both bounded
    val result = a.endpoint.compareTo(b.endpoint)

    if (result != 0) {
      // Not a tie
      return result
    }
    // a and b is at the same endpoint
    if (a.type == b.type) {
      return 0
    }
    if (a.type == BoundType.OPEN) {
      // Exclusive boundaries go first
      return -1
    }
    return 1
  }
}
