package threads;

import functions.Functions;

public class SimpleIntegrator implements Runnable {
    private final Task task;
    private volatile boolean generatorFinished = false;

    public SimpleIntegrator(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            int taskCount = task.getTaskCount();

            while (task.getProcessedCount() < taskCount) {
                double leftBound, rightBound, step;

                synchronized (task) {
                    // Ждем новое задание или завершение генератора
                    while (task.getGeneratedCount() <= task.getProcessedCount()) {
                        // Проверяем, не завершил ли генератор работу
                        if (task.getGeneratedCount() >= taskCount &&
                                task.getProcessedCount() >= task.getGeneratedCount()) {
                            System.out.println("Интегратор: Все задания обработаны");
                            return;
                        }

                        try {
                            task.wait(50); // Уменьшаем время ожидания
                        } catch (InterruptedException e) {
                            System.out.println("Интегратор прерван");
                            return;
                        }
                    }

                    // Чтение параметров
                    leftBound = task.getLeftBound();
                    rightBound = task.getRightBound();
                    step = task.getStep();
                    task.incrementProcessedCount();

                    // Выводим сразу после чтения
                    System.out.println("Интегратор: Получено задание " + task.getProcessedCount() +
                            "/" + taskCount + " - " + String.format("%.4f %.4f %.6f", leftBound, rightBound, step));

                    task.notifyAll();
                }

                try {
                    // Вычисление вне synchronized блока
                    double integral = Functions.integrate(task.getFunction(), leftBound, rightBound, step);

                    System.out.println("Интегратор: Result " +
                            String.format("%.4f %.4f %.6f %.10f", leftBound, rightBound, step, integral));

                } catch (Exception e) {
                    System.out.println("Интегратор: Ошибка вычисления - " + e.getMessage());
                }

                Thread.sleep(50); // Уменьшаем задержку
            }

            System.out.println("Интегратор: Все " + taskCount + " заданий обработаны");

        } catch (InterruptedException e) {
            System.out.println("Интегратор прерван");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Интегратор: Неожиданная ошибка - " + e.getMessage());
        }
    }
}