package threads;

import functions.Function;

public class Task {
    private Function function;
    private double leftBound;
    private double rightBound;
    private double step;
    private int taskCount;
    private int generatedCount;
    private int processedCount;

    public Task() {
        this.generatedCount = 0;
        this.processedCount = 0;
    }

    // Геттеры и сеттеры
    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public double getLeftBound() {
        return leftBound;
    }

    public void setLeftBound(double leftBound) {
        this.leftBound = leftBound;
    }

    public double getRightBound() {
        return rightBound;
    }

    public void setRightBound(double rightBound) {
        this.rightBound = rightBound;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getGeneratedCount() {
        return generatedCount;
    }

    public void incrementGeneratedCount() {
        this.generatedCount++;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void incrementProcessedCount() {
        this.processedCount++;
    }

    public String getCurrentState() {
        return String.format("Generated: %d/%d, Processed: %d/%d",
                generatedCount, taskCount, processedCount, taskCount);
    }
}