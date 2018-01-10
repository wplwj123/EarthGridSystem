package cn.edu.njnu.earthgrid.core.geometry;

/**
 * Sphere Icosahedron
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class Icosahedron {
    /**
     * vertxes of Sphere Icosahedron
     */
    private CartesianCoord[] p =new CartesianCoord[12];

    /**
     * get one vertex of Sphere Icosahedron
     * @param i index of vertex
     * @return p[i]
     */
    public final CartesianCoord p(int i) {
        return p[i];
    }

    /**
     * default Constructor
     */
    private Icosahedron(){
        p[0] = new CartesianCoord(0.0000000000, 0.0000000000, 6400000.0000000000);
        p[1] = new CartesianCoord(5724334.0223994618, 0.0000000000, 2862167.0111997304);
        p[2] = new CartesianCoord(1768916.4944001348, 5444165.1734530553, 2862167.0111997304);
        p[3] = new CartesianCoord(-4631083.5055998648, 3364679.1175624561, 2862167.0111997304);
        p[4] = new CartesianCoord(-4631083.5055998657, -3364679.1175624547, 2862167.0111997304);
        p[5] = new CartesianCoord(1768916.4944001336, -5444165.1734530563, 2862167.0111997304);
        p[6] = new CartesianCoord(4631083.5055998657, 3364679.1175624551, -2862167.0111997304);
        p[7] = new CartesianCoord(-1768916.4944001341, 5444165.1734530563, -2862167.0111997304);
        p[8] = new CartesianCoord(-5724334.0223994618, 0.0000000007, -2862167.0111997304);
        p[9] = new CartesianCoord(-1768916.4944001355, -5444165.1734530553, -2862167.0111997304);
        p[10] = new CartesianCoord(4631083.5055998648, -3364679.1175624565, -2862167.0111997304);
        p[11] = new CartesianCoord(0.0000000000, 0.0000000000, -6400000.0000000000);
    }

    /**
     * Singleton Pattern
     */
    private static Icosahedron instance = null;

    /**
     * get Singleton instance
     * @return Singleton instance
     */
    public static Icosahedron getInstance(){
        if(instance == null){
            instance = new Icosahedron();
        }
        return instance;
    }
}
