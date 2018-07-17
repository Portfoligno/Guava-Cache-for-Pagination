@file:JvmName("Cuts")
package io.github.portfoligno.guava.cache.sorted

import com.google.common.collect.Range

val <C> Range<C>.lowerBound: Cut<C> where C : Any, C : Comparable<C>
  get() =
    if (!hasLowerBound())
      Cut.unbounded()
    else
      Cut.bounded(lowerBoundType(), lowerEndpoint())

val <C> Range<C>.upperBound: Cut<C> where C : Any, C : Comparable<C>
  get() =
    if (!hasUpperBound())
      Cut.unbounded()
    else
      Cut.bounded(upperBoundType(), upperEndpoint())
