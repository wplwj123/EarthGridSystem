package cn.edu.njnu.earthgrid.core.codes;

import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.core.geometry.Trigon;

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
     * 0000 represents node
     * 0001,0010,0011 represents edge of nw,center,sw
     * 0100,0101 represents upper and bottom cell
     *
     * @see ElementType
     */
    private int elementCode;

    /**
     * the domain of this code located in
     * note: for EQTM
     * 0-9 for cell and edge
     * but for node 10 for north pole and 11 for south pole
     */
    private int domainID;

    /**
     * the level of this code
     */
    private int level;

    /**
     * Constructor
     *
     * @param codeType
     * @param domainID
     * @param elementCode
     * @param level
     */
    public BaseCode(CodeType codeType, int domainID, int elementCode, int level) {
        this.codeType = codeType;
        this.domainID = domainID;
        this.elementCode = elementCode;
        this.level = level;
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
     * get level of this code
     *
     * @return level
     */
    public final int getLevel() {
        return this.level;
    }

    /**
     * set level of this code
     *
     * @param level value of level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * get domain id of this code
     *
     * @return domain id
     */
    public final int getDomainID() {
        return this.domainID;
    }

    /**
     * set domain id of this code
     *
     * @param domainID value of doamin id
     */
    public void setDomainID(int domainID) {
        this.domainID = domainID;
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
    public void setElementCode(int elementCode) {
        this.elementCode = elementCode;
    }

    /**
     * get code type
     *
     * @return
     */
    public CodeType getCodeType() {
        return this.codeType;
    }

    /**
     * get the code's element type
     *
     * @return element type
     * @see ElementType
     */
    public abstract ElementType getElementType();

    public abstract Trigon toTrigon();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseCode))
            return false;

        return this.codeType == ((BaseCode) obj).codeType
                && this.domainID == ((BaseCode) obj).domainID
                && this.elementCode == ((BaseCode) obj).elementCode
                && this.level == ((BaseCode) obj).level;
    }

    /**
     * The type of grid code
     */
    public enum CodeType {
        EQCode,
        EHcode;
    }
}
