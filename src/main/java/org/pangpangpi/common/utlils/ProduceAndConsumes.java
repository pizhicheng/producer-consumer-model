package org.pangpangpi.common.utlils;

import org.pangpangpi.common.utlils.consumer.Consumer;
import org.pangpangpi.common.utlils.producer.Producer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ProduceAndConsumes {

    public  static <T> Consumer<T> toConsumer(Consumer<List<T>> listConsumer, int batchSize) {
        return new Consumer<T>() {
            final List<T> dataList = new ArrayList<>();

            @Override
            public void doBefore() {
                listConsumer.doBefore();
            }

            @Override
            public void doAfter() {
                listConsumer.doAfter();
            }

            @Override
            public void accept(T data) {
                if (data == null || dataList.size() >= batchSize) {
                    listConsumer.accept(dataList);
                    dataList.clear();
                } else {
                    dataList.add(data);
                }
            }
        };
    }

    public  static <T> Producer<List<T>> toListProducer(Producer<T> producer, int batchSize) {
        return new Producer<List<T>>() {

            @Override
            public void doBefore() {
                producer.doBefore();
            }

            @Override
            public void doAfter() {
                producer.doAfter();
            }

            @Nullable
            @Override
            public List<T> produce() {
                List<T> dataList = new ArrayList<>(batchSize);
                for (int i = 0; i < batchSize; i++) {
                    if(producer.hasNext()) {
                        T data = producer.produce();
                        if (data != null) {
                            dataList.add(data);
                        }
                    }
                }
                if(dataList.isEmpty()) {
                    return null;
                }
                return dataList;
            }

            @Override
            public boolean hasNext() {
                return producer.hasNext();
            }
        };
    }

    public static <T,R> Producer<R> getProducer(Producer<T> producer, Function<T, R> transfer) {
        return new Producer<R>() {

            @Override
            public void doBefore() {
                producer.doBefore();
            }

            @Override
            public void doAfter() {
                producer.doAfter();
            }

            @Nullable
            @Override
            public R produce() {
                T data = producer.produce();
                if (data != null) {
                    return transfer.apply(data);
                } else {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {
                return producer.hasNext();
            }
        };
    }

    public static <T> Producer<T> getProducer(Iterator<T> iterator) {
        return new Producer<T>() {
            @Nullable
            @Override
            public T produce() {
                return iterator.next();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
        };
    }

    public static <T> void run(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber) throws InterruptedException {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producer, consumer, produceThreadNumber, consumeThreadNumber);
        produceAndConsume.start();
        produceAndConsume.waitForFinish();
    }

    public static <T> void run(ProduceAndConsume.ProducerFactory<T> producerFactory, ProduceAndConsume.ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber) throws InterruptedException {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber);
        produceAndConsume.start();
        produceAndConsume.waitForFinish();
    }

    public static <T> void run(ProduceAndConsume.ProducerFactory<T> producerFactory, ProduceAndConsume.ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod) throws InterruptedException {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber, callBackMethod);
        produceAndConsume.start();
        produceAndConsume.waitForFinish();
    }

    public static <T> ProduceAndConsume<T> runBackend(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber) {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producer, consumer, produceThreadNumber, consumeThreadNumber);
        produceAndConsume.start();
        return produceAndConsume;
    }

    public static <T> ProduceAndConsume<T> runBackend(ProduceAndConsume.ProducerFactory<T> producerFactory, ProduceAndConsume.ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber) {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber);
        produceAndConsume.start();
        return produceAndConsume;
    }

    public static <T> ProduceAndConsume<T> runBackend(ProduceAndConsume.ProducerFactory<T> producerFactory, ProduceAndConsume.ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod) {
        ProduceAndConsume<T> produceAndConsume = new ProduceAndConsume<>(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber, callBackMethod);
        produceAndConsume.start();
        return produceAndConsume;
    }

}
