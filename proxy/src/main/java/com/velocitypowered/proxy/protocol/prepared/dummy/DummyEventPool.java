package com.velocitypowered.proxy.protocol.prepared.dummy;

import io.netty.channel.*;
import io.netty.util.concurrent.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DummyEventPool implements EventLoop {

    @Override
    public EventLoopGroup parent() {
        return null;
    }

    @Override
    public boolean inEventLoop() {
        return true;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return true;
    }

    @Override
    public <V> Promise<V> newPromise() {
        return null;
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return null;
    }

    @Override
    public <V> Future<V> newSucceededFuture(V v) {
        return null;
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable throwable) {
        return null;
    }

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public Future<?> shutdownGracefully() {
        return null;
    }

    @Override
    public Future<?> shutdownGracefully(long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> terminationFuture() {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long l, @NonNull TimeUnit timeUnit) {
        return false;
    }

    @Override
    public EventLoop next() {
        return null;
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return null;
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return null;
    }

    @NonNull
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> collection) {
        return null;
    }

    @NonNull
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> collection, long l, @NonNull TimeUnit timeUnit) {
        return null;
    }

    @NonNull
    @Override
    public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> collection) {
        return null;
    }

    @Override
    public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> collection, long l, @NonNull TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return null;
    }

    @Override
    public ChannelFuture register(ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise channelPromise) {
        return null;
    }

    @Override
    public void execute(@NonNull Runnable runnable) {

    }
}
