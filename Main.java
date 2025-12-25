import threads.Task;
import threads.Synchronized;
import threads.Generator;
import threads.Integrator;

public class Main {

    public static void complicatedThreads() {
        System.out.println("=== ВЕРСИЯ С СЕМАФОРАМИ ===");
        System.out.println("Потоки будут прерваны через 50 мс\n");

        // Создаем объекты
        Task task = new Task();
        Synchronized lock = new Synchronized();

        // Устанавливаем количество заданий
        int taskCount = 100;
        task.setTaskCount(taskCount);

        System.out.println("Количество заданий: " + taskCount);
        System.out.println("Запуск потоков...\n");

        // Создаем и запускаем потоки
        Generator generator = new Generator(task, lock);
        Integrator integrator = new Integrator(task, lock);

        generator.start();
        integrator.start();

        // Ждем 50 мс и прерываем потоки
        try {
            Thread.sleep(50);

            System.out.println("\nОсновной поток: Прерываю рабочие потоки через 50 мс...");
            generator.interrupt();
            integrator.interrupt();

        } catch (InterruptedException e) {
            System.out.println("Основной поток прерван");
        }

        // Ждем завершения потоков
        try {
            generator.join();
            integrator.join();
        } catch (InterruptedException e) {
            System.out.println("Ожидание потоков прервано");
        }

        System.out.println("\n=== РЕЗУЛЬТАТЫ ===");
        System.out.println("Сгенерировано: " + task.getGeneratedCount());
        System.out.println("Обработано: " + task.getProcessedCount());
    }

    public static void main(String[] args) {
        System.out.println("=== ПРОГРАММА С СЕМАФОРАМИ ===");
        System.out.println();

        complicatedThreads();

        System.out.println("\n=== ПРОГРАММА ЗАВЕРШЕНА ===");
    }
}