public class Consumer implements Runnable 
{
    private final JobQueue<Job> queue;
    private final int jobs_todo;

    public Consumer(JobQueue<Job> queue, int jobs_todo) {
        this.queue = queue;
        this.jobs_todo = jobs_todo;
    }

    
    public void run() {
        int count = 0;
        
        while (count < jobs_todo) {
            Job job = queue.deq();
            
            if (job != null) 
            {
                count++;
            } else 
            {
                Thread.yield();
            }
        }
    }
}
