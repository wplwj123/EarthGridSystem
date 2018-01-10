package cn.edu.njnu.earthgrid.feature;

import java.util.ArrayList;

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
     * get Inner Rings
     * @return
     */
    public ArrayList<Polyline> getInnerRings() {
        return innerRings;
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
}
