package com.velocitypowered.proxy.util.collect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import io.netty.util.collection.IntObjectMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OverlayIntObjectMap<K> extends OverlayMap<Integer, K> implements IntObjectMap<K> {

    public OverlayIntObjectMap(Map<Integer, K> parent, Map<Integer, K> overlay) {
        super(parent, overlay);
    }

    @Override
    public K get(int key) {
        return super.get(key);
    }

    @Override
    public K put(int key, K value) {
        return super.put(key, value);
    }

    @Override
    public K remove(int key) {
        return super.remove(key);
    }

    @Override
    public Iterable<PrimitiveEntry<K>> entries() {
        return Iterables.concat(((IntObjectMap<K>) parent).entries(), ((IntObjectMap<K>) overlay).entries());
    }

    @Override
    public boolean containsKey(int key) {
        return super.containsKey(key);
    }

    @Override
    public Set<Integer> keySet() {
        return Streams.concat(this.parent.keySet().stream(), this.overlay.keySet().stream()).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Collection<K> values() {
        return Streams.concat(this.parent.values().stream(), this.overlay.values().stream()).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Set<Entry<Integer, K>> entrySet() {
        return Streams.concat(this.parent.entrySet().stream(), this.overlay.entrySet().stream()).collect(Collectors.toSet());
    }
}
