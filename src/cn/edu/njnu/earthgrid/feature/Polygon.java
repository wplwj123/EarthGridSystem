package cn.edu.njnu.earthgrid.feature;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * vector grid polygon
 * using some grid polylines to represent a vector polygon
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class Polygon extends Geometry {
    /**
     * Exterior Ring
     */
    private Polyline exteriorRing;
    /**
     * Inner Rings
     */
    private ArrayList<Polyline> innerRings;

    /**
     * Default Constructor
     */
    public Polygon() {
        super(ShapeType.Polygon);
        this.exteriorRing = new Polyline();
        this.innerRings = new ArrayList<Polyline>();
    }

    /**
     * Constructor
     * @param exteriorRing Exterior Ring
     */
    public Polygon(Polyline exteriorRing) {
        super(ShapeType.Polygon);
        this.exteriorRing = exteriorRing;
        this.innerRings = new ArrayList<Polyline>();
    }

    /**
     * Constructor
     * @param exteriorRing Exterior Ring
     * @param innerRings Inner Rings
     */
    public Polygon(Polyline exteriorRing, ArrayList<Polyline> innerRings) {
        super(ShapeType.Polygon);
        this.exteriorRing = exteriorRing;
        this.innerRings = innerRings;
    }

    /**
     * get Exterior Ring
     * @return
     */
    public Polyline getExteriorRing() {
        return exteriorRing;
    }

    /**
     * set Exterior Ring
     * @param exteriorRing
     */
    public void setExteriorRing(Polyline exteriorRing) {
        this.exteriorRing = exteriorRing;
    }

    /**
     * get Inner Ring
     * @return
     */
    public Polyline getInnerRing(int innerID) {
        return innerRings.get(innerID);
    }

    /**
     * set Inner Rings
     * @param innerRings
     */
    public void setInnerRings(ArrayList<Polyline> innerRings) {
        this.innerRings = innerRings;
    }

    /**
     * add Inner Ring
     * @param innerRing
     */
    public void addInnerRing(Polyline innerRing){
        if(this.innerRings == null){
            this.innerRings = new ArrayList<Polyline>();
        }

        this.innerRings.add(innerRing);
    }

    /**
     * remove Inner Ring
     * @param innerRing
     * @return
     */
    public boolean removeInnerRing(Polyline innerRing){
        return this.innerRings.remove(innerRing);
    }

    /**
     * get the Number of all Rings, include Exterior and Inner
     * @return
     */
    public int getRingNum() {
        return innerRings.size() + 1;
    }

    /**
     * weather this polygon contains the geometry(point, polyline, polygon)
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Contains(Geometry geometry){
        if(!getExtend().Contains(geometry))
            return false;

        if(ShapeType.Point == geometry.getShapeType()){
            Point point = (Point) geometry;

            return MathUtil.Contain(this, point);

//            SpericalCoord lineStart = getExtend().getTopLeft();
//            SpericalCoord lineEnd = point.getPosition().toSpericalCoord();
//            int intersectCount = MathUtil.GetIntersectNum(this, lineStart, lineEnd);
//
//            if(0 == (intersectCount % 2))
//                return false;
        }
        else if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;

            for(int i = 0; i < polyline.getPointNum(); ++i){
                if(!Contains(polyline.getPoint(i)))
                    return false;
            }

            for(int i = 0; i < polyline.getPointNum() - 1; ++i){
                SpericalCoord lineStart = polyline.getPoint(i).getPosition().toSpericalCoord();
                SpericalCoord lineEnd = polyline.getPoint(i + 1).getPosition().toSpericalCoord();
                int intersectCount = MathUtil.GetIntersectNum(this, lineStart, lineEnd);
                if(intersectCount > 0 && 0 == (intersectCount % 2))
                    return false;
            }
        }
        else if(ShapeType.Polygon == geometry.getShapeType()){      //maybe have error, need more test
            Polygon polygon = (Polygon) geometry;

            if(!Contains(polygon.getExteriorRing()))
                return false;

            for(int i = 0; i < polygon.getRingNum() - 1; ++i){
                if(!Contains(polygon.getInnerRing(i)))
                    return false;
            }

            for(int i = 0; i < this.getRingNum() - 1; ++i){
                if(polygon.getExtend().Contains(this.getInnerRing(i)))
                    return false;
            }
        }

        return true;
    }

    /**
     * polygon can only within polygon
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

        return false;
    }

    @Override
    public boolean Disjoint(Geometry geometry) {
        if(!getExtend().Contains(geometry))
            return true;

        if(ShapeType.Point == geometry.getShapeType()){
            return !this.Contains(geometry);
        }

        Polyline polyline;
        if(ShapeType.Polyline == geometry.getShapeType()){
            polyline = (Polyline) geometry;
        }
        else {
            Polygon polygon = (Polygon) geometry;
            polyline = polygon.getExteriorRing();
        }

        for(int i = 0; i < polyline.getPointNum(); ++i){
            if(this.Contains(polyline.getPoint(i)))
                return false;
        }

        return true;
    }

    @Override
    public boolean Overlaps(Geometry geometry) {
        if(ShapeType.Polyline == geometry.getShapeType()){
            Polyline polyline = (Polyline) geometry;

            boolean isContain = false;
            boolean isDisjoint = false;
            for(int i = 0; i < polyline.getPointNum(); ++i){
                if(this.Contains(polyline.getPoint(i)))
                    isContain = true;
                else
                    isDisjoint = true;

                if(isContain && isDisjoint)
                    return true;
            }

        }
        else if(ShapeType.Polygon == geometry.getShapeType()){
            Polygon polygon = (Polygon) geometry;
            return this.Overlaps(polygon.getExteriorRing());
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Polygon))
            return false;

        return super.equals(obj) &&
                exteriorRing.equals(((Polygon) obj).exteriorRing) && innerRings.equals(((Polygon) obj).innerRings);
    }

    /**
     * get polygon from a format string
     *
     * @param polygonStr (Extent) ExteriorRing InnerRings...
     * @return
     */
    public static Polygon FromString(String polygonStr){
        String[] strs = polygonStr.split(" ");
        Polygon polygon = new Polygon();
        polygon.setExtend(Extent.FromString(strs[0]));
        polygon.setExteriorRing(Polyline.FormString(strs[1]));

        if(strs.length > 2){
            for(int i = 2; i < strs.length; ++ i)
                polygon.addInnerRing(Polyline.FormString(strs[i]));
        }

        return polygon;
    }
}
