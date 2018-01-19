package cn.edu.njnu.earthgrid.layer;

import cn.edu.njnu.earthgrid.core.codes.BaseCode;
import cn.edu.njnu.earthgrid.feature.Extent;
import cn.edu.njnu.earthgrid.feature.Feature;
import cn.edu.njnu.earthgrid.feature.FeatureClass;

import java.util.ArrayList;

/**
 * the abstract feature layer of feature grid layer
 *
 * @author LWJie, ZhouLCh
 * @version EGS 1.0
 */
public class FeatureLayer extends BaseLayer {
    /**
     * collection of grid feature
     */
    private ArrayList<Feature> features;

    /**
     * feature class
     */
    private FeatureClass featureClass;

    /**
     * enveloping rectangle of layer
     */
    private Extent ext;

    /**
     * Constructor
     *
     * @param name
     * @param level
     * @param codeType
     * @param layerType
     * @param featureClass
     */
    public FeatureLayer(String name, int level, BaseCode.CodeType codeType, LayerType layerType,
                        FeatureClass featureClass) {
        super(name, level, codeType, layerType);

        this.featureClass = featureClass;
        this.features = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param name
     * @param level
     * @param codeType
     * @param layerType
     * @param featureClass
     * @param features
     */
    public FeatureLayer(String name, int level, BaseCode.CodeType codeType, LayerType layerType,
                        FeatureClass featureClass, ArrayList<Feature> features) {
        super(name, level, codeType, layerType);

        this.featureClass = featureClass;
        this.features = features;
    }

    public Extent getExtend() {
        return ext;
    }

    public void setExtend(Extent ext) {
        this.ext = ext;
    }

    /**
     * add Feature
     *
     * @param feature
     */
    public void addFeature(Feature feature) {
        this.features.add(feature);
    }

    /**
     * remove Feature
     *
     * @param index
     */
    public void removeFeature(int index) {
        features.remove(index);
    }

    /**
     * remove all feature
     */
    public void removeAllFeature() {
        this.features.clear();
    }

    /**
     * get Feature
     *
     * @param index
     */
    public Feature getFeature(int index) {
        return features.get(index);
    }

    /**
     * get the count of feature
     *
     * @return
     */
    public int getFeatureCount() {
        return this.features.size();
    }

    /**
     * get feature class
     *
     * @return
     */
    public FeatureClass getFeatureClass() {
        return featureClass;
    }
}
