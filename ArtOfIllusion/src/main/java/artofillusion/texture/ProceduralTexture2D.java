/* Copyright (C) 2000-2008 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.texture;

import artofillusion.*;
import artofillusion.api.ImplementationVersion;
import artofillusion.image.*;
import artofillusion.math.*;
import artofillusion.procedural.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;

/**
 * This is a Texture2D which uses a Procedure to calculate its properties.
 */
@ImplementationVersion(current = 1, min = 1)
public class ProceduralTexture2D extends Texture2D implements ProcedureOwner {

    private final Procedure proc;
    private double antialiasing;
    private ThreadLocal<Procedure> renderingProc;

    public ProceduralTexture2D() {
        proc = createProcedure();
        antialiasing = 1.0;
        initThreadLocal();
    }

    /**
     * Create a Procedure object for this texture.
     */
    private Procedure createProcedure() {
        return new Procedure(new OutputModule(Translate.text("Diffuse"), Translate.text("white"), new RGBColor(1.0f, 1.0f, 1.0f)),
                new OutputModule(Translate.text("Specular"), Translate.text("white"), new RGBColor(1.0f, 1.0f, 1.0f)),
                new OutputModule(Translate.text("Transparent"), Translate.text("white"), new RGBColor(1.0f, 1.0f, 1.0f)),
                new OutputModule(Translate.text("Emissive"), Translate.text("black"), new RGBColor()),
                new OutputModule(Translate.text("Transparency"), "0", 0.0),
                new OutputModule(Translate.text("Specularity"), "0", 0.0),
                new OutputModule(Translate.text("Shininess"), "0", 0.0),
                new OutputModule(Translate.text("Roughness"), "0", 0.0),
                new OutputModule(Translate.text("Cloudiness"), "0", 0.0),
                new OutputModule(Translate.text("BumpHeight"), "0", 0.0),
                new OutputModule(Translate.text("Displacement"), "0", 0.0));
    }

    /**
     * Reinitialize the ThreadLocal that holds copies of the Procedure during rendering.
     */
    private void initThreadLocal() {
        renderingProc = ThreadLocal.withInitial(() -> {
            Procedure localProc = createProcedure();
            localProc.copy(proc);
            return localProc;
        });
    }

    @Override
    public String getTypeName() {
        return "Procedural 2D";
    }

    @Override
    public void getAverageSpec(TextureSpec spec, double time, double[] param) {
        getTextureSpec(spec, 0.0, 0.0, 1e3, 1e3, 1.0, time, param);
    }

    @Override
    public void getTextureSpec(TextureSpec spec, double x, double y, double xsize, double ysize, double angle, double t, double[] param) {
        Procedure pr = renderingProc.get();
        OutputModule[] output = pr.getOutputModules();
        PointInfo info = new PointInfo();
        info.x = x;
        info.y = y;
        info.z = 0.0;
        info.xsize = xsize * antialiasing;
        info.ysize = ysize * antialiasing;
        info.zsize = 0.0;
        info.viewangle = angle;
        info.t = t;
        info.param = param;
        pr.initForPoint(info);
        double transparency = output[4].getAverageValue();
        double specularity = output[5].getAverageValue();
        double shininess = output[6].getAverageValue();
        if (transparency < 0.0) {
            transparency = 0.0;
        }
        if (transparency > 1.0) {
            transparency = 1.0;
        }
        if (specularity < 0.0) {
            specularity = 0.0;
        }
        if (specularity > 1.0) {
            specularity = 1.0;
        }
        if (shininess < 0.0) {
            shininess = 0.0;
        }
        if (shininess > 1.0) {
            shininess = 1.0;
        }
        output[0].getColor(spec.diffuse);
        output[1].getColor(spec.specular);
        output[2].getColor(spec.transparent);
        output[3].getColor(spec.emissive);
        spec.hilight.copy(spec.specular);
        spec.diffuse.scale((1.0f - transparency) * (1.0f - specularity));
        spec.specular.scale((1.0f - transparency) * specularity);
        spec.hilight.scale((1.0f - transparency) * shininess);
        spec.transparent.scale(transparency);
        spec.roughness = output[7].getAverageValue();
        spec.cloudiness = output[8].getAverageValue();
        if (spec.roughness < 0.0) {
            spec.roughness = 0.0;
        }
        if (spec.roughness > 1.0) {
            spec.roughness = 1.0;
        }
        if (spec.cloudiness < 0.0) {
            spec.cloudiness = 0.0;
        }
        if (spec.cloudiness > 1.0) {
            spec.cloudiness = 1.0;
        }
        output[9].getValueGradient(spec.bumpGrad);
        spec.bumpGrad.scale(0.04);
    }

    @Override
    public void getTransparency(RGBColor trans, double x, double y, double xsize, double ysize, double angle, double t, double[] param) {
        Procedure pr = renderingProc.get();
        OutputModule[] output = pr.getOutputModules();
        PointInfo info = new PointInfo();
        info.x = x;
        info.y = y;
        info.z = 0.0;
        info.xsize = xsize * antialiasing;
        info.ysize = ysize * antialiasing;
        info.zsize = 0.0;
        info.viewangle = angle;
        info.t = t;
        info.param = param;
        pr.initForPoint(info);
        double transparency = output[4].getAverageValue();
        if (transparency < 0.0) {
            transparency = 0.0;
        }
        if (transparency > 1.0) {
            transparency = 1.0;
        }
        output[2].getColor(trans);
        trans.scale(transparency);
    }

    /**
     * Get the procedure used by this texture.
     */
    public Procedure getProcedure() {
        return proc;
    }

    /**
     * Determine whether this Texture uses the specified image.
     */
    @Override
    public boolean usesImage(ImageMap image) {
        return proc.getModules().stream().anyMatch(module -> module instanceof ImageModule && ((ImageModule) module).getMap() == image);
    }

    @Override
    public double getDisplacement(double x, double y, double xsize, double ysize, double t, double[] param) {
        Procedure pr = renderingProc.get();
        OutputModule[] output = pr.getOutputModules();
        PointInfo info = new PointInfo();
        info.x = x;
        info.y = y;
        info.z = 0.0;
        info.xsize = xsize * antialiasing;
        info.ysize = ysize * antialiasing;
        info.zsize = 0.0;
        info.viewangle = 1.0;
        info.t = t;
        info.param = param;
        pr.initForPoint(info);
        return output[10].getAverageValue();
    }

    @Override
    public Texture duplicate() {
        ProceduralTexture2D tex = new ProceduralTexture2D();

        tex.proc.copy(proc);
        tex.setName(getName());
        tex.antialiasing = antialiasing;
        return tex;
    }

    /**
     * Get the list of parameters for this texture.
     */
    @Override
    public TextureParameter[] getParameters() {
        var  modules = proc.getModules();
        int count = 0;

        for (var module: modules) {
            if (module instanceof ParameterModule) {
                count++;
            }
        }
        TextureParameter[] params = new TextureParameter[count];
        count = 0;
        for (var module: modules) {
            if (module instanceof ParameterModule) {
                params[count] = ((ParameterModule) module).getParameter(this);
                ((ParameterModule) module).setIndex(count++);
            }
        }
        return params;
    }

    /**
     * Determine whether this texture has a non-zero value anywhere for a particular component.
     *
     * @param component the texture component to check for (one of the *_COMPONENT constants)
     */
    @Override
    public boolean hasComponent(int component) {
        var  outputs = proc.getOutputModules();
        switch (component) {
            case DIFFUSE_COLOR_COMPONENT:
                return true;
            case SPECULAR_COLOR_COMPONENT:
                return outputs[5].inputConnected(0);
            case TRANSPARENT_COLOR_COMPONENT:
                return outputs[4].inputConnected(0);
            case HILIGHT_COLOR_COMPONENT:
                return outputs[6].inputConnected(0);
            case EMISSIVE_COLOR_COMPONENT:
                return outputs[3].inputConnected(0);
            case BUMP_COMPONENT:
                return outputs[9].inputConnected(0);
            case DISPLACEMENT_COMPONENT:
                return outputs[10].inputConnected(0);
            default:
                return false;
        }
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void edit(WindowWidget<?> fr, Scene sc) {
        new ProcedureEditor(proc, this, sc);
    }

    public ProceduralTexture2D(DataInputStream in, Scene theScene) throws IOException {
        short version = in.readShort();

        if (version != 1) { throw new InvalidObjectException(""); }

        setName(in.readUTF());
        antialiasing = in.readDouble();
        proc = createProcedure();
        proc.readFromStream(in, theScene);

        initThreadLocal();
    }

    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(1);
        out.writeUTF(getName());
        out.writeDouble(antialiasing);
        proc.writeToStream(out, theScene);
    }

    /**
     * Get the title of the procedure's editing window.
     */
    @Override
    public String getWindowTitle() {
        return "Procedural 2D Texture";
    }

    /**
     * Create an object which displays a preview of the procedure.
     */
    @Override
    public MaterialPreviewer getPreview() {
        return new MaterialPreviewer(this, null, 200, 160);
    }

    /**
     * Update the display of the preview.
     */
    @Override
    public void updatePreview(MaterialPreviewer preview) {
        initThreadLocal();
        preview.render();
    }

    /**
     * Determine whether the procedure may contain View Angle modules.
     */
    @Override
    public boolean allowViewAngle() {
        return true;
    }

    /**
     * This is called when the user clicks OK in the procedure editor.
     */
    @Override
    public void acceptEdits(ProcedureEditor editor) {
        initThreadLocal();
        int i = editor.getScene().indexOf(this);
        if (i > -1) {
            editor.getScene().changeTexture(i);
        }
    }

    /**
     * Display the Properties dialog.
     */
    @Override
    public void editProperties(ProcedureEditor editor) {
        ValueField aliasField = new ValueField(antialiasing, ValueField.POSITIVE);
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), Translate.text("editTextureTitle"),
                new Widget[]{aliasField},
                new String[]{Translate.text("Antialiasing")});
        if (!dlg.clickedOk()) {
            return;
        }
        editor.saveState(false);
        antialiasing = aliasField.getValue();
        editor.updatePreview();
    }
}
