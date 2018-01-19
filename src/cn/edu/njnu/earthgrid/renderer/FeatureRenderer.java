package cn.edu.njnu.earthgrid.renderer;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.feature.*;
import cn.edu.njnu.earthgrid.feature.Point;
import cn.edu.njnu.earthgrid.layer.FeatureLayer;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwindx.examples.util.RandomShapeAttributes;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * renderer for grid feature layer
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class FeatureRenderer extends BaseRenderer {
    private FeatureLayer featureLayer;

    public FeatureRenderer(FeatureLayer featureLayer) {
        this.featureLayer = featureLayer;
    }

    @Override
    public RenderableLayer getWWRenderableLayer() {
        RenderableLayer renderableLayer = new RenderableLayer();
        renderableLayer.setName(featureLayer.getName());
        renderableLayer.setPickEnabled(false);

        if(ShapeType.Point == featureLayer.getFeatureClass().getShapeType()){
            PointLayerRenderer(renderableLayer);
        }
        else if(ShapeType.Polyline == featureLayer.getFeatureClass().getShapeType()){
            PolylineLayerRenderer(renderableLayer);
        }
        else if(ShapeType.Polygon == featureLayer.getFeatureClass().getShapeType()){
            PolygonLayerRenderer(renderableLayer);
        }

        return renderableLayer;
    }

    private void PointLayerRenderer(RenderableLayer renderableLayer){
        final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

        for(int pointID = 0; pointID < featureLayer.getFeatureCount(); ++pointID){
            Point point = (Point) featureLayer.getFeature(pointID).getShape();
            BaseCode position = point.getPosition();
            ShapeAttributes arrr = randomAttrs.nextAttributes().asShapeAttributes();
            renderableLayer.addRenderable(getWWPolygon(position, arrr));
        }
    }

    private void PolylineLayerRenderer(RenderableLayer renderableLayer){
        final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

        for(int polylineID = 0; polylineID < featureLayer.getFeatureCount(); ++polylineID){
            Polyline polyline = (Polyline) featureLayer.getFeature(polylineID).getShape();

            ArrayList<BaseCode> codes = MathUtil.getCodesInPolyline(polyline);

            ShapeAttributes arrr = randomAttrs.nextAttributes().asShapeAttributes();

            for(int codeID = 0; codeID < codes.size(); ++codeID){
                BaseCode code = codes.get(codeID);
                renderableLayer.addRenderable(getWWPolygon(code, arrr));
            }
        }
    }

    /**
     * only support EQTM
     *
     * @param renderableLayer
     */
    private void PolygonLayerRenderer(RenderableLayer renderableLayer){

        final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

        ArrayList<ShapeAttributes> attrs = new ArrayList<>();
        for(int polygonID = 0; polygonID < featureLayer.getFeatureCount(); ++polygonID){
            ShapeAttributes att = randomAttrs.nextAttributes().asShapeAttributes();
            PolygonRenderer(renderableLayer, featureLayer.getFeature(polygonID),att);
        }

//        int cellCount = (int) (MathUtil.Pow(2, featureLayer.getLevel()) * MathUtil.Pow(2, featureLayer.getLevel()));
//        for(int domID = 0; domID < 10; ++domID){
//            EQCode code = new EQCode(domID, 4, featureLayer.getLevel(), 0);
//
//            for(int morID = 0; morID < cellCount; ++morID){
//                code.setMorton(morID);
//                code.setElementCode(4);
//
//                cn.edu.njnu.earthgrid.feature.Polygon polygon;
//
//                for(int polygonID = 0; polygonID < featureLayer.getFeatureCount(); ++polygonID){
//                    polygon = (cn.edu.njnu.earthgrid.feature.Polygon) featureLayer.getFeature(polygonID).getShape();
//                    if(MathUtil.Contain(polygon, code)){
//                        System.out.println(domID + "," + morID + "U: " + polygonID);
//                        renderableLayer.addRenderable(getWWPolygon(code, attrs.get(polygonID)));
//                        break;
//                    }
//                }
//
//                code.setElementCode(5);
//                for(int polygonID = 0; polygonID < featureLayer.getFeatureCount(); ++polygonID){
//                    polygon = (cn.edu.njnu.earthgrid.feature.Polygon) featureLayer.getFeature(polygonID).getShape();
//                    if(MathUtil.Contain(polygon, code)){
//                        System.out.println(domID + "," + morID + "L: " + polygonID);
//                        renderableLayer.addRenderable(getWWPolygon(code, attrs.get(polygonID)));
//                        break;
//                    }
//                }
//            }
//        }
    }

    private void PolygonRenderer(RenderableLayer renderableLayer, Feature feature, ShapeAttributes attr){
        if(ShapeType.Polygon != feature.getShape().getShapeType())
            return;

        cn.edu.njnu.earthgrid.feature.Polygon polygon = (cn.edu.njnu.earthgrid.feature.Polygon) feature.getShape();

        PolygonDecode polygonDecode = new PolygonDecode(polygon);

        int cellCount = (int) (MathUtil.Pow(2, featureLayer.getLevel()) * MathUtil.Pow(2, featureLayer.getLevel()));

        for(int domID : polygonDecode.getDomainSet()){
            EQCode code = new EQCode(domID, 4, featureLayer.getLevel(), 0);
            SpericalCoord sc;

            for(int morID = 0; morID < cellCount; ++morID){
                //upper cell
                code.setMorton(morID);
                code.setElementCode(4);
                if(polygonDecode.Contain(code))
                    renderableLayer.addRenderable(getWWPolygon(code, attr));

                //lower cell
                code.setElementCode(5);
                if(polygonDecode.Contain(code))
                    renderableLayer.addRenderable(getWWPolygon(code, attr));
            }
        }
    }

    private Polygon getWWPolygon(BaseCode code, ShapeAttributes attr){
        SpericalCoord[] v = code.toTrigon().toSpericalCoord();
        double elevation = 30000.;

        ArrayList<Position> positions = new ArrayList<Position>();
        positions.add(new Position(Angle.fromDegrees(v[0].getLatitude()),Angle.fromDegrees(v[0].getLongitude()),elevation));
        positions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
        positions.add(new Position(Angle.fromDegrees(v[2].getLatitude()),Angle.fromDegrees(v[2].getLongitude()),elevation));

        Polygon trigon = new Polygon(positions);
        trigon.setAttributes(attr);
        trigon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);

        return trigon;
    }

    private class PolygonDecode{
        private ArrayList<SpericalCoord> extRingPoints;
        private ArrayList<ArrayList<SpericalCoord>> innRingsPoints;
        private Set<Integer> domainSet;
        private final Extent ext;

        public PolygonDecode(cn.edu.njnu.earthgrid.feature.Polygon polygon) {
            this.extRingPoints = new ArrayList<>();
            this.innRingsPoints = new ArrayList<>();
            this.domainSet = new HashSet<>();
            this.ext = polygon.getExtend();

            MathUtil.DecodePolyline(polygon.getExteriorRing(), extRingPoints, domainSet);

            for(int ringID = 0; ringID < polygon.getRingNum() - 1; ++ringID){
                Polyline innerRing = polygon.getInnerRing(ringID);
                ArrayList<SpericalCoord> innRingPoints = new ArrayList<>();
                MathUtil.DecodePolyline(innerRing, innRingPoints, domainSet);
                innRingsPoints.add(innRingPoints);
            }
        }

        public Set<Integer> getDomainSet() {
            return domainSet;
        }

        public boolean Contain(BaseCode code){
            SpericalCoord sc = code.toSpericalCoord();
            if(!ext.Contains(sc))
                return false;

            if(!Contain(extRingPoints, sc))
                return false;

            for(int i = 0; i < innRingsPoints.size(); ++i){
                if(Contain(innRingsPoints.get(i), sc))
                    return false;
            }

            return true;
        }

        private boolean Contain(ArrayList<SpericalCoord> ringPoints, SpericalCoord sc){
            int wn = 0;
            SpericalCoord sc1, sc2;

            for(int pointID = 0; pointID < ringPoints.size() - 1; ++pointID){
                sc1 = ringPoints.get(pointID);
                sc2 = ringPoints.get(pointID + 1);

                if (sc1.getLatitude() <= sc.getLatitude()) {
                    if (sc2.getLatitude() > sc.getLatitude()) { // upward
                        if (MathUtil.PointLeftLine(sc, sc1, sc2) > 0) {
                            ++wn;
                        }
                    }
                }
                else {
                    if (sc2.getLatitude() <= sc.getLatitude()) { // downward
                        if (MathUtil.PointLeftLine(sc, sc1, sc2) < 0) {
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
    }

}
