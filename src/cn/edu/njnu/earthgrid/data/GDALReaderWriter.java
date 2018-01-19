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

    private int level = 7;
    private ElementType elementType = ElementType.GridCell;
    private BaseCode.CodeType codeType = BaseCode.CodeType.EQCode;

    /**
     * read raster and convert to grid field layer
     *
     * @param fileName
     * @param fieldCodeType
     * @param fieldElementType
     * @param fieldLevel
     * @return
     */
    public FieldLayer ReadRaster(String fileName, BaseCode.CodeType fieldCodeType,
                                        ElementType fieldElementType, int fieldLevel){

        level = fieldLevel;
        elementType = fieldElementType;
        codeType = fieldCodeType;

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

    private Raster getRaster(String fileName){
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

        dataset.delete();

        return raster;
    }

    public void WriteRaster(FieldLayer layer, String fileName){}

    private class Raster {

        /**
         * top left corner's lat/lon
         */
        private float xTopLeft;
        private float yTopLeft;

        /**
         * pixel's size
         */
        private float xCellSize;
        private float yCellSize;

        /**
         * number of row/colum
         */
        private int nRow;
        private int nCol;

        /**
         * several bands may appear
         */
        private int nBand;
        private float[] noData;
        private float[][] data;

        /**
         * maxim value and minmal value
         */
        private float[] max;
        private float[] min;

        public Raster(float xTopLeft, float yTopLeft, float xCellSize, float yCellSize, int nRow, int nCol, int nBand) {
            this.xTopLeft = xTopLeft;
            this.yTopLeft = yTopLeft;
            this.xCellSize = xCellSize;
            this.yCellSize = yCellSize;
            this.nRow = nRow;
            this.nCol = nCol;
            this.nBand = nBand;

            noData = new float[nBand];
            data = new float[nBand][];

            max = new float[nBand];
            min = new float[nBand];
        }

        public int getBandCount() {
            return nBand;
        }

        public void setNoData(int band, float val) {
            this.noData[band] = val;
        }

        public float getNoData(int band) {
            return noData[band];
        }

        public float getMax(int band) {
            return max[band];
        }

        public void setMax(int band, float val) {
            this.max[band] = val;
        }

        public float getMin(int band) {
            return this.min[band];
        }

        public void setMin(int band, float val) {
            this.min[band] = val;
        }

        public void setBand(int band, float[] bandData) {
            this.data[band] = bandData;
        }

        /**
         * get the pixel value
         *
         * @param band
         * @param row
         * @param col
         * @return
         */
        public float getValue(int band, int row, int col){
            if(isValidIndex(band, row, col)){
                return this.data[band][col + nCol * row];
            }
            else {
                return noData[band];
            }
        }


        public float getValue(int band, int row, int col, float defaultValue){
            float ret;
            if (!isValidIndex(band, row, col))	{
                ret = defaultValue;
            }
            else{
                ret = getValue(band, row, col);
            }

            return ret;
        }

        /**
         * get the pixel value using lon and lat
         *
         * @param band
         * @param x longitude
         * @param y latitude
         * @return
         */
        public float getValue(int band, float x, float y){
            int col = (int)((x - xTopLeft) / xCellSize);
            int row = (int)((y - yTopLeft) / yCellSize);

            return getValue(band, row, col);
        }

        /**
         * judge if target cell is valid
         *
         * @param band
         * @param row
         * @param col
         * @return
         */
        private boolean isValidIndex(int band, int row, int col){
            if(0 > band || nBand < band){
                return false;
            }

            if (row < 0 || row >= nRow || col < 0 || col >= nCol){
                return false;
            }

            return true;
        }

        /**
         * get the value of the grid point in lat/lon format
         *
         * @param band
         * @param x longitude
         * @param y latitude
         * @return
         */
        public float getValueFromBilinearInterpolation(int band, double x, double y){
            /************************************/
            /*(xTopLeft,yTopLeft)               */
            /*      q11 -------- q21            */
            /*       |     (u,v)  |             */
            /*       |            |             */
            /*      q12 -------- q22            */
            /*                                  */
            /************************************/
            if (x < xTopLeft || y > yTopLeft){
                return noData[band];
            }

            int col = (int)((x - xTopLeft) / xCellSize);
            int row = (int)((y - yTopLeft) / yCellSize);

            float u = (float) ((x - xTopLeft - col * xCellSize) / xCellSize);
            float v = (float) ((yTopLeft - y + row * yCellSize) / -yCellSize);

            float  q11, q21, q22, q12;
            if (!isValidIndex(band, row, col)){
                //return getNoDataValue(band);
                int tmpRow = row;
                int tmpCol = col;

                if (row >= nRow)
                    tmpRow = nRow - 1;

                if (row < 0) tmpRow = 0;

                if (col >= nCol)
                    tmpCol = nCol - 1;

                if (col < 0) tmpCol = 0;

                return getValue(tmpRow, tmpCol, 0);
            }

            else{
                q11 = getValue(row, col, band);
            }

            if (!isValidIndex(band, row, col + 1))	{
                q12 = q11;
            }
            else{
                q12 = getValue(row, col + 1, band);
            }

            if (!isValidIndex(band, row + 1, col)){
                q21 = q11;
            }
            else{
                q21 = getValue(row + 1, col, band);
            }

            if (!isValidIndex(band, row + 1, col + 1)){
                q22 = q11;
            }
            else{
                q22 = getValue(row + 1, col + 1, band);
            }

            return (float)((1 - u) * (1 - v) * q11 + (1 - u) * v * q12
                    + u * (1 - v) * q21 + u * v * q22);
        }

        /**
         * get the value of the grid point in lat/lon format
         *
         * @param band
         * @param x longitude
         * @param y latitude
         * @return
         */
        public float getValueFromBicubicInterpolatation(int band, double x, double y){
            /************************************************/
            /*(xTopLeft,yTopLeft)   						*/
            /*      p(-1,-1)   p(0,-1)    p(1,-1)   p(2,-1)	*/
            /*      p(-1,0)    *p(0,0)*   p(1,0)    p(2,0)	*/
            /*      p(-1,1)    p(0,1)     p(1,1)    p(2,1)	*/
            /*      p(-1,2)    p(0,2)     p(1,2)    p(2,2)  */
            /************************************************/
            if (x < xTopLeft || y > yTopLeft){
                return noData[band];
            }

            int col = (int)((x - xTopLeft) / xCellSize);
            int row = (int)((y - yTopLeft) / yCellSize);

            float u = (float) ((x - xTopLeft - col * xCellSize) / xCellSize);
            float v = (float) ((yTopLeft - y + row * yCellSize) / -yCellSize);

            float[][] a = new float[1][4];
            a[0][0] = sinXDivx(u + 1); a[0][1] = sinXDivx(u); a[0][2] = sinXDivx(u - 1); a[0][3] = sinXDivx(u - 2);

            float[][] b = new float[4][4];
            // v11
            if (!isValidIndex(band, row, col)){
                int tmpRow = row;
                int tmpCol = col;

                if (row >= nRow)
                    tmpRow = nRow - 1;

                if (row < 0) tmpRow = 0;

                if (col >= nCol)
                    tmpCol = nCol - 1;

                if (col < 0) tmpCol = 0;
                return getValue(band, tmpRow, tmpCol);
            }
            else {
                b[1][1] = getValue(band, row, col);
            }

            b[0][0] = getValue(band, row - 1, col - 1, b[1][1]); // v00
            b[0][1] = getValue(band, row, col - 1, b[1][1]); // v01
            b[0][2] = getValue(band, row + 1, col - 1, b[1][1]); // v02
            b[0][3] = getValue(band, row + 2, col - 1, b[1][1]); // v03
            b[1][0] = getValue(band, row - 1, col, b[1][1]); // v10
            b[1][2] = getValue(band, row + 1, col, b[1][1]); // v12
            b[1][3] = getValue(band, row + 2, col, b[1][1]); // v13
            b[2][0] = getValue(band, row - 1, col + 1, b[1][1]); // v20
            b[2][1] = getValue(band, row, col + 1, b[1][1]); // v21
            b[2][2] = getValue(band, row + 1, col + 1, b[1][1]); // v22
            b[2][3] = getValue(band, row + 2, col + 1, b[1][1]); // v23
            b[3][0] = getValue(band, row - 1, col + 2, b[1][1]); // v30
            b[3][1] = getValue(band, row, col + 2, b[1][1]); // v31
            b[3][2] = getValue(band, row + 1, col + 2, b[1][1]); // v32
            b[3][3] = getValue(band, row + 2, col + 2, b[1][1]); // v33

            float[][] c = new float[4][1];
            c[0][0] = sinXDivx(v + 1);
            c[1][0] = sinXDivx(v);
            c[2][0] = sinXDivx(v - 1);
            c[3][0] = sinXDivx(v - 2);

            float[][] temp = MatrixMulti(a, b);
            temp = MatrixMulti(temp, c);

            return temp[0][0];
        }

        private float sinXDivx(double x){
            final float a = -1; // adjust number to circumstance,such as -2, -1, -0.75, -0.5

            if (x < 0){
                x = -x;
            }

            double squareX = x * x;
            double cubicX = squareX * x;

            if (x <= 1){
                return (float)((a + 2) * cubicX - (a + 3) * squareX + 1);
            }
            else if (x <= 2){
                return (float)(a * cubicX - 5 * a * squareX + (8 * a) * x - 4 * a);
            }
            else{
                return 0;
            }
        }

        /**
         * 矩阵乘法运算a 矩阵与矩阵相乘
         * @param matrix_a 矩阵a
         * @param matrix_b 矩阵b
         * @return result3 运算合法，返回结果; null 运算不合法
         */
        private float[][] MatrixMulti(float[][] matrix_a, float[][] matrix_b){
            if(matrix_a[0].length != matrix_b.length){
                return null;
            }

            float[][] result = new float[matrix_a.length][matrix_b[0].length];
            for(int i = 0; i < matrix_a.length; i++){
                for(int j = 0;j < matrix_b[0].length; j++){
                    result[i][j] = CalculateSingleResult(matrix_a, matrix_b, i, j);
                }
            }
            return result;
        }

        /**
         * 矩阵乘法a中result每个元素的单一运算
         * @param matrix_a 矩阵a
         * @param matrix_b 矩阵b
         * @param row 参与单一运算的行标
         * @param col 参与单一运算的列标
         * @return result 运算结果
         */
        private float CalculateSingleResult(float[][] matrix_a, float[][] matrix_b, int row, int col){
            float result = 0;
            for(int i = 0; i < matrix_a[0].length; i++){
                result += matrix_a[row][i] * matrix_b[i][col];
            }
            return result;
        }
    }
}