package io.github.pizhicheng.common.tasks;

import io.github.pizhicheng.common.consumer.Consumer;
import io.github.pizhicheng.common.ProduceAndConsume;
import io.github.pizhicheng.common.ProduceAndConsumes;
import io.github.pizhicheng.common.producer.Producer;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
public class Tasks {

    private static final AtomicLong taskId = new AtomicLong(1);
    private static final Map<Long, ProduceAndConsume<?>> taskMap = new ConcurrentHashMap<>();
    private static final Map<Long, TaskInfo> taskInfoMap = new ConcurrentHashMap<>();

    public static <T> TaskInfo startTask(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumerThreadNumber) {
        return startTask(() -> producer, () -> consumer, produceThreadNumber, consumerThreadNumber, null);
    }

    public static <T> TaskInfo startTask(Producer<T> producer, Consumer<T> consumer, int produceThreadNumber, int consumerThreadNumber, Runnable callBackMethod) {
        return startTask(() -> producer, () -> consumer, produceThreadNumber, consumerThreadNumber, callBackMethod);
    }

    public static <T> TaskInfo startTask(ProduceAndConsume.ProducerFactory<T> producerFactory, ProduceAndConsume.ConsumerFactory<T> consumerFactory, int produceThreadNumber, int consumerThreadNumber, Runnable callBackMethod) {
        Long id = taskId.getAndIncrement();
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(id);
        taskInfo.setTaskMessage("Create time:" + Instant.now());
        taskInfo.setFinished(false);
        taskInfoMap.put(id, taskInfo);
        List<Long> produceTimeList = Collections.synchronizedList(new ArrayList<>());
        ProduceAndConsume.ProducerFactory<T> timeCounterProduceFactory = getTimeCountProducerFactory(producerFactory, produceTimeList);
        List<Long> consumeTimeList = Collections.synchronizedList(new ArrayList<>());
        ProduceAndConsume.ConsumerFactory<T> timeCounterConsumerFactory = getTimeCountConsumerFactory(consumerFactory, consumeTimeList);
        Runnable run;
        if (callBackMethod == null) {
            run = () -> {
                taskMap.remove(id);
                taskInfo.setFinished(true);
                taskInfo.setProduceSpeed(getSpeedResult(produceTimeList, produceThreadNumber));
                taskInfo.setConsumeSpeed(getSpeedResult(consumeTimeList, consumerThreadNumber));
            };
        } else {
            run = () -> {
                callBackMethod.run();
                taskMap.remove(id);
                taskInfo.setFinished(true);
                taskInfo.setProduceSpeed(getSpeedResult(produceTimeList, produceThreadNumber));
                taskInfo.setConsumeSpeed(getSpeedResult(consumeTimeList, consumerThreadNumber));
            };
        }
        ProduceAndConsume<T> produceAndConsume = ProduceAndConsumes.runBackend(timeCounterProduceFactory, timeCounterConsumerFactory, produceThreadNumber, consumerThreadNumber, run);
        taskMap.put(id, produceAndConsume);
        return taskInfo;
    }

    public static TaskInfo getTaskInfo(Long id) {
        return taskInfoMap.get(id);
    }

    public static Collection<TaskInfo> getTaskList() {
        return taskInfoMap.values();
    }

    public static void waitForFinish(long taskId) {
        try {
            ProduceAndConsume<?> produceAndConsume = taskMap.get(taskId);
            if (produceAndConsume != null) {
                produceAndConsume.waitForFinish();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        taskMap.remove(taskId);
    }

    public static void removeTask(long taskId) {
        try {
            ProduceAndConsume<?> produceAndConsume = taskMap.get(taskId);
            if (produceAndConsume != null) {
                produceAndConsume.stop();
                produceAndConsume.waitForFinish();
            }
            taskMap.remove(taskId);
            taskInfoMap.remove(taskId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void removeFinished() {
        try {
            for (Long taskId : taskInfoMap.keySet()) {
                ProduceAndConsume<?> produceAndConsume = taskMap.get(taskId);
                if (produceAndConsume != null) {
                    if (produceAndConsume.isFinished()) {
                        // Make sure that this task is really finished
                        produceAndConsume.waitForFinish();
                        taskMap.remove(taskId);
                    }
                } else {
                    taskInfoMap.remove(taskId);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static <T> ProduceAndConsume.ConsumerFactory<T> getTimeCountConsumerFactory(ProduceAndConsume.ConsumerFactory<T> consumerFactory, List<Long> consumeTimeList) {
        return () -> {
            Consumer<T> coreConsumer = consumerFactory.createConsumer();
            return new Consumer<T>() {

                @Override
                public void doBefore() {
                    coreConsumer.doBefore();
                }

                @Override
                public void doAfter() {
                    coreConsumer.doAfter();
                }

                @Override
                public void accept(T data) {
                    long t0, t1;
                    t0 = System.nanoTime();
                    coreConsumer.accept(data);
                    t1 = System.nanoTime();
                    consumeTimeList.add(t1 - t0);
                }
            };
        };
    }

    private static <T> ProduceAndConsume.ProducerFactory<T> getTimeCountProducerFactory(ProduceAndConsume.ProducerFactory<T> producerFactory, List<Long> produceTimeList) {
        return () -> {
            Producer<T> coreProducer = producerFactory.createProducer();
            return new Producer<T>() {

                @Override
                public void doBefore() {
                    coreProducer.doBefore();
                }

                @Override
                public void doAfter() {
                    coreProducer.doAfter();
                }

                @Nullable
                @Override
                public T produce() {
                    long t0, t1;
                    t0 = System.nanoTime();
                    T data = coreProducer.produce();
                    t1 = System.nanoTime();
                    produceTimeList.add(t1 - t0);
                    return data;
                }

                @Override
                public boolean hasNext() {
                    return coreProducer.hasNext();
                }
            };
        };
    }

    public static SpeedReport getSpeedResult(List<Long> result, int threadNumber) {
        result.sort(Long::compareTo);
        List<Long> midResult;
        if(result.size() == 0) {
            return new SpeedReport();
        }
        midResult = result.subList(result.size() / 10, result.size() - result.size() / 10);
        SpeedReport speedReport = new SpeedReport();
        speedReport.setMidValue(result.get(result.size() / 2) / 1000_000d);
        speedReport.setMidMaxValue(midResult.get(midResult.size() - 1) / 1000_000d);
        speedReport.setMidMinValue(midResult.get(0) / 1000_000d);
        speedReport.setMaxValue(result.get(result.size() - 1) / 1000_000d);
        speedReport.setMinValue(result.get(0) / 1000_1000d);
        long avg = 0;
        for (Long time : result) {
            avg += time / result.size();
        }
        speedReport.setAverage(avg / 1000_000d);
        speedReport.setTotalCostPerThread(avg * result.size() / 1000_000_000d / threadNumber);
        speedReport.setQps(1000_000_000d / avg * threadNumber);
        long midAvg = 0;
        for (Long time : midResult) {
            midAvg += time / midResult.size();
        }
        speedReport.setMidAverage(midAvg / 1000_000d);
        speedReport.setMidCostPerThread(midAvg * midResult.size() / 1000_000_000d / threadNumber);
        speedReport.setMidQps(1000_000_000d / midAvg * threadNumber);
        return speedReport;
    }

}
