package com.my.v1.component;

public class MySequenceBarrier {

    private final MySequence currentProducerSequence;
    private final MyWaitStrategy myWaitStrategy;

    public MySequenceBarrier(MySequence currentProducerSequence, MyWaitStrategy myWaitStrategy) {
        this.currentProducerSequence = currentProducerSequence;
        this.myWaitStrategy = myWaitStrategy;
    }

    /**
     * 获得可用的消费者下标
     * @param currentConsumeSequence
     * @return
     */
    public long getAvailableConsumeSequence(long currentConsumeSequence) throws InterruptedException {
        return this.myWaitStrategy.waitFor(currentConsumeSequence, currentProducerSequence);
    }

}
