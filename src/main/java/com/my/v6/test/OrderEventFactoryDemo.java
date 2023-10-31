package com.my.v6.test;

import com.my.v6.component.MyEventFactory;

/**
 * 订单事件工厂
 */
public class OrderEventFactoryDemo implements MyEventFactory<OrderEventModel> {

    @Override
    public OrderEventModel newInstance() {
        return new OrderEventModel();
    }

}
