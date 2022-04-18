package com.mckj.consumer;


@FunctionalInterface
public interface Consumer2<T, O> {

    void accept(T t, O o);

}