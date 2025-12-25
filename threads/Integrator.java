package threads;

import functions.Functions;

public class Integrator extends Thread {
    private final Task task;
    private final Synchronized lock;

    public Integrator(Task task, Synchronized lock) {
        this.task = task;
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            int taskCount = task.getTaskCount();
            int processed = 0;

            while (processed < taskCount) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Поток прерван во время интегрирования");
                }

                double leftBound = 0, rightBound = 0, step = 0;
                boolean hasNewTask = false;

                // Захватываем семафор для чтения
                lock.beginRead();
                try {
                    // Проверяем, есть ли новые задания для обработки
                    if (task.getGeneratedCount() > processed) {
                        hasNewTask = true;
                        // Читаем данные из задания
                        leftBound = task.getLeftBound();
                        rightBound = task.getRightBound();
                        step = task.getStep();
                    }
                } finally {
                    lock.endRead();
                }

                if (!hasNewTask) {
                    // Нет новых заданий - короткая пауза
                    Thread.sleep(10);
                    continue;
                }

                // Вычисляем интеграл вне блокировки
                try {
                    double integral = Functions.integrate(task.getFunction(), leftBound, rightBound, step);
                    processed++;

                    // Увеличиваем счетчик обработанных задач
                    lock.beginWrite();
                    try {
                        task.incrementProcessedCount();
                    } finally {
                        lock.endWrite();
                    }

                    // Выводим результат
                    System.out.println("Интегратор [" + Thread.currentThread().getId() +
                            "]: Результат " + processed + "/" + taskCount +
                            " - Result " + String.format("%.4f %.4f %.6f %.10f",
                            leftBound, rightBound, step, integral));

                } catch (Exception e) {
                    processed++;
                    lock.beginWrite();
                    try {
                        task.incrementProcessedCount();
                    } finally {
                        lock.endWrite();
                    }
                    System.out.println("Интегратор [" + Thread.currentThread().getId() +
                            "]: Ошибка в задании " + processed + " - " + e.getMessage());
                }

                // Короткая пауза между вычислениями
                Thread.sleep(30);
            }

            System.out.println("Интегратор [" + Thread.currentThread().getId() +
                    "]: Все " + taskCount + " заданий обработаны");

        } catch (InterruptedException e) {
            System.out.println("Интегратор [" + Thread.currentThread().getId() +
                    "]: Прерван - " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Интегратор [" + Thread.currentThread().getId() +
                    "]: Неожиданная ошибка - " + e.getMessage());
        }
    }
}