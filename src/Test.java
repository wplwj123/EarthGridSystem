import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.data.EQGridReaderWriter;
import cn.edu.njnu.earthgrid.data.GDALReaderWriter;
import cn.edu.njnu.earthgrid.data.ShapeReaderWriter;
import cn.edu.njnu.earthgrid.feature.Extent;
import cn.edu.njnu.earthgrid.feature.Point;
import cn.edu.njnu.earthgrid.feature.Polygon;
import cn.edu.njnu.earthgrid.feature.Polyline;
import cn.edu.njnu.earthgrid.layer.FeatureLayer;
import cn.edu.njnu.earthgrid.layer.FieldLayer;

public class Test {
    public static void main(String[] args) {
        // EQGrid Read and Write test
        //FieldLayer layer = EQGridReaderWriter.ReadFieldFile("C:\\Users\\LWJie\\Desktop\\img_level6.dgg");
        //EQGridReaderWriter.WriteFieldFile(layer, "C:\\Users\\LWJie\\Desktop\\demc_level6.dgg");
        //FeatureLayer layer = EQGridReaderWriter.ReadFeatureFile("C:\\Users\\LWJie\\Desktop\\data\\WorldPolygon_lev10.dggs");
        //FeatureLayer layer = EQGridReaderWriter.ReadFeatureFile("C:\\Users\\LWJie\\Desktop\\data\\Coastline_lev10.dggs");
        //FeatureLayer layer = EQGridReaderWriter.ReadFeatureFile("C:\\Users\\LWJie\\Desktop\\data\\ElevationPoint_lev10.dggs");

        // Shapefile Read and Write test
        //ShapeReaderWriter shapeReaderWriter = new ShapeReaderWriter();

        //FeatureLayer layer = shapeReaderWriter.ReadShapefile("E:\\Data\\Natural Earth Vector\\WorldShp\\world_110m_polys.shp", BaseCode.CodeType.EQCode, 7);
        //EQGridReaderWriter.WriteFeatureFile(layer, "C:\\Users\\LWJie\\Desktop\\data\\WorldPolygon_lev7");
        //FeatureLayer layer = ShapeReaderWriter.ReadShapefile("E:\\Data\\Natural Earth Vector\\110m_physical\\ne_110m_coastline.shp", BaseCode.CodeType.EQCode, 9);
        //EQGridReaderWriter.WriteFeatureFile(layer, "C:\\Users\\LWJie\\Desktop\\data\\Coastline_lev9");
        //FeatureLayer layer = ShapeReaderWriter.ReadShapefile("E:\\Data\\Natural Earth Vector\\110m_physical\\ne_110m_geography_regions_elevation_points.shp", BaseCode.CodeType.EQCode, 10);
        //EQGridReaderWriter.WriteFeatureFile(layer, "C:\\Users\\LWJie\\Desktop\\data\\ElevationPoint_lev10");


        //Raster Read and Write test
        //FieldLayer layer = GDALReaderWriter.ReadRaster("E:\\Data\\Natural Earth Vector\\WorldImg\\NE1_50M_SR_W.tif");
        //layer = GDALReaderWriter.ReadRaster("E:\\Data\\ETOPO1\\etopo1tiff.tif");
        //EQGridReaderWriter.WriteFieldFile(layer, "C:\\Users\\LWJie\\Desktop\\img_level7.dgg");
    }
}
