public class Producer implements Runnable {
    private final JobQueue<Job> queue;
    private final int jobs_tomake;
    private final int producerId;

    public Producer(JobQueue<Job> queue, int jobs_tomake, int producerId) {
        this.queue = queue;
        this.jobs_tomake = jobs_tomake;
        this.producerId = producerId;
    }

    public void run() {
        for (int i = 0; i < jobs_tomake; i++) {
            int uniqueId = (producerId * 1000) + i;
            queue.enq(new Job(uniqueId));
        }
    }
}