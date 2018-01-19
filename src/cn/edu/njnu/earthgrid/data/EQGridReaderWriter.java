package cn.edu.njnu.earthgrid.data;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.feature.*;
import cn.edu.njnu.earthgrid.field.FieldBand;
import cn.edu.njnu.earthgrid.layer.BaseLayer;
import cn.edu.njnu.earthgrid.layer.FeatureLayer;
import cn.edu.njnu.earthgrid.layer.FieldLayer;

import java.io.*;
import java.util.ArrayList;

/**
 * DataIO
 * Reader and Writer for grid layer
 * !!! old format of EQTM file need to modify
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class EQGridReaderWriter {

    private static BaseCode.CodeType codeType = BaseCode.CodeType.EQCode;

    /**
     * read grid field layer file
     *
     * @param fileName field file path
     * @return field layer
     */
    public static FieldLayer ReadFieldFile_OLD(String fileName) {
        FieldLayer fieldLayer = null;
        String layerName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("."));

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            if(!reader.readLine().equals("eqtm_header")){
                System.out.println("error file!");
                assert false;
            }

            String[] lineStr;

            //read file header
            lineStr = reader.readLine().split(" ");
            String format = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            String dataType = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            String eleTypeStr = lineStr[lineStr.length - 1];
            ElementType elementType;
            if(eleTypeStr.equals("cell")){
                elementType = ElementType.GridCell;
            }
            else if(eleTypeStr.equals("edge")){
                elementType = ElementType.GridEdge;
            }
            else if(eleTypeStr.equals("node")){
                elementType = ElementType.GridNode;
            }
            else {
                elementType = ElementType.NoDef;
            }

            lineStr = reader.readLine().split(" ");
            int level = Integer.parseInt(lineStr[lineStr.length - 1]);

            lineStr = reader.readLine().split(" ");
            int bandNum = Integer.parseInt(lineStr[lineStr.length - 1]);

            lineStr = reader.readLine().split(" ");
            float nodata = Float.parseFloat(lineStr[lineStr.length - 1]);

            reader.readLine();
            //end read header


            ArrayList<FieldBand> bands = new ArrayList<>();
            for(int bandID = 0; bandID < bandNum; ++bandID) {

                FieldBand band = new FieldBand(nodata, elementType);

                //read mbs
                ArrayList<EQCode> offsetList = new ArrayList<>();

                while (!(lineStr = reader.readLine().split(","))[0].equals("end_mbs")) {
                    int domainID = (int) MathUtil.BinaryToDecimal(lineStr[0].substring(0, 4));
                    int elementCode = (int) MathUtil.BinaryToDecimal(lineStr[0].substring(4, 8));
                    long morton = MathUtil.QuaternaryToDecimal(lineStr[0].substring(8));

                    EQCode offset = new EQCode(domainID, elementCode, level, morton);
                    int size = Integer.parseInt(lineStr[1]);
                    offsetList.add(offset);

                    band.setMBS(offset, size);
                }
                //end read mbs

                //read body
                float min = 0, max = 0;
                for (int i = 0; i < offsetList.size(); ++i) {
                    int domain = offsetList.get(i).getDomainID();
                    int pos = 0;
                    lineStr = reader.readLine().split("\\(|\\)|,");
                    for (String attrStr : lineStr) {
                        if (attrStr.equals("")) {
                            continue;
                        }
                        float attr = Float.parseFloat(attrStr);
                        if(attr > nodata){
                            max = Math.max(max, attr);
                            min = Math.min(min, attr);
                        }
                        band.addAttribute(domain, pos,attr);
                        ++pos;

                    }
                }

                band.setMax(max);
                band.setMin(min);
                bands.add(band);
                //end read body
            }


            fieldLayer = new FieldLayer(layerName, level, BaseCode.CodeType.EQCode, bands);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return fieldLayer;
    }

    /**
     * read grid field layer file
     *
     * @param fileName field file path
     * @return field layer
     */
    public static FieldLayer ReadFieldFile(String fileName) {
        FieldLayer fieldLayer = null;
        String layerName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("."));

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            if(!reader.readLine().equals("eqtm_header")){
                System.out.println("error file!");
                assert false;
            }

            String[] lineStr;

            //read file header
            lineStr = reader.readLine().split(" ");
            String format = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            String dataType = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            int level = Integer.parseInt(lineStr[lineStr.length - 1]);

            lineStr = reader.readLine().split(" ");
            int bandNum = Integer.parseInt(lineStr[lineStr.length - 1]);

            reader.readLine();      //read "end_header"
            reader.readLine();      //read void line

            //read bands
            ArrayList<FieldBand> bands = new ArrayList<>();
            for(int bandID = 0; bandID < bandNum; ++bandID) {
                lineStr = reader.readLine().split(" ");
                if(bandID != Integer.parseInt(lineStr[lineStr.length - 1])){
                    assert false;
                }

                lineStr = reader.readLine().split(" ");
                String eleTypeStr = lineStr[lineStr.length - 1];
                ElementType elementType;
                if(eleTypeStr.equals("cell")){
                    elementType = ElementType.GridCell;
                }
                else if(eleTypeStr.equals("edge")){
                    elementType = ElementType.GridEdge;
                }
                else if(eleTypeStr.equals("node")){
                    elementType = ElementType.GridNode;
                }
                else {
                    elementType = ElementType.NoDef;
                }

                lineStr = reader.readLine().split(" ");
                float max = Float.parseFloat(lineStr[lineStr.length - 1]);

                lineStr = reader.readLine().split(" ");
                float min = Float.parseFloat(lineStr[lineStr.length - 1]);

                lineStr = reader.readLine().split(" ");
                float nodata = Float.parseFloat(lineStr[lineStr.length - 1]);

                //end read header

                FieldBand band = new FieldBand(nodata, elementType);
                band.setMin(min);
                band.setMax(max);

                //read mbs
                if(reader.readLine() != "mbs"){
                    assert false;
                }

                ArrayList<EQCode> offsetList = new ArrayList<>();

                while (!(lineStr = reader.readLine().split(","))[0].equals("end_mbs")) {
                    int domainID = (int) MathUtil.BinaryToDecimal(lineStr[0].substring(0, 4));
                    int elementCode = (int) MathUtil.BinaryToDecimal(lineStr[0].substring(4, 8));
                    long morton = MathUtil.QuaternaryToDecimal(lineStr[0].substring(8));

                    EQCode offset = new EQCode(domainID, elementCode, level, morton);
                    int size = Integer.parseInt(lineStr[1]);
                    offsetList.add(offset);

                    band.setMBS(offset, size);
                }
                //end read mbs

                //read body
                for (int i = 0; i < offsetList.size(); ++i) {
                    int domain = offsetList.get(i).getDomainID();
                    int pos = 0;
                    lineStr = reader.readLine().split("\\(|\\)|,");
                    for (String attrStr : lineStr) {
                        if (attrStr.equals("")) {
                            continue;
                        }
                        float attr = Float.parseFloat(attrStr);
                        band.addAttribute(domain, pos,attr);
                        ++pos;
                    }
                }

                bands.add(band);
                reader.readLine();
                //end read body
            }


            fieldLayer = new FieldLayer(layerName, level, codeType, bands);

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return fieldLayer;
    }

    /**
     * write grid field layer to file
     *
     * @param layer    field layer
     * @param fileName field file path
     */
    public static void WriteFieldFile(FieldLayer layer, String fileName) {
        File fieldFile = new File(fileName);
        BufferedWriter writer = null;

        try {
            if(!fieldFile.exists()) {
                fieldFile.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(fieldFile));

            //write header
            writer.write("eqtm_header\n");
            writer.write("format ascii\n");
            writer.write("data_type raster\n");
            writer.write("level " + layer.getLevel() + " \n");
            writer.write("band_num " + layer.getBandNum() + " \n");
            writer.write("end_header\n\n");
            writer.flush();

            //write bands
            for(int bandID = 0; bandID < layer.getBandNum(); ++bandID){
                FieldBand band = layer.getBands(bandID);
                writer.write("band " + (bandID + 1) + "\n");
                writer.write("element_type ");
                if(band.getElementType() == ElementType.GridCell)
                    writer.write("cell\n");
                else  if(band.getElementType() == ElementType.GridEdge)
                    writer.write("edge\n");
                else if(band.getElementType() == ElementType.GridNode)
                    writer.write("node\n");

                writer.write("max " + band.getMax() + "\n");
                writer.write("min " + band.getMin() + "\n");
                writer.write("nodata " + band.getNoData() + " \n");

                //write mbs
                writer.write("mbs\n");
                for(int domID = 0; domID < band.getMBSCount(); ++domID){
                    FieldBand.MinBoundSeg mbs = band.getMBS(domID);
                    EQCode offset = mbs.getOffset();
                    writer.write(offset.toString() + "," + mbs.getSize() + "\n");
                }
                writer.write("end_mbs\n");
                writer.flush();

                for(int domID = 0; domID < band.getMBSCount(); ++domID){
                    writer.write("(");
                    FieldBand.MinBoundSeg mbs = band.getMBS(domID);

                    for(int pos = 0; pos < mbs.getSize(); ++pos){
                        writer.write(band.getAttribute(domID, pos) + ",");
                    }

                    writer.write(")\n");
                }

                writer.write("\n");
            }

            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    /**
     * read grid feature layer file
     *
     * @param fileName feature file path
     * @return feature layer
     */
    public static FeatureLayer ReadFeatureFile(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String attrFileName = fileName.substring(0, pos) + ".dggsa";

        ArrayList<ArrayList<String>> attrs = new ArrayList<>();
        FeatureClass featureClass = ReadAttributeFile(attrFileName, attrs);

        FeatureLayer featureLayer = ReadMainFile(fileName, featureClass, attrs);

        return featureLayer;
    }

    private static FeatureClass ReadAttributeFile(String fileName, ArrayList<ArrayList<String>> attrs){
        FeatureClass featureClass = null;

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            if(!reader.readLine().equals("eqtm_header")){
                System.out.println("error file!");
                assert false;
            }

            String[] lineStr;

            //read file header
            lineStr = reader.readLine().split(" ");
            String format = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            String dataType = lineStr[lineStr.length - 1];
            ShapeType shapeType = ShapeType.Unknown;
            switch (dataType.split("_")[0]){
                case "Point":
                    shapeType = ShapeType.Point;
                    break;
                case "Polyline":
                    shapeType = ShapeType.Polyline;
                    break;
                case  "Polygon":
                    shapeType = ShapeType.Polygon;
                    break;
                default:
                    break;
            }

            lineStr = reader.readLine().split(" ");
            int fieldCount = Integer.parseInt(lineStr[1]);

            lineStr = reader.readLine().split(" ");
            int featureCount = Integer.parseInt(lineStr[1]);

            reader.readLine();     //read end_header
            reader.readLine();     //read void line

            featureClass = new FeatureClass(shapeType);

            //read field name and type
            for(int i = 0; i < fieldCount; ++i){
                lineStr = reader.readLine().split(" ");
                String fieldName = lineStr[0];
                Field.FieldType fieldType = Field.FieldType.FieldUnknown;
                switch (lineStr[1]){
                    case "FieldInt":
                        fieldType = Field.FieldType.FieldInt;
                        break;
                    case "FieldReal":
                        fieldType = Field.FieldType.FieldReal;
                        break;
                    case "FieldString":
                        fieldType = Field.FieldType.FieldString;
                    default:
                        break;
                }

                Field field = new Field(fieldName, fieldType);
                featureClass.addField(field);
            }

            reader.readLine();   //read end_field_name
            reader.readLine();   //read void line

            //read field values of feature
            for(int i = 0; i < featureCount; ++i){
                lineStr = reader.readLine().split("\\(");
                ArrayList<String> featureAttr = new ArrayList<>();
                for(String s : lineStr){
                    if(s.equals(""))
                        continue;
                    featureAttr.add(s.substring(0, s.length() - 2));      //remove ( and )
                }
                attrs.add(featureAttr);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return featureClass;
    }

    private static FeatureLayer ReadMainFile(String fileName, FeatureClass featureClass, ArrayList<ArrayList<String>> attrs){
        FeatureLayer featureLayer = null;
        String layerName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("."));

        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            if(!reader.readLine().equals("eqtm_header")){
                System.out.println("error file!");
                assert false;
            }

            String[] lineStr;

            //read file header
            lineStr = reader.readLine().split(" ");
            String format = lineStr[lineStr.length - 1];

            lineStr = reader.readLine().split(" ");
            String dataTypeStr = lineStr[lineStr.length - 1];
            ShapeType shapeType = ShapeType.Unknown;
            switch (dataTypeStr){
                case "Polygon":
                    shapeType = ShapeType.Polygon;
                    break;
                case "Polyline":
                    shapeType = ShapeType.Polyline;
                    break;
                case "Point":
                    shapeType = ShapeType.Point;
                    break;
            }

            lineStr = reader.readLine().split(" ");
            String eleTypeStr = lineStr[lineStr.length - 1];
            ElementType elementType;
            if(eleTypeStr.equals("cell")){
                elementType = ElementType.GridCell;
            }
            else{
                assert false;
            }

            lineStr = reader.readLine().split(" ");
            int level = Integer.parseInt(lineStr[lineStr.length - 1]);

            lineStr = reader.readLine().split(" ");
            int featureCount = Integer.parseInt(lineStr[1]);

            //read end_header
            reader.readLine();

            //read mbr, waitng to realize
            lineStr = reader.readLine().split(" ");
            Extent layerExt = Extent.FromString(lineStr[1]);

            reader.readLine();  //read end_mbr

            if(ShapeType.Polygon == shapeType){
                featureLayer = new FeatureLayer(layerName, level, codeType, BaseLayer.LayerType.PolygonLayer, featureClass);
                featureLayer.setExtend(layerExt);
                for(int polygonID = 0; polygonID < featureCount; ++polygonID){
                    String polygonStr = reader.readLine();
                    Feature polygonFeature = featureClass.CreatFeature();
                    polygonFeature.setShape(Polygon.FromString(polygonStr));
                    for(int fieldID = 0; fieldID < featureClass.getFieldCount(); ++fieldID){
                        polygonFeature.setFieldValue(fieldID, attrs.get(polygonID).get(fieldID));
                    }

                    featureLayer.addFeature(polygonFeature);
                }
            }
            else if(ShapeType.Polyline == shapeType){
                featureLayer = new FeatureLayer(layerName, level, codeType, BaseLayer.LayerType.PolylineLyer, featureClass);
                for(int polylineID = 0; polylineID < featureCount; ++polylineID){
                    String polylineStr = reader.readLine();
                    Feature polylineFeature = featureClass.CreatFeature();
                    polylineFeature.setShape(Polyline.FormString(polylineStr));
                    for(int fieldID = 0; fieldID < featureClass.getFieldCount(); ++fieldID){
                        polylineFeature.setFieldValue(fieldID, attrs.get(polylineID).get(fieldID));
                    }

                    featureLayer.addFeature(polylineFeature);
                }
            }
            else if(ShapeType.Point == shapeType){
                featureLayer = new FeatureLayer(layerName, level, codeType, BaseLayer.LayerType.PointLayer, featureClass);
                lineStr = reader.readLine().split(",");
                for(int pointID = 0; pointID < lineStr.length; ++pointID){
                    Feature pointFeature = featureClass.CreatFeature();
                    pointFeature.setShape(Point.FromString(lineStr[pointID]));
                    for(int fieldID = 0; fieldID < featureClass.getFieldCount(); ++fieldID){
                        pointFeature.setFieldValue(fieldID, attrs.get(pointID).get(fieldID));
                    }

                    featureLayer.addFeature(pointFeature);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return featureLayer;
    }


    /**
     * write grid feature layer to file
     *
     * @param layer    feature layer
     * @param fileName feature file path
     */
    public static void WriteFeatureFile(FeatureLayer layer, String fileName) {
        WriteMainFile(layer, fileName + ".dggs");
        WriteAttributeFile(layer, fileName + ".dggsa");
    }

    private static void WriteMainFile(FeatureLayer layer, String fileName) {
        File file = new File(fileName);
        BufferedWriter writer = null;

        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            writer.write("eqtm_header\n");
            writer.write("format ascii\n");
            writer.write("data_type " + layer.getFeatureClass().getShapeType() + "\n");
            writer.write("element_type cell\n");
            writer.write("level " + layer.getLevel() + " \n");
            writer.write("feature_num " + layer.getFeatureCount() + " \n");
            writer.write("end_header\n");

            writer.write("mbr (" + layer.getExtend().toString() + ")\n");
            writer.write("end_mbs\n");

            if(layer.getFeatureClass().getShapeType() == ShapeType.Polygon){
                for(int featureID = 0; featureID < layer.getFeatureCount(); ++featureID){
                    Feature feature = layer.getFeature(featureID);

                    Polygon  polygon = (Polygon) feature.getShape();

                    writer.write("(" + polygon.getExtend().toString() + ") ");

                    writer.write(polygon.getExteriorRing().toString() + " ");

                    for(int innerID = 0; innerID < polygon.getRingNum() - 1; ++innerID){
                        writer.write(polygon.getInnerRing(innerID).toString() + " ");
                    }

                    writer.write("\n");
                }
            }
            else  if(layer.getFeatureClass().getShapeType() == ShapeType.Polyline){
                for(int featureID = 0; featureID < layer.getFeatureCount(); ++featureID){
                    Feature feature = layer.getFeature(featureID);

                    Polyline polyline = (Polyline) feature.getShape();

                    writer.write(polyline.toString());

                    writer.write("\n");
                }
            }
            else if(layer.getFeatureClass().getShapeType() == ShapeType.Point){
                for(int featureID = 0; featureID < layer.getFeatureCount(); ++featureID){
                    Feature feature = layer.getFeature(featureID);
                    Point point = (Point) feature.getShape();
                    writer.write(point.toString() + ",");
                }
            }

            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    private static void WriteAttributeFile(FeatureLayer layer, String fileName){
        File file = new File(fileName);
        BufferedWriter writer = null;

        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            writer.write("eqtm_header\n");
            writer.write("format ascii\n");
            writer.write("data_type " + layer.getFeatureClass().getShapeType() + "_attribute\n");
            writer.write("field_num " + layer.getFeatureClass().getFieldCount() + "\n");
            writer.write("feature_num " + layer.getFeatureCount() + "\n");
            writer.write("end_header\n\n");

            ArrayList<Field> fields = layer.getFeatureClass().getFields();
            for(int i = 0; i < fields.size(); ++i){
                writer.write(fields.get(i).getFieldName() + " " + fields.get(i).getFieldType() + "\n");
            }
            writer.write("end_field_name\n\n");

            for(int featureID = 0; featureID < layer.getFeatureCount(); ++featureID){
                Feature feature = layer.getFeature(featureID);
                for(int i = 0; i < feature.getFieldCount(); ++i){
                    writer.write("(" + feature.getFieldValue(i) + ") ");
                }
                writer.write("\n");
            }

            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }
        }
    }
}
