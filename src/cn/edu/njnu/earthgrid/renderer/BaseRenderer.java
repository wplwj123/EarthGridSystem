package cn.edu.njnu.earthgrid.renderer;

import gov.nasa.worldwind.layers.RenderableLayer;

/**
 * base renderer class
 *
 * @author LWJie
 * @version EGS 1.0
 */
public abstract class BaseRenderer {
    /**
     * get world wind renderable layer from grid layer
     *
     * @return
     */
    public abstract RenderableLayer getWWRenderableLayer();
}
