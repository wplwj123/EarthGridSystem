package cn.edu.njnu.earthgrid.feature;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;

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

}
