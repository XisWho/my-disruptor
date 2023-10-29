package com.my.v1.component;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞等待策略
 */
public class MyBlockingWaitStrategy implements MyWaitStrategy {

    private final Lock lock = new ReentrantLock();
    private final Condition processorNotifyCondition = lock.newCondition();

    @Override
    public long waitFor(long currentConsumeSequence, MySequence currentProducerSequence) throws InterruptedException {
        // 强一致的读生产者序列号
        // 如果ringBuffer的生产者下标小于当前消费者所需的下标，说明目前消费者消费速度大于生产者生产速度
        if (currentProducerSequence.get() < currentConsumeSequence) {
            lock.lock();
            try {
                while (currentProducerSequence.get() < currentConsumeSequence) {
                    // 消费者的消费速度比生产者的生产速度快时，阻塞等待
                    processorNotifyCondition.await();
                }
            }
            finally {
                lock.unlock();
            }
        }
        return currentConsumeSequence;
    }

    @Override
    public void signalWhenBlocking() {
        lock.lock();
        try {
            // signal唤醒所有阻塞在条件变量上的消费者线程
            processorNotifyCondition.signal();
        } finally {
            lock.unlock();
        }
    }

}
