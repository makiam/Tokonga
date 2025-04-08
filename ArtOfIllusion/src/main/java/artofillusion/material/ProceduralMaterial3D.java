/* Copyright (C) 2000-2008 by Peter Eastman
   Changes copyright (C) 2020-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.material;

import artofillusion.*;
import artofillusion.image.*;
import artofillusion.math.*;
import artofillusion.procedural.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.awt.*;
import java.io.*;

/**
 * This is a Material3D which uses a Procedure to calculate its properties.
 */
public class ProceduralMaterial3D extends Material3D implements ProcedureOwner {

    private final Procedure proc;
    private boolean shadows;
    private double stepSize, antialiasing;
    private ThreadLocal<Procedure> renderingProc;

    public ProceduralMaterial3D() {
        proc = createProcedure();
        shadows = true;
        stepSize = 0.1;
        antialiasing = 1.0;
        initThreadLocal();
    }

    /**
     * Create a Procedure object for this material.
     */
    private Procedure createProcedure() {
        return new Procedure(new OutputModule[]{
            new OutputModule(Translate.text("EmissiveColor"), Translate.text("black"), 0.0, new RGBColor(0.0f, 0.0f, 0.0f), IOPort.COLOR),
            new OutputModule(Translate.text("TransparentColor"), Translate.text("white"), 0.0, new RGBColor(1.0f, 1.0f, 1.0f), IOPort.COLOR),
            new OutputModule(Translate.text("ScatteringColor"), Translate.text("gray"), 0.0, new RGBColor(0.5f, 0.5f, 0.5f), IOPort.COLOR),
            new OutputModule(Translate.text("Transparency"), "" + 0.5, 0.5, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Scattering"), "0", 0.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Density"), "" + 1.0, 1.0, null, IOPort.NUMBER),
            new OutputModule(Translate.text("Eccentricity"), "0", 0.0, null, IOPort.NUMBER)});
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
        return "Procedural";
    }

    @Override
    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double step) {
        stepSize = step;
    }

    @Override
    public void getMaterialSpec(MaterialSpec spec, double x, double y, double z, double xsize, double ysize, double zsize, double t) {
        Procedure pr = renderingProc.get();
        OutputModule[] output = pr.getOutputModules();
        PointInfo info = new PointInfo();
        info.x = x;
        info.y = y;
        info.z = z;
        info.xsize = xsize * stepSize;
        info.ysize = ysize * stepSize;
        info.zsize = zsize * stepSize;
        info.t = t;
        info.param = null;
        pr.initForPoint(info);
        double density = output[5].getAverageValue();
        double eccentricity = output[6].getAverageValue();
        if (density < 0.0) {
            density = 0.0;
        }
        if (density > 1.0) {
            density = 1.0;
        }
        if (eccentricity < -1.0) {
            eccentricity = -1.0;
        }
        if (eccentricity > 1.0) {
            eccentricity = 1.0;
        }
        spec.eccentricity = eccentricity;
        output[0].getColor(spec.color);
        if (density == 0.0) {
            spec.transparency.setRGB(1.0f, 1.0f, 1.0f);
            spec.scattering.setRGB(0.0f, 0.0f, 0.0f);
            return;
        }
        double scattering = output[4].getAverageValue();
        if (scattering < 0.0) {
            scattering = 0.0;
        }
        if (scattering > 1.0) {
            scattering = 1.0;
        }
        output[1].getColor(spec.transparency);
        spec.transparency.scale(output[3].getAverageValue());
        double tr = spec.transparency.getRed(), tg = spec.transparency.getGreen(), tb = spec.transparency.getBlue();
        if (tr < 0.0) {
            tr = 0.0;
        }
        if (tg < 0.0) {
            tg = 0.0;
        }
        if (tb < 0.0) {
            tb = 0.0;
        }
        spec.transparency.setRGB((float) Math.pow(tr, density), (float) Math.pow(tg, density), (float) Math.pow(tb, density));
        output[2].getColor(spec.scattering);
        spec.scattering.scale(density * scattering);
    }

    /**
     * Determine whether this Material uses the specified image.
     */
    @Override
    public boolean usesImage(ImageMap image) {
        return proc.getModules().stream().anyMatch(module -> module instanceof ImageModule && ((ImageModule) module).getMap() == image);
    }

    /**
     * The material scatters light if there is anything connected to the scattering output.
     */
    @Override
    public boolean isScattering() {
        OutputModule[] output = proc.getOutputModules();
        return output[4].inputConnected(0);
    }

    @Override
    public boolean castsShadows() {
        return shadows;
    }

    @Override
    public Material duplicate() {
        ProceduralMaterial3D mat = new ProceduralMaterial3D();

        mat.proc.copy(proc);
        mat.setName(getName());
        mat.setIndexOfRefraction(indexOfRefraction());
        mat.shadows = shadows;
        mat.antialiasing = antialiasing;
        mat.stepSize = stepSize;
        return mat;
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void edit(WindowWidget fr, Scene sc) {
        new ProcedureEditor(proc, this, sc);
    }

    public ProceduralMaterial3D(DataInputStream in, Scene theScene) throws IOException {
        short version = in.readShort();

        if (version != 1) { throw new InvalidObjectException(""); }

        setName(in.readUTF());
        proc = createProcedure();
        setIndexOfRefraction(in.readDouble());
        shadows = in.readBoolean();
        antialiasing = in.readDouble();
        stepSize = in.readDouble();
        proc.readFromStream(in, theScene);

        initThreadLocal();
    }

    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        out.writeShort(1);
        out.writeUTF(getName());
        out.writeDouble(indexOfRefraction());
        out.writeBoolean(shadows);
        out.writeDouble(antialiasing);
        out.writeDouble(stepSize);
        proc.writeToStream(out, theScene);
    }

    /**
     * Get the title of the procedure's editing window.
     */
    @Override
    public String getWindowTitle() {
        return "Procedural Material";
    }

    /**
     * Create an object which displays a preview of the procedure.
     */
    @Override
    public MaterialPreviewer getPreview() {
        return new MaterialPreviewer(null, this, 200, 160);
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
     * This is called when the user clicks OK in the procedure editor.
     */
    @Override
    public void acceptEdits(ProcedureEditor editor) {
        initThreadLocal();
        int i = editor.getScene().indexOf(this);
        if (i > -1) {
            editor.getScene().changeMaterial(i);
        }
    }

    /**
     * Display the Properties dialog.
     */
    @Override
    public void editProperties(ProcedureEditor editor) {
        ValueField refractField = new ValueField(indexOfRefraction(), ValueField.POSITIVE);
        ValueField stepField = new ValueField(stepSize, ValueField.POSITIVE);
        ValueField aliasField = new ValueField(antialiasing, ValueField.POSITIVE);
        BCheckBox shadowBox = new BCheckBox(Translate.text("CastsShadows"), shadows);
        ComponentsDialog dlg = new ComponentsDialog(editor.getParentFrame(), Translate.text("editMaterialTitle"),
                new Widget[]{refractField, stepField, aliasField, shadowBox},
                new String[]{Translate.text("IndexOfRefraction"), Translate.text("integrationStepSize"),
                    Translate.text("Antialiasing"), ""});
        if (!dlg.clickedOk()) {
            return;
        }
        editor.saveState(false);
        setIndexOfRefraction(refractField.getValue());
        setStepSize(stepField.getValue());
        antialiasing = aliasField.getValue();
        shadows = shadowBox.getState();
        editor.updatePreview();
    }
}
