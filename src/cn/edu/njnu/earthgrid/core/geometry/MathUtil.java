package cn.edu.njnu.earthgrid.core.geometry;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.feature.Point;
import cn.edu.njnu.earthgrid.feature.Polygon;
import cn.edu.njnu.earthgrid.feature.Polyline;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Spatial Geometry Calculation
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class MathUtil {

    private MathUtil(){
        throw new AssertionError();
    }

    //Math Constant Define
    /**
     * π
     */
    public static final double PI = 3.14159265358979323846;
    /**
     * Epsilon for float calculation
     */
    public static final double EPS = 0.0000000001;
    /**
     * Earth's Radius Length
     */
    public static final double RADIUS = 6400000.000000;


    /*****************************************************************************************************/
    /**——————————————————util for encode and decode————————————————————**/

    /**
     * get final sub diamond.
     *
     * @param d      the located diamond, firstly initial parent diamond should be passed
     * @param morton the morton code
     * @param level  grid's level
     */
    public static void GetDiamond(DiamondBlock d, long morton, int level) {
        if (level <= 0) {
            return;
        }

        long andValue = Pow(2, level * 2 - 1) + Pow(2, level * 2 - 2);

        long ret = morton & andValue;
        ret = ret >> (level * 2 - 2);

        GetSubDiamond(d, ret);

        GetDiamond(d, morton, level - 1);
    }

    /**
     * get point's morton code.
     * you could ignore it for default value set
     *
     * @param morton the morton code
     * @param cc     the point's cartesian coordination
     * @param d      initial parent diamond
     * @param level  grid's level
     */
    public static long CalcMorton(long morton, CartesianCoord cc, DiamondBlock d, int level) {
        if (level <= 0) {
            return morton;
        }

        int subid = GetSubDiamondID(d, cc);
        morton = morton << 2;
        morton += subid;

        return CalcMorton(morton, cc, d, level - 1);
    }

    /**
     * get sub diamond.
     * note:
     *               1
     *            0   3
     *              2
     *
     * @param d   the parent diamond
     * @param ret sub diamond's morton id
     */
    public static void GetSubDiamond(DiamondBlock d, long ret) {
        CartesianCoord m0 = new CartesianCoord();
        CartesianCoord m1 = new CartesianCoord();
        CartesianCoord m2 = new CartesianCoord();
        CartesianCoord m3 = new CartesianCoord();
        DiamondMids(d, m0, m1, m2, m3);

        CartesianCoord center = DiamondCenter(d);

        CartesianCoord v0, v1, v2, v3;

        if (ret == 1) {
            v0 = d.v(0);
            v1 = m0;
            v2 = center;
            v3 = m3;
        } else if (ret == 3) {
            v0 = m0;
            v1 = d.v(1);
            v2 = m1;
            v3 = center;
        } else if (ret == 2) {
            v0 = center;
            v1 = m1;
            v2 = d.v(2);
            v3 = m2;
        } else {
            v0 = m3;
            v1 = center;
            v2 = m2;
            v3 = d.v(3);
        }

        d.setValue(v0, v1, v2, v3);
    }

    /**
     * get sub diamond id, and updata current diamond
     *
     * @param d  the parent diamond
     * @param cc the point in the diamond, ignore checking now
     * @return sub diamond id
     */
    public static int GetSubDiamondID(DiamondBlock d, CartesianCoord cc) {
        CartesianCoord m0 = new CartesianCoord();
        CartesianCoord m1 = new CartesianCoord();
        CartesianCoord m2 = new CartesianCoord();
        CartesianCoord m3 = new CartesianCoord();
        DiamondMids(d, m0, m1, m2, m3);

        CartesianCoord center = DiamondCenter(d);

        // cacculate subdiamond id
        int pos0 = PointAbovePlane(cc, center, m0);
        int pos1 = PointAbovePlane(cc, center, m1);
        int pos2 = PointAbovePlane(cc, center, m2);
        int pos3 = PointAbovePlane(cc, center, m3);

        int subid = -1;
        if (pos3 == 1 && pos2 == -1) {
            subid = 0;
        } else if (pos0 == 1 && pos3 != 1) {
            subid = 1;
        } else if (pos1 == -1 && pos2 != -1) {
            subid = 2;
        } else {
            subid = 3;
        }

        //updata diamond
        CartesianCoord v0, v1, v2, v3;
        if (subid == 1) {
            v0 = d.v(0);
            v1 = m0;
            v2 = center;
            v3 = m3;
        } else if (subid == 3) {
            v0 = m0;
            v1 = d.v(1);
            v2 = m1;
            v3 = center;
        } else if (subid == 2) {
            v0 = center;
            v1 = m1;
            v2 = d.v(2);
            v3 = m2;
        } else {
            v0 = m3;
            v1 = center;
            v2 = m2;
            v3 = d.v(3);
        }
        d.setValue(v0, v1, v2, v3);

        return subid;
    }

    /**
     * get point's domain and type.
     * note: call get_nearest_edge or get_located_cell
     *
     * @param cc  the point's cartesian coordination
     * @param d   final sub diamond
     * @param ele element identifier
     */
    public static int CalcType(CartesianCoord cc, DiamondBlock d, ElementType ele) {
        int type = -1;

        if (ele == ElementType.GridNode) {
            type = 0;
        } else if (ele == ElementType.GridEdge) {
            type = GetNearestEdge(cc, d);
        } else if (ele == ElementType.GridCell) {
            type = GetLocatedCell(cc, d);
        }

        return type;
    }

    /**
     * get element's lat/lon in this targeted sub diamond.
     *
     * @param d        the located sub diamond
     * @param typeCode domain and type, in fact type is only needed
     */
    public static CartesianCoord GetDiamondElement(DiamondBlock d, int typeCode) {
        CartesianCoord cc = new CartesianCoord();

        switch (typeCode) {
            case 0:
                cc = d.v(3);
                break;
            case 1:
                cc = MidGreatArc(d.v(3), d.v(0));
                break;
            case 2:
                cc = MidGreatArc(d.v(3), d.v(1));
                break;
            case 3:
                cc = MidGreatArc(d.v(3), d.v(2));
                break;
            case 4:
                cc.Add(d.v(0));
                cc.Add(d.v(1));
                cc.Add(d.v(3));
                cc.Divide(3.0);
                cc = Normalize(cc, RADIUS);
                break;
            case 5:
                cc.Add(d.v(2));
                cc.Add(d.v(1));
                cc.Add(d.v(3));
                cc.Divide(3.0);
                cc = Normalize(cc, RADIUS);
                break;
            default:
                break;
        }
        return cc;
    }

    /**
     * get point's nearest edge's type.
     *
     * @param cc the point's cartesian coordination
     * @param d  the targeted sub diamond
     */
    private static int GetNearestEdge(CartesianCoord cc, DiamondBlock d) {
        CartesianCoord edgeNW = MidGreatArc(d.v(0), d.v(3)); // northwest
        CartesianCoord edgeC = MidGreatArc(d.v(1), d.v(3)); // center
        CartesianCoord edgeSW = MidGreatArc(d.v(2), d.v(3)); // southwest

        double disNW = LinearLength(edgeNW, cc);
        double disC = LinearLength(edgeC, cc);
        double disSW = LinearLength(edgeSW, cc);

        if (disNW < disC && disNW < disSW) {
            return 1;
        } else if (disC < disNW && disC < disSW) {
            return 2;
        } else {
            return 3;
        }
    }


    /**
     * get point's located triangle cell's dt.
     *
     * @param cc the point's cartesian coordination
     * @param d  the targeted sub diamond
     */
    private static int GetLocatedCell(CartesianCoord cc, DiamondBlock d) {
        int pos = PointAbovePlane(cc, d.v(3), d.v(1));
        if (pos == 1) {
            return 4;
        } else {
            return 5;
        }
    }

    /**——————————————————util for encode and decode————————————————————**/
    /*****************************************************************************************************/


    /********************************************************************************************************/
    /**——————————————util for transformation between digit format————————————————**/

    //all not yet test

    /**
     * decimal to quaternary char array.
     * note: actually its length is one char more than this
     *       call decimal_to_binary firstly
     *
     * @param decimal converted decimal number
     * @param len     array length without end '\0'
     * @return quaternary array
     */
    public static String DecimalToQuaternary(long decimal, int len) {
        char[] quat = new char[len];
        for(int i = 0; i < quat.length; i++){
            quat[i] = '0';
        }
        while (decimal != 0) {
            int rem = (int) (decimal & 0x03);
            quat[--len] = (char) (rem + (int) '0');

            decimal = decimal >> 2;
        }

        return new String(quat);
    }

    /**
     * quaternary char array to decimal.
     *
     * @param quat input quaternary
     */
    public static long QuaternaryToDecimal(String quat) {
        assert quat != null;

        int len = quat.length(); // not include '\0'

        long ret = 0;
        for (int i = 0; i < len; ++i) {
            long cur_quat_num = (long) (quat.charAt(i) - '0');
            ret += Pow(4, len - i - 1) * cur_quat_num;
        }

        return ret;
    }

    /**
     * decimal to binary char array.
     *
     * @param decimal converted decimal number
     * @param len     array length without end '\0'
     * @return binary array
     */
    public static String DecimalToBinary(long decimal, int len) {
        char[] bin = new char[len];
        for(int i = 0; i < bin.length; i++){
            bin[i] = '0';
        }
        while (decimal != 0) {
            // use bit operation
            int rem = (int) (decimal & 0x01);
            bin[--len] = (char) ((int) ('0') + rem);
            decimal = decimal >> 1;
        }

        return new String(bin);
    }

    /**
     * binary char array to decimal.
     *
     * @param bin returned decimal
     */
    public static long BinaryToDecimal(String bin) {
        assert bin != null;

        int len = bin.length(); // not include '\0'

        long ret = 0;
        for (int i = 0; i < len; ++i) {
            long cur_bin_num = (long) (bin.charAt(i) - '0');
            ret += Pow(2, len - i - 1) * cur_bin_num;
        }

        return ret;
    }

    // row/col

    /**——————————————util for transformation between code format————————————————**/
    /********************************************************************************************************/


    /*********************************************************************************************************/
    /**——————————————————util for geometric calculation————————————————————**/

    /**
     * predict which domain this point locates in.
     *
     * @param c the targeted point
     */
    public static int PredictDomain(CartesianCoord c) {
        Icosahedron ico = Icosahedron.getInstance();

        int apart = 9;

        if (PointAbovePlane(c, ico.p(5), ico.p(9)) != -1 && PointAbovePlane(c, ico.p(9), ico.p(11)) != -1
                && PointAbovePlane(c, ico.p(11), ico.p(10)) == 1 && PointAbovePlane(c, ico.p(10), ico.p(5)) == 1) {
            apart = 8;
        } else if (PointAbovePlane(c, ico.p(4), ico.p(8)) != -1 && PointAbovePlane(c, ico.p(8), ico.p(11)) != -1
                && PointAbovePlane(c, ico.p(11), ico.p(9)) == 1 && PointAbovePlane(c, ico.p(9), ico.p(4)) == 1) {
            apart = 7;
        } else if (PointAbovePlane(c, ico.p(3), ico.p(7)) != -1 && PointAbovePlane(c, ico.p(7), ico.p(11)) != -1
                && PointAbovePlane(c, ico.p(11), ico.p(8)) == 1 && PointAbovePlane(c, ico.p(8), ico.p(3)) == 1) {
            apart = 6;
        } else if (PointAbovePlane(c, ico.p(2), ico.p(6)) != -1 && PointAbovePlane(c, ico.p(6), ico.p(11)) != -1
                && PointAbovePlane(c, ico.p(11), ico.p(7)) == 1 && PointAbovePlane(c, ico.p(7), ico.p(2)) == 1) {
            apart = 5;
        } else if (PointAbovePlane(c, ico.p(0), ico.p(5)) != -1 && PointAbovePlane(c, ico.p(5), ico.p(10)) != -1
                && PointAbovePlane(c, ico.p(10), ico.p(1)) == 1 && PointAbovePlane(c, ico.p(1), ico.p(0)) == 1) {
            apart = 4;
        } else if (PointAbovePlane(c, ico.p(0), ico.p(4)) != -1 && PointAbovePlane(c, ico.p(4), ico.p(9)) != -1
                && PointAbovePlane(c, ico.p(9), ico.p(5)) == 1 && PointAbovePlane(c, ico.p(5), ico.p(0)) == 1) {
            apart = 3;
        } else if (PointAbovePlane(c, ico.p(0), ico.p(3)) != -1 && PointAbovePlane(c, ico.p(3), ico.p(8)) != -1
                && PointAbovePlane(c, ico.p(8), ico.p(4)) == 1 && PointAbovePlane(c, ico.p(4), ico.p(0)) == 1) {
            apart = 2;
        } else if (PointAbovePlane(c, ico.p(0), ico.p(2)) != -1 && PointAbovePlane(c, ico.p(2), ico.p(7)) != -1
                && PointAbovePlane(c, ico.p(7), ico.p(3)) == 1 && PointAbovePlane(c, ico.p(3), ico.p(0)) == 1) {
            apart = 1;
        } else if (PointAbovePlane(c, ico.p(0), ico.p(1)) != -1 && PointAbovePlane(c, ico.p(1), ico.p(6)) != -1
                && PointAbovePlane(c, ico.p(6), ico.p(2)) == 1 && PointAbovePlane(c, ico.p(2), ico.p(0)) == 1) {
            apart = 0;
        } else if (Math.abs(c.getZ() - RADIUS) <= EPS) {
            apart = 4;
        }

        return apart;
    }

    /**
     * whether this point is above this plane.
     * right hand rule
     *
     * @param pt the targeted point
     * @param pA face's point
     * @param pB face's point
     * @return 1 is above, 0 is in and -1 is under
     */
    public static int PointAbovePlane(CartesianCoord pt, CartesianCoord pA, CartesianCoord pB) {
        CartesianCoord normal = GetNormalVector(pA, pB);

        double angle = VectorsAngle(normal, pt);
        double minus = angle - PI / 2.;

        if (minus < -EPS) {
            return 1;
        }
        if (Math.abs(minus) <= EPS) {
            return 0;
        }
        return -1;
    }

    /**
     * whether this point is in this diamond.
     *
     * @param pt the targeted point
     * @param d  the diamond
     * @return true is in and false is out or on the right boundary
     */
    public static boolean PointInDiamond(CartesianCoord pt, DiamondBlock d) {
        if (PointAbovePlane(pt, d.v(1), d.v(0)) == 1
                && PointAbovePlane(pt, d.v(2), d.v(1)) == 1
                && PointAbovePlane(pt, d.v(3), d.v(2)) != -1
                && PointAbovePlane(pt, d.v(0), d.v(3)) != -1) {
            return true;
        }

        return false;
    }

    /**
     * angle between arms.
     *
     * @param arm0   arm's endpoint
     * @param arm1   arm's endpoint
     * @param vertex arms' common point
     */
    public static double InnerAngle(CartesianCoord arm0, CartesianCoord arm1, CartesianCoord vertex) {
        double inner_product = (arm0.getX() - vertex.getX()) * (arm1.getX() - vertex.getX())
                + (arm0.getY() - vertex.getY()) * (arm1.getY() - vertex.getY())
                + (arm0.getZ() - vertex.getZ()) * (arm1.getZ() - vertex.getZ());
        double modulus = Math.sqrt((arm0.getX() - vertex.getX()) * (arm0.getX() - vertex.getX())
                + (arm0.getY() - vertex.getY()) * (arm0.getY() - vertex.getY())
                + (arm0.getZ() - vertex.getZ()) * (arm0.getZ() - vertex.getZ()))
                * Math.sqrt(((arm1.getX() - vertex.getX()) * (arm1.getX() - vertex.getX())
                + (arm1.getY() - vertex.getY()) * (arm1.getY() - vertex.getY())
                + (arm1.getZ() - vertex.getZ()) * (arm1.getZ() - vertex.getZ())));

        return Math.acos(inner_product / modulus);
    }

    /**
     * great arc's length between two points.
     *
     * @param c0 start point
     * @param c1 end point
     */
    public static double ArcLength(CartesianCoord c0, CartesianCoord c1) {
        double innerProduct = (c0.getX() * c1.getX() + c0.getY() * c1.getY() + c0.getZ() * c1.getZ());
        double modulus = Math.sqrt(c0.getX() * c0.getX() + c0.getY() * c0.getY() + c0.getZ() * c0.getZ()) *
                Math.sqrt(c1.getX() * c1.getX() + c1.getY() * c1.getY() + c1.getZ() * c1.getZ());

        double arcRad = Math.acos(innerProduct / modulus);
        return arcRad * RADIUS;
    }

    /**
     * get normal vector of the face composed of two points on sphere and center of sphere.
     *
     * @param start start point on sphere
     * @param end   end point on sphere
     */
    public static CartesianCoord GetNormalVector(CartesianCoord start, CartesianCoord end) {
        double a1 = start.getX();
        double a2 = start.getY();
        double a3 = start.getZ();

        double b1 = end.getX();
        double b2 = end.getY();
        double b3 = end.getZ();

        CartesianCoord res = new CartesianCoord();
        res.setX(a2 * b3 - a3 * b2);
        res.setY(a3 * b1 - a1 * b3);
        res.setZ(a1 * b2 - a2 * b1);

        double len = Math.sqrt(res.getX() * res.getX() + res.getY() * res.getY() + res.getZ() * res.getZ());

        res.setX(res.getX() / len);
        res.setY(res.getY() / len);
        res.setZ(res.getZ() / len);

        return res;
    }

    /**
     * angle of 2 vectors.
     *
     * @param vA 3D vector from {0,0,0}
     * @param vB 3D vector from {0,0,0}
     */
    public static double VectorsAngle(CartesianCoord vA, CartesianCoord vB) {
        double angle = Math.acos((vA.getX() * vB.getX() + vA.getY() * vB.getY() + vA.getZ() * vB.getZ()) /
                (Math.sqrt(vA.getX() * vA.getX() + vA.getY() * vA.getY() + vA.getZ() * vA.getZ())
                        * Math.sqrt(vB.getX() * vB.getX() + vB.getY() * vB.getY() + vB.getZ() * vB.getZ())));

        return angle;
    }

    /**
     * get initial diamond's vertices.
     * @param domain targeted diamond
     * @param cc0   v0
     * @param cc1   v1
     * @param cc2   v2
     * @param cc3   v3
     */
    public static void GetDomainCorner(int domain, CartesianCoord cc0, CartesianCoord cc1,
                                       CartesianCoord cc2, CartesianCoord cc3) {
        Icosahedron ico = Icosahedron.getInstance();
        switch (domain) {
            case 0: {
                cc0.setValue(ico.p(0));
                cc1.setValue(ico.p(2));
                cc2.setValue(ico.p(6));
                cc3.setValue(ico.p(1));
            }
            break;
            case 1: {
                cc0.setValue(ico.p(0));
                cc1.setValue(ico.p(3));
                cc2.setValue(ico.p(7));
                cc3.setValue(ico.p(2));
            }
            break;
            case 2: {
                cc0.setValue(ico.p(0));
                cc1.setValue(ico.p(4));
                cc2.setValue(ico.p(8));
                cc3.setValue(ico.p(3));
            }
            break;
            case 3: {
                cc0.setValue(ico.p(0));
                cc1.setValue(ico.p(5));
                cc2.setValue(ico.p(9));
                cc3.setValue(ico.p(4));
            }
            break;
            case 4: {
                cc0.setValue(ico.p(0));
                cc1.setValue(ico.p(1));
                cc2.setValue(ico.p(10));
                cc3.setValue(ico.p(5));
            }
            break;
            case 5: {
                cc0.setValue(ico.p(2));
                cc1.setValue(ico.p(7));
                cc2.setValue(ico.p(11));
                cc3.setValue(ico.p(6));
            }
            break;
            case 6: {
                cc0.setValue(ico.p(3));
                cc1.setValue(ico.p(8));
                cc2.setValue(ico.p(11));
                cc3.setValue(ico.p(7));
            }
            break;
            case 7: {
                cc0.setValue(ico.p(4));
                cc1.setValue(ico.p(9));
                cc2.setValue(ico.p(11));
                cc3.setValue(ico.p(8));
            }
            break;
            case 8: {
                cc0.setValue(ico.p(5));
                cc1.setValue(ico.p(10));
                cc2.setValue(ico.p(11));
                cc3.setValue(ico.p(9));
            }
            break;
            case 9: {
                cc0.setValue(ico.p(1));
                cc1.setValue(ico.p(6));
                cc2.setValue(ico.p(11));
                cc3.setValue(ico.p(10));
            }
            break;
            default:
                assert false;
                ;
        }
    }

    /**
     * get diamond's center's coordination.
     *
     * @param d targeted diamond
     */
    public static CartesianCoord DiamondCenter(DiamondBlock d) {
        return MidGreatArc(d.v(1), d.v(3));
    }

    /**
     * get diamond's edges' middle points.
     * @param d   targeted diamond
     * @param m0  midpoint of v0v1
     * @param m1  midpoint of v1v2
     * @param m2  midpoint of v2v3
     * @param m3  midpoint of v3v0
     */
    public static void DiamondMids(DiamondBlock d,
                                   CartesianCoord m0, CartesianCoord m1, CartesianCoord m2, CartesianCoord m3) {
        m0.setValue(MidGreatArc(d.v(0), d.v(1)));
        m1.setValue(MidGreatArc(d.v(1), d.v(2)));
        m2.setValue(MidGreatArc(d.v(2), d.v(3)));
        m3.setValue(MidGreatArc(d.v(3), d.v(0)));
    }

    /**
     * middle point of great arc.
     * @param c1
     * @param c2
     * @return midpoint of c1c2
     */
    public static CartesianCoord MidGreatArc(CartesianCoord c1, CartesianCoord c2) {
        double x = (c1.getX() + c2.getX()) * 0.5;
        double y = (c1.getY() + c2.getY()) * 0.5;
        double z = (c1.getZ() + c2.getZ()) * 0.5;

        double length = Math.sqrt(x * x + y * y + z * z);
        x = x / length * RADIUS;
        y = y / length * RADIUS;
        z = z / length * RADIUS;

        return new CartesianCoord(x, y, z);
    }

    /**
     * middle of lat/lon.
     * @param c1
     * @param c2
     * @return midpoint of c1c2
     */
    public static CartesianCoord MidLonLat(CartesianCoord c1, CartesianCoord c2) {
        CartesianCoord mid_axis = new CartesianCoord();
        mid_axis.Add(c1);
        mid_axis.Add(c2);
        mid_axis.Divide(0.5);

        double mid_axis_length = Math.sqrt(mid_axis.getX() * mid_axis.getX() + mid_axis.getY() * mid_axis.getY());
        double square_of_radius_dir = mid_axis.getZ() * mid_axis.getZ();
        double radius_horizental = Math.sqrt(RADIUS * RADIUS - square_of_radius_dir);

        double x = mid_axis.getX() * radius_horizental / mid_axis_length;
        double y = mid_axis.getY() * radius_horizental / mid_axis_length;
        double z = mid_axis.getZ();

        return new CartesianCoord(x, y, z);
    }

    /**
     * linear length between two points.
     *
     * @param c0 start point
     * @param c1 end point
     */
    public static double LinearLength(CartesianCoord c0, CartesianCoord c1) {
        return Math.sqrt((c0.getX() - c1.getX()) * (c0.getX() - c1.getX())
                + (c0.getY() - c1.getY()) * (c0.getY() - c1.getY())
                + (c0.getZ() - c1.getZ()) * (c0.getZ() - c1.getZ()));
    }

    /**
     * normalize the point or vector to the targeted length.
     *
     * @param c point
     * @param r length
     */
    public static CartesianCoord Normalize(CartesianCoord c, double r) {
        double len = Math.sqrt(c.getX() * c.getX() + c.getY() * c.getY() + c.getZ() * c.getZ());
        CartesianCoord ret = new CartesianCoord(c.getX() * (r / len), c.getY() * (r / len), c.getZ() * (r / len));
        return ret;
    }

    /**
     * two vectors' cross product, or face's normal vector
     */
    public static CartesianCoord Cross(CartesianCoord c1, CartesianCoord c2) {
        CartesianCoord normal_vec = new CartesianCoord();
        normal_vec.setX(c1.getY() * c2.getZ() - c1.getZ() * c2.getY());
        normal_vec.setY(c1.getZ() * c2.getX() - c1.getX() * c2.getZ());
        normal_vec.setZ(c1.getX() * c2.getY() - c1.getY() * c2.getX());

        return normal_vec;
    }

    /**
     * rotate vector by the targeted axis and the angle
     */
    public static CartesianCoord RotateVectorByAxis(CartesianCoord v, CartesianCoord axis, double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double x = axis.getX(), y = axis.getY(), z = axis.getZ();
        double px = v.getX(), py = v.getY(), pz = v.getZ();

		/* rotation matrix
		|RM00 RM01 RM11|    |x|
		|RM10 RM11 RM12| *  |y|
		|RM20 RM21 RM22|    |z|
		*/
        double RM00 = x * x * (1 - c) + c;
        double RM01 = x * y * (1 - c) - z * s;
        double RM02 = x * z * (1 - c) + y * s;
        double RM10 = y * x * (1 - c) + z * s;
        double RM11 = y * y * (1 - c) + c;
        double RM12 = y * z * (1 - c) - x * s;
        double RM20 = x * z * (1 - c) - y * s;
        double RM21 = y * z * (1 - c) + x * s;
        double RM22 = z * z * (1 - c) + c;

        CartesianCoord cc = new CartesianCoord();
        cc.setX(px * RM00 + py * RM01 + pz * RM02);
        cc.setY(px * RM10 + py * RM11 + pz * RM12);
        cc.setZ(px * RM20 + py * RM21 + pz * RM22);

        return cc;
    }

    /**
     * get grid nodes in this initial diamond.
     *
     * @param ccList  returned grid nodes in ij array
     * @param d       intial parent diamond
     * @param index0I - index3J array index in the direction of i and j
     * @param level   grid's level
     */
    public static void RecursiveSplit(CartesianCoord[][] ccList, DiamondBlock d,
                                      int index0I, int index0J, int index1I, int index1J,
                                      int index2I, int index2J, int index3I, int index3J,
                                      int level) {
        if (ccList != null) {
            ccList[index0I][index0J] = d.v(0);
            ccList[index1I][index1J] = d.v(1);
            ccList[index2I][index2J] = d.v(2);
            ccList[index3I][index3J] = d.v(3);
        }

        if (level <= 0) {
            return;
        }

        CartesianCoord m0 = new CartesianCoord();
        CartesianCoord m1 = new CartesianCoord();
        CartesianCoord m2 = new CartesianCoord();
        CartesianCoord m3 = new CartesianCoord();
        DiamondMids(d, m0, m1, m2, m3);
        CartesianCoord center = DiamondCenter(d);

        if (ccList != null) {
            ccList[(index0I + index3I) / 2][index0J] = m3;
            ccList[index0I][(index0J + index1J) / 2] = m0;
            ccList[(index0I + index3I) / 2][index1J] = m1;
            ccList[index3I][(index0J + index1J) / 2] = m2;
            ccList[(index0I + index3I) / 2][(index0J + index1J) / 2] = center;

            RecursiveSplit(ccList, new DiamondBlock(d.v(0), m0, center, m3),
                    index0I, index0J, index0I, (index0J + index1J) / 2,
                    (index0I + index3I) / 2, (index0J + index1J) / 2, (index0I + index3I) / 2, index0J,
                    level - 1);
            RecursiveSplit(ccList, new DiamondBlock(m0, d.v(1), m1, center),
                    index0I, (index0J + index1J) / 2, index1I, index1J,
                    (index0I + index3I) / 2, index1J, (index0I + index3I) / 2, (index0J + index1J) / 2,
                    level - 1);
            RecursiveSplit(ccList, new DiamondBlock(center, m1, d.v(2), m2),
                    (index0I + index3I) / 2, (index0J + index1J) / 2, (index0I + index3I) / 2, index1J,
                    index2I, index2J, index3I, (index0J + index1J) / 2,
                    level - 1);
            RecursiveSplit(ccList, new DiamondBlock(m3, center, m2, d.v(3)),
                    (index0I + index3I) / 2, index0J, (index0I + index3I) / 2, (index0J + index1J) / 2,
                    index3I, (index0J + index1J) / 2, index3I, index3J,
                    level - 1);
        }
    }

    /**
     * split great arc into n part
     */
    public static void SplitArcToNPart(CartesianCoord sp, CartesianCoord ep, CartesianCoord[] splitList, int nPart) {
        double angle = VectorsAngle(sp, ep);
        double angleUnit = angle / (nPart - 1);

        CartesianCoord normal = GetNormalVector(sp, ep);
        splitList[0] = sp;
        for (int i = 1; i < nPart - 1; i++) {
            CartesianCoord nc = RotateVectorByAxis(sp, normal, angleUnit * i);
            splitList[i] = nc;
        }
        splitList[nPart - 1] = ep;
    }

    /**
     * intersection of two arcs
     * @param c00 start of arc1
     * @param c01 end of arc1
     * @param c10 start of arc2
     * @param c11 end of arc1
     * @return intersection
     */
    public CartesianCoord Intersection(CartesianCoord c00, CartesianCoord c01, CartesianCoord c10, CartesianCoord c11) {
        CartesianCoord n0 = GetNormalVector(c00, c01);
        CartesianCoord n1 = GetNormalVector(c10, c11);
        CartesianCoord inter = GetNormalVector(n1, n0);

        return Normalize(inter, RADIUS);
    }

    /**——————————————————util for geometric calculation————————————————————**/
    /*********************************************************************************************************/

    /**
     * math power for long
     *
     * @param base base
     * @param exp  exponential
     * @return base^exponential
     */
    public static long Pow(int base, int exp) {
        if (exp == 0) {
            return 1;
        }
        return base * Pow(base, exp - 1);
    }

    public static double Gauss(float x, double y0, double a, double x0, double b) {
        double temp = y0 + a * Math.exp(-(x0 - x) * (x0 - x) / (2 * b * b));
        return temp;
    }

    public static double Circle(float x, double y0, double a, double b, double x0) {
        double temp = y0 + b * Math.sqrt(1 - (x - x0) * (x - x0) / (a * a));
        return temp;
    }

    public static Color GrayToRGB(float gray){
        double r, g, b;
        r = Gauss(gray, 0.00973, 0.95734, 0.68447, 0.40538);
        if (gray >= 0 && gray < 0.5) {
            g = Gauss(gray, -.70487, 1.57141, 0.51782, 0.54700);
        }
        else {
            g = Circle(gray, -.97384, 0.96412, 1.96264, 0.17749);
        }
        b = Gauss(gray, -.05837, 1.05992, 0.28797, 0.39754);

        return new Color((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }

    /*********************************************************************************************************/

    /**
     * get code of middle point between code1 and code2
     *
     * @param code1
     * @param code2
     * @return
     */
    public static BaseCode MidCode(BaseCode code1, BaseCode code2){
        CartesianCoord c1 = CartesianCoord.FromSpericalCoord(code1.toSpericalCoord());
        CartesianCoord c2 = CartesianCoord.FromSpericalCoord(code2.toSpericalCoord());

        SpericalCoord mid = CartesianCoord.ToSpericalCoord(MidGreatArc(c1, c2));

        BaseCode code = SpericalCoord.ToCode(mid, code1.getLevel(), code1.getCodeType(), code1.getElementType());

        if(code1.equals(code) || code2.equals(code))
            return null;
        return code;
    }

    /**
     * get all code in line
     *
     * @param start
     * @param end
     * @param codes
     */
    public static void getCodesInLine(BaseCode start, BaseCode end, ArrayList<BaseCode> codes){
        BaseCode midcode = MidCode(start, end);

        if(null == midcode){
            codes.add(start);
            return;
        }
        else {
            getCodesInLine(start, midcode, codes);
            getCodesInLine(midcode, end, codes);
        }
    }

    /**
     * get all code in polyline
     *
     * @param polyline
     * @return
     */
    public static ArrayList<BaseCode> getCodesInPolyline(Polyline polyline){
        ArrayList<BaseCode> codes = new ArrayList<>();

        for(int pointID = 0; pointID < polyline.getPointNum() - 1; ++pointID){
            BaseCode start = polyline.getPoint(pointID).getPosition();
            BaseCode end = polyline.getPoint(pointID + 1).getPosition();
            MathUtil.getCodesInLine(start, end, codes);
        }

        return codes;
    }

    /**
     * test two line intersect
     *
     * @param startA lineA start point
     * @param endA  lineB end point
     * @param startB lineB start point
     * @param endB lineB end point
     * @return
     */
    public static boolean isLineIntersect(SpericalCoord startA, SpericalCoord endA,
                                          SpericalCoord startB, SpericalCoord endB){
        double x1 = startA.getLongitude();
        double y1 = startA.getLatitude();
        double x2 = endA.getLongitude();
        double y2 = endA.getLatitude();
        double x3 = startB.getLongitude();
        double y3 = startB.getLatitude();
        double x4 = endB.getLongitude();
        double y4 = endB.getLatitude();
        double result = (x2 - x1)*(y4 - y3) - (x4 - x3)*(y2 - y1);
        if (Math.abs(result) > EPS )      //result
        {
            double G_x1y1 = (y1 - y3)*(x4 - x3) - (y4 - y3)*(x1 - x3);
            double G_x2y2 = (y2 - y3)*(x4 - x3) - (y4 - y3)*(x2 - x3);
            double F_x3y3 = (y3 - y1)*(x2 - x1) - (y2 - y1)*(x3 - x1);
            double F_x4y4 = (y4 - y1)*(x2 - x1) - (y2 - y1)*(x4 - x1);
            if (F_x3y3*F_x4y4 <= 0 && G_x1y1*G_x2y2 <= 0)
                return true;
            else
                return false;
        }
        else
        {
            return false;
        }
    }

    /**
     * get intersection number between line and this polygon
     *
     * @param lineStart
     * @param lineEnd
     * @return
     */
    public static int GetIntersectNum(Polygon polygon, SpericalCoord lineStart, SpericalCoord lineEnd){
        SpericalCoord segStart, segEnd;
        int intersectCount = 0;

        ArrayList<SpericalCoord> extRingPoints = new ArrayList<>();
        Set<Integer> domainSet = new HashSet<>();
        DecodePolyline(polygon.getExteriorRing(), extRingPoints, domainSet);

        for(int pointID = 0; pointID < polygon.getExteriorRing().getPointNum() - 1; ++pointID){
            segStart = extRingPoints.get(pointID);
            segEnd = extRingPoints.get(pointID + 1);

            if(MathUtil.isLineIntersect(lineStart, lineEnd, segStart, segEnd))
                ++intersectCount;
        }

        for(int ringID = 0; ringID < polygon.getRingNum() - 1; ++ringID){
            Polyline innerRing = polygon.getInnerRing(ringID);
            ArrayList<SpericalCoord> innRingPoints = new ArrayList<>();
            DecodePolyline(innerRing, innRingPoints, domainSet);

            for(int pointID = 0; pointID < innerRing.getPointNum() - 1; ++pointID){
                segStart = innRingPoints.get(pointID);
                segEnd = innRingPoints.get(pointID + 1);

                if(MathUtil.isLineIntersect(lineStart, lineEnd, segStart, segEnd))
                    ++intersectCount;
            }
        }

        return intersectCount;
    }

    /**
     * test point sc to the left of line
     *
     * @param sc
     * @param start line start point
     * @param end line end point
     * @return >0 if sc to the left of left
     *          =0 if in line
     *          <0 if right side
     */
    public static double PointLeftLine(SpericalCoord sc, SpericalCoord start, SpericalCoord end){
        return ((end.getLongitude() - start.getLongitude()) * (sc.getLatitude() - start.getLatitude())
                - (end.getLatitude() - start.getLatitude()) * (sc.getLongitude() - start.getLongitude()));
    }

    public static boolean Contain(ArrayList<SpericalCoord> ringPoints, SpericalCoord sc){
        int wn = 0;
        SpericalCoord sc1, sc2;

        for(int pointID = 0; pointID < ringPoints.size() - 1; ++pointID){
            sc1 = ringPoints.get(pointID);
            sc2 = ringPoints.get(pointID + 1);

            if (sc1.getLatitude() <= sc.getLatitude()) {
                if (sc2.getLatitude() > sc.getLatitude()) { // upward
                    if (PointLeftLine(sc, sc1, sc2) > 0) {
                        ++wn;
                    }
                }
            }
            else {
                if (sc2.getLatitude() <= sc.getLatitude()) { // downward
                    if (PointLeftLine(sc, sc1, sc2) < 0) {
                        --wn;
                    }
                }
            }
        }

        if (wn == 0)
            return false;
        else
            return true;
    }

    /**
     * test polygon contains a lon/lat point, using Winding Number Method
     *
     * @param polygon
     * @param sc
     * @return
     * @see "http://geomalgorithms.com/a03-_inclusion.html"
     */
    public static boolean Contain(Polygon polygon, SpericalCoord sc){
        if(!polygon.getExtend().Contains(sc))
            return false;

        if(polygon.getExteriorRing().getPointNum() <= 3)
            return false;

        SpericalCoord sc1, sc2;

        ArrayList<SpericalCoord> extRingPoints = new ArrayList<>();
        Set<Integer> domainSet = new HashSet<>();
        DecodePolyline(polygon.getExteriorRing(), extRingPoints, domainSet);

        if(!Contain(extRingPoints, sc))
            return false;

        for(int ringID = 0; ringID < polygon.getRingNum() - 1; ++ringID){
            Polyline innerRing = polygon.getInnerRing(ringID);

            ArrayList<SpericalCoord> innRingPoints = new ArrayList<>();
            DecodePolyline(innerRing, innRingPoints, domainSet);

            if(Contain(innRingPoints, sc))
                return false;
        }

        return true;
    }

    /**
     * test polygon contains a code, using Winding Number Method
     *
     * @param polygon
     * @param code
     * @return
     */
    public static boolean Contain(Polygon polygon, BaseCode code){
        SpericalCoord sc = code.toSpericalCoord();
        return Contain(polygon, sc);
    }

    /**
     * test polygon contains a point, using Winding Number Method
     *
     * @param polygon
     * @param point
     * @return
     */
    public static boolean Contain(Polygon polygon, Point point){
        if(!polygon.getExtend().Contains(point))
            return false;
        BaseCode code = point.getPosition();

        return Contain(polygon, code);
    }

    /**
     * decode all points in polyline
     *
     * @param polyline
     * @param points
     * @param domainSet
     */
    public static void DecodePolyline(Polyline polyline, ArrayList<SpericalCoord> points, Set<Integer> domainSet){
        SpericalCoord scCur, scPre;

        scPre = polyline.getPoint(0).getPosition().toSpericalCoord();
        if(Math.abs(scPre.getLongitude() - 180d) < MathUtil.EPS){
            scCur = polyline.getPoint(1).getPosition().toSpericalCoord();
            if(scCur.getLatitude() > scPre.getLatitude())
                scPre.setLongitude(-scPre.getLongitude());
        }
        points.add(scPre);
        domainSet.add(polyline.getPoint(0).getPosition().getDomainID());

        for (int i = 1; i < polyline.getPointNum(); ++i) {
            scCur = polyline.getPoint(i).getPosition().toSpericalCoord();
            if(Math.abs(scCur.getLongitude() - scPre.getLongitude()) > 350d)
                scCur.setLongitude(-scCur.getLongitude());

            points.add(scCur);
            domainSet.add(polyline.getPoint(i).getPosition().getDomainID());
            scPre = scCur;
        }
    }

}
