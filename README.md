## What is this?

This is template for creating a producer and consumer model task.

## Feature

- Support multi-thread producer and consumer
- Support produce and consume speed calculate

## Getting started

- Create a produce and comsumer task

  1. Create a producer:

     ```java
         private static Producer<Integer> natureNumberGenerator(int i) {
             AtomicInteger atomicInteger = new AtomicInteger(1);
             return new Producer<Integer>() {
                 @Override
                 public Integer produce() {
                     Integer product = atomicInteger.getAndAdd(1);
                     System.out.println("Produce number: " + product);
                     return product;
                 }
     
                 @Override
                 public boolean hasNext() {
                     return atomicInteger.get() < i;
                 }
             };
         }
     ```

     We create a producer produce integer from 1 to the param value.

  2. Create a consumer:
  
     ```java
         private static Consumer<Integer> natureNumberConsumer() {
             return new Consumer<Integer>() {
                 @Override
                 public void accept(Integer data) {
                     System.out.println("Consume number: " + data);
                 }
             };
       }
     ```
  
     We create a consumer consume the product.
  
  3. Create a task:
  
     ```java
         public static void testProducerAndConsumer() throws InterruptedException {
             System.out.println("Running producer and consumer");
             ProduceAndConsumes.run(natureNumberGenerator(100), natureNumberConsumer(), 3, 5);
         }
     ```
  
     We create a produce and consume task with 3 producer thread and 5 consumer thread.
  
- You can also run this task in backend

  ```java
      public static void testProducerAndConsumerBackend() throws InterruptedException {
          System.out.println("Running producer and consumer in backend");
          ProduceAndConsume<Integer> produceAndConsume = ProduceAndConsumes
                  .runBackend(natureNumberGenerator(100), natureNumberConsumer(), 3, 5);
          System.out.println("Waiting for finish");
          produceAndConsume.waitForFinish();
      }
  ```

  ProduceAndConsumes#runBackend can create a ProduceAndConsume instance. Which you can manage this task.

- If you want to know the time cost for producer and consumer

  ```java
      public static void testTaskRun() {
          TaskInfo taskInfo = Tasks.startTask(natureNumberGenerator(1000), natureNumberConsumer(), 3, 5);
          taskInfo.waitForFinish(taskInfo.getTaskId());
          System.out.println("Produce speed:" + taskInfo.getProduceSpeed());
          System.out.println("Consume speed:" + taskInfo.getConsumeSpeed());
      }
  ```

## License

This project is under the Apache 2.0 license. See the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) file for more details.

