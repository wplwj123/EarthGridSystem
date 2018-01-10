package cn.edu.njnu.earthgrid.core.geometry;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EHCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;

/**
 * Sperical coordinate: longitude/latitude
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class SpericalCoord {

    private double longitude;
    private double latitude;

    public final double getLongitude() {
        return longitude;
    }

    public final double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    /**
     * default Constructor
     */
    public SpericalCoord() {
        this.longitude = 0.;
        this.latitude = 0.;
    }

    /**
     * Constructor
     * @param longitude
     * @param latitude
     */
    public SpericalCoord(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * convert grid code into Sperical coordinate
     * @param code
     * @return lat/lon
     */
    public static SpericalCoord FromCode(BaseCode code){
        return code.toSpericalCoord();
    }

    /**
     * convert Sperical coordinate into grid code.
     * @param sc Sperical coordinate
     * @param level the level of grid code
     * @param type the type of grid code
     * @param ele the element of grid code
     * @return grid code
     */
    public static BaseCode ToCode(SpericalCoord sc, int level, BaseCode.CodeType type, ElementType ele){
        BaseCode code;

        //need change if add code type
        if(type == BaseCode.CodeType.EQCode){
            code = new EQCode();
        }
        else {
            code = new EHCode();
        }

        code.fromSpericalCoord(sc, level, ele);
        return code;
    }

    /**
     * convert cartesian coordinate to sperical coordinate
     * @param cc cartesian coordinate
     * @return lat/lon
     */
    public static SpericalCoord FromCartesianCoord(CartesianCoord cc){
        return CartesianCoord.ToSpericalCoord(cc);
    }

    /**
     * convert sperical coordinate to cartesian coordinate
     *
     * @param sc lat/lon
     * @return cartesian coordinate
     */
    public static CartesianCoord ToCartesianCoord(SpericalCoord sc){
        return CartesianCoord.FromSpericalCoord(sc);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SpericalCoord)){
            return false;
        }
        return super.equals(obj) &&
                ((SpericalCoord) obj).latitude == this.latitude &&
                ((SpericalCoord) obj).longitude == this.longitude;
    }

    @Override
    public int hashCode() {
        int result = 17;
        long temp = Double.doubleToLongBits(this.longitude);
        result = result + (int)(temp ^ temp >>> 32);
        temp = Double.doubleToLongBits(this.latitude);
        result = result + (int)(temp ^ temp >>> 32);
        return result;
    }
}
