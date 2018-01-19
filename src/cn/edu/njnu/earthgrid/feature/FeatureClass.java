package cn.edu.njnu.earthgrid.feature;

import java.util.ArrayList;

/**
 * feature class define
 *
 * @author ZhouLCh, LWJie
 * @version EGS 1.0
 */
public class FeatureClass {

    private ShapeType shapeType;
    private ArrayList<Field> fields;

    public FeatureClass() {
        shapeType = ShapeType.Unknown;
        this.fields = new ArrayList<>();
    }

    public FeatureClass(ShapeType shapeType) {
        this.shapeType = shapeType;
        this.fields = new ArrayList<>();
    }

    public void addField(Field field){
        this.fields.add(field);
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public int getFieldCount(){
        return fields.size();
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public Feature CreatFeature(){
        Feature feature = new Feature();
        ArrayList<Field> fields = new ArrayList<>();
        for(int i = 0; i < this.fields.size(); ++i){
            fields.add(this.fields.get(i));
        }
        feature.setFields(fields);
        return feature;
    }
}
