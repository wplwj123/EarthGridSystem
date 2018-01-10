import cn.edu.njnu.earthgrid.data.EQGridReaderWriter;
import cn.edu.njnu.earthgrid.data.ShapeReaderWriter;
import cn.edu.njnu.earthgrid.layer.FieldLayer;

public class Test {
    public static void main(String[] args) {
        //FieldLayer layer = EQGridReaderWriter.ReadFieldFile_OLD("E:\\Program\\ExtendedQTMSystem\\data\\demc_level8.dgg");
        //FieldLayer layer = EQGridReaderWriter.ReadFieldFile("C:\\Users\\LWJie\\Desktop\\test2.dgg");
        //EQGridReaderWriter.WriteFieldFile(layer, "C:\\Users\\LWJie\\Desktop\\test3.dgg");

        //ShapeReaderWriter.ReadShapefile("E:\\Data\\Natural Earth Vector\\WorldShp\\world_110m_polys.shp");
        ShapeReaderWriter.ReadShapefile("E:\\Data\\Natural Earth Vector\\110m_physical\\ne_110m_geography_regions_elevation_points.shp");

    }
}
