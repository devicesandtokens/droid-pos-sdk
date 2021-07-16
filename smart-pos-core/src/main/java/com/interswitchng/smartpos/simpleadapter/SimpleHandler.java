package com.interswitchng.smartpos.simpleadapter;

public interface SimpleHandler<T> {
    void accept(T response, Throwable throwable);
}