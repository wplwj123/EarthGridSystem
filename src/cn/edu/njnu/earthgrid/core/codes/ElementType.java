package cn.edu.njnu.earthgrid.core.codes;

/**
 * grid's component element
 * note: Only support EQTM in EGS 1.0
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public enum ElementType {
    /**
     * default, use to represents diamond cell in EQTM
     */
    NoDef,
    /**
     * 0000 represents node in EQTM
     */
    GridNode,
    /**
     * 0001,0010,0011 represent edge of nw,center,sw in EQTM
     */
    GridEdge,
    /**
     * 0100,0101 represent upper and bottom cell in EQTM
     */
    GridCell,
    /**
     *
     */
    GridPixel;
}
