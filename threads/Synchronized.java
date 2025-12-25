package threads;

public class Synchronized {
    private int readers = 0;        // Количество читающих потоков
    private int writers = 0;        // Количество пишущих потоков
    private int writeRequests = 0;  // Количество запросов на запись

    public synchronized void beginRead() throws InterruptedException {
        // Ждем, пока нет пишущих потоков и запросов на запись
        while (writers > 0 || writeRequests > 0) {
            wait();
        }
        readers++;
    }

    public synchronized void endRead() {
        readers--;
        notifyAll(); // Уведомляем ожидающие потоки
    }

    public synchronized void beginWrite() throws InterruptedException {
        writeRequests++;

        // Ждем, пока нет читающих и пишущих потоков
        while (readers > 0 || writers > 0) {
            wait();
        }

        writeRequests--;
        writers++;
    }

    public synchronized void endWrite() {
        writers--;
        notifyAll(); // Уведомляем ожидающие потоки
    }
}
