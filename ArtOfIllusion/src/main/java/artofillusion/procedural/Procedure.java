/* Copyright (C) 2000-2004 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.math.RGBColor;
import artofillusion.math.Vec3;
import artofillusion.procedural.Module;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * This represents a procedure for calculating a set of values (typically, the parameters
 * for a texture or material).
 */
@Slf4j
public class Procedure {

    private final List<OutputModule> outputs;
    private Module[] modules;
    private Link[] links;

    public Procedure(OutputModule... output) {
        this.outputs = new ArrayList<>(Arrays.asList(output));
        modules = new Module[0];
        links = new Link[0];
    }

    /**
     * Get the list of output modules.
     */
    public OutputModule[] getOutputModules() {
        return outputs.toArray(OutputModule[]::new);
    }

    /**
     * Get the list of all other modules.
     */
    public Module[] getModules() {
        return modules;
    }

    /**
     * Get the index of a particular module.
     */
    public int getModuleIndex(Module mod) {
        for (int i = 0; i < modules.length; i++) {
            if (modules[i] == mod) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the index of a particular output module.
     */
    public int getOutputIndex(Module mod) {
        return outputs.indexOf(mod);
    }

    /**
     * Add a module to the procedure.
     */
    public void addModule(Module mod) {
        Module[] newmod = new Module[modules.length + 1];
        for (int i = 0; i < modules.length; i++) {
            newmod[i] = modules[i];
        }
        newmod[modules.length] = mod;
        modules = newmod;
    }

    /**
     * Delete a module from the procedure. Any links involving this module should be deleted
     * before* calling this method.
     */
    public void deleteModule(int which) {
        Module[] newmod = new Module[modules.length - 1];
        int i, j;
        for (i = 0, j = 0; i < modules.length; i++) {
            if (i != which) {
                newmod[j++] = modules[i];
            }
        }
        modules = newmod;
    }

    /**
     * Get the list of links between modules.
     */
    public Link[] getLinks() {
        return links;
    }

    /**
     * Add a link to the procedure.
     */
    public void addLink(Link ln) {
        Link[] newlink = new Link[links.length + 1];
        for (int i = 0; i < links.length; i++) {
            newlink[i] = links[i];
        }
        newlink[links.length] = ln;
        links = newlink;
        ln.to.getModule().setInput(ln.to, ln.from);
    }

    /**
     * Delete a link from the procedure.
     */
    public void deleteLink(int which) {
        Link[] newlink = new Link[links.length - 1];
        int i, j;

        if (links[which].to.getType() == IOPort.INPUT) {
            links[which].to.getModule().setInput(links[which].to, null);
        } else {
            links[which].from.getModule().setInput(links[which].from, null);
        }
        for (i = 0, j = 0; i < links.length; i++) {
            if (i != which) {
                newlink[j++] = links[i];
            }
        }
        links = newlink;
    }

    /**
     * Check for feedback loops in this procedure.
     */
    public boolean checkFeedback() {
        for (OutputModule outer : outputs) {
            for (OutputModule inner : outputs) {
                inner.checked = false;
            }
            for (var     module : modules) {
                module.checked = false;
            }
            if (outer.checkFeedback()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This routine is called before the procedure is evaluated. The PointInfo object
     * describes the point for which it is to be evaluated.
     */
    public void initForPoint(PointInfo p) {
        for (var     module : modules) {
            module.init(p);
        }
    }

    /**
     * This routine returns the value of the specified output module. If that output does
     * not have value type NUMBER, the results are undefined.
     */
    public double getOutputValue(int which) {
        return outputs.get(which).getAverageValue(0, 0.0);
    }

    /**
     * This routine returns the gradient of the specified output module. If that output does
     * not have value type NUMBER, the results are undefined.
     */
    public void getOutputGradient(int which, Vec3 grad) {
        outputs.get(which).getValueGradient(0, grad, 0.0);
    }

    /**
     * This routine returns the color of the specified output module. If that output does
     * not have value type COLOR, the results are undefined.
     */
    public void getOutputColor(int which, RGBColor color) {
        outputs.get(which).getColor(0, color, 0.0);
    }

    /**
     * Make this procedure identical to another one. The output modules must already
     * be set up before calling this method.
     */
    public void copy(Procedure proc) {
        modules = new Module[proc.modules.length];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = proc.modules[i].duplicate();
        }
        links = new Link[proc.links.length];
        for (int i = 0; i < links.length; i++) {
            var  fromModule = proc.links[i].from.getModule();
            var  toModule = proc.links[i].to.getModule();
            int fromIndex = proc.getModuleIndex(fromModule);
            int toIndex = toModule instanceof OutputModule ? proc.getOutputIndex(toModule) : proc.getModuleIndex(toModule);
            IOPort from = modules[fromIndex].getOutputPorts()[proc.modules[fromIndex].getOutputIndex(proc.links[i].from)];
            IOPort to = toModule instanceof OutputModule
                    ? outputs.get(toIndex).getInputPorts()[proc.outputs.get(toIndex).getInputIndex(proc.links[i].to)]
                    : modules[toIndex].getInputPorts()[proc.modules[toIndex].getInputIndex(proc.links[i].to)];
            links[i] = new Link(from, to);
            to.getModule().setInput(to, from);
        }
    }

    /**
     * Write this procedure to an output stream.
     */
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(0);
        out.writeInt(modules.length);
        for (var     module : modules) {
            out.writeUTF(module.getClass().getName());
            out.writeInt(module.getBounds().x);
            out.writeInt(module.getBounds().y);
            module.writeToStream(out, theScene);
        }
        out.writeInt(links.length);
        for (Link link : links) {
            out.writeInt(getModuleIndex(link.from.getModule()));
            out.writeInt(link.from.getModule().getOutputIndex(link.from));
            if (link.to.getModule() instanceof OutputModule) {
                out.writeInt(-getOutputIndex(link.to.getModule()) - 1);
            } else {
                out.writeInt(getModuleIndex(link.to.getModule()));
                out.writeInt(link.to.getModule().getInputIndex(link.to));
            }
        }
    }

    /**
     * Reconstruct this procedure from an input stream. The output modules must already
     * be set up before calling this method.
     */
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException {
        short version = in.readShort();

        if (version != 0) {
            throw new InvalidObjectException("");
        }
        for (OutputModule output1 : outputs) {
            output1.setInput(output1.getInputPorts()[0], null);
        }
        modules = new Module[in.readInt()];
        try {
            for (int i = 0; i < modules.length; i++) {
                String classname = in.readUTF();
                Point point = new Point(in.readInt(), in.readInt());
                Class<?> cls = ArtOfIllusion.getClass(classname);
                Constructor<?> con = cls.getConstructor(Point.class);
                modules[i] = (Module) con.newInstance(point);
                modules[i].readFromStream(in, theScene);
            }
        } catch (InvocationTargetException ex) {
            log.atError().setCause(ex.getTargetException()).log("Invocation error: {}", ex.getTargetException().getMessage());
            throw new IOException();
        } catch (IOException | ReflectiveOperationException | SecurityException ex) {
            log.atError().setCause(ex).log("Error creating module: {}", ex.getMessage());
            throw new IOException();
        }
        links = new Link[in.readInt()];
        for (int i = 0; i < links.length; i++) {
            IOPort to, from = modules[in.readInt()].getOutputPorts()[in.readInt()];
            int j = in.readInt();
            if (j < 0) {
                to = outputs.get(-j - 1).getInputPorts()[0];
            } else {
                to = modules[j].getInputPorts()[in.readInt()];
            }
            links[i] = new Link(from, to);
            to.getModule().setInput(to, from);
        }
    }
}
