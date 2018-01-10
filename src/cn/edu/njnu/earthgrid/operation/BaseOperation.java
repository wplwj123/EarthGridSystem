package cn.edu.njnu.earthgrid.operation;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;

/**
 * operation for topology and geometry of grid data
 *
 * @author LWJie
 * @version EGS 1.0
 */
public abstract class BaseOperation {

    private BaseOperation(){
        throw new AssertionError();
    }

    /**
     * get parent code of current cell code
     *
     * @return parent code
     */
    public abstract BaseCode GetParentCode(BaseCode code);

    /**
     * get sub codes of current code
     *
     * @return sub codes
     */
    public abstract BaseCode[] GetSubCode(BaseCode code);

    /**
     * calculate the distance between code1 and code2
     *
     * @param code1
     * @param code2
     * @return distance of code1 and code2
     */
    public abstract double Distance(BaseCode code1, BaseCode code2);

    /**
     * calculate the area of grid cell of code
     *
     * @param code
     * @return
     */
    public abstract double Area(BaseCode code);

    /**
     * whether code1 and code2 meet at the boundary
     *
     * @param code1
     * @param code2
     * @return true if code1 and code2 meet
     */
    public abstract boolean Meet(BaseCode code1, BaseCode code2);

    /**
     *  whether no intersection area between code1 and code2
     *
     * @param code1
     * @param code2
     * @return true if no intersection area between code1 and code2
     */
    public abstract boolean Disjoint(BaseCode code1, BaseCode code2);

    /**
     * whether code1 contains code2
     * @param code1
     * @param code2
     * @return true if code1 contains code2
     */
    public abstract boolean Contains(BaseCode code1, BaseCode code2);

    /**
     * whether code1 and cod2 overlap
     *
     * @param code1
     * @param code2
     * @return true if overlap
     */
    public abstract boolean Overlap(BaseCode code1, BaseCode code2);
}
