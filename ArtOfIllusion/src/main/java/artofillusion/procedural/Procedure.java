/* Copyright (C) 2000-2004 by Peter Eastman
   Changes copyright (C) 2018-2019 by Maksim Khramov

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
    private final List<Module> modules = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();

    public Procedure(OutputModule... output) {
        this.output = output;
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
        return links.toArray(new Link[0]);
    }

    /**
     * Add a link to the procedure.
     */
    public void addLink(Link link) {
        link.to.getModule().setInput(link.to, link.from);
        links.add(link);
    }

    /**
     * Delete a link from the procedure.
     */
    public void deleteLink(int which) {
        Link link = links.get(which);
        if(link.to.getType() == IOPort.INPUT) {
            link.to.getModule().setInput(link.to, null);
        } else {
            link.from.getModule().setInput(link.from, null);
        }
        links.remove(link);
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

        links.clear();
        source.links.forEach((Link link) -> {
            int fromIndex = source.getModuleIndex(link.from.getModule());
            
            Module toModule = link.to.getModule();
            
            int toIndex = toModule instanceof OutputModule ? source.getOutputIndex(toModule) : source.getModuleIndex(toModule);
            IOPort from = modules.get(fromIndex).getOutputPorts()[source.modules.get(fromIndex).getOutputIndex(link.from)];
            IOPort to = toModule instanceof OutputModule
                    ? output[toIndex].getInputPorts()[source.output[toIndex].getInputIndex(link.to)]
                    : modules.get(toIndex).getInputPorts()[source.modules.get(toIndex).getInputIndex(link.to)];
            links.add(new Link(from, to));
            to.getModule().setInput(to, from);
        });

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

        out.writeInt(links.size());
        for (Link link: links) {
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
        
        counter = in.readInt();
        links.clear();
        for (int i = 0; i < counter; i++) {
            IOPort to, from = modules.get(in.readInt()).getOutputPorts()[in.readInt()];
            int j = in.readInt();
            if (j < 0) {
                to = output[-j - 1].getInputPorts()[0];
            } else {
                to = modules.get(j).getInputPorts()[in.readInt()];
            }
            links.add(new Link(from, to));
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
        final String white = Translate.text("white");
        final RGBColor whiteColor = new RGBColor(1.0F, 1.0F, 1.0F);
        return new Procedure(
            new OutputModule(Translate.text("Diffuse"), white, 0.0, whiteColor, IOPort.COLOR),
            new OutputModule(Translate.text("Specular"), white, 0.0, whiteColor, IOPort.COLOR),
            new OutputModule(Translate.text("Transparent"), white, 0.0, whiteColor, IOPort.COLOR),
            new OutputModule(Translate.text("Emissive"), Translate.text("black"), 0.0, new RGBColor(0.0F, 0.0F, 0.0F), IOPort.COLOR),
            new OutputModule(Translate.text("Transparency"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Specularity"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Shininess"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Roughness"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Cloudiness"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("BumpHeight"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Displacement"), "0", 0.0, null, IOPort.NUMBER));
    }

}
