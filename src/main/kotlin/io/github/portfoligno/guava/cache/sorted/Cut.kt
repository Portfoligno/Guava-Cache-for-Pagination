package io.github.portfoligno.guava.cache.sorted

import com.google.common.collect.BoundType
import com.google.common.collect.Range

sealed class Cut<C> where C : Any, C : Comparable<C> {
  abstract fun upTo(other: Cut<C>): Range<C>

  // Identity equivalence
  class Unbounded<C> private constructor () : Cut<C>() where C : Any, C : Comparable<C> {
    internal
    companion object {
      val INSTANCE = Cut.Unbounded<Comparable<Comparable<*>>>()
    }

    override
    fun upTo(other: Cut<C>): Range<C> {
      if (other !is Bounded) {
        return Range.all()
      }
      return Range.upTo(other.endpoint, other.type)
    }
  }

  sealed class Bounded<C> : Cut<C>() where C : Any, C : Comparable<C> {
    abstract val endpoint: C
    abstract val type: BoundType

    override fun upTo(other: Cut<C>): Range<C> {
      if (other !is Bounded) {
        return Range.downTo(endpoint, type)
      }
      return Range.range(endpoint, type, other.endpoint, other.type)
    }

    data class Open<C> (
        override
        val endpoint: C
    ) : Bounded<C>() where C : Any, C : Comparable<C> {
      override
      val type: BoundType
        get() = BoundType.OPEN
    }

    @Suppress("EqualsOrHashCode") // Use generated `equals`
    data class Closed<C> (
        override
        val endpoint: C
    ) : Bounded<C>() where C : Any, C : Comparable<C> {
      override
      val type: BoundType
        get() = BoundType.CLOSED

      override
      fun hashCode(): Int {
        return endpoint.hashCode() + 0xD704AF73.toInt()
      }
    }
  }

  companion object {
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <C> unbounded(): Unbounded<C> where C : Any, C : Comparable<C> =
        Unbounded.INSTANCE as Unbounded<C>

    @JvmStatic
    fun <C> bounded(type: BoundType, endpoint: C): Bounded<C> where C : Any, C : Comparable<C> =
        if (type == BoundType.OPEN) open(endpoint) else closed(endpoint)

    @JvmStatic
    fun <C> open(endpoint: C): Bounded.Open<C> where C : Any, C : Comparable<C> =
        Bounded.Open(endpoint)

    @JvmStatic
    fun <C> closed(endpoint: C): Bounded.Closed<C> where C : Any, C : Comparable<C> =
        Bounded.Closed(endpoint)
  }
}
