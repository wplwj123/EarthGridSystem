package cn.edu.njnu.earthgrid.feature;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;

/**
 * enveloping rectangle
 */
public class Extent {
    private Point topleft;
    private Point topright;
    private Point botleft;
    private Point botright;

    private ExtentLonLat extentLonLat = null;

    public Extent(Point topleft, Point topright, Point botleft, Point botright) {
        this.topleft = topleft;
        this.topright = topright;
        this.botleft = botleft;
        this.botright = botright;
    }

    public boolean Contains(SpericalCoord sc){
        if(null == extentLonLat)
            getExtentLonLat();

        if(sc.getLongitude() < extentLonLat.minLon || sc.getLongitude() > extentLonLat.maxLon
                || sc.getLatitude() < extentLonLat.minLat || sc.getLatitude() > extentLonLat.maxLat)
            return false;
        else
            return true;
    }

    public boolean Contains(Geometry geometry){
        if(ShapeType.Point == geometry.getShapeType()){
            Point point = (Point) geometry;
            SpericalCoord[] scs = point.getPosition().toTrigon().toSpericalCoord();

            if(!Contains(scs[0]) || !Contains(scs[1]) || !Contains(scs[2]))
                return false;
            else
                return true;
        }

        if(null == extentLonLat)
            getExtentLonLat();

        Extent geomExtent = geometry.getExtend();
        if(null == geomExtent.extentLonLat)
            geomExtent.getExtentLonLat();

        if(geomExtent.extentLonLat.minLon < extentLonLat.minLon || geomExtent.extentLonLat.maxLon > extentLonLat.maxLon
                || geomExtent.extentLonLat.minLat < extentLonLat.minLat || geomExtent.extentLonLat.maxLat > extentLonLat.maxLat)
            return false;
        else
            return true;
    }

    public double getMinLon(){
        return extentLonLat.minLon;
    }

    private void getExtentLonLat(){
        double minLon, maxLon, minLat, maxLat;

        SpericalCoord[] scs = topleft.getPosition().toTrigon().toSpericalCoord();
        minLon = maxLon = scs[0].getLongitude();
        if(Math.abs(minLon - 180d) < MathUtil.EPS)
            minLon = maxLon = -180d;

        minLat = maxLat = scs[0].getLatitude();
        for(int i = 1; i < 3; ++ i){
            minLon = minLon > scs[i].getLongitude() ? scs[i].getLongitude() : minLon;
            maxLat = maxLat < scs[i].getLatitude() ? scs[i].getLatitude() : maxLat;
        }

        scs = topright.getPosition().toTrigon().toSpericalCoord();
        for(int i = 0; i < 3; ++i){
            maxLon = maxLon < scs[i].getLongitude() ? scs[i].getLongitude() : maxLon;
            maxLat = maxLat < scs[i].getLatitude() ? scs[i].getLatitude() : maxLat;
        }

        scs = botleft.getPosition().toTrigon().toSpericalCoord();
        for(int i = 0; i < 3; ++i){
            minLon = minLon > scs[i].getLongitude() ? scs[i].getLongitude() : minLon;
            minLat = minLat > scs[i].getLatitude() ? scs[i].getLatitude() : minLat;
        }

        scs = botright.getPosition().toTrigon().toSpericalCoord();
        for(int i = 0; i < 3; ++i){
            maxLon = maxLon < scs[i].getLongitude() ? scs[i].getLongitude() : maxLon;
            minLat = minLat > scs[i].getLatitude() ? scs[i].getLatitude() : minLat;
        }

        if(Math.abs(maxLat - 90) < MathUtil.EPS || Math.abs(minLat + 90) < MathUtil.EPS){
            minLon = -180d;
            maxLon = 180d;
        }

        this.extentLonLat =new ExtentLonLat(minLon, maxLon, minLat, maxLat);
    }

    @Override
    public String toString() {
        return topleft.toString() + "," + topright.toString() +
                "," + botleft.toString() + "," + botright.toString();
    }

    /**
     *
     * @param extStr (topleft, topright, botleft, botright)
     * @return
     */
    public static Extent FromString(String extStr){
        extStr = extStr.substring(1, extStr.length() - 1);
        String[] pointsStr = extStr.split(",");

        return new Extent(Point.FromString(pointsStr[0]), Point.FromString(pointsStr[1]),
                            Point.FromString(pointsStr[2]),Point.FromString(pointsStr[3]));
    }

    public static Extent FromString(String topleftStr, String toprightStr, String botleftStr, String botrightStr){
        return new Extent(Point.FromString(topleftStr), Point.FromString(toprightStr),
                            Point.FromString(botleftStr),Point.FromString(botrightStr));
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Extent))
            return false;
        return topleft.equals(((Extent) obj).topleft) && topright.equals(((Extent) obj).topright)
                && botleft.equals(((Extent) obj).botleft) && botright.equals(((Extent) obj).botright);
    }

    private class ExtentLonLat{
        public final double minLon;
        public final double maxLon;
        public final double minLat;
        public final double maxLat;

        public ExtentLonLat(double minLon, double maxLon, double minLat, double maxLat) {
            this.minLon = minLon;
            this.maxLon = maxLon;
            this.minLat = minLat;
            this.maxLat = maxLat;
        }
    }
}
