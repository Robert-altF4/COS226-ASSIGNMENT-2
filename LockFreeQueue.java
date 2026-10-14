import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {

    private static class Node<T> {
        final T value;
        final AtomicReference<Node<T>> next;

        public Node(T value) {
            this.value = value;
            this.next = new AtomicReference<>(null);
        }
    }

    private final AtomicReference<Node<T>> head;
    private final AtomicReference<Node<T>> tail;

    public LockFreeQueue() {
        // Initialize with a sentinel node (value is null)
        Node<T> sentinel = new Node<>(null);
        this.head = new AtomicReference<>(sentinel);
        this.tail = new AtomicReference<>(sentinel);
    }

    public void enq(T value) {
        Node node = new Node(value);
        while (true) {
            Node<T> last = tail.get();
            Node<T> next = last.next.get();
            
            if (last == tail.get()) { // Consistency check
                if (next == null) {
                    // Try to link the new node at the end
                    if (last.next.compareAndSet(next, node)) {
                        tail.compareAndSet(last, node);
                        return;
                    }
                } else {
                    // Tail is lagging behind; help advance it
                    tail.compareAndSet(last, next);
                }
            }
        }
    }


    public T deq() {
        while (true) {
            Node<T> first = head.get();
            Node<T> last = tail.get();
            Node<T> next = first.next.get();
            
            if (first == head.get()) { // Consistency check
                if (first == last) {
                    if (next == null) {
                        // Queue is empty (returns null or throws custom exception)
                        return null; 
                    }
                    // Tail is lagging behind; help advance it
                    tail.compareAndSet(last, next);
                } else {
                    T value = next.value;
                    // Try to swing head pointer forward
                    if (head.compareAndSet(first, next)) {
                        return value;
                    }
                }
            }
        }
    }
}