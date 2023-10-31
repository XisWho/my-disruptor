package com.my.v6.component;

import java.util.Collections;
import java.util.List;

public class MySequenceBarrier {

    private volatile boolean alerted = false;
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
    public long getAvailableConsumeSequence(long currentConsumeSequence) throws InterruptedException, MyAlertException {
        // 每次都检查下是否有被唤醒，被唤醒则会抛出MyAlertException代表当前消费者要终止运行了
        checkAlert();

        long availableSequence =  this.myWaitStrategy.waitFor(currentConsumeSequence, currentProducerSequence, dependentSequencesList);
        // 正常来说，availableSequence >= currentConsumeSequence
        if (availableSequence < currentConsumeSequence) {
            return availableSequence;
        }

        // 多线程生产者中，需要进一步约束
        return myProducerSequencer.getHighestPublishedSequence(currentConsumeSequence, availableSequence);
    }

    /**
     * 检查当前消费者的被唤醒状态
     * */
    public void checkAlert() throws MyAlertException {
        if (alerted) {
            throw MyAlertException.INSTANCE;
        }
    }

    /**
     * 唤醒可能处于阻塞态的消费者
     * */
    public void alert() {
        this.alerted = true;
        this.myWaitStrategy.signalWhenBlocking();
    }

    /**
     * 重新启动时，清除标记
     */
    public void clearAlert() {
        this.alerted = false;
    }

}
