package threads;

import functions.basic.Log;
import java.util.Random;

public class Generator extends Thread {
    private final Task task;
    private final Synchronized lock;
    private final Random random;

    public Generator(Task task, Synchronized lock) {
        this.task = task;
        this.lock = lock;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            int taskCount = task.getTaskCount();

            for (int i = 0; i < taskCount; i++) {
                // Проверяем, не прерван ли поток
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException("Поток прерван во время генерации");
                }

                // Генерация параметров вне блокировки
                double base = 1 + random.nextDouble() * 9;
                if (Math.abs(base - 1.0) < 1e-10) base = 1.1;

                double leftBound = random.nextDouble() * 99.9 + 0.1;
                double rightBound = 100 + random.nextDouble() * 100;

                double step = random.nextDouble();
                if (step < 1e-10) step = 0.01;

                // Захватываем семафор для записи
                lock.beginWrite();
                try {
                    // Записываем данные в задание
                    task.setFunction(new Log(base));
                    task.setLeftBound(leftBound);
                    task.setRightBound(rightBound);
                    task.setStep(step);
                    task.incrementGeneratedCount();

                    // Выводим информацию
                    System.out.println("Генератор [" + Thread.currentThread().getId() +
                            "]: Задание " + task.getGeneratedCount() + "/" + taskCount +
                            " - Source " + String.format("%.4f %.4f %.6f", leftBound, rightBound, step));

                } finally {
                    lock.endWrite(); // Всегда освобождаем семафор
                }

                // Короткая пауза между заданиями
                Thread.sleep(30);
            }

            System.out.println("Генератор [" + Thread.currentThread().getId() +
                    "]: Все " + taskCount + " заданий сгенерированы");

        } catch (InterruptedException e) {
            System.out.println("Генератор [" + Thread.currentThread().getId() +
                    "]: Прерван - " + e.getMessage());
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        } catch (Exception e) {
            System.out.println("Генератор [" + Thread.currentThread().getId() +
                    "]: Ошибка - " + e.getMessage());
        }
    }
}