/* Copyright (C) 2000-2011 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.*;
import artofillusion.image.*;
import artofillusion.math.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 * This is a Module which outputs an image.
 */
@ProceduralModule.Category("Modules:menu.patterns")
public class ImageModule extends ProceduralModule<ImageModule> {

    private ImageMap map;
    private boolean tileX, tileY, mirrorX, mirrorY, wrapX, wrapY;
    private boolean pointOk;
    private boolean colorOk;
    private final boolean[] valueOk;
    private final boolean[] gradOk;
    private boolean outside;
    private double xScale;
    private double yScale;
    private double xInv;
    private double yInv;
    private final double[] componentValue;
    private double x, y, xSize, ySize, lastBlur;
    private int maxComponent, colorModel;
    private PointInfo point;
    private final RGBColor color;
    private final RGBColor tempColor;
    private final Vec2 tempGrad;
    private final Vec3[] gradient;

    public static final int RGB_MODEL = 0;
    public static final int HSV_MODEL = 1;
    public static final int HLS_MODEL = 2;

    public ImageModule() {
        this(new Point());
    }

    public ImageModule(Point position) {
        super("(" + Translate.text("Modules:menu.imageModule") + ")", new IOPort[]{new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "X", "(X)"),
            new IOPort(IOPort.NUMBER, IOPort.INPUT, IOPort.LEFT, "Y", "(Y)")},
                new IOPort[]{new IOPort(IOPort.COLOR, IOPort.OUTPUT, IOPort.RIGHT, Translate.text("Color")),
                    new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, Translate.text("Red")),
                    new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, Translate.text("Green")),
                    new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, Translate.text("Blue")),
                    new IOPort(IOPort.NUMBER, IOPort.OUTPUT, IOPort.RIGHT, Translate.text("Mask"))},
                position);
        xScale = yScale = xInv = yInv = 1.0;
        tileX = tileY = true;
        colorModel = RGB_MODEL;
        color = new RGBColor(0.0f, 0.0f, 0.0f);
        tempColor = new RGBColor();
        tempGrad = new Vec2();
        gradient = new Vec3[]{new Vec3(), new Vec3(), new Vec3(), new Vec3()};
        componentValue = new double[4];
        valueOk = new boolean[4];
        gradOk = new boolean[4];
    }

    /**
     * Get the image map used by this module.
     */
    public ImageMap getMap() {
        return map;
    }

    /**
     * Set the image map used by this module.
     */
    public void setMap(ImageMap map) {
        this.map = map;
        maxComponent = (map == null ? 0 : map.getComponentCount() - 1);
    }

    /**
     * Get the X scale.
     */
    public double getXScale() {
        return xScale;
    }

    /**
     * Set the X scale.
     */
    public void setXScale(double scale) {
        xScale = scale;
        xInv = 1.0 / scale;
    }

    /**
     * Get the Y scale.
     */
    public double getYScale() {
        return yScale;
    }

    /**
     * Set the Y scale.
     */
    public void setYScale(double scale) {
        yScale = scale;
        yInv = 1.0 / scale;
    }

    /**
     * Get whether the image is tiled in the X direction.
     */
    public boolean getTileX() {
        return tileX;
    }

    /**
     * Set whether the image is tiled in the X direction.
     */
    public void setTileX(boolean b) {
        tileX = b;
    }

    /**
     * Get whether the image is tiled in the Y direction.
     */
    public boolean getTileY() {
        return tileY;
    }

    /**
     * Set whether the image is tiled in the Y direction.
     */
    public void setTileY(boolean b) {
        tileY = b;
    }

    /**
     * Get whether the image is mirrored in the X direction.
     */
    public boolean getMirrorX() {
        return mirrorX;
    }

    /**
     * Set whether the image is mirrored in the X direction.
     */
    public void setMirrorX(boolean b) {
        mirrorX = b;
    }

    /**
     * Get whether the image is mirrored in the Y direction.
     */
    public boolean getMirrorY() {
        return mirrorY;
    }

    /**
     * Set whether the image is mirrored in the Y direction.
     */
    public void setMirrorY(boolean b) {
        mirrorY = b;
    }

    /**
     * Get the color model to output (RGB, HSV, or HLS).
     */
    public int getColorModel() {
        return colorModel;
    }

    /**
     * Get the color model to output (RGB, HSV, or HLS).
     */
    public void setColorModel(int model) {
        colorModel = model;
    }

    /**
     * New point, so the color will need to be recalculated.
     */
    @Override
    public void init(PointInfo p) {
        point = p;
        pointOk = colorOk = false;
        valueOk[0] = valueOk[1] = valueOk[2] = valueOk[3] = false;
        gradOk[0] = gradOk[1] = gradOk[2] = gradOk[3] = false;
    }

    /**
     * Find the point at which the image is being evaluated.
     */
    private void findPoint(double blur) {
        pointOk = true;
        colorOk = valueOk[0] = valueOk[1] = valueOk[2] = valueOk[3] = false;
        x = (linkFrom[0] == null) ? point.x : linkFrom[0].getAverageValue(linkFromIndex[0], blur);
        y = (linkFrom[1] == null) ? point.y : linkFrom[1].getAverageValue(linkFromIndex[1], blur);
        x *= xInv;
        y *= yInv;
        outside = (!tileX && (x < 0.0 || x > 1.0)) || (!tileY && (y < 0.0 || y > 1.0));
        if (outside) {
            return;
        }
        if (mirrorX) {
            double f = FastMath.floor(x);
            if ((((int) f) & 1) == 0) {
                x = 1.0 + f - x;
            } else {
                x = x - f;
            }
        } else {
            x = x - FastMath.floor(x);
        }
        if (mirrorY) {
            double f = FastMath.floor(y);
            if ((((int) f) & 1) == 0) {
                y = 1.0 + f - y;
            } else {
                y = y - f;
            }
        } else {
            y = y - FastMath.floor(y);
        }
        xSize = (linkFrom[0] == null) ? 0.5 * point.xsize + blur : linkFrom[0].getValueError(linkFromIndex[0], blur);
        ySize = (linkFrom[1] == null) ? 0.5 * point.ysize + blur : linkFrom[1].getValueError(linkFromIndex[1], blur);
        xSize *= xInv;
        ySize *= yInv;
        wrapX = tileX && !mirrorX;
        wrapY = tileY && !mirrorY;
    }

    /**
     * Calculate the color.
     */
    @Override
    public void getColor(int which, RGBColor c, double blur) {
        if (colorOk && blur == lastBlur) {
            c.copy(color);
            return;
        }
        if (map == null) {
            color.setRGB(0.0f, 0.0f, 0.0f);
            c.copy(color);
            return;
        }
        if (!pointOk || blur != lastBlur) {
            findPoint(blur);
        }
        colorOk = true;
        lastBlur = blur;
        if (outside) {
            // The point is outside the map.

            color.setRGB(0.0f, 0.0f, 0.0f);
            c.copy(color);
            return;
        }
        map.getColor(color, wrapX, wrapY, x, y, xSize, ySize);
        c.copy(color);
    }

    /**
     * Get the value of one of the components.
     */
    @Override
    public double getAverageValue(int which, double blur) {
        int component = which - 1;
        if (component > maxComponent) {
            if (component == 3) {
                return 0.0;
            }
            component = 0;
        }
        if (valueOk[component] && blur == lastBlur) {
            return componentValue[component];
        }
        if (map == null) {
            return 0.0;
        }
        if (!pointOk || blur != lastBlur) {
            findPoint(blur);
        }
        if (outside) {
            return 0.0;
        }
        if (colorModel == RGB_MODEL || component == 3) {
            valueOk[component] = true;
            componentValue[component] = map.getComponent(component, wrapX, wrapY, x, y, xSize, ySize);
        } else {
            colorOk = true;
            valueOk[0] = valueOk[1] = valueOk[2] = true;
            lastBlur = blur;
            map.getColor(color, wrapX, wrapY, x, y, xSize, ySize);
            float[] components = (colorModel == HSV_MODEL ? color.getHSV() : color.getHLS());
            componentValue[0] = components[0] / 360.0;
            componentValue[1] = components[1];
            componentValue[2] = components[2];
        }
        return componentValue[component];
    }

    /**
     * Get the gradient of one of the components.
     */
    @Override
    public void getValueGradient(int which, Vec3 grad, double blur) {
        int component = which - 1;
        if (component > maxComponent) {
            if (component == 3) {
                grad.set(0.0, 0.0, 0.0);
                return;
            }
            component = 0;
        }
        if (gradOk[component] && blur == lastBlur) {
            grad.set(gradient[component]);
            return;
        }
        if (map == null) {
            grad.set(0.0, 0.0, 0.0);
            return;
        }
        if (!pointOk || blur != lastBlur) {
            findPoint(blur);
        }
        if (outside) {
            grad.set(0.0, 0.0, 0.0);
            return;
        }
        if (colorModel == RGB_MODEL || component == 3) {
            map.getGradient(tempGrad, component, wrapX, wrapY, x, y, xSize, ySize);
        } else {
            double value = getAverageValue(which, blur);
            if (x >= 1.0) {
                tempGrad.x = 0.0;
            } else {
                double dx = xSize;
                if (x + dx > 1.0) {
                    dx = 1.0 - x;
                }
                map.getColor(tempColor, wrapX, wrapY, x + dx, y, xSize, ySize);
                float[] components = (colorModel == HSV_MODEL ? tempColor.getHSV() : tempColor.getHLS());
                components[0] /= 360.0;
                tempGrad.x = (components[component] - value) / dx;
            }
            if (y >= 1.0) {
                tempGrad.y = 0.0;
            } else {
                double dy = ySize;
                if (y + dy > 1.0) {
                    dy = 1.0 - y;
                }
                map.getColor(tempColor, wrapX, wrapY, x, y + dy, xSize, ySize);
                float[] components = (colorModel == HSV_MODEL ? tempColor.getHSV() : tempColor.getHLS());
                components[0] /= 360.0;
                tempGrad.y = -(components[component] - value) / dy;
            }
        }
        calcGradient(component, grad, blur);
        grad.set(gradient[component]);
    }

    /**
     * Calculate the gradient of a component. This assumes that tempGrad has already been
     * set to the appropriate gradient of the image.
     */
    private void calcGradient(int component, Vec3 grad, double blur) {
        double dx = tempGrad.x * xInv, dy = tempGrad.y * yInv;
        Vec3 g = gradient[component];
        if (dx != 0.0) {
            if (linkFrom[0] == null) {
                g.set(dx, 0.0, 0.0);
            } else {
                linkFrom[0].getValueGradient(linkFromIndex[0], grad, blur);
                g.x = dx * grad.x;
                g.y = dx * grad.y;
                g.z = dx * grad.z;
            }
        } else {
            g.set(0.0, 0.0, 0.0);
        }
        if (dy != 0.0) {
            if (linkFrom[1] == null) {
                g.y += dy;
            } else {
                linkFrom[1].getValueGradient(linkFromIndex[1], grad, blur);
                g.x += dy * grad.x;
                g.y += dy * grad.y;
                g.z += dy * grad.z;
            }
        }
        gradOk[component] = true;
    }

    @Override
    public void calcSize() {
        bounds.width = ImageMap.PREVIEW_WIDTH + IOPort.SIZE * 2;
        bounds.height = ImageMap.PREVIEW_HEIGHT + IOPort.SIZE * 2;
        if (output.length * IOPort.SIZE * 3 > bounds.height) {
            bounds.height = output.length * IOPort.SIZE * 3;
        }
    }

    @Override
    protected void drawContents(Graphics2D g) {
        if (map == null) {
            super.drawContents(g);
            return;
        }
        g.drawImage(map.getPreview(), bounds.x + bounds.width / 2 - ImageMap.PREVIEW_WIDTH / 2, bounds.y + bounds.height / 2 - ImageMap.PREVIEW_HEIGHT / 2, null);
    }

    /**
     * Create a duplicate of this module.
     */
    @Override
    public ImageModule duplicate() {
        ImageModule mod = new ImageModule(new Point(bounds.x, bounds.y));

        mod.map = map;
        mod.xScale = xScale;
        mod.yScale = yScale;
        mod.xInv = xInv;
        mod.yInv = yInv;
        mod.color.copy(color);
        mod.tileX = tileX;
        mod.tileY = tileY;
        mod.mirrorX = mirrorX;
        mod.mirrorY = mirrorY;
        mod.wrapX = wrapX;
        mod.wrapY = wrapY;
        mod.maxComponent = maxComponent;
        mod.colorModel = colorModel;
        return mod;
    }

    /**
     * Set the outputs based on what color model is selected.
     */
    private void setupOutputs() {
        if (colorModel == RGB_MODEL) {
            output[1].setDescription(Translate.text("Red"));
            output[2].setDescription(Translate.text("Green"));
            output[3].setDescription(Translate.text("Blue"));
        } else if (colorModel == HSV_MODEL) {
            output[1].setDescription(Translate.text("Hue"));
            output[2].setDescription(Translate.text("Saturation"));
            output[3].setDescription(Translate.text("Value"));
        } else if (colorModel == HLS_MODEL) {
            output[1].setDescription(Translate.text("Hue"));
            output[2].setDescription(Translate.text("Lightness"));
            output[3].setDescription(Translate.text("Saturation"));
        }
    }

    /**
     * Allow the user to set a new value.
     */
    @Override
    public boolean edit(final ProcedureEditor editor, final Scene theScene) {
        ImageMap oldMap = map;
        final ValueField xField = new ValueField(xScale, ValueField.NONE, 10);
        final ValueField yField = new ValueField(yScale, ValueField.NONE, 10);
        final BCheckBox tilexBox = new BCheckBox("X", tileX);
        final BCheckBox tileyBox = new BCheckBox("Y", tileY);
        final BCheckBox mirrorxBox = new BCheckBox("X", mirrorX);
        final BCheckBox mirroryBox = new BCheckBox("Y", mirrorY);
        final BComboBox modelChoice = new BComboBox(new String[]{"RGB", "HSV", "HLS"});
        Object listener = new Object() {
            void processEvent() {
                xScale = xField.getValue();
                yScale = yField.getValue();
                xInv = 1.0 / xScale;
                yInv = 1.0 / yScale;
                tileX = tilexBox.getState();
                tileY = tileyBox.getState();
                mirrorX = mirrorxBox.getState();
                mirrorY = mirroryBox.getState();
                colorModel = modelChoice.getSelectedIndex();
                if (map == null) {
                    maxComponent = 0;
                } else if (colorModel == RGB_MODEL) {
                    maxComponent = map.getComponentCount() - 1;
                } else if (map.getComponentCount() > 3) {
                    maxComponent = 3;
                } else {
                    maxComponent = 2;
                }
                editor.updatePreview();
            }
        };
        xField.addEventLink(ValueChangedEvent.class, listener);
        yField.addEventLink(ValueChangedEvent.class, listener);
        tilexBox.addEventLink(ValueChangedEvent.class, listener);
        tileyBox.addEventLink(ValueChangedEvent.class, listener);
        mirrorxBox.addEventLink(ValueChangedEvent.class, listener);
        mirroryBox.addEventLink(ValueChangedEvent.class, listener);
        modelChoice.addEventLink(ValueChangedEvent.class, listener);
        modelChoice.setSelectedIndex(colorModel);
        final BLabel preview = new BLabel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(ImageMap.PREVIEW_WIDTH, ImageMap.PREVIEW_HEIGHT);
            }
        };
        if (map != null) {
            preview.setIcon(new ImageIcon(map.getPreview()));
        }
        preview.setAlignment(BLabel.CENTER);
        BOutline outline = new BOutline(preview, BorderFactory.createLineBorder(Color.black)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(ImageMap.PREVIEW_WIDTH + 2, ImageMap.PREVIEW_HEIGHT + 2);
            }
        };
        preview.addEventLink(MouseClickedEvent.class, new Object() {
            void processEvent() {
                ImagesDialog dlg = new ImagesDialog(editor.getParentFrame(), theScene, map);
                if (dlg.getSelection() != map && dlg.getSelection() != null) {
                    int w = dlg.getSelection().getWidth();
                    int h = dlg.getSelection().getHeight();
                    if (w > h) {
                        xField.setValue(1.0);
                        yField.setValue(((double) h) / w);
                    } else {
                        xField.setValue(((double) w) / h);
                        yField.setValue(1.0);
                    }
                }
                map = dlg.getSelection();
                if (map == null) {
                    maxComponent = 0;
                } else if (colorModel == RGB_MODEL) {
                    maxComponent = map.getComponentCount() - 1;
                } else if (map.getComponentCount() > 3) {
                    maxComponent = 3;
                } else {
                    maxComponent = 2;
                }
                preview.setIcon(map == null ? null : new ImageIcon(map.getPreview()));
                editor.updatePreview();
            }
        });
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), "Click to Set Image:",
                new Widget[]{outline, xField, yField, tilexBox, tileyBox, mirrorxBox, mirroryBox, modelChoice},
                new String[]{null, "X Size", "Y Size", "Tile", "", "Mirror", "", "Outputs"});
        if (!dlg.clickedOk()) {
            return false;
        }
        setupOutputs();
        return true;
    }

    /**
     * Write out the parameters.
     */
    @Override
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeInt(-2);
        if (map == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(theScene.indexOf(map));
        }
        out.writeDouble(xScale);
        out.writeDouble(yScale);
        out.writeBoolean(tileX);
        out.writeBoolean(tileY);
        out.writeBoolean(mirrorX);
        out.writeBoolean(mirrorY);
        out.writeInt(colorModel);
    }

    /**
     * Read in the parameters.
     */
    @Override
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException {
        int version = in.readInt();
        if (version < -2) {
            throw new InvalidObjectException("");
        }
        int index = (version > -2 ? version : in.readInt());

        if (index > -1) {
            map = theScene.getImage(index);
        } else {
            map = null;
        }
        xScale = in.readDouble();
        yScale = in.readDouble();
        xInv = 1.0 / xScale;
        yInv = 1.0 / yScale;
        tileX = in.readBoolean();
        tileY = in.readBoolean();
        mirrorX = in.readBoolean();
        mirrorY = in.readBoolean();
        colorModel = (version == -2 ? in.readInt() : RGB_MODEL);
        if (map == null) {
            maxComponent = 0;
        } else if (colorModel == RGB_MODEL) {
            maxComponent = map.getComponentCount() - 1;
        } else if (map.getComponentCount() > 3) {
            maxComponent = 3;
        } else {
            maxComponent = 2;
        }
        setupOutputs();
    }
}
