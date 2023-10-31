package com.my.v4.test;

/**
 * 订单事件对象
 */
public class OrderEventModel {

    private String message;

    private int price;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderEventModel{" +
                "message='" + message + '\'' +
                ", price=" + price +
                '}';
    }
}
