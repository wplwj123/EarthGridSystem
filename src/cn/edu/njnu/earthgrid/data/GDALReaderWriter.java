package cn.edu.njnu.earthgrid.data;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.field.FieldBand;
import cn.edu.njnu.earthgrid.layer.FieldLayer;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;

import java.util.ArrayList;

/**
 * DataIO
 * Reader and Writer for tradition raster data
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class GDALReaderWriter {

    private static int level = 6;
    private static ElementType elementType = ElementType.GridCell;
    private static BaseCode.CodeType codeType = BaseCode.CodeType.EQCode;

    public static FieldLayer ReadRaster(String fileName){
        Raster raster = getRaster(fileName);

        ArrayList<FieldBand> bands = new ArrayList<>();

        int cellCount = (int) (MathUtil.Pow(2, level) * MathUtil.Pow(2, level));

        for(int nband = 0; nband < raster.getBandCount(); ++nband){
            FieldBand band = new FieldBand(raster.getNoData(nband), elementType);
            band.setMin(raster.getMin(nband));
            band.setMax(raster.getMax(nband));

            for(int domID = 0; domID < 10; ++domID){
                EQCode offset = new EQCode(domID, 4, level, 0);
                band.setMBS(offset, cellCount * 2);

                EQCode code = new EQCode(offset);
                for(int morID = 0; morID < cellCount; ++morID){
                    System.out.println("band_" + nband + ", domID_" + domID + ", morID_" + morID);

//                    if(domID == 3 && morID == 1038){
//                        System.out.println("band_" + nband + ", domID_" + domID + ", morID_" + morID);
//                    }

                    code.setMorton(offset.getMorton() + morID);
                    code.setElementCode(4);

                    SpericalCoord sc = code.toSpericalCoord();
                    float value = raster.getValueFromBicubicInterpolatation(nband, sc.getLongitude(), sc.getLatitude());
                    band.addAttribute(domID, 2 * morID, value);

                    code.setElementCode(5);
                    sc = code.toSpericalCoord();
                    value = raster.getValueFromBicubicInterpolatation(nband, sc.getLongitude(), sc.getLatitude());
                    band.addAttribute(domID, 2 * morID + 1, value);
                }
            }

            bands.add(band);
        }

        FieldLayer fieldLayer = new FieldLayer("", level, codeType, bands);

        return fieldLayer;
    }

    private static Raster getRaster(String fileName){
        //register all gdal driver
        gdal.AllRegister();

        // support Chinese
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");
        gdal.SetConfigOption("SHAPE_ENCODING","");

        //open raster
        Dataset dataset = gdal.Open(fileName, gdalconstConstants.GA_ReadOnly);
        if(null == dataset){
            System.out.println("open raster failed");
            assert false;
        }


        // number of row/colum
        int nCol = dataset.getRasterXSize();
        int nRow = dataset.getRasterYSize();

        double[] adfGeoTransform = dataset.GetGeoTransform();
        if(null == adfGeoTransform){
            System.out.println("not GeoTiff format");
            assert false;
        }
        //top left corner's lat/lon
        float xTopLeft = (float)adfGeoTransform[0];
        float yTopLeft = (float)adfGeoTransform[3];

        //pixel's size
        float xCellSize = (float)adfGeoTransform[1];
        float yCellSize = (float)adfGeoTransform[5];

        //number of bands
        int nBand = dataset.getRasterCount();

        Raster raster = new Raster(xTopLeft, yTopLeft, xCellSize, yCellSize, nRow, nCol, nBand);

        for(int i = 0; i < nBand; ++i){
            Band band = dataset.GetRasterBand(i + 1);

            float[] bandData = new float[band.getXSize() * band.getYSize()];
            band.ReadRaster(0, 0, band.getXSize(), band.getYSize(), bandData);
            raster.setBand(i, bandData);

            Double[] nodata = new Double[1];
            band.GetNoDataValue(nodata);
            if(null == nodata[0]){
                raster.setNoData(i, 99999.f);
            }
            else {
                raster.setNoData(i, nodata[0].floatValue());
            }

            Double[] min = new Double[1];
            band.GetMinimum(min);
            Double[] max = new Double[1];
            band.GetMaximum(max);

            if(null != min[0] && null != max[0]){
                raster.setMin(i, min[0].floatValue());
                raster.setMax(i, max[0].floatValue());
            }
            else {
                float tempMax = bandData[0];
                float tempMin = bandData[0];
                for(int index = 1; index < bandData.length; ++index){
                    if(tempMax < bandData[index])
                        tempMax = bandData[index];
                    if(tempMin > bandData[index])
                        tempMin = bandData[index];
                }
                raster.setMin(i, tempMin);
                raster.setMax(i, tempMax);
            }
        }

        gdal.GDALDestroyDriverManager();

        return raster;
    }

    public static void WriteRaster(FieldLayer layer, String fileName){}
}
