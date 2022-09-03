package com.example.tomcatmemshelldemo.listener;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

//@WebListener
public class TestListener implements ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("初始化");
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("销毁了");
    }
}
