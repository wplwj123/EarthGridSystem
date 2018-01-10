package cn.edu.njnu.earthgrid.renderer;

import cn.edu.njnu.earthgrid.layer.FeatureLayer;
import gov.nasa.worldwind.layers.RenderableLayer;

/**
 * renderer for grid feature layer
 *
 * @author LWJie
 * @version EGS 1.0
 */
public class FeatureRenderer extends BaseRenderer {
    private FeatureLayer featureLayer;

    public FeatureRenderer(FeatureLayer featureLayer) {
        this.featureLayer = featureLayer;
    }

    @Override
    public RenderableLayer[] getWWRenderableLayer() {
        return null;
    }
}
