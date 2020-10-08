package io.github.pizhicheng.common.producer;

import javax.annotation.Nullable;

public interface Producer<T> {

    @Nullable
    T produce();

    boolean hasNext();

    default void doBefore(){}

    default void doAfter(){}

}
