package cn.edu.njnu.earthgrid.core.geometry;

/**
 * Trigon cell of EQTM
 * using Cartesian coordinate
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class Trigon {
    /**
     * three vertxes of diamond cell
     * v[0], top vertex
     * v[1], left vertex
     * v[2], right vertex
     */
    private CartesianCoord[] v = new CartesianCoord[3];

    /**
     * Constructor
     * @param v0 top vertex
     * @param v1 left vertex
     * @param v2 right vertex
     */
    public Trigon(CartesianCoord v0, CartesianCoord v1, CartesianCoord v2) {
        this.v[0] = v0;
        this.v[1] = v1;
        this.v[2] = v2;
    }

    /**
     * get one vertex of trigon cell
     * v(0), top vertex
     * v(1), left vertex
     * v(2), right vertex
     *
     * @param i index of vertex
     * @return v[i]
     */
    public final CartesianCoord v(int i) {
        if(0 <= i && 2 >= i){
            return v[i];
        }
        else{
            return  null;
        }
    }

    /**
     * change vertxes's value
     * @param v0 top vertex
     * @param v1 left vertex
     * @param v2 right vertex
     */
    public void setValue(CartesianCoord v0, CartesianCoord v1, CartesianCoord v2) {
        this.v[0] = v0;
        this.v[1] = v1;
        this.v[2] = v2;
    }

    /**
     * area of this Trigon
     * @return
     */
    public double getArea(){
        return 0.0;
    }
}
