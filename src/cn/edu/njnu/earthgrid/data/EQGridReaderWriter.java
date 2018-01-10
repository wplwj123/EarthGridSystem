package cn.edu.njnu.earthgrid.data;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.field.FieldBand;
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

    /**
     * read grid field layer file
     *
     * @param fileName field file path
     * @return field layer
     */
    public static FieldLayer ReadFieldFile_OLD(String fileName) {
        FieldLayer layer = null;

        File fieldFile = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fieldFile));

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


            layer = new FieldLayer(fieldFile.getName(), level, BaseCode.CodeType.EQCode, bands);

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

        return layer;
    }

    /**
     * read grid field layer file
     *
     * @param fileName field file path
     * @return field layer
     */
    public static FieldLayer ReadFieldFile(String fileName) {
        FieldLayer layer = null;

        File fieldFile = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fieldFile));

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


            layer = new FieldLayer(fieldFile.getName(), level, BaseCode.CodeType.EQCode, bands);

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

        return layer;
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
                    String domStr = MathUtil.DecimalToBinary(offset.getDomainID(), 4);
                    String typeStr = MathUtil.DecimalToBinary(offset.getElementCode(), 4);
                    String mortonStr = MathUtil.DecimalToQuaternary(offset.getMorton(), offset.getLevel());
                    writer.write(domStr + typeStr + mortonStr + "," + mbs.getSize() + "\n");
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
    public static FieldLayer ReadFeatureFile(String fileName) {
        return null;
    }

    /**
     * write grid feature layer to file
     *
     * @param layer    feature layer
     * @param fileName feature file path
     */
    public static void WriteFeatureFile(FeatureLayer layer, String fileName) {
    }

}
