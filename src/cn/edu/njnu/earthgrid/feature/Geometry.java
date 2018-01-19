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
    private Extent ext;

    public Extent getExtend() {
        return ext;
    }

    public void setExtend(Extent ext) {
        this.ext = ext;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public Geometry(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public abstract boolean Contains(Geometry geometry);

    public abstract boolean Within(Geometry geometry);

    public abstract boolean Disjoint(Geometry geometry);

    public boolean Intersects(Geometry geometry){
        return !this.Disjoint(geometry);
    }

    public abstract boolean Overlaps(Geometry geometry);

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Geometry))
            return false;
        return ext.equals(((Geometry) obj).ext) && shapeType == ((Geometry) obj).shapeType;
    }
}
