package artofillusion.translators;

import artofillusion.math.RGBColor;

/**
 * Class for storing information about a material in a .mtl file.
 */
public final class WavefrontTextureInfo {

    public String name;
    public RGBColor ambient, diffuse, specular;
    public double shininess, transparency, specularity, roughness;
    public String ambientMap, diffuseMap, specularMap, transparentMap, bumpMap;

    /**
     * This should be called once, after the TextureInfo is created but
     * before it is actually used. It converts from the representation used
     * by .obj files to the one used by Art of Illusion.
     */
    public void resolveColors() {
        if (diffuse == null) {
            if (diffuseMap == null) {
                diffuse = new RGBColor();
            } else {
                diffuse = new RGBColor(1.0, 1.0, 1.0);
            }
        }
        if (ambient == null) {
            ambient = new RGBColor();
        }
        if (specular == null) {
            specular = new RGBColor();
        } else {
            specularity = 1.0;
        }
        diffuse.scale(1.0 - transparency);
        specular.scale(1.0 - transparency);
        roughness = 1.0 - (shininess - 1.0) / 128.0;
        if (roughness > 1.0) {
            roughness = 1.0;
        }
        checkColorRange(ambient);
        checkColorRange(diffuse);
        checkColorRange(specular);
    }

    /**
     * Make sure that the components of a color are all between 0 and 1.
     *
     * @param c Description of the Parameter
     */
    private void checkColorRange(RGBColor c) {
        float r = c.getRed();
        float g = c.getGreen();
        float b = c.getBlue();
        if (r < 0.0f) {
            r = 0.0f;
        }
        if (r > 1.0f) {
            r = 1.0f;
        }
        if (g < 0.0f) {
            g = 0.0f;
        }
        if (g > 1.0f) {
            g = 1.0f;
        }
        if (b < 0.0f) {
            b = 0.0f;
        }
        if (b > 1.0f) {
            b = 1.0f;
        }
        c.setRGB(r, g, b);
    }
}
