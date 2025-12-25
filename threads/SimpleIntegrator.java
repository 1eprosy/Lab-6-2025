package threads;

import functions.Function;
import functions.Functions;

public class SimpleIntegrator implements Runnable {
    private final Task task;

    public SimpleIntegrator(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        int taskCount = task.getTaskCount();
        int processed = 0;

        while (processed < taskCount) {
            // Проверяем, не прерван ли поток
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            double leftBound = 0, rightBound = 0, step = 0;
            Function function = null;

            // Чтение параметров с блокировкой
            synchronized (task) {
                // Проверяем, есть ли новые задания для обработки
                if (task.getGeneratedCount() <= processed) {
                    // Нет новых заданий - короткая пауза
                    try {
                        task.wait(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    continue;
                }

                // Читаем параметры задачи
                leftBound = task.getLeftBound();
                rightBound = task.getRightBound();
                step = task.getStep();
                function = task.getFunction();

                // Увеличиваем счетчик обработанных задач
                task.incrementProcessedCount();
            }

            // Вычисляем интеграл вне блокировки
            try {
                if (function != null) {
                    double integral = Functions.integrate(function, leftBound, rightBound, step);
                    processed++;

                    // Вывод результата
                    System.out.println("SimpleIntegrator: Result " +
                            String.format("%.4f %.4f %.6f %.10f",
                                    leftBound, rightBound, step, integral));
                }
            } catch (Exception e) {
                processed++;
                System.out.println("SimpleIntegrator: Ошибка в задании " + processed +
                        " - " + e.getMessage());
            }

            // Короткая пауза между вычислениями
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("SimpleIntegrator: Завершил обработку " + taskCount + " заданий");
    }
}