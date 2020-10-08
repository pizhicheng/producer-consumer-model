package io.github.pizhicheng.common;

import io.github.pizhicheng.common.consumer.Consumer;
import io.github.pizhicheng.common.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ProduceAndConsume<T> {

    private static final Logger logger = LoggerFactory.getLogger(ProduceAndConsume.class);

    private static final int DEFAULT_QUEUE_SIZE = 100;

    private final BlockingQueue<T> queue;
    private final int produceThreadNumber;
    private final int consumeThreadNumber;
    private final AtomicInteger finishedProducerNumber;
    private final AtomicInteger finishedConsumerNumber;
    private final Semaphore semaphore;
    private final ProducerFactory<T> producerFactory;
    private final ConsumerFactory<T> consumerFactory;
    private final Runnable callBackMethod;
    private final Object finishNotifier;
    private boolean stop;
    private boolean finished;

    public ProduceAndConsume(Producer<T> producer, Consumer<T> consumer) {
        this(() -> producer, () -> consumer, 1, 1, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, ConsumerFactory<T> consumerFactory) {
        this(producerFactory, consumerFactory, 1, 1, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber) {
        this(() -> producer, () -> consumer, produceThreadNumber, consumeThreadNumber, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber) {
        this(producerFactory, () -> consumer, produceThreadNumber, consumeThreadNumber, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(Producer<T> producer, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber) {
        this(() -> producer, consumerFactory, produceThreadNumber, consumeThreadNumber, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber) {
        this(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber, null, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod) {
        this(producerFactory, () -> consumer, produceThreadNumber, consumeThreadNumber, callBackMethod, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(Producer<T> producer, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod) {
        this(() -> producer, consumerFactory, produceThreadNumber, consumeThreadNumber, callBackMethod, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod) {
        this(producerFactory, consumerFactory, produceThreadNumber, consumeThreadNumber, callBackMethod, DEFAULT_QUEUE_SIZE);
    }

    public ProduceAndConsume(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod, int queueSize) {
        this(() -> producer, () -> consumer, produceThreadNumber, consumeThreadNumber, callBackMethod, queueSize);
    }

    public ProduceAndConsume(Producer<T> producer, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod, int queueSize) {
        this(() -> producer, consumerFactory, produceThreadNumber, consumeThreadNumber, callBackMethod, queueSize);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, Consumer<T> consumer, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod, int queueSize) {
        this(producerFactory, () -> consumer, produceThreadNumber, consumeThreadNumber, callBackMethod, queueSize);
    }

    public ProduceAndConsume(ProducerFactory<T> producerFactory, ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumeThreadNumber, Runnable callBackMethod, int queueSize) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
        this.produceThreadNumber = produceThreadNumber;
        this.consumeThreadNumber = consumeThreadNumber;
        this.callBackMethod = callBackMethod;
        queue = new LinkedBlockingDeque<>(queueSize);
        semaphore = new Semaphore(0);
        finishNotifier = new Object();
        finishedProducerNumber = new AtomicInteger(0);
        finishedConsumerNumber = new AtomicInteger(0);
        stop = false;
        finished = false;
    }

    public void start() {
        startProduce();
        startConsume();
        startCallBackThread();
    }

    private void startProduce() {
        for (int i = 0; i < produceThreadNumber; i++) {
            Thread thread = new Thread(() -> {
                Producer<T> producer = producerFactory.createProducer();
                producer.doBefore();
                while (!Thread.interrupted() && producer.hasNext() && !stop) {
                    try {
                        T data = producer.produce();
                        if (data != null) {
                            queue.put(data);
                            semaphore.release(1);
                        } else {
                            logger.warn("Data Empty");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                producer.doAfter();
                synchronized (producerFactory) {
                    int finishNumber = finishedProducerNumber.incrementAndGet();
                    if (finishNumber >= produceThreadNumber) {
                        //Open the door to tell all the consumer that there is nothing left
                        semaphore.release(consumeThreadNumber);
                        producerFactory.notifyAll();
                        logger.info("Finish ALL Offer");
                    }
                }
            });
            String parentName = Thread.currentThread().getName();
            thread.setName(parentName + "-Producer-" + i);
            thread.start();
        }
    }

    private void startConsume() {
        for (int i = 0; i < consumeThreadNumber; i++) {
            Thread thread = new Thread(() -> {
                Consumer<T> consumer = consumerFactory.createConsumer();
                consumer.doBefore();
                //As long as there is a producer alive, we will not stop consumption
                while (!Thread.interrupted() && finishedProducerNumber.get() < produceThreadNumber || !queue.isEmpty()) {
                    try {
                        semaphore.acquire(1);
                        T data = queue.poll();
                        if (data != null && !stop) {
                            consumer.accept(data);
                        } else {
                            logger.warn("No data to consume");
                            if (finishedProducerNumber.get() < produceThreadNumber) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                consumer.doAfter();
                synchronized (consumerFactory) {
                    int finishNumber = finishedConsumerNumber.incrementAndGet();
                    if (finishNumber >= consumeThreadNumber) {
                        consumerFactory.notifyAll();
                        logger.info("Finish All Consume");
                    }
                }
            });
            String parentName = Thread.currentThread().getName();
            thread.setName(parentName + "-Consumer-" + i);
            thread.start();
        }
    }

    private void startCallBackThread() {
        new Thread(() -> {
            try {
                this.waitForFinishConsume();
                if (callBackMethod != null) {
                    callBackMethod.run();
                }
                synchronized (finishNotifier) {
                    finished = true;
                    finishNotifier.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        stop = true;
        queue.clear();
    }

    public boolean isFinished() {
        return finished;
    }

    public void waitForFinish() throws InterruptedException {
        if (!finished) {
            synchronized (finishNotifier) {
                if (!finished) {
                    finishNotifier.wait();
                }
            }
        }
    }

    private void waitForFinishProduce() throws InterruptedException {
        synchronized (producerFactory) {
            if (finishedProducerNumber.get() < produceThreadNumber) {
                producerFactory.wait();
            }
        }
    }

    private void waitForFinishConsume() throws InterruptedException {
        synchronized (consumerFactory) {
            if (finishedConsumerNumber.get() < consumeThreadNumber) {
                consumerFactory.wait();
            }
        }
    }

    public interface ConsumerFactory<T> {
        Consumer<T> createConsumer();
    }

    public interface ProducerFactory<T> {
        Producer<T> createProducer();
    }

}
