package org.pangpangpi.common.utlils.producer;

import javax.annotation.Nullable;

public interface Producer<T> {

    @Nullable
    T produce();

    boolean hasNext();

    default void doBefore(){}

    default void doAfter(){}

}
