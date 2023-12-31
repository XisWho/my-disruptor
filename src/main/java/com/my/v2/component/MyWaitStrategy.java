package com.my.v2.component;

import java.util.List;

/**
 * 如果生产者生产速度跟不上消费者消费速度，那么消费者需要等待的策略
 */
public interface MyWaitStrategy {

    /**
     * 类似jdk Condition的await，如果不满足条件就会阻塞在该方法内，不返回
     * */
    long waitFor(long currentConsumeSequence, MySequence currentProducerSequence, List<MySequence> dependentSequences) throws InterruptedException;

    /**
     * 类似jdk Condition的signal，唤醒waitFor阻塞在该等待策略对象上的消费者线程
     * */
    void signalWhenBlocking();

}
