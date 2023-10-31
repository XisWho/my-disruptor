package com.my.v5.component;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 生产者和消费者内部都会维护一个自己独立的MySequence序列对象
 */
public class MySequence {

    /**
     * 需要被生产者、消费者同时访问，因此内部是一个volatile修饰的long值
     * 序列起始值默认是-1，保证下一个序列恰好是0（即第一个合法的序列号）
     * value是会一直单调递增的，不会取余
     * long类型足够大，可以不考虑溢出
     */
    private volatile long value = -1;

    private static final Unsafe UNSAFE;

    private static final long VALUE_OFFSET;

    static {
        try {
            // Unsafe只能被jdk信任的类来直接使用，所以需要使用反射来绕过这一限制
            Field getUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            getUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) getUnsafe.get(null);
            VALUE_OFFSET = UNSAFE.objectFieldOffset(MySequence.class.getDeclaredField("value"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MySequence() {}

    public MySequence(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
    }

    public void lazySet(long value) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, value);
    }

    public boolean compareAndSet(long expect, long update){
        return UNSAFE.compareAndSwapLong(this, VALUE_OFFSET, expect, update);
    }

}
