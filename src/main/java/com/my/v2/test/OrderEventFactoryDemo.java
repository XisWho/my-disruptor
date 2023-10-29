package com.my.v2.test;

import com.my.v2.component.MyEventFactory;

/**
 * 订单事件工厂
 */
public class OrderEventFactoryDemo implements MyEventFactory<OrderEventModel> {

    @Override
    public OrderEventModel newInstance() {
        return new OrderEventModel();
    }

}
