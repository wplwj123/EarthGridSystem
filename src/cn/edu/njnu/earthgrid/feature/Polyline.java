package cn.edu.njnu.earthgrid.feature;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;

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
    public Point getPoint(int index) {
        return this.points.get(index);
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

    /**
     * weather this polygon contains the point or polyline
     * if geometry if polygon, return false
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Contains(Geometry geometry) {
        if(!getExtend().Contains(geometry))
            return false;

        ArrayList<BaseCode> codes = MathUtil.getCodesInPolyline(this);

        if(ShapeType.Point == geometry.getShapeType()){
            Point point = (Point) geometry;
            if(codes.contains(point.getPosition())){
                return true;
            }
        }
        else if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;
            ArrayList<BaseCode> codes2 = MathUtil.getCodesInPolyline(this);

            for(int i = 0; i < codes2.size(); ++i){
                if(!codes.contains(codes2.get(i)))
                    return false;
            }

            return true;
        }

        return false;
    }

    /**
     * polyline can within polygon and polyline
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Within(Geometry geometry) {
        if(ShapeType.Polygon == geometry.getShapeType()){
            if(geometry.Contains(this))
                return true;
        }
        else if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;

            ArrayList<BaseCode> codes = MathUtil.getCodesInPolyline(polyline);
            ArrayList<BaseCode> codes2 = MathUtil.getCodesInPolyline(this);

            for(int i = 0; i < codes2.size(); ++i){
                if(!codes.contains(codes2.get(i)))
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean Disjoint(Geometry geometry) {
        if(!getExtend().Contains(geometry))
            return true;

        if(ShapeType.Point == geometry.getShapeType()){
            return !this.Contains(geometry);
        }
        else if(ShapeType.Polygon == geometry.getShapeType()){
            return geometry.Disjoint(this);
        }
        else if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;

            ArrayList<BaseCode> codes = MathUtil.getCodesInPolyline(polyline);
            ArrayList<BaseCode> codes2 = MathUtil.getCodesInPolyline(this);

            for(int i = 0; i < codes2.size(); ++i){
                if(codes.contains(codes2.get(i)))
                    return false;
            }
        }

        return true;
    }

    @Override
    public boolean Overlaps(Geometry geometry) {
        if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;

            ArrayList<BaseCode> codes = MathUtil.getCodesInPolyline(polyline);
            ArrayList<BaseCode> codes2 = MathUtil.getCodesInPolyline(this);

            int nDisjoint = 0;
            int nContain = 0;
            for(int i = 0; i < codes2.size(); ++i){
                if(!codes.contains(codes2.get(i)))
                    ++nDisjoint;
                else
                    ++nContain;

                if(nDisjoint > 0 && nContain > 1){
                    return true;
                }
            }

        }
        else if(ShapeType.Polygon == geometry.getShapeType()){
            return geometry.Overlaps(this);
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Polyline))
            return false;
        return super.equals(obj) && points.equals(((Polyline) obj).points);
    }

    @Override
    public String toString() {
        String str = "(";
        str = str + getExtend().toString() + ",";
        for(int i = 0; i < getPointNum() - 1; ++i){
            str = str + points.get(i).toString() + ",";
        }
        str = str + getLastPoint().toString() + ")";

        return str;
    }

    public static Polyline FormString(String polylineStr){
        polylineStr = polylineStr.substring(1, polylineStr.length() - 1);    //remove ( & )
        String[] pointsStr = polylineStr.split(",");

        Polyline polyline = new Polyline();
        polyline.setExtend(Extent.FromString(pointsStr[0], pointsStr[1], pointsStr[2], pointsStr[3]));

        for(int i = 4; i < pointsStr.length; ++i){
            polyline.addPoint(Point.FromString(pointsStr[i]));
        }

        return polyline;
    }
}
