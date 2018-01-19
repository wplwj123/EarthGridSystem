package cn.edu.njnu.earthgrid.data;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EHCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.feature.*;
import cn.edu.njnu.earthgrid.feature.Feature;
import cn.edu.njnu.earthgrid.layer.BaseLayer;
import cn.edu.njnu.earthgrid.layer.FeatureLayer;
import org.gdal.gdal.gdal;
import org.gdal.ogr.*;

import java.util.ArrayList;

/**
 * DataIO
 * Reader and Writer for tradition shapefile data
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class ShapeReaderWriter {


    private int level = 8;
    private BaseCode.CodeType codeType = BaseCode.CodeType.EQCode;

    /**
     * read shapefile and convert to grid feature layer
     *
     * @param fileName
     * @param featureCodeType
     * @param featureCodeLevel
     * @return
     */
    public FeatureLayer ReadShapefile(String fileName, BaseCode.CodeType featureCodeType, int featureCodeLevel){

        level = featureCodeLevel;
        codeType = featureCodeType;

        // register all ogr driver
        ogr.RegisterAll();

        // support Chinese
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");
        gdal.SetConfigOption("SHAPE_ENCODING","");

        //open shapefile
        DataSource dataSource = ogr.Open(fileName);
        if(dataSource == null){
            System.out.println("open shapefile failed");
            assert false;
        }

        //get feature layer, a shapefile has only one layer
        Layer oLayer = dataSource.GetLayer(0);
        if(oLayer ==null){
            assert false;
        }
        oLayer.ResetReading();

        FeatureClass featureClass = getFeatureClass(oLayer.GetLayerDefn());

        FeatureLayer featureLayer;
        switch (featureClass.getShapeType()){
            case Polygon:
                featureLayer = new FeatureLayer("", level, codeType, BaseLayer.LayerType.PolygonLayer, featureClass);
                break;
            case Polyline:
                featureLayer = new FeatureLayer("", level, codeType, BaseLayer.LayerType.PolylineLyer, featureClass);
                break;
            case Point:
                featureLayer = new FeatureLayer("", level, codeType, BaseLayer.LayerType.PointLayer, featureClass);
                break;
            default:
                featureLayer = new FeatureLayer("", level, codeType, BaseLayer.LayerType.UnknownLayer, featureClass);
                break;
        }

        double[] oExt = oLayer.GetExtent();
        Extent ext = getCodeExtent(oExt);
        featureLayer.setExtend(ext);

        org.gdal.ogr.Feature oFeature;
        while (null != (oFeature = oLayer.GetNextFeature())){
            switch (oFeature.GetGeometryRef().GetGeometryType()){
                case ogr.wkbPoint:
                case ogr.wkbLineString:
                case ogr.wkbPolygon:
                    featureLayer.addFeature(getFeature(oFeature, featureClass));
                    break;
                case ogr.wkbMultiPoint:
                case ogr.wkbMultiLineString:
                case ogr.wkbMultiPolygon:
                    org.gdal.ogr.Geometry oGeometrys = oFeature.GetGeometryRef();
                    for(int i = 0; i < oGeometrys.GetGeometryCount(); ++i){
                        featureLayer.addFeature(getFeature(oFeature, i, featureClass));
                    }
                    break;
                default:
                    assert false;
                    break;
            }
        }

        dataSource.delete();

        return featureLayer;
    }

    /**
     * get feature type and attribute table header, to initialize featureclass
     *
     * @param oDefn
     * @return
     */
    private FeatureClass getFeatureClass(FeatureDefn oDefn){
        int geomType = oDefn.GetGeomType();

        ShapeType shapeType = ShapeType.Unknown;
        switch (geomType){
            case ogr.wkbPoint:
            case ogr.wkbMultiPoint:
                shapeType = ShapeType.Point;
                break;
            case ogr.wkbLineString:
            case ogr.wkbMultiLineString:
                shapeType = ShapeType.Polyline;
                break;
            case ogr.wkbPolygon:
            case ogr.wkbMultiPolygon:
                shapeType = ShapeType.Polygon;
                break;
            default:
                assert false;
                break;
        }
        FeatureClass featureClass = new FeatureClass(shapeType);

        //read and add field to featureclass
        for(int i = 0; i < oDefn.GetFieldCount(); ++i){
            FieldDefn oField = oDefn.GetFieldDefn(i);

            Field.FieldType fieldType = Field.FieldType.FieldUnknown;
            switch (oField.GetFieldType()){
                case ogr.OFTInteger:
                case ogr.OFTInteger64:
                    fieldType = Field.FieldType.FieldInt;
                    break;
                case ogr.OFTString:
                    fieldType = Field.FieldType.FieldString;
                    break;
                case ogr.OFTReal:
                    fieldType = Field.FieldType.FieldReal;
                    break;
                default:
                    break;
            }

            Field field = new Field(oField.GetName(), fieldType);
            featureClass.addField(field);
        }

        return featureClass;
    }

    /**
     * get code Extend by lon/lat Extent
     *
     * @param oExt minLon, maxLon, minLat, maxLat
     * @return
     */
    private Extent getCodeExtent(double[] oExt){
        Point topleft = new Point(SpericalCoord.ToCode(new SpericalCoord(oExt[0], oExt[3]), level, codeType, ElementType.GridCell));
        Point topright = new Point(SpericalCoord.ToCode(new SpericalCoord(oExt[1], oExt[3]), level, codeType, ElementType.GridCell));
        Point botleft = new Point(SpericalCoord.ToCode(new SpericalCoord(oExt[0], oExt[2]), level, codeType, ElementType.GridCell));
        Point botright = new Point(SpericalCoord.ToCode(new SpericalCoord(oExt[1], oExt[2]), level, codeType, ElementType.GridCell));

        return new Extent(topleft, topright, botleft, botright);
    }

    private Feature getFeature(org.gdal.ogr.Feature oFeature, int iGeom , FeatureClass featureClass){
        Feature feature = featureClass.CreatFeature();

        //get fields
        for(int i = 0; i < oFeature.GetFieldCount(); ++i){
            feature.setFieldValue(i, oFeature.GetFieldAsString(i));
        }

        //get geometry
        org.gdal.ogr.Geometry oGeometry = oFeature.GetGeometryRef().GetGeometryRef(iGeom);

        switch (oGeometry.GetGeometryType()){
            case ogr.wkbPoint:
                feature.setShape(getPoint(oGeometry));
                break;
            case ogr.wkbLineString:
                feature.setShape(getPolyline(oGeometry));
                break;
            case ogr.wkbPolygon:
                feature.setShape(getPolygon(oGeometry));
                break;
            default:
                assert false;
                break;
        }

        return feature;
    }

    private Feature getFeature(org.gdal.ogr.Feature oFeature, FeatureClass featureClass){
        Feature feature = featureClass.CreatFeature();

        //get fields
        for(int i = 0; i < oFeature.GetFieldCount(); ++i){
            feature.setFieldValue(i, oFeature.GetFieldAsString(i));
        }

        //get geometry
        org.gdal.ogr.Geometry oGeometry = oFeature.GetGeometryRef();

        switch (oGeometry.GetGeometryType()){
            case ogr.wkbPoint:
                feature.setShape(getPoint(oGeometry));
                break;
            case ogr.wkbLineString:
                feature.setShape(getPolyline(oGeometry));
                break;
            case ogr.wkbPolygon:
                feature.setShape(getPolygon(oGeometry));
                break;
            default:
                assert false;
                break;
        }

        return feature;
    }

    private Polygon getPolygon(org.gdal.ogr.Geometry oGeometry){
        int ringCount = oGeometry.GetGeometryCount();
        if(0 >= ringCount){
            return null;
        }

        Polygon polygon = new Polygon();

        double[] oExt = new double[4];
        oGeometry.GetEnvelope(oExt);
        polygon.setExtend(getCodeExtent(oExt));

        org.gdal.ogr.Geometry oExteriorRing = oGeometry.GetGeometryRef(0);
        Polyline exteriorRing = getPolyline(oExteriorRing);
        polygon.setExteriorRing(exteriorRing);

        if(1 < ringCount){
            for(int i = 1; i < oGeometry.GetGeometryCount(); ++i){
                org.gdal.ogr.Geometry oInnerRing = oGeometry.GetGeometryRef(i);
                Polyline innerRing = getPolyline(oInnerRing);
                polygon.addInnerRing(innerRing);
            }
        }

        return  polygon;
    }

    private Polyline getPolyline(org.gdal.ogr.Geometry oGeometry){
        Polyline polyline = new Polyline();
        double[] oExt = new double[4];
        oGeometry.GetEnvelope(oExt);
        polyline.setExtend(getCodeExtent(oExt));

        ArrayList<BaseCode> codes = new ArrayList<>();
        double lon, lat;
        for(int i = 0; i < oGeometry.GetPointCount(); ++i){
            lon = oGeometry.GetX(i);
            lat = oGeometry.GetY(i);

            BaseCode code;
            if(codeType == BaseCode.CodeType.EQCode)
                code = new EQCode();
            else
                code = new EHCode();

            code.fromSpericalCoord(new SpericalCoord(lon, lat), level, ElementType.GridCell);

            if(!codes.contains(code) || (i == oGeometry.GetPointCount() - 1)){
                codes.add(code);
                Point point = new Point(code);
                polyline.addPoint(point);
            }
        }

        return polyline;
    }

    private Point getPoint(org.gdal.ogr.Geometry oGeometry){
        double lon = oGeometry.GetX();
        double lat = oGeometry.GetY();

        BaseCode code;
        if(codeType == BaseCode.CodeType.EQCode)
            code = new EQCode();
        else
            code = new EHCode();

        code.fromSpericalCoord(new SpericalCoord(lon, lat), level, ElementType.GridCell);

        return new Point(code);
    }

    public void WriteShapefile(FeatureLayer layer, String fileName){}
}
