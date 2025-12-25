package functions;

import java.io.Serializable;
import java.util.Objects;

public class FunctionPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;


    public FunctionPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public FunctionPoint(FunctionPoint point) {
        this.x = point.x;
        this.y = point.y;
    }

    public FunctionPoint() {
        this(0.0, 0.0);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Возвращает текстовое описание точки в формате (x; y)
     * @return строковое представление точки
     */
    @Override
    public String toString() {
        return "(" + x + "; " + y + ")";
    }

    /**
     * Сравнивает текущую точку с другим объектом
     * @param obj объект для сравнения
     * @return true, если объекты равны (оба являются FunctionPoint с одинаковыми координатами)
     */
    @Override
    public boolean equals(Object obj) {
        // Проверяем, является ли объект тем же самым
        if (this == obj) {
            return true;
        }

        // Проверяем, является ли объект экземпляром FunctionPoint
        if (!(obj instanceof FunctionPoint)) {
            return false;
        }

        // Приводим к типу FunctionPoint
        FunctionPoint other = (FunctionPoint) obj;

        // Используем Double.compare для точного сравнения double
        return Double.compare(this.x, other.x) == 0 &&
                Double.compare(this.y, other.y) == 0;
    }

    /**
     * Возвращает хэш-код точки
     * @return хэш-код, рассчитанный на основе координат
     */
    @Override
    public int hashCode() {
        // Используем Objects.hash для согласованности с equals
        return Objects.hash(x, y);


    }

    /**
     * Создает и возвращает копию текущей точки
     * @return копия объекта FunctionPoint
     */
    @Override
    public Object clone() {
        try {
            // Используем конструктор копирования
            return new FunctionPoint(this.x, this.y);
        } catch (Exception e) {
            // В данном случае исключение маловероятно, но для соблюдения сигнатуры
            throw new InternalError("Ошибка при клонировании");
        }
    }
}