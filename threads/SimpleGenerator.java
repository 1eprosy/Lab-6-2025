package threads;

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
        try {
            int taskCount = task.getTaskCount();

            for (int i = 0; i < taskCount; i++) {
                synchronized (task) {
                    // Генерация параметров
                    double base = 1 + random.nextDouble() * 9;
                    if (Math.abs(base - 1.0) < 1e-10) base = 1.1;

                    double leftBound = random.nextDouble() * 99.9 + 0.1;
                    double rightBound = 100 + random.nextDouble() * 100;

                    double step = random.nextDouble();
                    if (step < 1e-10) step = 0.01;

                    // Установка параметров
                    task.setFunction(new Log(base));
                    task.setLeftBound(leftBound);
                    task.setRightBound(rightBound);
                    task.setStep(step);
                    task.incrementGeneratedCount();

                    // Вывод
                    System.out.println("Генератор: Задание " + task.getGeneratedCount() +
                            "/" + taskCount + " - Source " +
                            String.format("%.4f %.4f %.6f", leftBound, rightBound, step));

                    task.notifyAll(); // Уведомляем интегратор

                    // Небольшая пауза, чтобы интегратор успел обработать
                    if (task.getGeneratedCount() > task.getProcessedCount() + 3) {
                        try {
                            task.wait(10);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                Thread.sleep(20); // Уменьшаем задержку между заданиями
            }

            System.out.println("Генератор: Все " + taskCount + " заданий сгенерированы");

            // Последнее уведомление для интегратора
            synchronized (task) {
                task.notifyAll();
            }

        } catch (InterruptedException e) {
            System.out.println("Генератор прерван");
        } catch (Exception e) {
            System.out.println("Генератор: Ошибка - " + e.getMessage());
        }
    }
}