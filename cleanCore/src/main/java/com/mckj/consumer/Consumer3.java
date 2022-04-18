package com.mckj.consumer;


@FunctionalInterface
public interface Consumer3<T, O, P> {
    void accept(T t, O o, P p);

}