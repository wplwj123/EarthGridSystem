package cn.edu.njnu.earthgrid.core.codes;

import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;

/**
 * The base class of all grid code
 * note: Only support EQTM in EGS 1.0
 *
 * @author LWJie
 * @version EGS 1.0
 */
public abstract class BaseCode {

    /**
     * The type of this grid code
     *
     * @see CodeType
     */
    private CodeType codeType;

    /**
     * code's element type code
     * note:  for EQTM
     *          0000 represents node
     *          0001,0010,0011 represents edge of nw,center,sw
     *          0100,0101 represents upper and bottom cell
     *
     * @see ElementType
     */
    private int elementCode;

    public BaseCode(CodeType codeType, int elementCode) {
        this.codeType = codeType;
        this.elementCode = elementCode;
    }

    /**
     * convert grid code into lat/lon.
     *
     * @return lat/lon of cell element
     */
    public abstract SpericalCoord toSpericalCoord();

    /**
     * convert lat/lon into grid code.
     *
     * @param sc    the point's lat/lon
     * @param level grid's level
     * @param ele   element identifier
     */
    public abstract void fromSpericalCoord(SpericalCoord sc, int level, ElementType ele);

    /**
     * get the type of grid code
     *
     * @return Code Type
     * @see CodeType
     */
    public final CodeType getType() {
        return this.codeType;
    }

    /**
     * get the code's element type code
     *
     * @return element type code
     * @see ElementType
     */
    public final int getElementCode() {
        return this.elementCode;
    }

    /**
     * set the code's element type code
     *
     * @param elementCode value of the code's element type code
     * @see ElementType
     */
    public void setElementCode(int elementCode){
        this.elementCode = elementCode;
    }

    /**
     * get the code's element type
     *
     * @return element type
     * @see ElementType
     */
    public abstract ElementType getElementType();

    /**
     * The type of grid code
     */
    public enum CodeType {
        EQCode,
        EHcode;
    }
}
