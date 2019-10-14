/* Copyright (C) 2000-2004 by Peter Eastman
   Changes copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.procedural;

import artofillusion.*;
import artofillusion.image.ImageMap;
import artofillusion.math.*;
import artofillusion.texture.Texture;
import artofillusion.ui.Translate;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This represents a procedure for calculating a set of values (typically, the parameters for a
 * texture or material).
 */
public class Procedure {

    OutputModule output[];
    private List<Module> modules = new ArrayList<>();
    private Link links[];

    public Procedure(OutputModule... output) {
        this.output = output;        
        links = new Link[0];
    }

    /**
     * Get the list of output modules.
     */
    public OutputModule[] getOutputModules() {
        return output;
    }

    /**
     * Get the list of all other modules.
     */
    public Module[] getModules() {
        return modules.toArray(new Module[0]);
    }

    /**
     * Get the index of a particular module.
     */
    public int getModuleIndex(Module mod) {
        return modules.indexOf(mod);
    }

    /**
     * Get the index of a particular output module.
     */
    public int getOutputIndex(Module mod) {
        for (int i = 0; i < output.length; i++) {
            if (output[i] == mod) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add a module to the procedure.
     */
    public void addModule(Module mod) {
        modules.add(mod);
    }

    /**
     * Delete a module from the procedure. Any links involving this module should be deleted before*
     * calling this method.
     */
    public void deleteModule(int which) {
        modules.remove(which);
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
        Link newlink[] = new Link[links.length + 1];
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
        Link newlink[] = new Link[links.length - 1];
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
        for (int i = 0; i < output.length; i++) {
            for (int j = 0; j < output.length; j++) {
                output[j].checked = false;
            }
            modules.forEach(module -> module.checked = false);

            if (output[i].checkFeedback()) {
                return true;
            }
        }
        return false;
    }

    /**
     * This routine is called before the procedure is evaluated. The PointInfo object describes the
     * point for which it is to be evaluated.
     */
    public void initForPoint(PointInfo p) {
        modules.forEach(module -> module.init(p));
    }

    /**
     * This routine returns the value of the specified output module. If that output does not have
     * value type NUMBER, the results are undefined.
     */
    public double getOutputValue(int which) {
        return output[which].getAverageValue(0, 0.0);
    }

    /**
     * This routine returns the gradient of the specified output module. If that output does not
     * have value type NUMBER, the results are undefined.
     */
    public void getOutputGradient(int which, Vec3 grad) {
        output[which].getValueGradient(0, grad, 0.0);
    }

    /**
     * This routine returns the color of the specified output module. If that output does not have
     * value type COLOR, the results are undefined.
     */
    public void getOutputColor(int which, RGBColor color) {
        output[which].getColor(0, color, 0.0);
    }

    /**
     * Make this procedure identical to another one. The output modules must already be set up
     * before calling this method.
     */
    public void copy(Procedure source) {
        modules.clear();
        source.modules.forEach(module -> modules.add(module.duplicate()));
        
        
        links = new Link[source.links.length];
        
        for (int i = 0; i < links.length; i++) {
            Module fromModule = source.links[i].from.getModule();
            Module toModule = source.links[i].to.getModule();
            int fromIndex = source.getModuleIndex(fromModule);
            int toIndex = toModule instanceof OutputModule ? source.getOutputIndex(toModule) : source.getModuleIndex(toModule);
            IOPort from = modules.get(fromIndex).getOutputPorts()[source.modules.get(fromIndex).getOutputIndex(source.links[i].from)];
            IOPort to = toModule instanceof OutputModule
                    ? output[toIndex].getInputPorts()[source.output[toIndex].getInputIndex(source.links[i].to)]
                    : modules.get(toIndex).getInputPorts()[source.modules.get(toIndex).getInputIndex(source.links[i].to)];
            links[i] = new Link(from, to);
            to.getModule().setInput(to, from);
        }
    }

    /**
     * Write this procedure to an output stream.
     */
    public void writeToStream(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(0);
        out.writeInt(modules.size());
        for(Module module: modules) {
           Rectangle bounds = module.getBounds();
           out.writeUTF(module.getClass().getName());
           out.writeInt(bounds.x);
           out.writeInt(bounds.y);
           module.writeToStream(out, theScene);            
        }

        out.writeInt(links.length);
        for (int i = 0; i < links.length; i++) {
            out.writeInt(getModuleIndex(links[i].from.getModule()));
            out.writeInt(links[i].from.getModule().getOutputIndex(links[i].from));
            if (links[i].to.getModule() instanceof OutputModule) {
                out.writeInt(-getOutputIndex(links[i].to.getModule()) - 1);
            } else {
                out.writeInt(getModuleIndex(links[i].to.getModule()));
                out.writeInt(links[i].to.getModule().getInputIndex(links[i].to));
            }
        }
    }

    /**
     * Reconstruct this procedure from an input stream. The output modules must already be set up
     * before calling this method.
     */
    public void readFromStream(DataInputStream in, Scene theScene) throws IOException, InvalidObjectException {
        short version = in.readShort();

        if (version != 0) {
            throw new InvalidObjectException("");
        }
        for (int i = 0; i < output.length; i++) {
            output[i].setInput(output[i].getInputPorts()[0], null);
        }
        int counter = in.readInt();
        modules.clear();
        try {
            for (int i = 0; i < counter; i++) {
                String classname = in.readUTF();
                Point point = new Point(in.readInt(), in.readInt());
                Class<?> cls = ArtOfIllusion.getClass(classname);
                Constructor<?> con = cls.getConstructor(Point.class);
                Module module = (Module) con.newInstance(point);
                module.readFromStream(in, theScene);
                modules.add(module);
                
            }
        } catch (InvocationTargetException ex) {
            ex.getTargetException().printStackTrace();
            throw new IOException();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException();
        }
        links = new Link[in.readInt()];
        for (int i = 0; i < links.length; i++) {
            IOPort to, from = modules.get(in.readInt()).getOutputPorts()[in.readInt()];
            int j = in.readInt();
            if (j < 0) {
                to = output[-j - 1].getInputPorts()[0];
            } else {
                to = modules.get(j).getInputPorts()[in.readInt()];
            }
            links[i] = new Link(from, to);
            to.getModule().setInput(to, from);
        }
    }

    public TextureParameter[] getTextureParameters(Object texture) {
        int count = 0;
        for (Module mod : modules) {
            if (mod instanceof ParameterModule) {
                count++;
            }
        }

        TextureParameter[] params = new TextureParameter[count];
        count = 0;
        for (Module mod : modules) {
            if (mod instanceof ParameterModule) {
                params[count] = ((ParameterModule) mod).getParameter(texture);
                ((ParameterModule) mod).setIndex(count++);
            }
        }
        return params;
    }

    /**
     * Determine whether given procedure texture has a non-zero value anywhere for a particular
     * component.
     *
     * @param component the texture component to check for (one of the Texture *_COMPONENT
     * constants)
     */
    public boolean hasTextureComponent(int component) {
        switch (component) {
            case Texture.DIFFUSE_COLOR_COMPONENT:
                return true;
            case Texture.SPECULAR_COLOR_COMPONENT:
                return output[5].inputConnected(0);
            case Texture.TRANSPARENT_COLOR_COMPONENT:
                return output[4].inputConnected(0);
            case Texture.HILIGHT_COLOR_COMPONENT:
                return output[6].inputConnected(0);
            case Texture.EMISSIVE_COLOR_COMPONENT:
                return output[3].inputConnected(0);
            case Texture.BUMP_COMPONENT:
                return output[9].inputConnected(0);
            case Texture.DISPLACEMENT_COMPONENT:
                return output[10].inputConnected(0);
        }
        return false;
    }

    public boolean usesImage(ImageMap image) {
         return modules.stream().anyMatch((mod) -> (mod instanceof ImageModule && ((ImageModule) mod).getMap() == image));
    }

    /**
     * Create a Procedure object for texture.
     */
    public static Procedure createTextureProcedure() {
        return new Procedure(new OutputModule[]{new OutputModule(Translate.text("Diffuse"), Translate.text("white"), 0.0, new RGBColor(1.0F, 1.0F, 1.0F), IOPort.COLOR),
            new OutputModule(Translate.text("Specular"), Translate.text("white"), 0.0, new RGBColor(1.0F, 1.0F, 1.0F), IOPort.COLOR),
            new OutputModule(Translate.text("Transparent"), Translate.text("white"), 0.0, new RGBColor(1.0F, 1.0F, 1.0F), IOPort.COLOR),
            new OutputModule(Translate.text("Emissive"), Translate.text("black"), 0.0, new RGBColor(0.0F, 0.0F, 0.0F), IOPort.COLOR),
            new OutputModule(Translate.text("Transparency"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Specularity"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Shininess"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Roughness"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Cloudiness"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("BumpHeight"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Displacement"), "0", 0.0, null, IOPort.NUMBER)});
    }

}
