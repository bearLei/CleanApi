package com.mckj.consumer;


@FunctionalInterface
public interface Consumer<T> {

    void accept(T t);

}
