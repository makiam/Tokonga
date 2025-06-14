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
import artofillusion.math.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;

import lombok.Getter;

import java.util.Arrays;

import java.util.List;
import java.util.ArrayList;


/**
 * This represents a module in a procedure. This is an abstract class, whose subclasses
 * represent specific kinds of modules.
 *
 * @deprecated as of 3.1, to be removed 4.0. use {@link artofillusion.procedural.ProceduralModule}
 */
@Deprecated
public class Module<M extends Module> {

    private List<IOPort> input;
    protected IOPort[] output;
    public Module[] linkFrom;
    public int[] linkFromIndex;
    protected String name;
    /**
     * Get the boundary rectangle for this module.
     */
    @Getter
    protected Rectangle bounds;
    protected boolean checked;

    protected static final Font defaultFont = new Font("Sans Serif", Font.PLAIN, 10);

    protected static final FontMetrics defaultMetrics = Toolkit.getDefaultToolkit().getFontMetrics(defaultFont);

    @Deprecated
    Module() {
        //Do not use, this is just so that the replacement class can temporarily extend this one
    }

    public Module(String name, IOPort[] input, IOPort[] output, Point position) {
        this.name = name;

        this.input = new ArrayList<>(Arrays.asList(input));
        this.input.forEach(item -> item.setModule(this));

        this.output = output;
        linkFrom = new Module[this.input.size()];
        linkFromIndex = new int[this.input.size()];

        for (IOPort ioPort : output) ioPort.setModule(this);
        bounds = new Rectangle(position.x, position.y, 0, 0);
        layout();
    }

    /**
     * Get the name of this module.
     */
    public String getName() {
        return name;
    }

    /**
     * Move this module to a new location.
     */
    public void setPosition(int x, int y) {
        bounds.x = x;
        bounds.y = y;
        layout();
    }

    /**
     * Get a list of the input ports for this module.
     */
    public IOPort[] getInputPorts() {
        return input.toArray(IOPort[]::new);
    }

    /**
     * Get a list of the output ports for this module.
     */
    public IOPort[] getOutputPorts() {
        return output;
    }

    /**
     * Get the index of a particular input port.
     */
    public int getInputIndex(IOPort port) {
        return input.indexOf(port);
    }

    /**
     * Get the index of a particular output port.
     */
    public int getOutputIndex(IOPort port) {
        for (int i = 0; i < output.length; i++) {
            if (output[i] == port) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determine whether an input port is connected to anything.
     */
    public boolean inputConnected(int which) {
        return (linkFrom[which] != null);
    }

    /**
     * Determine whether the specified point is over an IOPort, and if so, return the port.
     */
    public IOPort getClickedPort(Point pos) {
        for (IOPort port: input) {
            if (port.contains(pos)) {
                return port;
            }
        }
        for (IOPort ioPort: output) {
            if (ioPort.contains(pos)) {
                return ioPort;
            }
        }
        return null;
    }

    /**
     * Specify the module and port which one of the input ports is connected to.
     */
    public void setInput(IOPort which, IOPort port) {
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) == which) {
                if (port == null) {
                    linkFrom[i] = null;
                    return;
                }
                Module<?> module = port.getModule();
                for (int j = 0; j < module.output.length; j++) {
                    if (module.output[j] == port) {
                        linkFrom[i] = module;
                        linkFromIndex[i] = j;
                    }
                }
            }
        }
    }

    /**
     * Calculate the size on the screen of this module. The default implementation makes it
     * large enough to display the name of the module.
     */
    public void calcSize() {
        bounds.width = defaultMetrics.stringWidth(name) + IOPort.SIZE * 4;
        bounds.height = defaultMetrics.getMaxAscent() + defaultMetrics.getMaxDescent() + IOPort.SIZE * 4;

        // Depending on how many ports there are, we might need to make it larger.
        int numtop = 0;
        int numbottom = 0;
        int numleft = 0;
        int portLocation;
        for (IOPort port: input) {
            portLocation = port.getLocation();
            if (portLocation == IOPort.TOP) {
                numtop++;
            } else if (portLocation == IOPort.BOTTOM) {
                numbottom++;
            } else {
                numleft++;
            }
        }
        if (Math.max(numtop, numbottom) * IOPort.SIZE * 4 > bounds.width) {
            bounds.width = Math.max(numtop, numbottom) * IOPort.SIZE * 4;
        }
        if (Math.max(numleft, output.length) * IOPort.SIZE * 4 > bounds.height) {
            bounds.height = Math.max(numleft, output.length) * IOPort.SIZE * 4;
        }
    }

    /**
     * Layout the module's onscreen representation. This should be called any time the
     * module is moved or resized.
     */
    public void layout() {
        calcSize();
        int numtop = 0, numbottom = 0, numleft = 0;
        int top = 0;
        int bottom = 0;
        int left = 0;
        int portLocation;

        for (IOPort port: input) {
            portLocation = port.getLocation();
            if (portLocation == IOPort.TOP) {
                numtop++;
            } else if (portLocation == IOPort.BOTTOM) {
                numbottom++;
            } else {
                numleft++;
            }
        }
        for (IOPort port: input) {
            portLocation = port.getLocation();
            if (portLocation == IOPort.TOP) {
                port.setPosition(bounds.x + (bounds.width * (++top)) / (numtop + 1), bounds.y);
            } else if (portLocation == IOPort.BOTTOM) {
                port.setPosition(bounds.x + (bounds.width * (++bottom)) / (numbottom + 1), bounds.y + bounds.height);
            } else {
                port.setPosition(bounds.x, bounds.y + (bounds.height * (++left)) / (numleft + 1));
            }
        }
        for (int i = 0; i < output.length; i++) {
            output[i].setPosition(bounds.x + bounds.width + IOPort.SIZE, bounds.y + (bounds.height * (i + 1)) / (output.length + 1));
        }
    }

    /**
     * Draw the module on the screen. This draws the outline and the ports, then calls
     * drawContents() to draw the contents.
     */
    // Left empty implementation for binary code compatibility
    public void draw(Graphics2D g, boolean selected) {
    }

    /**
     * Draw the contents of the module. The default implementation simply draws the name.
     */
    protected void drawContents(Graphics2D g) {
        g.setColor(Color.black);
        g.setFont(defaultFont);
        g.drawString(name, bounds.x + (bounds.width - defaultMetrics.stringWidth(name)) / 2,
                bounds.y + (bounds.height / 2) + (defaultMetrics.getAscent() / 2));
    }

    /**
     * This method is used to check feedback loops in a procedure.
     */
    public boolean checkFeedback() {
        if (checked) {
            return true;
        }
        checked = true;
        for (Module<?> module : linkFrom) {
            if (module == null) continue;
            if (module.checkFeedback()) {
                return true;
            }
        }
        checked = false;
        return false;
    }

    /**
     * This should display a user interface for editing the module, and return true if the
     * module is changed. The default implementation does nothing.
     *
     * @param editor the ProcedureEditor in which this module is being edited
     * @param theScene the Scene to which this module belongs
     * @return true if the edits were accepted, false if they should be cancelled. Returning false
     * will cause all edits to be reverted automatically. The module does not need to do
     * that itself.
     */
    public boolean edit(ProcedureEditor editor, Scene theScene) {
        return edit(editor.getParentFrame(), theScene);
    }

    /**
     * This is an old form of edit() that exists only to maintain compatibility with old plugins.
     * Subclasses should override the other form, not this one.
     */
    public boolean edit(BFrame fr, Scene theScene) {
        return false;
    }

    /**
     * This method initializes the module in preparation for evaluating the
     * procedure at a new point. The default implementation does nothing.
     * Subclasses whose output depends on the point should override this method.
     *
     * @param point - a point in space for which a module value is being evaluated.
     *
     */
    public void init(PointInfo point) {
    }

    /**
     * Get the average value of the specified output port. If the specified output port
     * does not have a value type of NUMBER, the result is undefined. Blur specifies the
     * amount of smoothing to use. Subclasses which can return values should override this
     * method.
     */
    public double getAverageValue(int which, double blur) {
        return 0.0;
    }

    /**
     * Get the uncertainty in the value of the specified output port. If the specified
     * output port does not have a value type of NUMBER, the result is undefined. Blur
     * specifies the amount of smoothing to use. Subclasses which can return values should
     * override this method.
     */
    public double getValueError(int which, double blur) {
        return 0.0;
    }

    /**
     * Get the gradient of the value of the specified output port. If the specified
     * output port does not have a value type of NUMBER, the result is undefined. Blur
     * specifies the amount of smoothing to use. Subclasses which can return values should
     * override this method.
     */
    public void getValueGradient(int which, Vec3 grad, double blur) {
        grad.set(0.0, 0.0, 0.0);
    }

    /**
     * Get the color of the specified output port. If the specified output port
     * does not have a value type of COLOR, the result is undefined. Blur specifies the
     * amount of smoothing to use. Subclasses which can return colors should override this
     * method.
     */
    public void getColor(int which, RGBColor color, double blur) {
    }

    /**
     * Create a duplicate of this module. Subclasses with adjustable parameters
     * should override this.
     */
    public M duplicate() {
        try {
            var mod = getClass().getConstructor().newInstance();
            mod.setPosition(bounds.x, bounds.y);
            return (M) mod;
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
            return null;
        }
    }

    /**
     * Write out the module's parameters to an output stream. Subclasses with editable
     * parameters should override this method.
     */
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
    }

    /**
     * Read in the module's parameters from an input stream. Subclasses with editable
     * parameters should override this method.
     */
    public void readFromStream(DataInputStream out, Scene theScene) throws IOException {
    }
}
