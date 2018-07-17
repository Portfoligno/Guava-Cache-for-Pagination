package io.github.portfoligno.guava.cache.sorted.test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import io.github.portfoligno.guava.cache.sorted.PaginatingCache;
import io.github.portfoligno.guava.cache.sorted.PaginatingCacheLoader;
import io.github.portfoligno.guava.cache.sorted.SortedCacheBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class PaginatingCacheTest {
  private Random random;
  private NavigableMap<Long, Integer> source;
  private PaginatingCache<Long, Integer> cache;

  @Before
  public void setUp() {
    random = new Random();
    source = new TreeMap<>();
    for (int i = 0; i < 10000; i++) {
      long key = random.nextLong();

      if (key > Long.MIN_VALUE) {
        source.put(key, random.nextInt());
      }
    }
    cache = SortedCacheBuilder
        .newBuilder()
        .build(512, new PaginatingCacheLoader<Long, Integer>() {
          @Override
          public @NotNull Iterable<Entry<Long, Integer>> loadGreaterThan(@NotNull Long key, int amount) {
            return Iterables.limit(source.tailMap(key, false).entrySet(), amount);
          }
        });
  }

  @Test
  public void getZeroSizedConsistent() {
    long key = random.nextLong();
    assertEquals(
        FluentIterable.from(source.tailMap(key, false).entrySet()).limit(0).toList(),
        FluentIterable.from(cache.getAllGreaterThan(key)).limit(0).toList());
  }

  @Test
  public void getOneConsistent() {
    long key = random.nextLong();
    assertEquals(
        FluentIterable.from(source.tailMap(key, false).entrySet()).limit(1).toList(),
        FluentIterable.from(cache.getAllGreaterThan(key)).limit(1).toList());
  }

  @Test
  public void getMiddleSizedConsistent() {
    long key = random.nextLong();
    int amount = 512 + random.nextInt(512);
    assertEquals(
        FluentIterable.from(source.tailMap(key, false).entrySet()).limit(amount).toList(),
        FluentIterable.from(cache.getAllGreaterThan(key)).limit(amount).toList());
  }

  @Test
  public void getAllConsistent() {
    assertEquals(
        ImmutableList.copyOf(source.entrySet()),
        ImmutableList.copyOf(cache.getAllGreaterThan(Long.MIN_VALUE)));
  }

  @Test
  public void invalidateAllHasNoEffectsOnValues() {
    long key = random.nextLong();
    List<Entry<Long, Integer>> entries = ImmutableList.copyOf(cache.getAllGreaterThan(key));
    cache.invalidateAll();
    assertEquals(entries, ImmutableList.copyOf(cache.getAllGreaterThan(key)));
  }
}
