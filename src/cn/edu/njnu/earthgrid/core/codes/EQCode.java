package cn.edu.njnu.earthgrid.core.codes;

import cn.edu.njnu.earthgrid.core.geometry.*;

/**
 * Extended QTM Grid Code
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class EQCode extends BaseCode {

    /**
     * the morton code within domain
     */
    private long morton;

    /**
     * default Constructor
     */
    public EQCode() {
        super(CodeType.EQCode, -1, -1, -1);
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
        super(CodeType.EQCode, domainID, elementCode, level);
        this.morton = morton;
    }

    /**
     * copy Constructor
     *
     * @param code
     */
    public EQCode(EQCode code) {
        super(CodeType.EQCode, code.getDomainID(), code.getElementCode(), code.getLevel());
        this.morton = code.getMorton();
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
        if (getDomainID() == 10) {      // north pole
            return new SpericalCoord(0., 90.);
        } else if (getDomainID() == 11) {    //south pole
            return new SpericalCoord(0., -90.);
        }

        DiamondBlock d = toDiamond();

        CartesianCoord cc = MathUtil.GetDiamondElement(d, this.getElementCode());
        SpericalCoord sc = CartesianCoord.ToSpericalCoord(cc);
        return sc;
    }

    @Override
    public Trigon toTrigon() {
        DiamondBlock d = toDiamond();

        if (4 == getElementCode()) {
            return new Trigon(d.v(0), d.v(1), d.v(3));
        } else if (5 == getElementCode()) {
            return new Trigon(d.v(2), d.v(1), d.v(3));
        }

        return null;
    }

    /**
     * convert EQTM code into diamond cell
     *
     * @return diamond of this EQTM code located in
     */
    public DiamondBlock toDiamond() {
        // check pole
        if (getDomainID() == 10) {      // north pole
            return null;
        } else if (getDomainID() == 11) {    //south pole
            return null;
        }

        CartesianCoord v0 = new CartesianCoord();
        CartesianCoord v1 = new CartesianCoord();
        CartesianCoord v2 = new CartesianCoord();
        CartesianCoord v3 = new CartesianCoord();
        MathUtil.GetDomainCorner(getDomainID(), v0, v1, v2, v3);

        DiamondBlock d = new DiamondBlock(v0, v1, v2, v3);
        MathUtil.GetDiamond(d, this.morton, this.getLevel());

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
        this.setLevel(level);

        //check pole
        if (ele == ElementType.GridNode) {
            if (Math.abs(sc.getLatitude() - 90.0) <= MathUtil.EPS) { // north pole
                this.setDomainID(10);
                this.setElementCode(0);
                this.morton = 0;
                return;
            }

            if (Math.abs(sc.getLatitude() + 90.0) <= MathUtil.EPS) { // south pole
                this.setDomainID(11);
                this.setElementCode(0);
                this.morton = 0;
                return;
            }
        }

        CartesianCoord cc = CartesianCoord.FromSpericalCoord(sc);
        this.setDomainID(MathUtil.PredictDomain(cc));

        CartesianCoord v0 = new CartesianCoord();
        CartesianCoord v1 = new CartesianCoord();
        CartesianCoord v2 = new CartesianCoord();
        CartesianCoord v3 = new CartesianCoord();
        MathUtil.GetDomainCorner(this.getDomainID(), v0, v1, v2, v3);      //wait to debug, maybe error

        DiamondBlock d = new DiamondBlock(v0, v1, v2, v3);
        //get morton
        long temp = 0;
        this.morton = MathUtil.CalcMorton(temp, cc, d, this.getLevel());

        this.setElementCode(MathUtil.CalcType(cc, d, ele));
    }

    @Override
    public String toString() {
        String domStr = MathUtil.DecimalToBinary(getDomainID(), 4);
        String typeStr = MathUtil.DecimalToBinary(getElementCode(), 4);
        String mortonStr = MathUtil.DecimalToQuaternary(getMorton(), getLevel());

        return domStr + typeStr + mortonStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EQCode))
            return false;

        return super.equals(obj) && this.morton == ((EQCode) obj).morton;
    }
}
