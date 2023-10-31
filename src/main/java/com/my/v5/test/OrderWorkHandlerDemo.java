package com.my.v5.test;

import com.my.v5.component.MyWorkHandler;

public class OrderWorkHandlerDemo implements MyWorkHandler {

    private String sign;

    public OrderWorkHandlerDemo(String sign) {
        this.sign = sign;
    }

    @Override
    public void consume(Object event) {
        System.out.println("消费者" + sign + "消费事件" + event);
    }

}
