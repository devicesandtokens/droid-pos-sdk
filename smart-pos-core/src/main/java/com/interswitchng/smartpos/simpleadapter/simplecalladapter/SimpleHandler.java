package com.interswitchng.smartpos.simpleadapter.simplecalladapter;

public interface SimpleHandler<T> {
    void accept(T response, Throwable throwable);
}