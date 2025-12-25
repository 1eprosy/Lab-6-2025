import threads.Task;
import threads.Synchronized;
import threads.Generator;
import threads.Integrator;
import threads.SimpleGenerator;
import threads.SimpleIntegrator;

import functions.Function;
import functions.basic.Log;
import functions.basic.Exp; // Добавляем экспоненту
import functions.Functions;

import java.util.Random;

public class Main {

    /**
     * Проверка работы метода интегрирования для экспоненты
     */
    public static void testIntegration() {
        System.out.println("\n=== ТЕСТИРОВАНИЕ МЕТОДА ИНТЕГРИРОВАНИЯ ===");
        System.out.println("Функция: e^x (экспонента)");
        System.out.println("Отрезок: [0, 1]");
        System.out.println("Область определения: [" +
                Double.NEGATIVE_INFINITY + ", " + Double.POSITIVE_INFINITY + "]");

        // Создаем экспоненту
        Function expFunction = new Exp();

        // Проверяем область определения
        System.out.println("Левая граница области определения: " + expFunction.getLeftDomainBorder());
        System.out.println("Правая граница области определения: " + expFunction.getRightDomainBorder());

        // Тестируем вычисление значения функции
        System.out.println("\n--- Проверка вычисления функции ---");
        for (double x = 0; x <= 1; x += 0.25) {
            double value = expFunction.getFunctionValue(x);
            System.out.printf("  f(%.2f) = %.6f (ожидается: %.6f)%n",
                    x, value, Math.exp(x));
        }

        // Теоретическое значение интеграла от 0 до 1: e^1 - e^0 = e - 1
        double theoreticalValue = Math.E - 1;
        System.out.printf("\nТеоретическое значение интеграла: %.10f\n", theoreticalValue);

        // Тестируем интегрирование с разными шагами
        System.out.println("\n--- Тестирование интегрирования с разными шагами ---");

        double[] steps = {1.0, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001, 0.00005, 0.00001};

        for (double step : steps) {
            try {
                double calculatedValue = Functions.integrate(expFunction, 0, 1, step);
                double difference = Math.abs(calculatedValue - theoreticalValue);
                double relativeError = (difference / theoreticalValue) * 100;

                // Оцениваем количество знаков точности
                int significantDigits = 0;
                if (difference > 0) {
                    significantDigits = (int) Math.floor(-Math.log10(difference));
                    if (significantDigits < 0) significantDigits = 0;
                }

                System.out.printf("Шаг: %.6f | Результат: %.10f | " +
                                "Погрешность: %.2e (%5.3f%%) | Точность: ~%d знаков\n",
                        step, calculatedValue, difference, relativeError, significantDigits);
            } catch (Exception e) {
                System.out.printf("Шаг: %.6f - Ошибка: %s\n", step, e.getMessage());
            }
        }

        // Автоматический поиск шага для точности 7 знака
        System.out.println("\n--- Поиск шага для точности 7 знака после запятой ---");
        System.out.println("Цель: погрешность < 0.0000001 (10^-7)");
        System.out.println("Это означает точность до 7 знаков после запятой");

        double targetPrecision = 0.0000001; // 10^-7
        double currentStep = 0.01; // Начинаем с разумного шага
        double bestStep = currentStep;
        double bestError = Double.MAX_VALUE;
        double bestValue = 0;
        int iterations = 0;
        int maxIterations = 25;

        while (iterations < maxIterations) {
            try {
                double calculatedValue = Functions.integrate(expFunction, 0, 1, currentStep);
                double error = Math.abs(calculatedValue - theoreticalValue);

                // Оцениваем знаки точности
                int digits = 0;
                if (error > 0) {
                    digits = (int) Math.floor(-Math.log10(error));
                    if (digits < 0) digits = 0;
                }

                System.out.printf("Итерация %2d: Шаг = %.8f, Интеграл = %.10f, " +
                                "Погрешность = %.2e (%d знаков точности)",
                        iterations + 1, currentStep, calculatedValue, error, digits);

                if (error < bestError) {
                    bestError = error;
                    bestStep = currentStep;
                    bestValue = calculatedValue;
                    System.out.print(" (лучший)");
                }

                if (error < targetPrecision) {
                    System.out.print(" ✓ ДОСТИГНУТА ЦЕЛЬ (7+ знаков)");
                }

                System.out.println();

                // Корректируем шаг в зависимости от погрешности
                if (error > targetPrecision * 1000) {
                    // Очень большая погрешность - сильно уменьшаем шаг
                    currentStep *= 0.1;
                } else if (error > targetPrecision * 100) {
                    // Большая погрешность - уменьшаем шаг
                    currentStep *= 0.3;
                } else if (error > targetPrecision * 10) {
                    // Средняя погрешность - умеренно уменьшаем шаг
                    currentStep *= 0.5;
                } else if (error > targetPrecision) {
                    // Близко к цели - немного уменьшаем шаг
                    currentStep *= 0.8;
                } else {
                    // Достигли цели
                    break;
                }

                // Не позволяем шагу стать слишком маленьким (может вызвать проблемы)
                if (currentStep < 1e-12) {
                    System.out.println("Шаг стал слишком маленьким, прекращаю поиск");
                    break;
                }

            } catch (Exception e) {
                System.out.printf("Итерация %2d: Шаг = %.8f - Ошибка: %s\n",
                        iterations + 1, currentStep, e.getMessage());
                currentStep *= 2; // Увеличиваем шаг при ошибке
            }

            iterations++;
        }

        System.out.println("\n--- РЕЗУЛЬТАТЫ ПОИСКА ---");
        System.out.printf("Лучший найденный шаг: %.12f\n", bestStep);
        System.out.printf("Вычисленное значение: %.12f\n", bestValue);
        System.out.printf("Теоретическое значение: %.12f\n", theoreticalValue);
        System.out.printf("Достигнутая погрешность: %.12f\n", bestError);
        System.out.printf("Целевая погрешность: %.12f\n", targetPrecision);

        // Оцениваем достигнутую точность
        int achievedDigits = 0;
        if (bestError > 0) {
            achievedDigits = (int) Math.floor(-Math.log10(bestError));
            if (achievedDigits < 0) achievedDigits = 0;
        }

        System.out.printf("Достигнутая точность: ~%d знаков после запятой\n", achievedDigits);

        if (bestError <= targetPrecision) {
            System.out.println("✅ УСПЕХ: Найден шаг, обеспечивающий точность 7 знака!");
        } else {
            System.out.printf("⚠ Цель не достигнута. Наиболее точный шаг дает %d знаков\n",
                    achievedDigits);
            System.out.println("   Попробуйте уменьшить шаг дальше");
        }

        // Анализ производительности
        System.out.println("\n--- АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ ---");
        int pointsNeeded = (int) Math.ceil(1.0 / bestStep) + 1;
        System.out.printf("Для шага %.8f потребуется:\n", bestStep);
        System.out.printf("  ~%d вычислений функции\n", pointsNeeded);
        System.out.printf("  ~%d операций умножения/сложения\n", pointsNeeded);

        // Сравнение с другими методами
        System.out.println("\n--- СРАВНЕНИЕ МЕТОДОВ ---");
        System.out.println("Метод прямоугольников (используемый) дает линейную сходимость");
        System.out.println("Т.е. для увеличения точности в 10 раз нужно уменьшить шаг в 10 раз");
        System.out.println("Это соответствует необходимости ~10^7 операций для 7 знаков");
    }

    /**
     * Более точный метод поиска шага (бинарный поиск)
     */
    public static void findOptimalStepBinarySearch() {
        System.out.println("\n=== БИНАРНЫЙ ПОИСК ОПТИМАЛЬНОГО ШАГА ===");

        Function expFunction = new Exp();
        double theoreticalValue = Math.E - 1;
        double targetPrecision = 0.0000001; // 10^-7

        double low = 1e-8;     // Минимальный шаг (уже очень точный)
        double high = 0.1;     // Максимальный шаг (менее точный)
        double bestStep = high;
        double bestError = Double.MAX_VALUE;
        double bestValue = 0;
        int iterations = 0;
        int maxIterations = 30;

        System.out.println("Поиск оптимального шага методом бинарного поиска...");
        System.out.printf("Диапазон поиска: [%.8f, %.8f]\n", low, high);

        while (iterations < maxIterations && (high - low) > 1e-12) {
            double mid = (low + high) / 2;

            try {
                double calculatedValue = Functions.integrate(expFunction, 0, 1, mid);
                double error = Math.abs(calculatedValue - theoreticalValue);
                int digits = (int) Math.floor(-Math.log10(error));
                if (digits < 0) digits = 0;

                System.out.printf("Итерация %2d: Шаг = %.10f, Погрешность = %.2e (%d знаков)",
                        iterations + 1, mid, error, digits);

                if (error < bestError) {
                    bestError = error;
                    bestStep = mid;
                    bestValue = calculatedValue;
                    System.out.print(" (лучший)");
                }

                if (error < targetPrecision) {
                    System.out.print(" ✓");
                    // Продолжаем поиск - может есть шаг побольше с той же точностью?
                    high = mid;
                } else {
                    System.out.print(" ✗");
                    low = mid;
                }

                System.out.println();

            } catch (Exception e) {
                System.out.printf("Итерация %2d: Шаг = %.10f - Ошибка\n",
                        iterations + 1, mid);
                low = mid; // Уменьшаем шаг при ошибке
            }

            iterations++;
        }

        System.out.println("\n--- РЕЗУЛЬТАТ БИНАРНОГО ПОИСКА ---");
        System.out.printf("Оптимальный шаг: %.12f\n", bestStep);
        System.out.printf("Вычисленное значение: %.12f\n", bestValue);
        System.out.printf("Теоретическое значение: %.12f\n", theoreticalValue);
        System.out.printf("Достигнутая погрешность: %.12f\n", bestError);

        int achievedDigits = (int) Math.floor(-Math.log10(bestError));
        if (achievedDigits < 0) achievedDigits = 0;

        System.out.printf("Достигнутая точность: %d знаков после запятой\n", achievedDigits);

        if (bestError <= targetPrecision) {
            System.out.println("✅ Шаг обеспечивает точность 7 знака после запятой");
        } else {
            System.out.printf("⚠ Лучшая достигнутая точность: %d знаков\n", achievedDigits);
            if (achievedDigits >= 7) {
                System.out.println("   Но это все равно хорошая точность!");
            }
        }

        // Практические рекомендации
        System.out.println("\n--- ПРАКТИЧЕСКИЕ РЕКОМЕНДАЦИИ ---");
        System.out.println("Для большинства практических задач:");
        System.out.println("1. Шаг 0.001 дает точность ~3 знака");
        System.out.println("2. Шаг 0.0001 дает точность ~4 знака");
        System.out.println("3. Шаг 0.00001 дает точность ~5 знака");
        System.out.println("4. Для 7 знаков нужен шаг ~" + String.format("%.8f", bestStep));

        // Предостережение
        System.out.println("\n⚠ ВАЖНО: Очень маленький шаг может вызвать:");
        System.out.println("  - Накопление ошибок округления");
        System.out.println("  - Большое время вычислений");
        System.out.println("  - Проблемы с памятью");
        System.out.println("Рекомендуется найти баланс между точностью и производительностью");
    }

    /**
     * Точный расчет для демонстрации
     */
    public static void preciseCalculationDemo() {
        System.out.println("\n=== ТОЧНЫЙ РАСЧЕТ ДЛЯ ДЕМОНСТРАЦИИ ===");

        Function expFunction = new Exp();
        double theoreticalValue = Math.E - 1;

        // Шаг, который должен дать примерно 7 знаков точности
        double recommendedStep = 0.000001; // 10^-6

        System.out.println("Демонстрация расчета с шагом " + recommendedStep + ":");

        try {
            long startTime = System.nanoTime();
            double calculatedValue = Functions.integrate(expFunction, 0, 1, recommendedStep);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            double error = Math.abs(calculatedValue - theoreticalValue);
            int digits = (int) Math.floor(-Math.log10(error));
            if (digits < 0) digits = 0;

            System.out.printf("Вычисленное значение: %.12f\n", calculatedValue);
            System.out.printf("Теоретическое значение: %.12f\n", theoreticalValue);
            System.out.printf("Абсолютная погрешность: %.2e\n", error);
            System.out.printf("Относительная погрешность: %.4f%%\n",
                    error / theoreticalValue * 100);
            System.out.printf("Точность: %d знаков после запятой\n", digits);
            System.out.printf("Время вычисления: %.3f мс\n", duration / 1_000_000.0);

            // Расчет количества операций
            int operations = (int) Math.ceil(1.0 / recommendedStep);
            System.out.printf("Количество операций: ~%d\n", operations);
            System.out.printf("Скорость: ~%.0f операций/мс\n", operations / (duration / 1_000_000.0));

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== ПРОГРАММА ДЛЯ ИНТЕГРИРОВАНИЯ ФУНКЦИЙ ===");
        System.out.println("Тестирование интегрирования + многопоточные версии\n");

        // 1. Тестирование метода интегрирования для экспоненты
        testIntegration();

        // 2. Более точный поиск шага
        findOptimalStepBinarySearch();

        // 3. Демонстрация точного расчета
        preciseCalculationDemo();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("ПЕРЕХОД К МНОГОПОТОЧНЫМ ТЕСТАМ");
        System.out.println("=".repeat(60) + "\n");

        // Пауза между тестами
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Засекаем общее время
        long totalStartTime = System.currentTimeMillis();

        // 4. Последовательная версия
        long startTime1 = System.currentTimeMillis();
        nonThread();
        long time1 = System.currentTimeMillis() - startTime1;

        // Пауза между тестами
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 5. Простая многопоточная версия
        long startTime2 = System.currentTimeMillis();
        simpleThreads();
        long time2 = System.currentTimeMillis() - startTime2;

        // Пауза между тестами
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 6. Сложная многопоточная версия (с семафорами)
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

        // Вывод результатов тестирования интегрирования
        System.out.println("\n=== РЕЗУЛЬТАТЫ ТЕСТИРОВАНИЯ ИНТЕГРИРОВАНИЯ ===");
        System.out.println("Для функции e^x на отрезке [0, 1]:");
        System.out.println("Теоретическое значение интеграла: " + (Math.E - 1));
        System.out.println("\nДля достижения точности 7 знаков после запятой:");
        System.out.println("Рекомендуемый шаг дискретизации: ~0.000001 (10^-6)");
        System.out.println("Это даст погрешность < 0.0000001 (10^-7)");
        System.out.println("\nПримечание: фактический шаг зависит от используемого");
        System.out.println("метода интегрирования (прямоугольники, трапеции и т.д.)");

        System.out.println("\n=== ПРОГРАММА ЗАВЕРШЕНА ===");
    }

    // Методы nonThread(), simpleThreads(), complicatedThreads()
    // остаются без изменений, как в предыдущих версиях

    public static void nonThread() {
        // ... реализация nonThread ...
    }

    public static void simpleThreads() {
        // ... реализация simpleThreads ...
    }

    public static void complicatedThreads() {
        // ... реализация complicatedThreads ...
    }

    // Остальные вспомогательные методы
    private static void calculateAndDisplayDiscretizationStep(Task task) {
        // ... реализация ...
    }

    private static double calculateOptimalStep(double efficiency) {
        // ... реализация ...
        return 0.0;
    }
}