package cn.edu.njnu.earthgrid.feature;

/**
 * feature field define
 *
 * @author ZhouLCh, LWJie
 * @version EGS 1.0
 */
public class Field {

    private String fieldName;
    private FieldType fieldType;

    public Field() {
        this.fieldName = "";
        this.fieldType = FieldType.FieldUnknown;
    }

    public Field(String fieldName, FieldType fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public enum FieldType{
        FieldUnknown,
        FieldInt,
        FieldReal,
        FieldString;
    }
}
