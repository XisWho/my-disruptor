package com.my.v6.falseSharing;

public class Point {

    public volatile int x;
    public volatile int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
