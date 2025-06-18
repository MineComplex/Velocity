package com.velocitypowered.proxy.util.collect;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class OverlayObject2IntMap<K> extends OverlayMap<K, Integer> implements Object2IntMap<K> {

    public OverlayObject2IntMap(Object2IntMap<K> parent, Object2IntMap<K> overlay) {
        super(parent, overlay);
        overlay.defaultReturnValue(parent.defaultReturnValue());
    }

    @Override
    public int getInt(Object key) {
        int value = ((Object2IntMap<K>) this.overlay).getInt(key);
        if (value == this.defaultReturnValue()) {
            value = ((Object2IntMap<K>) this.parent).getInt(key);
        }

        return value;
    }

    @Override
    public int put(K key, int value) {
        return ((Object2IntMap<K>) this.overlay).put(key, value);
    }

    @Override
    public void defaultReturnValue(int rv) {
        ((Object2IntMap<K>) this.overlay).defaultReturnValue(rv);
    }

    @Override
    public int defaultReturnValue() {
        return ((Object2IntMap<K>) this.overlay).defaultReturnValue();
    }

    @Override
    public ObjectSet<Object2IntMap.Entry<K>> object2IntEntrySet() {
        return new SetIsObjectSet<>(
                Streams
                        .concat(((Object2IntMap<K>) this.parent).object2IntEntrySet().stream(), ((Object2IntMap<K>) this.overlay).object2IntEntrySet().stream())
                        .collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public ObjectSet<K> keySet() {
        return new SetIsObjectSet<>(
                Streams
                        .concat(((Object2IntMap<K>) this.parent).keySet().stream(), ((Object2IntMap<K>) this.overlay).keySet().stream())
                        .collect(Collectors.toSet()));
    }

    @Override
    public IntCollection values() {
        return Streams
                .concat(((Object2IntMap<K>) this.parent).values().intStream(), ((Object2IntMap<K>) this.overlay).values().intStream())
                .boxed()
                .collect(Collectors.toCollection(IntArrayList::new));
    }

    @Override
    public boolean containsValue(int value) {
        return false;
    }
}
