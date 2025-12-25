package functions;

import java.io.*;

public class ArrayTabulatedFunctionExternalizable implements TabulatedFunction, Externalizable {
    private static final long serialVersionUID = 5L;
    private static final double EPSILON = 1e-10; // Точность для сравнения double

    private FunctionPoint[] points;
    private int size;

    // Конструктор по умолчанию, необходимый для Externalizable
    public ArrayTabulatedFunctionExternalizable() {
        this.points = new FunctionPoint[2];
        this.size = 0;
    }

    // Конструкторы из ArrayTabulatedFunction
    public ArrayTabulatedFunctionExternalizable(double leftX, double rightX, int pointsCount) {
        if (pointsCount < 2) {
            throw new IllegalArgumentException("pointsCount must be at least 2");
        }
        if (leftX >= rightX) {
            throw new IllegalArgumentException("leftX must be less than rightX");
        }

        this.points = new FunctionPoint[pointsCount + 2];
        this.size = pointsCount;
        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            this.points[i] = new FunctionPoint(x, 0);
        }
    }

    public ArrayTabulatedFunctionExternalizable(double leftX, double rightX, double[] values) {
        if (values.length < 2) {
            throw new IllegalArgumentException("values array must have at least 2 elements");
        }
        if (leftX >= rightX) {
            throw new IllegalArgumentException("leftX must be less than rightX");
        }

        int pointsCount = values.length;
        this.points = new FunctionPoint[pointsCount + 2];
        this.size = pointsCount;
        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            this.points[i] = new FunctionPoint(x, values[i]);
        }
    }

    public ArrayTabulatedFunctionExternalizable(FunctionPoint[] pointsArray) {
        if (pointsArray == null) {
            throw new IllegalArgumentException("Points array cannot be null");
        }
        if (pointsArray.length < 2) {
            throw new IllegalArgumentException("Points array must contain at least 2 points");
        }

        for (int i = 1; i < pointsArray.length; i++) {
            if (pointsArray[i] == null || pointsArray[i-1] == null) {
                throw new IllegalArgumentException("Points array cannot contain null elements");
            }
            if (pointsArray[i].getX() <= pointsArray[i-1].getX()) {
                throw new IllegalArgumentException(
                        "Points must be strictly increasing by x. " +
                                "Point " + i + " has x=" + pointsArray[i].getX() +
                                " which is not greater than point " + (i-1) +
                                " with x=" + pointsArray[i-1].getX()
                );
            }
        }

        this.points = new FunctionPoint[pointsArray.length + 2];
        this.size = pointsArray.length;

        for (int i = 0; i < pointsArray.length; i++) {
            this.points[i] = new FunctionPoint(pointsArray[i]);
        }
    }

    // Реализация методов Externalizable
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            out.writeDouble(points[i].getX());
            out.writeDouble(points[i].getY());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        size = in.readInt();

        if (size < 2) {
            throw new IOException("Invalid data: points count must be at least 2, got " + size);
        }

        points = new FunctionPoint[size + 2];
        double prevX = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            double x = in.readDouble();
            double y = in.readDouble();

            if (x <= prevX) {
                throw new IOException("Invalid data: points are not strictly increasing by x");
            }
            prevX = x;

            points[i] = new FunctionPoint(x, y);
        }
    }

    // Реализация методов TabulatedFunction
    @Override
    public int getPointsCount() {
        return size;
    }

    @Override
    public FunctionPoint getPoint(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return new FunctionPoint(points[index]);
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }

        if (!isValidXPosition(index, point.getX())) {
            throw new InappropriateFunctionPointException(
                    "New x-coordinate would violate the ordering of points"
            );
        }

        points[index] = new FunctionPoint(point);
    }

    @Override
    public double getPointX(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return points[index].getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }

        double currentX = points[index].getX();
        double y = points[index].getY();

        if (Math.abs(currentX - x) < EPSILON) {
            return;
        }

        if (!isValidXPosition(index, x)) {
            throw new InappropriateFunctionPointException(
                    "New x-coordinate would violate the ordering of points"
            );
        }

        for (int i = 0; i < size; i++) {
            if (i != index && Math.abs(points[i].getX() - x) < EPSILON) {
                throw new InappropriateFunctionPointException("Point with this x already exists");
            }
        }

        points[index] = new FunctionPoint(x, y);
    }

    @Override
    public double getPointY(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return points[index].getY();
    }

    @Override
    public void setPointY(int index, double y) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }

        double x = points[index].getX();
        points[index] = new FunctionPoint(x, y);
    }

    @Override
    public void deletePoint(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Index out of bounds: " + index);
        }
        if (size <= 2) {
            throw new IllegalStateException("Cannot delete point - function must have at least 2 points");
        }

        System.arraycopy(points, index + 1, points, index, size - index - 1);
        points[size - 1] = null;
        size--;
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }

        int insertIndex = 0;
        while (insertIndex < size && points[insertIndex].getX() < point.getX()) {
            insertIndex++;
        }

        if (insertIndex < size && Math.abs(points[insertIndex].getX() - point.getX()) < EPSILON) {
            throw new InappropriateFunctionPointException("Point with this x already exists");
        }

        if (size >= points.length) {
            expandArray();
        }

        if (insertIndex < size) {
            System.arraycopy(points, insertIndex, points, insertIndex + 1, size - insertIndex);
        }

        points[insertIndex] = new FunctionPoint(point);
        size++;
    }

    @Override
    public void printFunction() {
        System.out.println("Табулированная функция (Externalizable):");
        System.out.println("-----------------------");

        for (int i = 0; i < getPointsCount(); i++) {
            try {
                FunctionPoint point = getPoint(i);
                System.out.printf("Точка %d: (%.4f, %.4f)%n",
                        i, point.getX(), point.getY());
            } catch (FunctionPointIndexOutOfBoundsException e) {
                System.out.println("Ошибка при получении точки " + i);
            }
        }

        System.out.println("-----------------------");
        System.out.printf("Область определения: [%.4f, %.4f]%n",
                getLeftDomainBorder(), getRightDomainBorder());
        System.out.printf("Количество точек: %d%n", getPointsCount());
    }

    @Override
    public double getLeftDomainBorder() {
        if (size == 0) {
            return Double.NaN;
        }
        return points[0].getX();
    }

    @Override
    public double getRightDomainBorder() {
        if (size == 0) {
            return Double.NaN;
        }
        return points[size - 1].getX();
    }

    @Override
    public double getFunctionValue(double x) {
        if (size == 0) {
            return Double.NaN;
        }

        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        for (int i = 0; i < size - 1; i++) {
            double x1 = points[i].getX();
            double x2 = points[i + 1].getX();

            if (Math.abs(x - x1) < EPSILON) return points[i].getY();
            if (Math.abs(x - x2) < EPSILON) return points[i + 1].getY();

            if (x > x1 && x < x2) {
                return linearInterpolation(points[i], points[i + 1], x);
            }
        }

        return Double.NaN;
    }

    // === ПЕРЕОПРЕДЕЛЕННЫЕ МЕТОДЫ Object ===

    /**
     * Возвращает текстовое описание табулированной функции
     * @return строковое представление функции в формате {(x1; y1), (x2; y2), ...}
     */
    @Override
    public String toString() {
        if (size == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (int i = 0; i < size; i++) {
            sb.append("(");
            sb.append(points[i].getX());
            sb.append("; ");
            sb.append(points[i].getY());
            sb.append(")");

            if (i < size - 1) {
                sb.append(", ");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Сравнивает текущую табулированную функцию с другим объектом
     * @param obj объект для сравнения
     * @return true, если объекты равны (оба являются TabulatedFunction с одинаковыми точками)
     */
    @Override
    public boolean equals(Object obj) {
        // Проверяем, является ли объект тем же самым
        if (this == obj) {
            return true;
        }

        // Проверяем, является ли объект табулированной функцией
        if (!(obj instanceof TabulatedFunction)) {
            return false;
        }

        TabulatedFunction otherFunc = (TabulatedFunction) obj;

        // Проверяем количество точек
        if (this.size != otherFunc.getPointsCount()) {
            return false;
        }

        // Если другой объект тоже ArrayTabulatedFunctionExternalizable, используем оптимизированное сравнение
        if (obj instanceof ArrayTabulatedFunctionExternalizable) {
            ArrayTabulatedFunctionExternalizable other = (ArrayTabulatedFunctionExternalizable) obj;

            // Сравниваем точки напрямую
            for (int i = 0; i < size; i++) {
                if (!pointsAreEqual(this.points[i], other.points[i])) {
                    return false;
                }
            }
        } else {
            // Общий случай для любой TabulatedFunction
            try {
                for (int i = 0; i < size; i++) {
                    FunctionPoint myPoint = this.points[i];
                    FunctionPoint otherPoint = otherFunc.getPoint(i);

                    if (!pointsAreEqual(myPoint, otherPoint)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                // Если возникла ошибка при получении точек, считаем функции не равными
                return false;
            }
        }

        return true;
    }

    /**
     * Возвращает хэш-код табулированной функции
     * @return хэш-код, рассчитанный на основе количества точек и хэш-кодов всех точек
     */
    @Override
    public int hashCode() {
        int result = size; // Начинаем с количества точек

        // Добавляем хэш-код каждой точки
        for (int i = 0; i < size; i++) {
            result ^= points[i].hashCode();

            // Сдвигаем результат для лучшего распределения
            result = Integer.rotateLeft(result, 1);
        }

        return result;
    }

    /**
     * Создает и возвращает глубокую копию текущей табулированной функции
     * @return копия объекта ArrayTabulatedFunctionExternalizable
     */
    @Override
    public Object clone() {
        try {
            // Создаем новый массив точек
            FunctionPoint[] clonedPoints = new FunctionPoint[points.length];

            // Клонируем каждую точку
            for (int i = 0; i < size; i++) {
                clonedPoints[i] = (FunctionPoint) points[i].clone();
            }

            // Копируем остальные поля
            for (int i = size; i < points.length; i++) {
                clonedPoints[i] = null;
            }

            // Создаем новый объект
            ArrayTabulatedFunctionExternalizable clone = new ArrayTabulatedFunctionExternalizable();

            // Устанавливаем поля напрямую
            clone.points = clonedPoints;
            clone.size = this.size;

            return clone;

        } catch (Exception e) {
            throw new InternalError("Ошибка при клонировании: " + e.getMessage());
        }
    }

    // Вспомогательные методы
    private boolean isValidXPosition(int index, double newX) {
        if (index > 0 && newX <= points[index - 1].getX() + EPSILON) {
            return false;
        }
        if (index < size - 1 && newX >= points[index + 1].getX() - EPSILON) {
            return false;
        }
        return true;
    }

    private void expandArray() {
        int newCapacity = points.length * 3 / 2 + 1;
        FunctionPoint[] newArray = new FunctionPoint[newCapacity];
        System.arraycopy(points, 0, newArray, 0, size);
        points = newArray;
    }

    private double linearInterpolation(FunctionPoint p1, FunctionPoint p2, double x) {
        double x1 = p1.getX();
        double y1 = p1.getY();
        double x2 = p2.getX();
        double y2 = p2.getY();

        double k = (y2 - y1) / (x2 - x1);
        return y1 + k * (x - x1);
    }

    /**
     * Вспомогательный метод для сравнения двух точек с учетом погрешности
     */
    private boolean pointsAreEqual(FunctionPoint p1, FunctionPoint p2) {
        return Math.abs(p1.getX() - p2.getX()) < EPSILON &&
                Math.abs(p1.getY() - p2.getY()) < EPSILON;
    }

    // Метод для безопасного создания функции из точек
    public static ArrayTabulatedFunctionExternalizable createFromPoints(FunctionPoint[] pointsArray) {
        if (pointsArray == null || pointsArray.length < 2) {
            throw new IllegalArgumentException("Invalid points array");
        }

        java.util.Arrays.sort(pointsArray, (p1, p2) -> Double.compare(p1.getX(), p2.getX()));

        java.util.List<FunctionPoint> uniquePoints = new java.util.ArrayList<>();
        for (int i = 0; i < pointsArray.length; i++) {
            if (i == 0 || Math.abs(pointsArray[i].getX() - pointsArray[i-1].getX()) > EPSILON) {
                uniquePoints.add(pointsArray[i]);
            }
        }

        if (uniquePoints.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 unique points");
        }

        return new ArrayTabulatedFunctionExternalizable(
                uniquePoints.toArray(new FunctionPoint[0])
        );
    }
}