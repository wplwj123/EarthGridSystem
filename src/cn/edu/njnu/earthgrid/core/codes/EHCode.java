package cn.edu.njnu.earthgrid.core.codes;

import cn.edu.njnu.earthgrid.core.geometry.SpericalCoord;
import cn.edu.njnu.earthgrid.core.geometry.Trigon;

/**
 * Extended Hexagon Grid Code
 * waiting to realize
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class EHCode extends BaseCode {

    public EHCode() {
        super(CodeType.EHcode, -1, -1, -1);
    }

    @Override
    public SpericalCoord toSpericalCoord() {
        SpericalCoord sc = new SpericalCoord();

        return sc;
    }

    @Override
    public void fromSpericalCoord(SpericalCoord sc, int level, ElementType ele) {

    }

    @Override
    public final ElementType getElementType() {
        return ElementType.NoDef;
    }

    @Override
    public Trigon toTrigon(){
        return null;
    }
}
