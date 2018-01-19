package cn.edu.njnu.earthgrid.renderer;

import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.geometry.Icosahedron;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.field.FieldBand;
import cn.edu.njnu.earthgrid.layer.FieldLayer;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;

import java.awt.*;
import java.util.ArrayList;

/**
 * renderer for grid field layer
 * only support EQTM
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class FieldRenderer extends BaseRenderer {
    private FieldLayer fieldLayer;

    private double radius;

    public FieldRenderer(FieldLayer fieldLayer) {
        this.fieldLayer = fieldLayer;
        this.radius = getRadius(fieldLayer.getLevel());
    }

    @Override
    public RenderableLayer getWWRenderableLayer() {
        if(null == fieldLayer)
            return null;

        RenderableLayer layer;
        if(fieldLayer.getBandNum() == 1){
            layer = DEMRenderer();
        }
        else {
            layer = ImageRenderer();
        }

        return layer;
    }

    private RenderableLayer ImageRenderer(){
        if(fieldLayer.getBandNum() != 3){
            return null;
        }

        final FieldBand rBand = fieldLayer.getBands(0);
        final FieldBand gBand = fieldLayer.getBands(1);
        final FieldBand bBand = fieldLayer.getBands(2);

        RenderableLayer renderableLayer = new RenderableLayer();
        renderableLayer.setName(fieldLayer.getName());
        renderableLayer.setPickEnabled(false);
        double elevation = 30000.;


        for(int domID = 0; domID < rBand.getMBSCount(); ++domID){
            FieldBand.MinBoundSeg mbs = rBand.getMBS(domID);
            EQCode offset = mbs.getOffset();
            long size = mbs.getSize();

            EQCode code = new EQCode(offset);

            for(int i = 0; i < size / 2; ++i){
                int rUpperAttr = (int)rBand.getAttribute(domID, i * 2);
                int gUpperAttr = (int)gBand.getAttribute(domID, i * 2);
                int bUpperAttr = (int)bBand.getAttribute(domID, i * 2);
                int rLowerAttr = (int)rBand.getAttribute(domID, i * 2 + 1);
                int gLowerAttr = (int)gBand.getAttribute(domID, i * 2 + 1);
                int bLowerAttr = (int)bBand.getAttribute(domID, i * 2 + 1);

                code.setMorton(offset.getMorton() + i);
                SpericalCoord[] v = code.toDiamond().toSpericalCoord();

                //draw upper trigon cell
                ArrayList<Position> uPositions = new ArrayList<Position>();
                uPositions.add(new Position(Angle.fromDegrees(v[0].getLatitude()),Angle.fromDegrees(v[0].getLongitude()),elevation));
                uPositions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
                uPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Polygon upperTrigon = new Polygon(uPositions);
                SetDefaultMaterial(upperTrigon, rUpperAttr, gUpperAttr, bUpperAttr);
                renderableLayer.addRenderable(upperTrigon);

                //draw lower trigon cell
                ArrayList<Position> lPositions = new ArrayList<Position>();
                lPositions.add(new Position(Angle.fromDegrees(v[2].getLatitude()),Angle.fromDegrees(v[2].getLongitude()),elevation));
                lPositions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
                lPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Polygon lowerTrigon = new Polygon(lPositions);
                SetDefaultMaterial(lowerTrigon, rLowerAttr, gLowerAttr, bLowerAttr);
                renderableLayer.addRenderable(lowerTrigon);
            }

        }

        return renderableLayer;
    }

    private RenderableLayer DEMRenderer(){
        RenderableLayer layer;
        switch (fieldLayer.getBands(0).getElementType()){
            case GridNode:
                layer = NodeRenderer();
                break;
            case GridEdge:
                layer = EdgeRenderer();
                break;
            case GridCell:
                layer = CellRenderer();
                break;
            default:
                layer = null;
                break;
        }

        return layer;
    }

    private RenderableLayer NodeRenderer(){
        final FieldBand band = fieldLayer.getBands(0);
        if(null == band)
            return null;

        RenderableLayer layer = new RenderableLayer();
        layer.setName(fieldLayer.getName());
        layer.setPickEnabled(false);
        double elevation = 30000.;

        float max = band.getMax();
        float min = band.getMin();

        for(int domID = 0; domID < band.getMBSCount(); ++domID){
            FieldBand.MinBoundSeg mbs = band.getMBS(domID);
            EQCode offset = mbs.getOffset();
            long size = mbs.getSize();

            EQCode code = new EQCode(offset);

            for(int i = 0; i < size; ++i){
                float attr = band.getAttribute(domID, i);

                code.setMorton(offset.getMorton() + i);
                SpericalCoord node = code.toSpericalCoord();

                CappedCylinder nodeCylinder = new CappedCylinder();
                nodeCylinder.setCenter(LatLon.fromDegrees(node.getLatitude(), node.getLongitude()));
                nodeCylinder.setRadius(this.radius);
                nodeCylinder.setAltitudes(0d, 30000d);
                nodeCylinder.setTerrainConforming(true, true);

                nodeCylinder.getAttributes().setDrawOutline(false);
                nodeCylinder.getAttributes().setInteriorMaterial(new Material(MathUtil.GrayToRGB((attr - min)/(max - min))));

                layer.addRenderable(nodeCylinder);
            }

        }

        return layer;
    }

    private RenderableLayer EdgeRenderer(){
        final FieldBand band = fieldLayer.getBands(0);
        if(null == band)
            return null;

        RenderableLayer layer = new RenderableLayer();
        layer.setName(fieldLayer.getName());
        layer.setPickEnabled(false);
        double elevation = 30000.;

        float max = band.getMax();
        float min = band.getMin();

        for(int domID = 0; domID < band.getMBSCount(); ++domID){
            FieldBand.MinBoundSeg mbs = band.getMBS(domID);
            EQCode offset = mbs.getOffset();
            long size = mbs.getSize();

            EQCode code = new EQCode(offset);

            for(int i = 0; i < size / 3; ++i){
                float nwAttr = band.getAttribute(domID, i * 3);
                float midAttr = band.getAttribute(domID, i * 3 + 1);
                float swAttr = band.getAttribute(domID, i * 3 + 2);

                code.setMorton(offset.getMorton() + i);
                SpericalCoord[] v = code.toDiamond().toSpericalCoord();

                //nw edge
                //draw upper trigon cell
                ArrayList<Position> nwPositions = new ArrayList<Position>();
                nwPositions.add(new Position(Angle.fromDegrees(v[0].getLatitude()),Angle.fromDegrees(v[0].getLongitude()),elevation));
                nwPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Path nwEdge = new Path(nwPositions);
                SetDefaultMaterial(nwEdge, (nwAttr - min)/(max - min));
                layer.addRenderable(nwEdge);

                //mid edge
                ArrayList<Position> midPositions = new ArrayList<Position>();
                midPositions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
                midPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Path midEdge = new Path(midPositions);
                SetDefaultMaterial(midEdge, (midAttr - min)/(max - min));
                layer.addRenderable(midEdge);

                //sw edge
                ArrayList<Position> swPositions = new ArrayList<Position>();
                swPositions.add(new Position(Angle.fromDegrees(v[2].getLatitude()),Angle.fromDegrees(v[2].getLongitude()),elevation));
                swPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Path swEdge = new Path(swPositions);
                SetDefaultMaterial(swEdge, (swAttr - min)/(max - min));
                layer.addRenderable(swEdge);
            }

        }

        return layer;
    }

    private RenderableLayer CellRenderer(){
        final FieldBand band = fieldLayer.getBands(0);
        if(null == band)
            return null;

        RenderableLayer layer = new RenderableLayer();
        layer.setName(fieldLayer.getName());
        layer.setPickEnabled(false);
        double elevation = 30000.;

        float max = band.getMax();
        float min = band.getMin();

        for(int domID = 0; domID < band.getMBSCount(); ++domID){
            FieldBand.MinBoundSeg mbs = band.getMBS(domID);
            EQCode offset = mbs.getOffset();
            long size = mbs.getSize();

            EQCode code = new EQCode(offset);

            for(int i = 0; i < size / 2; ++i){
                float uAttr = band.getAttribute(domID, i * 2);
                float lAttr = band.getAttribute(domID, i * 2 + 1);

                code.setMorton(offset.getMorton() + i);
                SpericalCoord[] v = code.toDiamond().toSpericalCoord();

                //draw upper trigon cell
                ArrayList<Position> uPositions = new ArrayList<Position>();
                uPositions.add(new Position(Angle.fromDegrees(v[0].getLatitude()),Angle.fromDegrees(v[0].getLongitude()),elevation));
                uPositions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
                uPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Polygon upperTrigon = new Polygon(uPositions);
                SetDefaultMaterial(upperTrigon, (uAttr - min)/(max - min));
                layer.addRenderable(upperTrigon);

                //draw lower trigon cell
                ArrayList<Position> lPositions = new ArrayList<Position>();
                lPositions.add(new Position(Angle.fromDegrees(v[2].getLatitude()),Angle.fromDegrees(v[2].getLongitude()),elevation));
                lPositions.add(new Position(Angle.fromDegrees(v[1].getLatitude()),Angle.fromDegrees(v[1].getLongitude()),elevation));
                lPositions.add(new Position(Angle.fromDegrees(v[3].getLatitude()),Angle.fromDegrees(v[3].getLongitude()),elevation));

                Polygon lowerTrigon = new Polygon(lPositions);
                SetDefaultMaterial(lowerTrigon, (lAttr - min)/(max - min));
                layer.addRenderable(lowerTrigon);
            }

        }

        return layer;
    }

    private void SetDefaultMaterial(AbstractShape shape, float gray){
        Color color = MathUtil.GrayToRGB(gray);
        ShapeAttributes attributes = new BasicShapeAttributes();
        if(shape instanceof Polygon){
            attributes.setDrawOutline(false);
            attributes.setInteriorMaterial(new Material(color));
        }
        else if(shape instanceof Path){
            attributes.setOutlineMaterial(new Material(color));
            attributes.setOutlineWidth(1d);
            shape.setVisible(true);
            ((Path) shape).setPathType(AVKey.GREAT_CIRCLE);
        }
        shape.setAttributes(attributes);
        shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
    }

    private void SetDefaultMaterial(AbstractShape shape, int r, int g, int b){
        if(r > 255 || g > 255 || b > 255){
            r = g = b = 255;
        }

        Color color = new Color(r, g, b);
        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setDrawOutline(false);
        attributes.setInteriorMaterial(new Material(color));
        shape.setAttributes(attributes);
        shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
    }

    private double getRadius(int level){
        Icosahedron ico = Icosahedron.getInstance();
        double arcLen = MathUtil.ArcLength(ico.p(0), ico.p(2));
        return  arcLen/MathUtil.Pow(2, level + 1);
    }
}
