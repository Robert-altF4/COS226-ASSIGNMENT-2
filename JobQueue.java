public interface JobQueue<T> {
    void enq(T item);
    T deq();
}
