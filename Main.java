import threads.Task;
import threads.Synchronized;
import threads.Generator;
import threads.Integrator;
import threads.SimpleGenerator;
import threads.SimpleIntegrator;

import functions.Function;
import functions.basic.Log;
import functions.Functions;

import java.util.Random;

public class Main {

    /**
     * NON-THREADED VERSION (ПОСЛЕДОВАТЕЛЬНАЯ ВЕРСИЯ)
     * Реализация без потоков
     */
    public static void nonThread() {
        System.out.println("\n=== NON-THREADED VERSION (ПОСЛЕДОВАТЕЛЬНАЯ) ===");
        System.out.println("Количество заданий: 100");
        System.out.println("Все операции выполняются последовательно\n");

        // Создаем объект задания
        Task task = new Task();

        // Устанавливаем количество выполняемых заданий (минимум 100)
        int taskCount = 100;
        task.setTaskCount(taskCount);

        Random random = new Random();

        // Последовательное выполнение
        for (int i = 0; i < taskCount; i++) {
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

            // Выводим сообщение о генерации
            System.out.println("[" + (i+1) + "/" + taskCount + "] Source " +
                    String.format("%.4f %.4f %.6f", leftBound, rightBound, step));

            // Вычисляем интеграл
            try {
                double integral = Functions.integrate(task.getFunction(), leftBound, rightBound, step);

                // Выводим результат
                System.out.println("[" + (i+1) + "/" + taskCount + "] Result " +
                        String.format("%.4f %.4f %.6f %.10f",
                                leftBound, rightBound, step, integral));
            } catch (Exception e) {
                System.out.println("[" + (i+1) + "/" + taskCount + "] Ошибка: " + e.getMessage());
            }

            // Короткая пауза для имитации работы
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n✓ Последовательная версия завершена");
        System.out.println("Обработано заданий: " + taskCount);
    }

    /**
     * SIMPLE THREADED VERSION (ПРОСТАЯ МНОГОПОТОЧНАЯ ВЕРСИЯ)
     * С использованием простой синхронизации через synchronized
     */
    public static void simpleThreads() {
        System.out.println("\n=== SIMPLE THREADED VERSION (ПРОСТАЯ МНОГОПОТОЧНАЯ) ===");
        System.out.println("Количество заданий: 100");
        System.out.println("Используется простая синхронизация через synchronized\n");

        // Создаем объект задания
        Task task = new Task();

        // Устанавливаем количество выполняемых заданий
        int taskCount = 100;
        task.setTaskCount(taskCount);

        // Создаем потоки
        Thread generatorThread = new Thread(new SimpleGenerator(task));
        Thread integratorThread = new Thread(new SimpleIntegrator(task));

        // Устанавливаем приоритеты (можно экспериментировать)
        generatorThread.setPriority(Thread.NORM_PRIORITY);
        integratorThread.setPriority(Thread.NORM_PRIORITY);

        // Запускаем потоки
        generatorThread.start();
        integratorThread.start();

        // Ждем завершения потоков
        try {
            long startTime = System.currentTimeMillis();
            long timeout = 30000; // 30 секунд

            System.out.println("Запущены потоки:");
            System.out.println("1. SimpleGenerator (приоритет: " + generatorThread.getPriority() + ")");
            System.out.println("2. SimpleIntegrator (приоритет: " + integratorThread.getPriority() + ")");
            System.out.println("\nОжидание завершения...");

            // Мониторим прогресс
            while ((generatorThread.isAlive() || integratorThread.isAlive()) &&
                    (System.currentTimeMillis() - startTime) < timeout) {

                synchronized (task) {
                    int generated = task.getGeneratedCount();
                    int processed = task.getProcessedCount();

                    System.out.printf("\rПрогресс: Сгенерировано %d/%d, Обработано %d/%d",
                            generated, taskCount, processed, taskCount);

                    if (generated >= taskCount && processed >= taskCount) {
                        System.out.println("\n✓ Все задачи выполнены!");
                        break;
                    }
                }

                Thread.sleep(500);
            }

            // Проверяем таймаут
            if (System.currentTimeMillis() - startTime >= timeout) {
                System.out.println("\n⚠ Достигнут таймаут 30 секунд!");
            }

            // Прерываем потоки, если они еще работают
            if (generatorThread.isAlive()) {
                generatorThread.interrupt();
            }
            if (integratorThread.isAlive()) {
                integratorThread.interrupt();
            }

            // Ждем завершения
            generatorThread.join(2000);
            integratorThread.join(2000);

        } catch (InterruptedException e) {
            System.out.println("Основной поток прерван");
            Thread.currentThread().interrupt();
        }

        // Вывод результатов
        System.out.println("\n=== РЕЗУЛЬТАТЫ SIMPLE THREADED ===");
        System.out.println("Запланировано: " + taskCount);
        System.out.println("Сгенерировано: " + task.getGeneratedCount());
        System.out.println("Обработано: " + task.getProcessedCount());

        if (task.getGeneratedCount() == taskCount && task.getProcessedCount() == taskCount) {
            System.out.println("✓ Успех: все задания выполнены");
        } else {
            System.out.println("⚠ Внимание: не все задания выполнены");
        }
    }

    /**
     * COMPLICATED THREADED VERSION (СЛОЖНАЯ МНОГОПОТОЧНАЯ ВЕРСИЯ)
     * С использованием семафоров (reader-writer lock)
     */
    public static void complicatedThreads() {
        System.out.println("\n=== COMPLICATED THREADED VERSION (СЛОЖНАЯ МНОГОПОТОЧНАЯ) ===");
        System.out.println("Количество заданий: 100");
        System.out.println("Используется синхронизация через reader-writer lock\n");

        // Создаем объекты
        Task task = new Task();
        Synchronized lock = new Synchronized();

        // Устанавливаем количество заданий
        int taskCount = 100;
        task.setTaskCount(taskCount);

        // Создаем и запускаем потоки
        Generator generator = new Generator(task, lock);
        Integrator integrator = new Integrator(task, lock);

        generator.start();
        integrator.start();

        // Мониторим выполнение
        try {
            long startTime = System.currentTimeMillis();
            long timeout = 60000; // 60 секунд

            System.out.println("Запущены потоки:");
            System.out.println("1. Generator (с семафорами)");
            System.out.println("2. Integrator (с семафорами)");
            System.out.println("\nОжидание завершения...");

            int lastGenerated = -1;
            int lastProcessed = -1;
            int checkCounter = 0;

            // Мониторим прогресс
            while ((generator.isAlive() || integrator.isAlive()) &&
                    (System.currentTimeMillis() - startTime) < timeout) {

                // Используем семафор для чтения
                lock.beginRead();
                int generated = task.getGeneratedCount();
                int processed = task.getProcessedCount();
                lock.endRead();

                // Проверяем согласованность каждые 5 обновлений
                if (checkCounter % 5 == 0) {
                    System.out.printf("\nПроверка %d: Сгенерировано %d, Обработано %d",
                            checkCounter/5 + 1, generated, processed);

                    // Проверяем, что processed не превышает generated
                    if (processed > generated) {
                        System.out.println("\n⚠ ВНИМАНИЕ: Обработано больше, чем сгенерировано!");
                        System.out.println("   Это указывает на ошибку в логике счетчиков");
                    }

                    // Проверяем задержку между генерацией и обработкой
                    if (generated > processed) {
                        int pending = generated - processed;
                        System.out.printf("   Ожидают обработки: %d задач\n", pending);
                    }
                }

                // Выводим общий прогресс
                System.out.printf("\rПрогресс: Сгенерировано %d/%d, Обработано %d/%d",
                        generated, taskCount, processed, taskCount);

                lastGenerated = generated;
                lastProcessed = processed;
                checkCounter++;

                if (generated >= taskCount && processed >= taskCount) {
                    System.out.println("\n\n✓ Все задачи выполнены!");
                    break;
                }

                Thread.sleep(300); // Проверяем чаще
            }

            // Проверяем таймаут
            if (System.currentTimeMillis() - startTime >= timeout) {
                System.out.println("\n\n⚠ Достигнут таймаут 60 секунд!");
            }

            // Даем потокам 2 секунды на завершение
            if (generator.isAlive() || integrator.isAlive()) {
                System.out.println("\nДаю потокам 2 секунды на завершение...");
                Thread.sleep(2000);
            }

            // Прерываем потоки, если они еще работают
            if (generator.isAlive()) {
                System.out.println("Прерываю Generator...");
                generator.interrupt();
            }
            if (integrator.isAlive()) {
                System.out.println("Прерываю Integrator...");
                integrator.interrupt();
            }

            // Ждем завершения
            generator.join(2000);
            integrator.join(2000);

        } catch (InterruptedException e) {
            System.out.println("Основной поток прерван");
            Thread.currentThread().interrupt();
        }

        // ФИНАЛЬНАЯ ПРОВЕРКА
        System.out.println("\n=== ФИНАЛЬНАЯ ПРОВЕРКА СОГЛАСОВАННОСТИ ===");

        int finalGenerated = task.getGeneratedCount();
        int finalProcessed = task.getProcessedCount();

        System.out.println("Всего должно быть заданий: " + taskCount);
        System.out.println("Фактически сгенерировано: " + finalGenerated);
        System.out.println("Фактически обработано: " + finalProcessed);

        // ПОДРОБНЫЙ АНАЛИЗ
        System.out.println("\n--- АНАЛИЗ РЕЗУЛЬТАТОВ ---");

        // Проверка 1: Все ли задачи сгенерированы?
        if (finalGenerated == taskCount) {
            System.out.println("✓ 1. Все 100 задач успешно сгенерированы");
        } else {
            System.out.println("✗ 1. Не все задачи сгенерированы:");
            System.out.println("   Ожидалось: " + taskCount);
            System.out.println("   Получено: " + finalGenerated);
            System.out.println("   Не хватает: " + (taskCount - finalGenerated));
        }

        // Проверка 2: Все ли задачи обработаны?
        if (finalProcessed == taskCount) {
            System.out.println("✓ 2. Все 100 задач успешно обработаны");
        } else {
            System.out.println("✗ 2. Не все задачи обработаны:");
            System.out.println("   Ожидалось: " + taskCount);
            System.out.println("   Получено: " + finalProcessed);
            System.out.println("   Не хватает: " + (taskCount - finalProcessed));
        }

        // Проверка 3: Совпадают ли счетчики?
        if (finalGenerated == finalProcessed) {
            System.out.println("✓ 3. Счетчики сгенерированных и обработанных задач совпадают");
        } else {
            System.out.println("✗ 3. Счетчики не совпадают!");
            if (finalGenerated > finalProcessed) {
                System.out.println("   Сгенерировано больше, чем обработано");
                System.out.println("   Разница: " + (finalGenerated - finalProcessed) + " задач");
                System.out.println("   Вероятная причина: Integrator не успел обработать все задачи");
            } else {
                System.out.println("   Обработано больше, чем сгенерировано - ЭТО ОШИБКА!");
                System.out.println("   Разница: " + (finalProcessed - finalGenerated) + " задач");
                System.out.println("   Вероятная причина: ошибка в логике счетчиков");
            }
        }

        // Проверка 4: Достигнуты ли цели?
        if (finalGenerated == taskCount && finalProcessed == taskCount) {
            System.out.println("\n✅ УСПЕХ: Все 100 задач успешно сгенерированы и проинтегрированы!");
        } else {
            System.out.println("\n⚠ ПРЕДУПРЕЖДЕНИЕ: Не все задачи были корректно обработаны");

            if (finalGenerated < taskCount) {
                System.out.println("\nВозможные причины проблем с генерацией:");
                System.out.println("1. Generator был прерван до завершения");
                System.out.println("2. Ошибка в генерации функций");
                System.out.println("3. Недостаточно времени (таймаут)");
            }

            if (finalProcessed < finalGenerated) {
                System.out.println("\nВозможные причины проблем с интеграцией:");
                System.out.println("1. Integrator был прерван до завершения");
                System.out.println("2. Ошибки вычисления интегралов");
                System.out.println("3. Проблемы с синхронизацией");
                System.out.println("4. Integrator работает медленнее, чем Generator");
            }
        }

        // Расчет шага дискретизации
        if (finalGenerated > 0 && finalProcessed > 0) {
            calculateAndDisplayDiscretizationStep(task);

            // Дополнительная информация для анализа
            System.out.println("\n--- СТАТИСТИКА ДЛЯ ОПТИМИЗАЦИИ ---");
            double efficiency = (double) finalProcessed / finalGenerated;

            if (efficiency < 0.9) {
                System.out.println("Рекомендации по улучшению:");
                System.out.println("1. Увеличьте время работы Integrator (уменьшите sleep)");
                System.out.println("2. Проверьте, нет ли блокировок в Synchronized");
                System.out.println("3. Рассмотрите увеличение приоритета Integrator");
            }
        }

        // Проверка потоков
        System.out.println("\n--- СОСТОЯНИЕ ПОТОКОВ ---");
        System.out.println("Generator alive: " + generator.isAlive());
        System.out.println("Integrator alive: " + integrator.isAlive());
        System.out.println("Generator interrupted: " + generator.isInterrupted());
        System.out.println("Integrator interrupted: " + integrator.isInterrupted());
    }

    /**
     * Метод для расчета и отображения шага дискретизации
     */
    private static void calculateAndDisplayDiscretizationStep(Task task) {
        System.out.println("\n=== РАСЧЕТ ОПТИМАЛЬНОГО ШАГА ДИСКРЕТИЗАЦИИ ===");

        int generated = task.getGeneratedCount();
        int processed = task.getProcessedCount();

        // Эффективность обработки
        double efficiency = (double) processed / generated;
        System.out.printf("Эффективность системы: %.2f%%\n", efficiency * 100);

        // Рассчитываем рекомендуемый шаг
        double recommendedStep = calculateOptimalStep(efficiency);
        System.out.printf("Рекомендуемый шаг дискретизации: %.6f\n", recommendedStep);

        // Объяснение
        System.out.println("\nОбоснование:");
        if (recommendedStep < 0.001) {
            System.out.println("Высокая точность - подходит для научных расчетов");
        } else if (recommendedStep < 0.01) {
            System.out.println("Хорошая точность - оптимальный баланс");
        } else if (recommendedStep < 0.05) {
            System.out.println("Средняя точность - повышенная производительность");
        } else {
            System.out.println("Низкая точность - максимальная производительность");
        }
    }

    /**
     * Вычисление оптимального шага
     */
    private static double calculateOptimalStep(double efficiency) {
        double baseStep = 0.01;

        if (efficiency >= 0.95) {
            return baseStep * 0.01;
        } else if (efficiency >= 0.85) {
            return baseStep * 0.05;
        } else if (efficiency >= 0.75) {
            return baseStep * 0.1;
        } else if (efficiency >= 0.65) {
            return baseStep * 0.5;
        } else if (efficiency >= 0.50) {
            return baseStep;
        } else if (efficiency >= 0.35) {
            return baseStep * 2.0;
        } else {
            return baseStep * 5.0;
        }
    }

    /**
     * Демонстрация проблемы без синхронизации
     */
    public static void demonstrateProblem() {
        System.out.println("\n=== ДЕМОНСТРАЦИЯ ПРОБЛЕМ БЕЗ СИНХРОНИЗАЦИИ ===");
        System.out.println("Показывает проблемы, возникающие при отсутствии синхронизации");

        // Создаем задачу
        Task task = new Task();
        task.setTaskCount(10);

        // Создаем проблемный генератор (без синхронизации)
        Thread badGenerator = new Thread(() -> {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                // НЕТ СИНХРОНИЗАЦИИ - проблема!
                task.setLeftBound(random.nextDouble() * 100);
                // Между этими установками может вклиниться интегратор!
                Thread.yield(); // Даем возможность другому потоку выполниться
                task.setRightBound(100 + random.nextDouble() * 100);
                Thread.yield();
                task.setStep(random.nextDouble());
                Thread.yield();

                task.incrementGeneratedCount();
                System.out.println("BadGenerator: Установил задание " + task.getGeneratedCount());

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        // Создаем проблемный интегратор (без синхронизации)
        Thread badIntegrator = new Thread(() -> {
            int processed = 0;
            while (processed < 10) {
                // НЕТ СИНХРОНИЗАЦИИ - проблема!
                double left = task.getLeftBound();
                double right = task.getRightBound();
                double step = task.getStep();

                // Может получить несогласованные данные!
                System.out.printf("BadIntegrator: Получил [%.2f, %.2f, %.2f]%n", left, right, step);

                processed++;
                task.incrementProcessedCount();

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        // Запускаем
        badGenerator.start();
        badIntegrator.start();

        try {
            badGenerator.join(3000);
            badIntegrator.join(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n✓ Демонстрация завершена");
        System.out.println("Видно, как без синхронизации получаются несогласованные данные");
    }

    public static void main(String[] args) {
        System.out.println("=== ПРОГРАММА ДЛЯ ИНТЕГРИРОВАНИЯ ФУНКЦИЙ ===");
        System.out.println("Три версии реализации: nonThread, simpleThreads, complicatedThreads\n");

        // Засекаем общее время
        long totalStartTime = System.currentTimeMillis();

        // 1. Демонстрация проблемы (опционально)
        // demonstrateProblem();

        // 2. Последовательная версия
        long startTime1 = System.currentTimeMillis();
        nonThread();
        long time1 = System.currentTimeMillis() - startTime1;

        // Пауза между тестами
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 3. Простая многопоточная версия
        long startTime2 = System.currentTimeMillis();
        simpleThreads();
        long time2 = System.currentTimeMillis() - startTime2;

        // Пауза между тестами
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 4. Сложная многопоточная версия (с семафорами)
        long startTime3 = System.currentTimeMillis();
        complicatedThreads();
        long time3 = System.currentTimeMillis() - startTime3;

        // Итоговая статистика
        long totalTime = System.currentTimeMillis() - totalStartTime;

        System.out.println("\n=== ИТОГОВАЯ СТАТИСТИКА ===");
        System.out.println("1. nonThread (последовательная):");
        System.out.printf("   Время: %.2f сек\n", time1 / 1000.0);
        System.out.printf("   Скорость: %.1f задач/сек\n", 100.0 / (time1 / 1000.0));

        System.out.println("\n2. simpleThreads (простая многопоточная):");
        System.out.printf("   Время: %.2f сек\n", time2 / 1000.0);
        System.out.printf("   Скорость: %.1f задач/сек\n", 100.0 / (time2 / 1000.0));
        System.out.printf("   Ускорение: %.1f%%\n", (time1 - time2) * 100.0 / time1);

        System.out.println("\n3. complicatedThreads (сложная многопоточная):");
        System.out.printf("   Время: %.2f сек\n", time3 / 1000.0);
        System.out.printf("   Скорость: %.1f задач/сек\n", 100.0 / (time3 / 1000.0));
        System.out.printf("   Ускорение: %.1f%%\n", (time1 - time3) * 100.0 / time1);

        System.out.println("\nОбщее время выполнения всех тестов: " +
                (totalTime / 1000.0) + " сек");

        System.out.println("\n=== АНАЛИЗ ===");
        System.out.println("nonThread: Полная последовательность, нет проблем синхронизации");
        System.out.println("simpleThreads: Базовые блоки synchronized, защита от гонок данных");
        System.out.println("complicatedThreads: Reader-writer lock, оптимально для чтения/записи");

        System.out.println("\n=== ПРОГРАММА ЗАВЕРШЕНА ===");
    }
}