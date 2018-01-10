package cn.edu.njnu.earthgrid.layer;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;

/**
 * the abstract base layer of all grid layer
 *
 * @author LWJie
 * @version EGS 1.0
 */
public abstract class BaseLayer {
    /**
     * layer name
     */
    private String name;
    /**
     * the level of grid code in this layer
     */
    protected int level;
    /**
     * the grid code type of this layer
     */
    protected BaseCode.CodeType codeType;
    /**
     * the type of this layer
     * @see LayerType
     */
    protected LayerType layerType;

    public BaseLayer(String name, int level, BaseCode.CodeType codeType, LayerType layerType) {
        this.name = name;
        this.level = level;
        this.codeType = codeType;
        this.layerType = layerType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {

        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public BaseCode.CodeType getCodeType() {
        return codeType;
    }

    public void setCodeType(BaseCode.CodeType codeType) {
        this.codeType = codeType;
    }

    /**
     * write layer to grid file
     * @param fileName grid file path
     */
    //public abstract void writeLayerFile(String fileName);

    /**
     * The type of grid layer
     */
    public enum LayerType {
        UnknownLayer,
        PointLayer,
        PolylineLyer,
        PolygonLayer,
        FieldLayer;
    }
}
