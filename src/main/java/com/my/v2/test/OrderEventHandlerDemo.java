package com.my.v2.test;

import com.my.v2.component.MyEventHandler;

/**
 * 订单事件处理器
 */
public class OrderEventHandlerDemo implements MyEventHandler<OrderEventModel> {

    private String sign;

    public OrderEventHandlerDemo(String sign) {
        this.sign = sign;
    }

    @Override
    public void consume(OrderEventModel event, long sequence, boolean endOfBatch) {
        System.out.println("消费者" + sign + "消费事件" + event + " sequence=" + sequence + " endOfBatch=" + endOfBatch);
    }

}
