/*
 * Copyright (C) 2025 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.util.collect;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SetIsObjectSet<K> implements ObjectSet<K> {
  private final Set<K> set;

  public SetIsObjectSet(Set<K> set) {
    this.set = set;
  }

  @Override
  public int size() {
    return set.size();
  }

  @Override
  public boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return set.contains(o);
  }

  @Override
  public ObjectIterator<K> iterator() {
    return new IteratorIsObjectIterator<>(set.iterator());
  }

  @NonNull
  @Override
  public Object[] toArray() {
    return set.toArray();
  }

  @NonNull
  @Override
  public <T> T[] toArray(T @NonNull [] ts) {
    return set.toArray(ts);
  }

  @Override
  public boolean add(K k) {
    return set.add(k);
  }

  @Override
  public boolean remove(Object o) {
    return set.remove(o);
  }

  @Override
  public boolean containsAll(@NonNull Collection<?> collection) {
    return set.containsAll(collection);
  }

  @Override
  public boolean addAll(@NonNull Collection<? extends K> collection) {
    return set.addAll(collection);
  }

  @Override
  public boolean removeAll(@NonNull Collection<?> collection) {
    return set.removeAll(collection);
  }

  @Override
  public boolean retainAll(@NonNull Collection<?> collection) {
    return set.retainAll(collection);
  }

  @Override
  public void clear() {
    set.clear();
  }

  private static final class IteratorIsObjectIterator<K> implements ObjectIterator<K> {

    private final Iterator<K> iterator;

    private IteratorIsObjectIterator(Iterator<K> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public K next() {
      return iterator.next();
    }
  }
}
