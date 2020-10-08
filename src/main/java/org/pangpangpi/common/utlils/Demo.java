package org.pangpangpi.common.utlils;

import org.pangpangpi.common.utlils.consumer.Consumer;
import org.pangpangpi.common.utlils.producer.Producer;
import org.pangpangpi.common.utlils.tasks.TaskInfo;
import org.pangpangpi.common.utlils.tasks.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hello world!
 *
 */
public class Demo {

    static Logger logger = LoggerFactory.getLogger(Demo.class);

    public static void main( String[] args ) {
        try {
            testProducerAndConsumer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            testProducerAndConsumerBackend();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testTaskRun();
    }

    private static Producer<Integer> natureNumberGenerator(int i) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return new Producer<Integer>() {
            @Override
            public Integer produce() {
                Integer product = atomicInteger.getAndAdd(1);
                logger.info("Produce number: {}", product);
                return product;
            }

            @Override
            public boolean hasNext() {
                return atomicInteger.get() < i;
            }
        };
    }

    private static Consumer<Integer> natureNumberConsumer() {
        return new Consumer<Integer>() {
            @Override
            public void accept(Integer data) {
                logger.info("Consume number: {}", data);
            }
        };
    }

    public static void testProducerAndConsumer() throws InterruptedException {
        logger.info("Running producer and consumer");
        ProduceAndConsumes.run(natureNumberGenerator(100), natureNumberConsumer(), 10, 2);
    }

    public static void testProducerAndConsumerBackend() throws InterruptedException {
        logger.info("Running producer and consumer in backend");
        ProduceAndConsume<Integer> produceAndConsume = ProduceAndConsumes
                .runBackend(natureNumberGenerator(100), natureNumberConsumer(), 10, 2);
        logger.info("Waiting for finish");
        produceAndConsume.waitForFinish();
    }

    public static void testTaskRun() {
        TaskInfo taskInfo = Tasks.startTask(natureNumberGenerator(1000), natureNumberConsumer(), 2, 1);
        Tasks.waitForFinish(taskInfo.getTaskId());
        logger.info("Task info:{}", taskInfo);
    }
}
