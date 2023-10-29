package com.my.v1.test;

import com.my.v1.component.MyEventHandler;

/**
 * 订单事件处理器
 */
public class OrderEventHandlerDemo implements MyEventHandler<OrderEventModel> {

    @Override
    public void consume(OrderEventModel event, long sequence, boolean endOfBatch) {
        System.out.println("消费者消费事件" + event + " sequence=" + sequence + " endOfBatch=" + endOfBatch);
    }

}
