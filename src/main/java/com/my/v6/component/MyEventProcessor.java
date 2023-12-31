package com.my.v6.component;

/**
 * 事件处理器（消费者）接口
 * */
public interface MyEventProcessor extends Runnable{

    /**
     * 获得当前消费者序列对象
     * @return 当前消费者序列对象
     * */
    MySequence getCurrentConsumeSequence();

    void halt();

    boolean isRunning();

}
