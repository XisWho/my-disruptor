package com.my.v2.component;

import java.util.Collections;
import java.util.List;

public class MySequenceBarrier {

    private final MySequence currentProducerSequence;
    private final MyWaitStrategy myWaitStrategy;
    private final List<MySequence> dependentSequencesList;

    public MySequenceBarrier(MySequence currentProducerSequence, MyWaitStrategy myWaitStrategy, List<MySequence> dependentSequencesList) {
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
        return this.myWaitStrategy.waitFor(currentConsumeSequence, currentProducerSequence, dependentSequencesList);
    }

}
