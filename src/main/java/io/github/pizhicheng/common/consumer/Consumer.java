package io.github.pizhicheng.common.consumer;

public interface Consumer<T> {

    void accept(T data);

    default void doBefore(){}

    default void doAfter(){}

}
