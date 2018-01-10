package cn.edu.njnu.earthgrid.feature;

import java.util.ArrayList;

/**
 * feature define
 *
 * @author ZhouLCh, LWJie
 * @version EGS 1.0
 */
public class Feature {

    /**
     * geometry infomation
     */
    private Geometry shape;
    /**
     * attribute field
     */
    private ArrayList<Field> fields;
    /**
     * attribute value
     */
    private ArrayList<String> fieldValues;

    /**
     * default, only be used in this package
     */
    Feature() {
        this.shape = null;
        this.fields = new ArrayList<>();
        this.fieldValues = new ArrayList<>();
    }

    /**
     * default, only be used in this package
     */
    Feature(Geometry shape, ArrayList<Field> fields, ArrayList<String> fieldValues) {
        this.shape = shape;
        this.fields = fields;
        this.fieldValues = fieldValues;
    }

    public Geometry getShape() {
        return shape;
    }

    public void setShape(Geometry shape) {
        this.shape = shape;
    }

    /**
     * default, only be used in this package
     */
    void setFields(ArrayList<Field> fields) {
        for (Field field : fields) {
            this.fields.add(field);
            this.fieldValues.add("");
        }
    }

    public Field getField(String fieldName) {
        for (Field field : fields) {
            if (field.getFieldName() == fieldName) {
                return field;
            }
        }
        return null;
    }

    public String getFieldValue(String fieldName) {
        for (int i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getFieldName() == fieldName) {
                return fieldValues.get(i);
            }
        }
        return null;
    }

    public String getFieldValue(int index) {
        if (0 > index || fields.size() < index)
            return null;
        return fieldValues.get(index);
    }

    public void setFieldValue(String fieldName, String value) {
        for (int i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getFieldName() == fieldName) {
                fieldValues.set(i, value);
            }
        }
    }

    public void setFieldValue(int index, String value) {
        if (0 <= index && fields.size() > index)
            fieldValues.set(index, value);
    }

    public int getFieldCount() {
        return fields.size();
    }
}
