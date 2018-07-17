package io.github.portfoligno.guava.cache.sorted.test;

import com.google.common.collect.*;
import io.github.portfoligno.guava.cache.sorted.Cut;
import io.github.portfoligno.guava.cache.sorted.LoadingSortedCache;
import io.github.portfoligno.guava.cache.sorted.SortedCacheBuilder;
import io.github.portfoligno.guava.cache.sorted.SortedCacheLoader;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class LoadingSortedCacheTest2 {
  private Random random;
  private NavigableMap<Long, Integer> source;
  private LoadingSortedCache<Long, Integer> cache;

  @Before
  public void setUp() {
    random = new Random();
    source = ImmutableSortedMap
        .<Long, Integer>naturalOrder()
        .put(0L, random.nextInt()) // 0
        .put(1L, random.nextInt()) // 1
        .put(15L, random.nextInt()) // 2
        .put(16L, random.nextInt()) // 3
        .put(30L, random.nextInt()) // 4
        .put(31L, random.nextInt()) // 5
        .put(46L, random.nextInt()) // 6
        .put(56L, random.nextInt()) // 7
        .put(70L, random.nextInt()) // 8
        .build();
    cache = SortedCacheBuilder
        .newBuilder()
        .build(2, new SortedCacheLoader<Long, Integer>() {
          @Override
          public @NotNull Iterable<Entry<Long, Integer>> loadChunk(@NotNull Cut<Long> cut, int size) {
            return Iterables.limit(getSourceEntries(cut), size);
          }
        });
  }

  private Iterable<Entry<Long, Integer>> getSourceEntries(Cut<Long> cut) {
    if (cut instanceof Cut.Unbounded) {
      return source.entrySet();
    }
    Cut.Bounded<Long> c = (Cut.Bounded<Long>) cut;
    return source.tailMap(c.getEndpoint(), c.getType() == BoundType.CLOSED).entrySet();
  }

  @Test
  public void getEmptyRange() {
    assertEquals(
        ImmutableList.of(),
        ImmutableList.copyOf(cache.get(Range.closedOpen(0L, 0L))));
  }

  @Test
  public void getSingletonRange() {
    assertEquals(
        ImmutableList.of(Maps.immutableEntry(30L, source.get(30L))),
        ImmutableList.copyOf(cache.get(Range.singleton(30L))));
  }

  @Test
  public void getOpen() {
    assertEquals(
        3,
        Iterables.size(cache.get(Range.open(15L, 46L))));
  }

  @Test
  public void getClosed() {
    assertEquals(
        5,
        Iterables.size(cache.get(Range.closed(15L, 46L))));
  }

  @Test
  public void getOpenClosed() {
    assertEquals(
        4,
        Iterables.size(cache.get(Range.openClosed(15L, 46L))));
  }

  @Test
  public void getClosedOpen() {
    assertEquals(
        4,
        Iterables.size(cache.get(Range.closedOpen(15L, 46L))));
  }

  @Test
  public void getDownTo() {
    assertEquals(
        8,
        Iterables.size(cache.get(Range.downTo(1L, BoundType.CLOSED))));
  }

  @Test
  public void getUpTo() {
    assertEquals(
        2,
        Iterables.size(cache.get(Range.upTo(1L, BoundType.CLOSED))));
  }
}
