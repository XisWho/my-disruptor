package com.my.v4.test;

import com.my.v4.component.MyEventFactory;

/**
 * 订单事件工厂
 */
public class OrderEventFactoryDemo implements MyEventFactory<OrderEventModel> {

    @Override
    public OrderEventModel newInstance() {
        return new OrderEventModel();
    }

}
