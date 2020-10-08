package org.pangpangpi.common.utlils.consumer;

public interface Consumer<T> {

    void accept(T data);

    default void doBefore(){}

    default void doAfter(){}

}
