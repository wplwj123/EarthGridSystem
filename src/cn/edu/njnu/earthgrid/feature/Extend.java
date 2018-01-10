package cn.edu.njnu.earthgrid.feature;

/**
 * enveloping rectangle
 */
public class Extend {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public Extend(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }
}
