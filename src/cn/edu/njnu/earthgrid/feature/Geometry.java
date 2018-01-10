package cn.edu.njnu.earthgrid.feature;

/**
 * base Geometry
 *
 * @author LWJie
 * @version EGS 1.0
 */
public abstract class Geometry {
    /**
     * shape type
     */
    private ShapeType shapeType;

    /**
     * enveloping rectangle of geometry
     */
    private Extend ext;

    public Extend getExtend() {
        return ext;
    }

    public void setExtend(Extend ext) {
        this.ext = ext;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public Geometry(ShapeType shapeType) {
        this.shapeType = shapeType;
    }
}
