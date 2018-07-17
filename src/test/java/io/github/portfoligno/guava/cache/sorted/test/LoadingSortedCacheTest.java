package io.github.portfoligno.guava.cache.sorted.test;

import com.google.common.collect.*;
import io.github.portfoligno.guava.cache.sorted.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class LoadingSortedCacheTest {
  private Random random;
  private NavigableMap<Long, Integer> source;
  private LoadingSortedCache<Long, Integer> cache;

  @Before
  public void setUp() {
    random = new Random();
    source = new TreeMap<>();
    for (int i = 0; i < 10000; i++) {
      long key = random.nextLong();
      source.put(key, random.nextInt());
    }
    cache = SortedCacheBuilder
        .newBuilder()
        .build(512, new SortedCacheLoader<Long, Integer>() {
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

  private BoundType nextBoundType() {
    return random.nextBoolean() ? BoundType.CLOSED : BoundType.OPEN;
  }

  private Range<Long> toRange(Cut<Long> cut) {
    return cut.upTo(Cut.<Long>unbounded());
  }

  @Test
  public void getZeroSizedConsistent() {
    Cut.Bounded<Long> cut = Cut.bounded(nextBoundType(), random.nextLong());
    assertEquals(
        FluentIterable.from(getSourceEntries(cut)).limit(0).toList(),
        FluentIterable.from(cache.get(toRange(cut))).limit(0).toList());
  }

  @Test
  public void getOneConsistent() {
    Cut.Bounded<Long> cut = Cut.bounded(nextBoundType(), random.nextLong());
    assertEquals(
        FluentIterable.from(getSourceEntries(cut)).limit(1).toList(),
        FluentIterable.from(cache.get(toRange(cut))).limit(1).toList());
  }

  @Test
  public void getMiddleSizedConsistent() {
    Cut.Bounded<Long> cut = Cut.bounded(nextBoundType(), random.nextLong());
    int amount = 512 + random.nextInt(512);
    assertEquals(
        FluentIterable.from(getSourceEntries(cut)).limit(amount).toList(),
        FluentIterable.from(cache.get(toRange(cut))).limit(amount).toList());
  }

  @Test
  public void getAllConsistent() {
    assertEquals(
        ImmutableList.copyOf(source.entrySet()),
        ImmutableList.copyOf(cache.get(Range.<Long>all())));
  }

  @Test
  public void invalidateAllHasNoEffectsOnValues() {
    Cut.Bounded<Long> cut = Cut.bounded(nextBoundType(), random.nextLong());
    List<Entry<Long, Integer>> entries = ImmutableList.copyOf(cache.get(toRange(cut)));
    cache.invalidateAll();
    assertEquals(entries, ImmutableList.copyOf(cache.get(toRange(cut))));
  }
}
