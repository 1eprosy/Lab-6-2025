package threads;

import functions.Function;
import functions.basic.Log;
import java.util.Random;

public class SimpleGenerator implements Runnable {
    private final Task task;
    private final Random random;

    public SimpleGenerator(Task task) {
        this.task = task;
        this.random = new Random();
    }

    @Override
    public void run() {
        int taskCount = task.getTaskCount();

        for (int i = 0; i < taskCount; i++) {
            // Проверяем, не прерван ли поток
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            // Генерация параметров с блокировкой
            synchronized (task) {
                // Создаем логарифмическую функцию
                double base = 1 + random.nextDouble() * 9;
                if (Math.abs(base - 1.0) < 1e-10) base = 1.1;

                double leftBound = random.nextDouble() * 100;
                double rightBound = 100 + random.nextDouble() * 100;
                double step = random.nextDouble();
                if (step < 1e-10) step = 0.01;

                // Устанавливаем параметры в задачу
                task.setFunction(new Log(base));
                task.setLeftBound(leftBound);
                task.setRightBound(rightBound);
                task.setStep(step);
                task.incrementGeneratedCount();

                // Вывод информации
                System.out.println("SimpleGenerator: Source " +
                        String.format("%.4f %.4f %.6f", leftBound, rightBound, step));
            }

            // Короткая пауза между заданиями
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("SimpleGenerator: Завершил генерацию " + taskCount + " заданий");
    }
}