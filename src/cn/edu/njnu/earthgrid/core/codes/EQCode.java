package cn.edu.njnu.earthgrid.core.codes;

import cn.edu.njnu.earthgrid.core.geometry.CartesianCoord;
import cn.edu.njnu.earthgrid.core.geometry.DiamondBlock;
import cn.edu.njnu.earthgrid.core.geometry.MathUtil;
import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;

/**
 * Extended QTM Grid Code
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class EQCode extends BaseCode {

    /**
     * the domain of this EQTM code located in
     * note: 0-9 for cell and edge
     *       but for node 10 for north pole and 11 for south pole
     */
    private int domainID;

    /**
     * the level of this EQTM code
     */
    private int level;

    /**
     * the morton code within domain
     */
    private long morton;

    /**
     * default Constructor
     */
    public EQCode() {
        super(CodeType.EQCode, -1);
        this.domainID = -1;
        this.level = -1;
        this.morton = -1;
    }

    /**
     * Constructor
     *
     * @param domainID    the domain of this EQTM code located in
     * @param elementCode code's element type code
     * @param level       the level of this EQTM code
     * @param morton      the morton code within domain
     */
    public EQCode(int domainID, int elementCode, int level, long morton) {
        super(CodeType.EQCode, elementCode);
        this.domainID = domainID;
        this.level = level;
        this.morton = morton;
    }

    /**
     * copy Constructor
     * @param code
     */
    public EQCode(EQCode code) {
        super(CodeType.EQCode, code.getElementCode());
        this.domainID = code.getDomainID();
        this.level = code.getLevel();
        this.morton = code.getMorton();
    }
    /**
     * get domain id of this EQTM code
     *
     * @return domain id
     */
    public final int getDomainID() {
        return this.domainID;
    }

    /**
     * set domain id of this EQTM code
     *
     * @param domainID value of doamin id
     */
    public void setDomainID(int domainID) {
        this.domainID = domainID;
    }

    /**
     * get level of this EQTM code
     *
     * @return level
     */
    public final int getLevel() {
        return this.level;
    }

    /**
     * set level of this EQTM code
     *
     * @param level value of level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * get morton code of this EQTM code
     *
     * @return morton code
     */
    public final long getMorton() {
        return this.morton;
    }

    /**
     * set morton code of this EQTM code
     *
     * @param code value of morton code
     */
    public void setMorton(long code) {
        this.morton = code;
    }

    /**
     * get grid element type of this EQTM code
     *
     * @return grid element
     */
    @Override
    public final ElementType getElementType() {
        if (getElementCode() == 0) {
            return ElementType.GridNode;
        } else if (getElementCode() >= 1 && getElementCode() <= 3) {
            return ElementType.GridEdge;
        } else if (getElementCode() == 4 || getElementCode() == 5) {
            return ElementType.GridCell;
        } else {
            return ElementType.NoDef;
        }
    }

    /**
     * convert EQTM code into lat/lon.
     * note: return midpoint if tpye is grid edge
     *       return center if type is grid cell
     *
     * @return lat/lon of cell element
     */
    @Override
    public SpericalCoord toSpericalCoord() {
        // check pole
        if (domainID == 10) {      // north pole
            return new SpericalCoord(0., 90.);
        } else if (domainID == 11) {    //south pole
            return new SpericalCoord(0., -90.);
        }

        CartesianCoord v0 = new CartesianCoord();
        CartesianCoord v1 = new CartesianCoord();
        CartesianCoord v2 = new CartesianCoord();
        CartesianCoord v3 = new CartesianCoord();
        MathUtil.GetDomainCorner(this.domainID, v0, v1, v2, v3);      //wait to debug, maybe error

        DiamondBlock d = new DiamondBlock(v0, v1, v2, v3);
        MathUtil.GetDiamond(d, this.morton, this.level);

        CartesianCoord cc = MathUtil.GetDiamondElement(d, this.getElementCode());
        SpericalCoord sc = CartesianCoord.ToSpericalCoord(cc);
        return sc;
    }

    /**
     * convert EQTM code into diamond cell
     *
     * @return diamond of this EQTM code located in
     */
    public DiamondBlock toDiamond() {
        // check pole
        if (domainID == 10) {      // north pole
            return null;
        } else if (domainID == 11) {    //south pole
            return null;
        }

        CartesianCoord v0 = new CartesianCoord();
        CartesianCoord v1 = new CartesianCoord();
        CartesianCoord v2 = new CartesianCoord();
        CartesianCoord v3 = new CartesianCoord();
        MathUtil.GetDomainCorner(this.domainID, v0, v1, v2, v3);      //wait to debug, maybe error

        DiamondBlock d = new DiamondBlock(v0, v1, v2, v3);
        MathUtil.GetDiamond(d, this.morton, this.level);

        return d;
    }

    /**
     * convert lat/lon into EQTM code.
     *
     * @param sc    the point's lat/lon
     * @param level grid's level
     * @param ele   element identifier
     */
    @Override
    public void fromSpericalCoord(SpericalCoord sc, int level, ElementType ele) {

        this.level = level;

        //check pole
        if (ele == ElementType.GridNode) {
            if (Math.abs(sc.getLatitude() - 90.0) <= MathUtil.EPS) { // north pole
                this.setElementCode(0);
                this.domainID = 10;
                this.morton = 0;
                return;
            }

            if (Math.abs(sc.getLatitude() + 90.0) <= MathUtil.EPS) { // south pole
                this.setElementCode(0);
                this.domainID = 11;
                this.morton = 0;
                return;
            }
        }

        CartesianCoord cc = CartesianCoord.FromSpericalCoord(sc);
        this.domainID = MathUtil.PredictDomain(cc);

        CartesianCoord v0 = new CartesianCoord();
        CartesianCoord v1 = new CartesianCoord();
        CartesianCoord v2 = new CartesianCoord();
        CartesianCoord v3 = new CartesianCoord();
        MathUtil.GetDomainCorner(this.domainID, v0, v1, v2, v3);      //wait to debug, maybe error

        DiamondBlock d = new DiamondBlock(v0, v1, v2, v3);
        //get morton
        long temp = 0;
        this.morton = MathUtil.CalcMorton(temp, cc, d, this.level);

        this.setElementCode(MathUtil.CalcType(cc, d, ele));
    }
}
