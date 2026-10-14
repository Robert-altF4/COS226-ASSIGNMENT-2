public class Main {
    public static void main(String[] args) throws InterruptedException {

        final int Workload = 12000; 

        int no_producers = 4;
        int no_consumers = 4;
        
        int jobs_per_prod = Workload / no_producers;
        int jobs_per_cons = Workload / no_consumers;
    

        Thread[] producers = new Thread[no_producers];
        Thread[] consumers = new Thread[no_consumers];

        JobQueue<Job> queue = (JobQueue<Job>) new LockFreeQueue<Job>();
        System.out.println("Queue: Lock-Free Queue");

        // JobQueue<Job> queue = (JobQueue<Job>) new BoundedQueue<Job>(100);
        // System.out.println("Queue: Bounded Queue");

        
        System.out.println("Producers: " + no_producers);
        System.out.println("Consumers: " + no_consumers);

        long startTime = System.nanoTime();

        for (int i = 0; i < no_producers; i++) {
            producers[i] = new Thread(new Producer(queue, jobs_per_prod, i + 1));
            producers[i].start();
        }

        for (int i = 0; i < no_consumers; i++) {
            consumers[i] = new Thread(new Consumer(queue, jobs_per_cons));
            consumers[i].start();
        }

        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        long endTime = System.nanoTime();
        
        double duration = (endTime - startTime) / 1_000_000_000.0; //convert nanosecnods to seconds
        double throughput = Workload / duration;

        System.out.println("Jobs Processed: " + Workload);
        System.out.printf("Total Execution Time: %.4f seconds\n", duration);
        System.out.printf("Throughput: %.2f jobs/second\n", throughput);
    }
}