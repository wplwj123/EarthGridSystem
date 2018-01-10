package cn.edu.njnu.earthgrid.layer;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.field.FieldBand;

import java.util.ArrayList;

/**
 * grid layer to save field data
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class FieldLayer extends BaseLayer {

    /**
     * field data
     */
    private ArrayList<FieldBand> bands;

    /**
     * number of band
     */
    private int bandNum;

    /**
     * Constructor
     *
     * @param name
     * @param level
     * @param codeType
     */
    public FieldLayer(String name, int level, BaseCode.CodeType codeType) {
        super(name, level, codeType, LayerType.FieldLayer);
    }

    /**
     * Constructor
     *
     * @param name
     * @param level
     * @param codeType
     * @param bands    field bands
     */
    public FieldLayer(String name, int level, BaseCode.CodeType codeType, ArrayList<FieldBand> bands) {
        super(name, level, codeType, LayerType.FieldLayer);

        this.bands = bands;
        this.bandNum = bands.size();
    }

    public FieldBand getBands(int band) {
        if (0 > band || bandNum - 1 < band)
            return null;
        return this.bands.get(band);
    }

    public void addBand(FieldBand band) {
        this.bands.add(band);
    }

    public int getBandNum() {
        return this.bandNum;
    }

    public void setBandNum(int bandNum) {
        this.bandNum = bandNum;
    }

}
