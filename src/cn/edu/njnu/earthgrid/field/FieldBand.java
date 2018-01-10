package cn.edu.njnu.earthgrid.field;

import cn.edu.njnu.earthgrid.core.codes.EQCode;
import cn.edu.njnu.earthgrid.core.codes.ElementType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * the collection of field band, save field data
 * only support EQTM in 1.0
 *
 * @author LWJie, Xudepeng
 * @version EGS 1.0
 */
public class FieldBand {

    /**
     * the type of grid element to represent field data
     *
     * @see ElementType
     */
    private ElementType elementType;
    /**
     * max and min value
     */
    private float max, min;
    /**
     * no data value
     */
    private float noData;
    /**
     * collection of grid code attribute
     * 3 dimensions include:
     * first is band number
     * second is domain id
     * third is attribute
     */
    private Map<Integer, ArrayList<Float>> attrs;               //attrs[domain][pos]
    /**
     * collection of min bound segment
     * 10 domains or less, a domain has a mbs
     *
     * @see MinBoundSeg
     */
    private Map<Integer, MinBoundSeg> mbs;                                //mbs[domain]

    /**
     * Constructor
     *
     * @param noData      no data value
     * @param elementType The type of grid element
     */
    public FieldBand(float noData, ElementType elementType) {
        this.noData = noData;
        this.elementType = elementType;

        this.attrs = new HashMap<Integer, ArrayList<Float>>();
        this.mbs = new HashMap<Integer, MinBoundSeg>();
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public ElementType getElementType() {
        return this.elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public float getNoData() {
        return this.noData;
    }

    public void setNoData(float noData) {
        this.noData = noData;
    }

    public void setMBS(EQCode offset, int size) {
        int domain = offset.getDomainID();
        MinBoundSeg m = new MinBoundSeg(offset, size);
        this.mbs.put(domain, m);

        this.attrs.put(domain, new ArrayList<Float>(size));

    }

    public MinBoundSeg getMBS(int domain) {
        if (true == this.mbs.containsKey(domain)) {

            return this.mbs.get(domain);
        } else {
            return null;
        }
    }

    public int getMBSCount() {
        return this.mbs.size();
    }

    /**
     * set code's attribute in band
     *
     * @param code
     * @param attr
     */
    public void addAttribute(EQCode code, float attr) {

        int domain = code.getDomainID();
        if (false == this.mbs.containsKey(domain)) {
            assert false;
        }

        EQCode offset = this.mbs.get(domain).getOffset();
        long pos = 0;
        switch (this.elementType) {
            case GridNode:
                pos = (code.getMorton() - offset.getMorton()) +
                        (code.getElementCode() - offset.getElementCode());
                break;
            case GridEdge:
                pos = (code.getMorton() - offset.getMorton()) * 3 +
                        (code.getElementCode() - offset.getElementCode());
                break;
            case GridCell:
                pos = (code.getMorton() - offset.getMorton()) * 2 +
                        (code.getElementCode() - offset.getElementCode());
                break;
            default:
                break;
        }

        this.attrs.get(domain).add((int) (pos), attr);
    }

    public void addAttribute(int domain, int pos, float attr){
        this.attrs.get(domain).add((int) (pos), attr);
    }

    /**
     * get code's attribute in band
     *
     * @param code
     * @return
     */
    public float getAttribute(EQCode code) {

        int domain = code.getDomainID();
        if (false == this.mbs.containsKey(domain)) {
            assert false;
        }

        EQCode offset = this.mbs.get(domain).getOffset();
        long pos = 0;
        switch (this.elementType) {
            case GridNode:
                pos = (code.getMorton() - offset.getMorton()) +
                        (code.getElementCode() - offset.getElementCode());
                break;
            case GridEdge:
                pos = (code.getMorton() - offset.getMorton()) * 3 +
                        (code.getElementCode() - offset.getElementCode());
                break;
            case GridCell:
                pos = (code.getMorton() - offset.getMorton()) * 2 +
                        (code.getElementCode() - offset.getElementCode());
                break;
            default:
                break;
        }

        return this.attrs.get(domain).get((int) (pos));
    }

    public float getAttribute(int domain, int pos){
        return this.attrs.get(domain).get((int) (pos));
    }

    /**
     * min bound segment
     */
    public class MinBoundSeg {
        private EQCode offset;
        private int size;

        public MinBoundSeg(EQCode offset, int size) {
            this.offset = offset;
            this.size = size;
        }

        public EQCode getOffset() {
            return offset;
        }

        public void setOffset(EQCode offset) {
            this.offset = offset;
        }

        public long getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
