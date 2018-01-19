package cn.edu.njnu.earthgrid.core.geometry;

/**
 * Diamond Block of EQTM
 * using Cartesian coordinate
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class DiamondBlock {
    /**
     * four vertxes of diamond cell
     * v[0], north vertex
     * v[1], east vertex
     * v[2], south vertex
     * v[3], west vertex
     */
    private CartesianCoord[] v = new CartesianCoord[4];

    /**
     * Constructor
     * @param v0 north vertex
     * @param v1 east vertex
     * @param v2 south vertex
     * @param v3 west vertex
     */
    public DiamondBlock(CartesianCoord v0, CartesianCoord v1, CartesianCoord v2, CartesianCoord v3) {
        this.v[0] = v0;
        this.v[1] = v1;
        this.v[2] = v2;
        this.v[3] = v3;
    }

    /**
     * get one vertex of diamond cell
     * v(0), north vertex
     * v(1), east vertex
     * v(2), south vertex
     * v(3), west vertex
     *
     * @param i index of vertex
     * @return v[i]
     */
    public final CartesianCoord v(int i) {
        if(0 <= i && 3 >= i){
            return v[i];
        }
        else{
            return  null;
        }
    }

    /**
     * change vertxes's value
     * @param v0 north vertex
     * @param v1 east vertex
     * @param v2 south vertex
     * @param v3 west vertex
     */
    public void setValue(CartesianCoord v0, CartesianCoord v1, CartesianCoord v2, CartesianCoord v3) {
        this.v[0] = v0;
        this.v[1] = v1;
        this.v[2] = v2;
        this.v[3] = v3;
    }

    /**
     * convert vertexes's Cartesian Coord to SpericalCoord
     *
     * @return SpericalCoord array of vertexes
     */
    public SpericalCoord[] toSpericalCoord() {
        SpericalCoord[] gcs = new SpericalCoord[4];
        gcs[0] = SpericalCoord.FromCartesianCoord(v[0]);
        gcs[1] = SpericalCoord.FromCartesianCoord(v[1]);
        gcs[2] = SpericalCoord.FromCartesianCoord(v[2]);
        gcs[3] = SpericalCoord.FromCartesianCoord(v[3]);
        return gcs;
    }

    /**
     * area of this diamond block
     *
     * @return
     */
    public double getArea(){
        return 0.0;
    }
}
