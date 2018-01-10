package cn.edu.njnu.earthgrid.core.geometry;

/**
 * Three-dimensional Cartesian coordinate
 * note: unit is meter
 *       origin is the center of earth
 *       positive Z axis points to the north pole
 *       positive X axis points to the intersection of the prime meridian with the equator
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class CartesianCoord {
    /**
     * X coordinate
     */
    private double x;
    /**
     * Y coordinate
     */
    private double y;
    /**
     * Z coordinate
     */
    private double z;

    /**
     * default Constructor
     */
    public CartesianCoord() {
        this.x = 0.;
        this.y = 0.;
        this.z = 0.;
    }

    /**
     * Constructor
     *
     * @param x
     * @param y
     * @param z
     */
    public CartesianCoord(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * convert cartesian coordinate to sperical coordinate
     *
     * @param cc cartesian coordinate
     * @return lat/lon
     */
    public static SpericalCoord ToSpericalCoord(CartesianCoord cc) {

        double longitude = 0.;
        double latitude = 0.;

        if (Math.abs(cc.x - 0) <= MathUtil.EPS && cc.y > 0) {
            longitude = 90.;
        } else if (Math.abs(cc.x - 0) < MathUtil.EPS && cc.y < 0) {
            longitude = -90.;
        } else if (cc.x > 0 && Math.abs(cc.y - 0) < MathUtil.EPS) {
            longitude = 0.;
        } else if (cc.x < 0 && Math.abs(cc.y - 0) < MathUtil.EPS) {
            longitude = 180.;
        } else if (Math.abs(cc.x - 0) < MathUtil.EPS && Math.abs(cc.y - 0) < MathUtil.EPS) {
            longitude = 0.;
        } else {
            longitude = Math.atan(cc.y / cc.x) / MathUtil.PI * 180.;
        }

        if (cc.x < 0. && cc.y > 0.) {
            longitude += 180.;
        } else if (cc.x < 0. && cc.y < 0.) {
            longitude -= 180.;
        }

        if (longitude < -180.) {
            longitude += 180.;
        } else if (longitude > 180.) {
            longitude -= 180.;
        }

        latitude = Math.asin(cc.z / MathUtil.RADIUS) / MathUtil.PI * 180.;

        SpericalCoord sc = new SpericalCoord(longitude, latitude);

        return sc;
    }

    /**
     * convert sperical coordinate to cartesian coordinate
     *
     * @param sc lat/lon
     * @return cartesian coordinate
     */
    public static CartesianCoord FromSpericalCoord(SpericalCoord sc) {
        SpericalCoord tempsc = sc;

        if (tempsc.getLongitude() < MathUtil.EPS) {
            tempsc.setLongitude(360 + tempsc.getLongitude());
        }

        double x = MathUtil.RADIUS * Math.cos(tempsc.getLatitude() * MathUtil.PI / 180.0) * Math.cos(tempsc.getLongitude() * MathUtil.PI / 180.0);
        double y = MathUtil.RADIUS * Math.cos(tempsc.getLatitude() * MathUtil.PI / 180.0) * Math.sin(tempsc.getLongitude() * MathUtil.PI / 180.0);
        double z = MathUtil.RADIUS * Math.sin(tempsc.getLatitude() * MathUtil.PI / 180.0);

        CartesianCoord cc = new CartesianCoord(x, y, z);

        return cc;
    }

    public final double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public final double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public final double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     * operator=
     *
     * @param cc
     */
    public void setValue(CartesianCoord cc) {
        this.x = cc.x;
        this.y = cc.y;
        this.z = cc.z;
    }

    /**
     * operator+
     *
     * @param rhs
     */
    public void Add(CartesianCoord rhs) {
        this.x += rhs.x;
        this.y += rhs.y;
        this.z += rhs.z;
    }

    /**
     * operator/
     *
     * @param rhs
     */
    public void Divide(double rhs) {
        this.x /= rhs;
        this.y /= rhs;
        this.z /= rhs;
    }
}
