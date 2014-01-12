package me.FurH.Core.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @param <V> 
 * @author Barak Bar Orion
 */
public class DirectFutureTask<V> extends FutureTask<V> {

    public DirectFutureTask(Callable<V> vCallable) {
        super(vCallable);
    }

    public DirectFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        super.run();
        return super.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new IllegalArgumentException("Not implemented");
    }
}