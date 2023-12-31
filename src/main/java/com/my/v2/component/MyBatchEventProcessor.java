package com.my.v2.component;

/**
 * 单线程消费者
 * @param <T>
 */
public class MyBatchEventProcessor<T> implements Runnable {

    private final MySequence currentConsumeSequence = new MySequence(-1);
    private final MyRingBuffer<T> myRingBuffer;
    private final MyEventHandler<T> myEventConsumer;
    private final MySequenceBarrier mySequenceBarrier;

    public MyBatchEventProcessor(MyRingBuffer<T> myRingBuffer,
                                 MyEventHandler<T> myEventConsumer,
                                 MySequenceBarrier mySequenceBarrier) {
        this.myRingBuffer = myRingBuffer;
        this.myEventConsumer = myEventConsumer;
        this.mySequenceBarrier = mySequenceBarrier;
    }

    @Override
    public void run() {
        // 下一个需要消费的下标
        long nextConsumerIndex = currentConsumeSequence.get() + 1;

        // 消费者线程主循环逻辑，不断的尝试获取事件并进行消费（为了让代码更简单，暂不考虑优雅停止消费者线程的功能）
        while(true) {
            try {
                // 如果nextConsumerIndex位置上有正确的生产者生产的元素，那么就会返回nextConsumerIndex，消费者就可以正常消费
                // 否则，根据WaitStrategy进行相应的处理，可能是阻塞等待
                long availableConsumeIndex = this.mySequenceBarrier.getAvailableConsumeSequence(nextConsumerIndex);

                while (nextConsumerIndex <= availableConsumeIndex) {
                    // 取出可以消费的下标对应的事件，交给eventConsumer消费
                    T event = myRingBuffer.get(nextConsumerIndex);
                    this.myEventConsumer.consume(event, nextConsumerIndex, nextConsumerIndex == availableConsumeIndex);
                    // 批处理，一次主循环消费N个事件（下标加1，获取下一个）
                    nextConsumerIndex++;
                }

                // 更新当前消费者的消费的序列（lazySet，不需要生产者实时的强感知刷缓存，性能更好，因为生产者自己也不是实时的读消费者序列的）
                this.currentConsumeSequence.lazySet(availableConsumeIndex);
            } catch (final Throwable ex) {
                // 发生异常，消费进度依然推进（跳过这一批拉取的数据）（lazySet 原理同上）
                this.currentConsumeSequence.lazySet(nextConsumerIndex);
                nextConsumerIndex++;
            }
        }
    }

    public MySequence getCurrentConsumeSequence() {
        return this.currentConsumeSequence;
    }

}
