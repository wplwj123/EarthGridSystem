package cn.edu.njnu.earthgrid.feature;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;

/**
 * vector grid point
 * using a grid cell to represent a vector point
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class Point extends Geometry {
    /**
     * the position of point, record in grid code
     */
    private BaseCode position;

    /**
     * Default Constructor
     */
    public Point() {
        super(ShapeType.Point);
        this.position = new EQCode();
    }

    /**
     * Constructor
     * @param position the position of this point
     */
    public Point(BaseCode position) {
        super(ShapeType.Point);
        this.position = position;
    }

    /**
     * get position of this point
     * @return
     */
    public BaseCode getPosition() {

        return this.position;
    }

    /**
     * set position of this point
     * @param position
     */
    public void setPosition(BaseCode position) {
        this.position = position;
    }

    /**
     * point can't contain geometry
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Contains(Geometry geometry) {
        return false;
    }

    /**
     * point can within polygon and polyline
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Within(Geometry geometry) {
        if(ShapeType.Point != geometry.getShapeType()){
            if(geometry.Contains(this))
                return true;
        }

        return false;
    }

    @Override
    public boolean Disjoint(Geometry geometry) {
        if(ShapeType.Point == geometry.getShapeType()){
            return !this.equals(geometry);
        }

        return geometry.Disjoint(this);
    }

    /**
     * point can't overlap other geometry
     *
     * @param geometry
     * @return
     */
    @Override
    public boolean Overlaps(Geometry geometry) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Point))
            return false;
        return position.equals(((Point) obj).position);
    }

    @Override
    public String toString() {
        return position.toString();
    }

    public static Point FromString(String pointStr){
        int level = pointStr.length() - 8;
        int domainID = (int) MathUtil.BinaryToDecimal(pointStr.substring(0, 4));
        int elementCode = (int) MathUtil.BinaryToDecimal(pointStr.substring(4, 8));
        long morton = MathUtil.QuaternaryToDecimal(pointStr.substring(8));

        EQCode position = new EQCode(domainID, elementCode, level, morton);

        return new Point(position);
    }
}
