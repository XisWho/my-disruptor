package com.my.v5.component;

import java.util.Collections;
import java.util.List;

public class MySequenceBarrier {

    private final MyProducerSequencer myProducerSequencer;
    private final MySequence currentProducerSequence;
    private final MyWaitStrategy myWaitStrategy;
    private final List<MySequence> dependentSequencesList;

    public MySequenceBarrier(MyProducerSequencer myProducerSequencer, MySequence currentProducerSequence, MyWaitStrategy myWaitStrategy, List<MySequence> dependentSequencesList) {
        this.myProducerSequencer = myProducerSequencer;
        this.currentProducerSequence = currentProducerSequence;
        this.myWaitStrategy = myWaitStrategy;

        if (!dependentSequencesList.isEmpty()) {
            this.dependentSequencesList = dependentSequencesList;
        } else {
            // 如果传入的上游依赖序列为空，则生产者序列号作为兜底的依赖
            this.dependentSequencesList = Collections.singletonList(currentProducerSequence);
        }
    }

    /**
     * 获得可用的消费者下标
     * @param currentConsumeSequence
     * @return
     */
    public long getAvailableConsumeSequence(long currentConsumeSequence) throws InterruptedException {
        long availableSequence =  this.myWaitStrategy.waitFor(currentConsumeSequence, currentProducerSequence, dependentSequencesList);
        // 正常来说，availableSequence >= currentConsumeSequence
        if (availableSequence < currentConsumeSequence) {
            return availableSequence;
        }

        // 多线程生产者中，需要进一步约束
        return myProducerSequencer.getHighestPublishedSequence(currentConsumeSequence, availableSequence);
    }

}
