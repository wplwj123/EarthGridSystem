package cn.edu.njnu.earthgrid.feature;

import java.util.ArrayList;

/**
 * vector grid polyline
 * using serial grid points to represent a vector polygon
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class Polyline extends Geometry {

    /**
     * Break Points List
     */
    private ArrayList<Point> points;

    /**
     * Default Construct
     */
    public Polyline() {
        super(ShapeType.Polyline);
        this.points = new ArrayList<Point>();
    }

    /**
     * Constructor
     *
     * @param points Break Points List
     */
    public Polyline(ArrayList<Point> points) {
        super(ShapeType.Polyline);
        this.points = points;
    }

    /**
     * get Break Points List
     *
     * @return
     */
    public ArrayList<Point> getPoints() {
        return this.points;
    }

    /**
     * set Break Points List
     *
     * @param points
     */
    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    /**
     * get number of Break Points
     *
     * @return
     */
    public int getPointNum() {
        return this.points.size();
    }

    /**
     * get start point of grid polyline
     *
     * @return
     */
    public Point getFirstPoint() {
        if (getPointNum() > 0) {
            return this.points.get(0);
        } else {
            return null;
        }
    }

    /**
     * get end point of grid polyline
     *
     * @return
     */
    public Point getLastPoint() {
        if (getPointNum() > 0) {
            return this.points.get(getPointNum() - 1);
        } else {
            return null;
        }
    }

    /**
     * add break point p after end point
     *
     * @param p
     */
    public void addPoint(Point p) {
        this.points.add(p);
    }

    /**
     * add break point p after index i point
     *
     * @param i
     * @param p
     */
    public void addPoint(int i, Point p) {
        this.points.add(i, p);
    }

    /**
     * remove point p
     *
     * @param p
     * @return
     */
    public boolean removePoint(Point p) {
        return this.points.remove(p);
    }

    /**
     * remove index i point
     *
     * @param i
     * @return
     */
    public Point removePoint(int i) {
        if (getPointNum() - 1 > i) {
            return this.points.remove(i);
        } else {
            return null;
        }
    }

    /**
     * return true if this polyline is closed
     *
     * @return
     */
    public boolean isClosed() {
        if (getFirstPoint().equals(getLastPoint())) {
            return true;
        } else {
            return false;
        }
    }
}
