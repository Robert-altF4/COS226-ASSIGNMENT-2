import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> implements JobQueue<T> {

    private class Node {
        T item;
        Node next;
        
        Node(T item) {
            this.item = item;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private final int capacity;
    private final AtomicInteger size; 

    private final ReentrantLock enqLock = new ReentrantLock();
    private final Condition notFullCondition = enqLock.newCondition();

    private final ReentrantLock deqLock = new ReentrantLock();
    private final Condition notEmptyCondition = deqLock.newCondition();

    public BoundedQueue(int capacity) {
        this.capacity = capacity;
        this.size = new AtomicInteger(0);
        
        Node sentinel = new Node(null);
        this.head = sentinel;
        this.tail = sentinel;
    }

    public void enq(T x) {
        boolean mustWakeDequeuers = false; 
        enqLock.lock(); 
        try {
            while (size.get() == capacity) { 
                notFullCondition.await(); 
            }
            
            Node e = new Node(x); 
            tail.next = e; 
            tail = tail.next; 
            
            int c = size.getAndIncrement();
            if (c == 0) { 
                mustWakeDequeuers = true; 
            }

            if (c + 1 < capacity) {
                notFullCondition.signal();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            enqLock.unlock(); 
        }
        
        if (mustWakeDequeuers) {
            deqLock.lock();
            try {
                notEmptyCondition.signal();
            } finally {
                deqLock.unlock();
            }
        }
    }

    public T deq() {
        T result = null;
        boolean mustWakeEnqueuers = false;
        deqLock.lock();
        try {
            while (size.get() == 0) {
                notEmptyCondition.await();
            }
            
            Node first = head.next;
            result = first.item;
            head = first;
            head.item = null;
            
            int c = size.getAndDecrement();
            if (c == capacity) {
                mustWakeEnqueuers = true;
            }
            

            if (c > 1) {
                notEmptyCondition.signal();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            deqLock.unlock();
        }
        
        if (mustWakeEnqueuers) {
            enqLock.lock();
            try {
                notFullCondition.signal();
            } finally {
                enqLock.unlock();
            }
        }
        return result;
    }
}