package com.my.v5.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * 单线程生产者序列器
 * 因为是单线程，所以在设计上就是线程不安全的
 */
public class MySingleProducerSequencer implements MyProducerSequencer {

    /**
     * 生产者序列器所属ringBuffer的大小
     */
    private final int ringBufferSize;

    /**
     * 当前已发布的生产者序列器（区别于nextValue成员变量）
     */
    private final MySequence currentProducerSequence = new MySequence();
    /**
     * 当前已申请的序列（但是否发布了，还得看currentProductSequence）
     * 单线程生产者内部使用，所以就是普通的long。不考虑并发
     */
    private long nextValue = -1;

    /**
     * 消费者序列集合
     */
    private final List<MySequence> gatingConsumerSequenceList = new ArrayList<>();
    /**
     * 当前已缓存的最慢的消费者序列
     * 单线程生产者内部使用，所以就是普通的long，不考虑并发
     */
    private long cachedConsumerSequenceValue = -1;

    private final MyWaitStrategy myWaitStrategy;

    public MySingleProducerSequencer(int ringBufferSize, MyWaitStrategy myWaitStrategy) {
        this.ringBufferSize = ringBufferSize;
        this.myWaitStrategy = myWaitStrategy;
    }

    /**
     * 默认申请1个生产者序列号
     * @return
     */
    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        // 申请的下一个生产者位点
        long nextProducerSequence = this.nextValue + n;
        // 新申请的位点下，生产者恰好超过消费者一圈的环绕临界点序列
        long wrapPoint = nextProducerSequence - this.ringBufferSize;

        // 获得当前已缓存的消费者位点
        long cacheGatingSequence = this.cachedConsumerSequenceValue;

        // 注意，cacheGatingSequence是缓存的，并不是最新的
        // 所以如果缓存值都没有超过环绕点一圈，那么生产者是可以放心生产的
        // 每次发布都实时获取反而会触发对消费者sequence强一致的读，迫使消费者线程所在的CPU刷新缓存（而这是不需要的）
        if (wrapPoint > cacheGatingSequence) {
            // 比起disruptor省略了if中的cachedGatingSequence > nextProducerSequence逻辑
            // 原因请见：https://github.com/LMAX-Exchange/disruptor/issues/76
            // 看了一下这个网址，里面说：
            //     在Disruptor中有一个相当深奥的特性，其中会将sequence重置为旧值。
            //     如果是这样，那么cachedGatingSequence > nextProducerSequence

            // 比起disruptor省略了currentProducerSequence.set(nextProducerSequence);
            // 原因请见：https://github.com/LMAX-Exchange/disruptor/issues/291
            // 看了一下这个网址，里面说：
            //     This is there because of a feature that we added in the very early days to allow the dynamic addition of gating sequences to the RingBuffer. This requires a StoreLoad barrier when updating the sequence. So strictly for message passing it is not required.
            long minSequence;

            // 当生产者发现确实当前已经超过了一圈，则必须去读最新的消费者序列了，看看消费者的消费进度是否推进了
            // 自旋
            // 这里的consumerSequence.get是对volatile变量的读，是实时的，强一致的读
            while (wrapPoint > (minSequence = SequenceUtil.getMinimumSequence(nextProducerSequence, gatingConsumerSequenceList))) {
                // 如果确实超过了一圈，则生产者无法获取可用的队列空间，循环的间歇性park阻塞
                LockSupport.parkNanos(1L);
            }

            // 满足条件了，则缓存获得最新的消费者序列
            // 因为不是实时获取消费者序列，可能cachedValue比上一次的值要大得多
            // 这种情况下，待到下一次next申请时就可以不用去强一致的读consumerSequence了
            this.cachedConsumerSequenceValue = minSequence;
        }

        // 记录本次申请后的，已申请的生产者位点
        this.nextValue = nextProducerSequence;

        return nextProducerSequence;
    }

    @Override
    public void publish(long publishIndex) {
        // 发布时，更新生产者队列
        // lazyset，由于消费者可以批量的拉取数据，所以不必每次发布时都volatile的更新，允许消费者晚一点感知到，这样性能会更好
        // 设置写屏障
        this.currentProducerSequence.lazySet(publishIndex);
        // 发布完成后，唤醒可能阻塞等待的消费者线程
        this.myWaitStrategy.signalWhenBlocking();
    }

    @Override
    public MySequenceBarrier newBarrier(){
        return new MySequenceBarrier(this, this.currentProducerSequence,this.myWaitStrategy,new ArrayList<>());
    }

    @Override
    public MySequenceBarrier newBarrier(MySequence... dependenceSequences){
        return new MySequenceBarrier(this, this.currentProducerSequence,this.myWaitStrategy,new ArrayList<>(Arrays.asList(dependenceSequences)));
    }

    @Override
    public void addGatingConsumerSequence(MySequence newGatingConsumerSequence){
        this.gatingConsumerSequenceList.add(newGatingConsumerSequence);
    }

    @Override
    public void addGatingConsumerSequenceList(MySequence... newGatingConsumerSequences) {
        this.gatingConsumerSequenceList.addAll(Arrays.asList(newGatingConsumerSequences));
    }

    @Override
    public int getRingBufferSize() {
        return ringBufferSize;
    }

    @Override
    public long getHighestPublishedSequence(long nextSequence, long availableSequence) {
        return availableSequence;
    }

    @Override
    public MySequence getCurrentProducerSequence() {
        return currentProducerSequence;
    }

    @Override
    public void removeConsumerSequence(MySequence sequenceNeedRemove) {
        this.gatingConsumerSequenceList.remove(sequenceNeedRemove);
    }

}
