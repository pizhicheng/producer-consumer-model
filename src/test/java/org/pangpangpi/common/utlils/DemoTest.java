package org.pangpangpi.common.utlils;

import org.junit.Test;
import org.pangpangpi.common.utlils.consumer.Consumer;
import org.pangpangpi.common.utlils.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test for simple App.
 */
public class DemoTest
{

    Logger logger = LoggerFactory.getLogger(DemoTest.class);
    private Producer<Integer> natureNumberGenerator(int i) {
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

    private Consumer<Integer> natureNumberConsumer() {
        return new Consumer<Integer>() {
            @Override
            public void accept(Integer data) {
                logger.info("Consume number: {}", data);
            }
        };
    }

    @Test
    public void testProducerAndConsumer() throws InterruptedException {
        logger.info("Running producer and consumer in backend");
        ProduceAndConsumes.run(natureNumberGenerator(100), natureNumberConsumer(), 10, 2);
    }

    @Test
    public void testProducerAndConsumerBackend() throws InterruptedException {
        logger.info("Running producer and consumer in backend");
        ProduceAndConsume<Integer> produceAndConsume = ProduceAndConsumes
                .runBackend(natureNumberGenerator(100), natureNumberConsumer(), 10, 2);
        logger.info("Waiting for finish");
        produceAndConsume.waitForFinish();
    }
}
