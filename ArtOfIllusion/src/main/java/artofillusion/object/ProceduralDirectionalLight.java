/* Copyright (C) 1999-2008 by Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.object;

import artofillusion.*;
import artofillusion.animation.*;
import artofillusion.math.*;
import artofillusion.procedural.*;
import artofillusion.texture.*;
import artofillusion.ui.*;
import buoy.widget.*;
import java.io.*;
import java.util.*;

/**
 * This is a DirectionalLight whose emitted light is calculated by a Procedure.
 */
public class ProceduralDirectionalLight extends DirectionalLight {

    private final Procedure procedure;
    private ThreadLocal<Procedure> renderingProc;
    private double currentTime;
    private TextureParameter[] parameters;
    private double[] parameterValues;

    private static final Property[] PROPERTIES = new Property[]{
        new Property(Translate.text("AngularRadius"), 0.0, 45.0, 1.0),
        new Property(Translate.text("lightType"), new String[]{Translate.text("normalLight"), Translate.text("shadowlessLight"), Translate.text("ambientLight")}, Translate.text("normalLight"))
    };

    public ProceduralDirectionalLight(double theRadius) {
        super(new RGBColor(), 1.0f, theRadius);
        procedure = createProcedure();
        findParameters();
        initThreadLocal();
    }

    /**
     * Create a Procedure object for this light.
     */
    private Procedure createProcedure() {
        return new Procedure(new OutputModule("Color", "White", new RGBColor(1.0, 1.0, 1.0)),
                new OutputModule("Intensity", "1", 1.0));
    }

    /**
     * Reinitialize the ThreadLocal that holds copies of the Procedure during rendering.
     */
    private void initThreadLocal() {
        renderingProc = ThreadLocal.withInitial(() -> {
            Procedure localProc = createProcedure();
            localProc.copy(procedure);
            return localProc;
        });
    }

    /**
     * Find all parameters defined by the procedure.
     */
    private void findParameters() {
        var  modules = procedure.getModules();
        int count = 0;
        for (var       module : modules) {
            if (module instanceof ParameterModule) {
                count++;
            }
        }
        TextureParameter[] newParameters = new TextureParameter[count];
        double[] newValues = new double[count];
        count = 0;
        for (var       module : modules) {
            if (module instanceof ParameterModule) {
                newParameters[count] = ((ParameterModule) module).getParameter(this);
                newValues[count] = newParameters[count].defaultVal;
                if (parameters != null) {
                    for (int j = 0; j < parameters.length; j++) {
                        if (newParameters[count].equals(parameters[j])) {
                            newValues[count] = parameterValues[j];
                        }
                    }
                }
                ((ParameterModule) module).setIndex(count++);
            }
        }
        parameters = newParameters;
        parameterValues = newValues;
    }

    @Override
    public ProceduralDirectionalLight duplicate() {
        ProceduralDirectionalLight light = new ProceduralDirectionalLight(getRadius());
        light.copyObject(this);
        return light;
    }

    @Override
    public void copyObject(Object3D obj) {
        ProceduralDirectionalLight lt = (ProceduralDirectionalLight) obj;
        setRadius(lt.getRadius());
        procedure.copy(lt.procedure);
    }

    @Override
    public void sceneChanged(ObjectInfo info, Scene scene) {
        currentTime = scene.getTime();
    }

    /**
     * Evaluate the Procedure to determine the light color at a point.
     */
    @Override
    public void getLight(RGBColor light, Vec3 position) {
        PointInfo point = new PointInfo();
        point.x = position.x;
        point.y = position.y;
        point.z = position.z;
        point.t = currentTime;
        point.param = parameterValues;
        Procedure pr = renderingProc.get();
        pr.initForPoint(point);
        OutputModule[] output = pr.getOutputModules();
        output[0].getColor(light);
        light.scale(output[1].getAverageValue());
    }

    /* The following two methods are used for reading and writing files.  The first is a
     constructor which reads the necessary data from an input stream.  The other writes
     the object's representation to an output stream. */
    public ProceduralDirectionalLight(DataInputStream in, Scene theScene) throws IOException {
        super(in, theScene);
        short version = in.readShort();
        if (version != 0) {
            throw new InvalidObjectException("");
        }
        procedure = createProcedure();
        procedure.readFromStream(in, theScene);
        bounds = new BoundingBox(-0.15, 0.15, -0.15, 0.15, -0.15, 0.25);
        findParameters();
        initThreadLocal();
    }

    @Override
    public void writeToFile(DataOutputStream out, Scene theScene) throws IOException {
        super.writeToFile(out, theScene);
        out.writeShort(0);
        procedure.writeToStream(out, theScene);
    }

    @Override
    public void edit(EditingWindow parent, ObjectInfo info, Runnable cb) {
        ProcedureEditor editor = new ProcedureEditor(procedure, new LightProcedureOwner(info, cb), parent.getScene());
        editor.setEditingWindow(parent);
    }

    @Override
    public Property[] getProperties() {
        Property[] properties = new Property[parameters.length + 2];
        for (int i = 0; i < parameters.length; i++) {
            properties[i] = new Property(parameters[i].name, parameters[i].minVal, parameters[i].maxVal, parameters[i].defaultVal);
        }
        properties[properties.length - 2] = PROPERTIES[0];
        properties[properties.length - 1] = PROPERTIES[1];
        return properties;
    }

    @Override
    public Object getPropertyValue(int index) {
        if (index < parameterValues.length) {
            return parameterValues[index];
        }
        switch (index - parameterValues.length) {
            case 0:
                return getRadius();
            case 1:
                return PROPERTIES[1].getAllowedValues()[type];
        }
        return null;
    }

    @Override
    public void setPropertyValue(int index, Object value) {
        if (index < parameterValues.length) {
            parameterValues[index] = (Double) value;
        } else if (index == parameterValues.length) {
            setRadius((Double) value);
        } else if (index == parameterValues.length + 1) {
            Object[] values = PROPERTIES[index].getAllowedValues();
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    type = i;
                }
            }
        }
    }

    /* Return a Keyframe which describes the current pose of this object. */
    @Override
    public Keyframe getPoseKeyframe() {
        return new ProceduralLightKeyframe(this);
    }

    /* Modify this object based on a pose keyframe. */
    @Override
    public void applyPoseKeyframe(Keyframe k) {
        ProceduralLightKeyframe key = (ProceduralLightKeyframe) k;
        setRadius(key.radius);
        for (int i = 0; i < parameters.length; i++) {
            if (key.paramValues.containsKey(parameters[i])) {
                parameterValues[i] = key.paramValues.get(parameters[i]);
            } else {
                parameterValues[i] = parameters[i].defaultVal;
            }
        }
    }

    /**
     * This will be called whenever a new pose track is created for this object. It allows
     * the object to configure the track by setting its graphable values, subtracks, etc.
     */
    @Override
    public void configurePoseTrack(PoseTrack track) {
        String[] names = new String[parameters.length + 1];
        double[] defaults = new double[parameters.length + 1];
        double[][] ranges = new double[parameters.length + 1][];
        for (int i = 0; i < parameters.length; i++) {
            TextureParameter param = parameters[i];
            names[i] = param.name;
            defaults[i] = param.defaultVal;
            ranges[i] = new double[]{param.minVal, param.maxVal};
        }
        names[parameters.length] = Translate.text("AngularRadius");
        defaults[parameters.length] = getRadius();
        ranges[parameters.length] = new double[]{0.0, 45.0};
        track.setGraphableValues(names, defaults, ranges);
    }

    /**
     * Allow the user to edit a keyframe returned by getPoseKeyframe().
     */
    @Override
    public void editKeyframe(EditingWindow parent, Keyframe k, ObjectInfo info) {
        final ProceduralLightKeyframe key = (ProceduralLightKeyframe) k;
        ValueSelector[] fields = new ValueSelector[parameters.length + 1];
        String[] names = new String[parameters.length + 1];
        for (int i = 0; i < parameters.length; i++) {
            TextureParameter param = parameters[i];
            double value = key.paramValues.containsKey(param) ? key.paramValues.get(param) : param.defaultVal;
            double range = param.maxVal - param.minVal;
            if (range == 0.0 || Double.isInfinite(range)) {
                range = 1.0;
            }
            fields[i] = new ValueSelector(value, param.minVal, param.maxVal, range * 0.01);
            names[i] = param.name;
        }
        fields[fields.length - 1] = new ValueSelector(key.radius, 0.0, 45.0, 0.1);
        names[names.length - 1] = Translate.text("AngularRadius");
        ComponentsDialog dlg = new ComponentsDialog(parent.getFrame(), Translate.text("editDirectionalLightTitle"), fields, names);
        if (!dlg.clickedOk()) {
            return;
        }
        for (int i = 0; i < parameters.length; i++) {
            key.paramValues.put(parameters[i], fields[i].getValue());
        }
        key.radius = fields[fields.length - 1].getValue();
    }

    /**
     * Inner class representing a pose for a directional light.
     */
    public static class ProceduralLightKeyframe implements Keyframe {

        private final ProceduralDirectionalLight light;
        public final HashMap<TextureParameter, Double> paramValues;
        public double radius;

        public ProceduralLightKeyframe(ProceduralDirectionalLight light) {
            this.light = light;
            paramValues = new HashMap<>();
            for (int i = 0; i < light.parameters.length; i++) {
                paramValues.put(light.parameters[i], light.parameterValues[i]);
            }
            radius = light.getRadius();
        }

        /* Create a duplicate of this keyframe.. */
        @Override
        public Keyframe duplicate() {
            return duplicate(light);
        }

        /* Create a duplicate of this keyframe for a (possibly different) object. */
        @Override
        public Keyframe duplicate(Object owner) {
            ProceduralLightKeyframe key = new ProceduralLightKeyframe((ProceduralDirectionalLight) ((ObjectInfo) owner).getObject());
            key.paramValues.clear();
            for (Map.Entry<TextureParameter, Double> entry : paramValues.entrySet()) {
                key.paramValues.put(entry.getKey(), entry.getValue());
            }
            key.radius = radius;
            return key;
        }

        /* Get the list of graphable values for this keyframe. */
        @Override
        public double[] getGraphValues() {
            double[] values = new double[light.parameters.length + 1];
            for (int i = 0; i < light.parameters.length; i++) {
                TextureParameter param = light.parameters[i];
                values[i] = (paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal);
            }
            values[values.length - 1] = radius;
            return values;
        }

        /* Set the list of graphable values for this keyframe. */
        @Override
        public void setGraphValues(double[] values) {
            paramValues.clear();
            for (int i = 0; i < light.parameters.length; i++) {
                paramValues.put(light.parameters[i], values[i]);
            }
            radius = values[values.length - 1];
        }

        /* These methods return a new Keyframe which is a weighted average of this one and one,
       two, or three others. */
        @Override
        public Keyframe blend(Keyframe o2, double weight1, double weight2) {
            ProceduralLightKeyframe k2 = (ProceduralLightKeyframe) o2;
            ProceduralLightKeyframe key = new ProceduralLightKeyframe(light);
            key.radius = weight1 * radius + weight2 * k2.radius;
            for (TextureParameter param : light.parameters) {
                double val1 = paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal;
                double val2 = k2.paramValues.containsKey(param) ? k2.paramValues.get(param) : param.defaultVal;
                key.paramValues.put(param, weight1 * val1 + weight2 * val2);
            }
            return key;
        }

        @Override
        public Keyframe blend(Keyframe o2, Keyframe o3, double weight1, double weight2, double weight3) {
            ProceduralLightKeyframe k2 = (ProceduralLightKeyframe) o2, k3 = (ProceduralLightKeyframe) o3;
            ProceduralLightKeyframe key = new ProceduralLightKeyframe(light);
            key.radius = weight1 * radius + weight2 * k2.radius + weight3 * k3.radius;
            for (TextureParameter param : light.parameters) {
                double val1 = paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal;
                double val2 = k2.paramValues.containsKey(param) ? k2.paramValues.get(param) : param.defaultVal;
                double val3 = k3.paramValues.containsKey(param) ? k3.paramValues.get(param) : param.defaultVal;
                key.paramValues.put(param, weight1 * val1 + weight2 * val2 + weight3 * val3);
            }
            return key;
        }

        @Override
        public Keyframe blend(Keyframe o2, Keyframe o3, Keyframe o4, double weight1, double weight2, double weight3, double weight4) {
            ProceduralLightKeyframe k2 = (ProceduralLightKeyframe) o2, k3 = (ProceduralLightKeyframe) o3, k4 = (ProceduralLightKeyframe) o4;
            ProceduralLightKeyframe key = new ProceduralLightKeyframe(light);
            key.radius = weight1 * radius + weight2 * k2.radius + weight3 * k3.radius + weight4 * k4.radius;
            for (TextureParameter param : light.parameters) {
                double val1 = paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal;
                double val2 = k2.paramValues.containsKey(param) ? k2.paramValues.get(param) : param.defaultVal;
                double val3 = k3.paramValues.containsKey(param) ? k3.paramValues.get(param) : param.defaultVal;
                double val4 = k4.paramValues.containsKey(param) ? k4.paramValues.get(param) : param.defaultVal;
                key.paramValues.put(param, weight1 * val1 + weight2 * val2 + weight3 * val3 + weight4 * val4);
            }
            return key;
        }

        /* Determine whether this keyframe is identical to another one. */
        @Override
        public boolean equals(Keyframe k) {
            if (!(k instanceof ProceduralLightKeyframe)) {
                return false;
            }
            ProceduralLightKeyframe key = (ProceduralLightKeyframe) k;
            if (key.radius != radius) {
                return false;
            }
            for (TextureParameter param : light.parameters) {
                double val1 = paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal;
                double val2 = key.paramValues.containsKey(param) ? key.paramValues.get(param) : param.defaultVal;
                if (val1 != val2) {
                    return false;
                }
            }
            return true;
        }

        /* Write out a representation of this keyframe to a stream. */
        @Override
        public void writeToStream(DataOutputStream out) throws IOException {
            out.writeDouble(radius);
            for (TextureParameter param : light.parameters) {
                double val = paramValues.containsKey(param) ? paramValues.get(param) : param.defaultVal;
                out.writeDouble(val);
            }
        }

        /* Reconstructs the keyframe from its serialized representation. */
        public ProceduralLightKeyframe(DataInputStream in, Object parent) throws IOException {
            this((ProceduralDirectionalLight) ((ObjectInfo) parent).getObject());
            radius = in.readDouble();
            for (TextureParameter param : light.parameters) {
                paramValues.put(param, in.readDouble());
            }
        }
    }

    private class LightProcedureOwner implements ProcedureOwner {

        private final ObjectInfo info;
        private final Runnable callback;

        public LightProcedureOwner(ObjectInfo info, Runnable callback) {
            this.info = info;
            this.callback = callback;
        }

        @Override
        public String getWindowTitle() {
            return Translate.text("editProceduralDirectionalLightTitle");
        }

        @Override
        public MaterialPreviewer getPreview() {
            final MaterialPreviewer preview = new MaterialPreviewer(new UniformTexture(), null, 200, 160);
            for (ObjectInfo item : preview.getScene().getObjects()) {
                if (item.getObject() instanceof DirectionalLight) {
                    item.setObject(ProceduralDirectionalLight.this);
                }
            }
            return preview;
        }

        @Override
        public void updatePreview(MaterialPreviewer preview) {
            findParameters();
            initThreadLocal();
            preview.render();
        }

        @Override
        public boolean canEditName() {
            return false;
        }

        @Override
        public String getName() {
            return info.getName();
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public void acceptEdits(ProcedureEditor editor) {
            findParameters();
            initThreadLocal();
            callback.run();
        }

        @Override
        public void editProperties(ProcedureEditor editor) {
            ValueSelector radiusField = new ValueSelector(getRadius(), 0.0, 45.0, 0.1);
            BComboBox typeChoice = new BComboBox(new String[]{Translate.text("normalLight"), Translate.text("shadowlessLight"), Translate.text("ambientLight")});
            typeChoice.setSelectedIndex(type);
            final BFrame parentFrame = editor.getParentFrame();

            ComponentsDialog dlg = new ComponentsDialog(parentFrame, Translate.text("Properties"),
                    new Widget[]{radiusField, typeChoice}, new String[]{Translate.text("AngularRadius"), Translate.text("lightType")});
            if (!dlg.clickedOk()) {
                return;
            }
            setParameters(getColor(), getIntensity(), typeChoice.getSelectedIndex(), getDecayRate());
            setRadius(radiusField.getValue());
        }
    }
}
