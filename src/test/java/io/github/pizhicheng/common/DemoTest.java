package io.github.pizhicheng.common;

import io.github.pizhicheng.common.tasks.TaskInfo;
import io.github.pizhicheng.common.tasks.Tasks;
import org.junit.Assert;
import org.junit.Test;
import io.github.pizhicheng.common.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test for simple App.
 */
public class DemoTest {

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
                return atomicInteger.get() <= i;
            }
        };
    }

    private List<Integer> getSortedLists(int max) {
        List<Integer> integers = new ArrayList<>(max);
        for (int i = 1; i <= max; i++) {
            integers.add(i);
        }
        return integers;
    }

    @Test
    public void testProducerAndConsumer() throws InterruptedException {
        logger.info("Running producer and consumer in backend");
        List<Integer> list = new CopyOnWriteArrayList<>();
        ProduceAndConsumes.run(natureNumberGenerator(100),
                list::add, 2, 5);
        Assert.assertEquals(100, list.size());
        Assert.assertTrue(list.containsAll(getSortedLists(100)));
    }

    @Test
    public void testProducerAndConsumerBackend() throws InterruptedException {
        logger.info("Running producer and consumer in backend");
        List<Integer> list = new CopyOnWriteArrayList<>();
        ProduceAndConsume<Integer> produceAndConsume = ProduceAndConsumes
                .runBackend(natureNumberGenerator(100), list::add, 5, 2);
        logger.info("Waiting for finish");
        produceAndConsume.waitForFinish();
        Assert.assertEquals(100, list.size());
        Assert.assertTrue(list.containsAll(getSortedLists(100)));
    }

    @Test
    public void testTaskRun() {
        List<Integer> list = new CopyOnWriteArrayList<>();
        TaskInfo taskInfo = Tasks.startTask(natureNumberGenerator(1000), list::add, 2, 2);
        Tasks.waitForFinish(taskInfo.getTaskId());
        Assert.assertEquals(1000, list.size());
        Assert.assertTrue(list.containsAll(getSortedLists(1000)));
        logger.info("Task info:{}", taskInfo);
    }
}
