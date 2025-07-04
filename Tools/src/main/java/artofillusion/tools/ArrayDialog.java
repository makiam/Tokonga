/* Copyright 2001-2004 by Rick van der Meiden and Peter Eastman
   Changes copyright (C) 2017-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.tools;

import artofillusion.*;
import artofillusion.object.*;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;

import java.awt.Insets;
import java.util.*;

/**
 * This dialog box allows the user to specify options for creating array objects.
 *
 * @author Rick van der Meiden
 */
public class ArrayDialog extends ToolDialog {

    private final LayoutWindow window;
    private final ArraySpec spec;

    private final List<ObjectInfo> curvesVector;

    private BLabel linearCopiesLabel;
    private BLabel stepXLabel;
    private BLabel stepYLabel;
    private BLabel stepZLabel;
    private BRadioButton curveCopiesBox;
    private BRadioButton curveStepBox;
    private BRadioButton linearBox;
    private BRadioButton curveBox;
    private BCheckBox intervalXBox;
    private BCheckBox intervalYBox;
    private BCheckBox intervalZBox;
    private BCheckBox orientationBox;
    private BCheckBox useOrientationBox;
    private BCheckBox useOriginBox;
    private BCheckBox duplicateBox;
    private BCheckBox groupBox;
    private BCheckBox liveBox;
    private BCheckBox deepBox;
    private BComboBox curveChoice;
    private ValueField linearCopiesField;
    private ValueField stepXField;
    private ValueField stepYField;
    private ValueField stepZField;
    private ValueField curveCopiesField;
    private ValueField curveStepField;
    private final RadioButtonGroup methodGroup;
    private final RadioButtonGroup modeGroup;

    public ArrayDialog(LayoutWindow window) {
        super(window, Translate.text("Tools:array.dialog.name"));

        this.window = window;

        // set defaults from scene
        spec = new ArraySpec(window);

        // get available curves
        curvesVector = new Vector<>(10, 10);
        for (ObjectInfo obj : window.getScene().getObjects()) {
            if (obj.getObject() instanceof Curve) {
                curvesVector.add(obj);
            }
        }

        // layout dialog
        methodGroup = new RadioButtonGroup();
        modeGroup = new RadioButtonGroup();
        ColumnContainer content = new ColumnContainer();
        setContent(BOutline.createEmptyBorder(content, UIUtilities.getStandardDialogInsets()));
        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.NONE, new Insets(0, 0, 15, 0), null));
        content.add(new BLabel(Translate.text("Tools:array.dialog.title")), new LayoutInfo());
        content.add(createLinearPanel());
        content.add(createCurvePanel());
        content.add(createOptionsPanel());
        content.add(createFinishPanel());

        // don't allow user to use nil curve
        if (curvesVector.isEmpty()) {
            curveBox.setEnabled(false);
        }

        // update spec
        updateSpec();

        pack();
        UIUtilities.centerDialog(this, window);
        setVisible(true);
    }

    private Widget createLinearPanel() {
        FormContainer panel = new FormContainer(4, 4);
        panel.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null));
        panel.add(linearBox = new BRadioButton(Translate.text("Tools:array.type.linear"), (spec.method == ArraySpec.METHOD_LINEAR), methodGroup), 0, 0);
        linearBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(linearCopiesLabel = Translate.label("Tools:array.copies.number"), 1, 0);
        panel.add(linearCopiesField = new ValueField(spec.linearCopies, ValueField.POSITIVE + ValueField.INTEGER, 4), 2, 0);
        linearCopiesField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(stepXLabel = Translate.label("Tools:array.xstep.label"), 1, 1);
        panel.add(stepXField = new ValueField(spec.stepX, ValueField.NONE, 4), 2, 1);
        stepXField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(intervalXBox = new BCheckBox(Translate.text("Tools:array.xstep.size"), spec.intervalX), 3, 1);
        intervalXBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(stepYLabel = Translate.label("Tools:array.ystep.label"), 1, 2);
        panel.add(stepYField = new ValueField(spec.stepY, ValueField.NONE, 4), 2, 2);
        stepYField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(intervalYBox = new BCheckBox(Translate.text("Tools:array.ystep.size"), spec.intervalY), 3, 2);
        intervalYBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(stepZLabel = Translate.label("Tools:array.zstep.label"), 1, 3);
        panel.add(stepZField = new ValueField(spec.stepZ, ValueField.NONE, 4), 2, 3);
        stepZField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(intervalZBox = new BCheckBox(Translate.text("Tools:array.zstep.size"), spec.intervalZ), 3, 3);
        intervalZBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        return panel;
    }

    private Widget createCurvePanel() {
        FormContainer panel = new FormContainer(3, 6);
        panel.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null));
        panel.add(curveBox = new BRadioButton(Translate.text("Tools:array.type.curve"), (spec.method == ArraySpec.METHOD_CURVE), methodGroup), 0, 0);
        curveBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(curveChoice = new BComboBox(), 1, 0);

        // put names of possible curves in choice
        for (ObjectInfo info : curvesVector) {
            curveChoice.add(info.getName());
        }

        if (spec.curve != null) {
            curveChoice.setSelectedValue(spec.curve.getName());
        }
        curveChoice.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(curveCopiesBox = new BRadioButton(Translate.text("Tools:array.copies.number"), spec.curveMode == ArraySpec.MODE_COPIES, modeGroup), 1, 1);
        curveCopiesBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(curveCopiesField = new ValueField(spec.curveCopies, ValueField.POSITIVE + ValueField.INTEGER, 4), 2, 1);
        curveCopiesField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(curveStepBox = new BRadioButton(Translate.text("Tools:array.step.size"), spec.curveMode == ArraySpec.MODE_STEP, modeGroup), 1, 2);
        curveStepBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(curveStepField = new ValueField(spec.curveStep, ValueField.POSITIVE, 4), 2, 2);
        curveStepField.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(orientationBox = new BCheckBox(Translate.text("Tools:array.follow.curve"), spec.orientation), 1, 3);
        orientationBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(useOriginBox = new BCheckBox(Translate.text("Tools:array.use.original.position"), !spec.ignoreOrigin), 1, 4);
        useOriginBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(useOrientationBox = new BCheckBox(Translate.text("Tools:array.use.original.orientation"), !spec.ignoreOrientation), 1, 5);
        useOrientationBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        return panel;
    }

    private Widget createOptionsPanel() {
        FormContainer panel = new FormContainer(2, 2);
        panel.setDefaultLayout(new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(0, 0, 0, 5), null));
        panel.add(deepBox = new BCheckBox(Translate.text("Tools:array.include.children"), spec.deep), 0, 0);
        deepBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(groupBox = new BCheckBox(Translate.text("Tools:array.group"), spec.group), 0, 1);
        groupBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(duplicateBox = new BCheckBox(Translate.text("Tools:array.skip.first"), !spec.dupFirst), 1, 0);
        duplicateBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        panel.add(liveBox = new BCheckBox(Translate.text("Tools:array.copies.live"), spec.live), 1, 1);
        liveBox.addEventLink(ValueChangedEvent.class, this, "updateSpec");
        return panel;
    }

    private Widget createFinishPanel() {
        RowContainer panel = new RowContainer();

        panel.add(getOkButton());
        panel.add(getCancelButton());

        return panel;
    }

    @Override
    public void commit() {
        updateSpec();
        spec.createArray();
        window.rebuildItemList();
        window.updateImage();
    }

    // Update ArraySpec data
    private void updateSpec() {
        // get values

        if (linearBox.getState()) {
            spec.method = ArraySpec.METHOD_LINEAR;
        }
        if (curveBox.getState()) {
            spec.method = ArraySpec.METHOD_CURVE;
        }
        spec.linearCopies = (int) linearCopiesField.getValue();
        spec.stepX = stepXField.getValue();
        spec.stepY = stepYField.getValue();
        spec.stepZ = stepZField.getValue();
        spec.intervalX = intervalXBox.getState();
        spec.intervalY = intervalYBox.getState();
        spec.intervalZ = intervalZBox.getState();
        if (!curvesVector.isEmpty()) {
            spec.curve = curvesVector.get(curveChoice.getSelectedIndex());
        }

        if (curveCopiesBox.getState()) {
            spec.curveMode = ArraySpec.MODE_COPIES;
        }
        if (curveStepBox.getState()) {
            spec.curveMode = ArraySpec.MODE_STEP;
        }
        spec.curveStep = curveStepField.getValue();
        spec.curveCopies = (int) curveCopiesField.getValue();
        spec.orientation = orientationBox.getState();
        spec.ignoreOrientation = !useOrientationBox.getState();
        spec.ignoreOrigin = !useOriginBox.getState();
        spec.dupFirst = !duplicateBox.getState();
        spec.group = groupBox.getState();
        spec.live = liveBox.getState();
        spec.deep = deepBox.getState();

        // update enabled/disabled status
        linearCopiesField.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepXField.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepYField.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepZField.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        linearCopiesLabel.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepXLabel.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepYLabel.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        stepZLabel.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        intervalXBox.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        intervalYBox.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);
        intervalZBox.setEnabled(spec.method == ArraySpec.METHOD_LINEAR);

        curveChoice.setEnabled(spec.method == ArraySpec.METHOD_CURVE);
        curveCopiesField.setEnabled(spec.method == ArraySpec.METHOD_CURVE & spec.curveMode == ArraySpec.MODE_COPIES);
        curveCopiesBox.setEnabled(spec.method == ArraySpec.METHOD_CURVE);
        curveStepField.setEnabled(spec.method == ArraySpec.METHOD_CURVE & spec.curveMode == ArraySpec.MODE_STEP);
        curveStepBox.setEnabled(spec.method == ArraySpec.METHOD_CURVE);
        orientationBox.setEnabled(spec.method == ArraySpec.METHOD_CURVE);
        useOriginBox.setEnabled(spec.method == ArraySpec.METHOD_CURVE);
        useOrientationBox.setEnabled(spec.method == ArraySpec.METHOD_CURVE);

    }

}
