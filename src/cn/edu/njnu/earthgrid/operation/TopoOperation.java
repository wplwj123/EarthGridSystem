package cn.edu.njnu.earthgrid.operation;

import cn.edu.njnu.earthgrid.feature.Geometry;
import cn.edu.njnu.earthgrid.feature.Point;
import cn.edu.njnu.earthgrid.feature.Polygon;

/**
 * operation for topology relationshaip of grid Geometry
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class TopoOperation {

    private TopoOperation(){};

    public static boolean Contains(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Crosses(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Disjoint(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Equal(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Intersects(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Overlaps(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Touch(Geometry geom1, Geometry geom2){

        return false;
    }

    public static boolean Within(Geometry geom1, Geometry geom2){

        return false;
    }
}
