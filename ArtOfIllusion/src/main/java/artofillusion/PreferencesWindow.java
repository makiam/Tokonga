/* Copyright (C) 2002-2009 by Peter Eastman
   Changes Copyright (C) 2016-2019 by Petri Ihalainen
   Changes copyright (C) 2017-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.ui.*;
import artofillusion.keystroke.*;
import artofillusion.preferences.AppearancePreferencesPanel;
import artofillusion.preferences.ExtraPluginsPane;
import artofillusion.preferences.PreferencesEditor;
import buoy.widget.*;
import buoy.event.*;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * This is the window for editing application-wide preferences.
 */
public class PreferencesWindow {

    private PreferencesEditor appearance = new AppearancePreferencesPanel();
    private PreferencesEditor extras = new ExtraPluginsPane();
    private PreferencesEditor keystrokePanel = new KeystrokePreferencesPanel();
    
    private BComboBox defaultRendChoice, objectRendChoice, texRendChoice, toolChoice;
    private ValueField interactiveTolField, undoField, animationDurationField, animationFrameRateField;
    private BCheckBox drawActiveFrustumBox, drawCameraFrustumBox, showTravelCuesOnIdleBox, showTravelCuesScrollingBox;
    private BCheckBox showTiltDialBox;
    private BCheckBox glBox, backupBox, reverseZoomBox, useViewAnimationsBox;

    private static int lastTab;
    private boolean cameraFrustumState, travelCuesState;

    public PreferencesWindow(BFrame parent) {
        BTabbedPane tabs = new BTabbedPane();
        tabs.add(createGeneralPanel(), Translate.text("general"));
        
        tabs.add(appearance.getPreferencesPanel(), appearance.getName());        
        tabs.add(keystrokePanel.getPreferencesPanel(), keystrokePanel.getName());        
        tabs.add(extras.getPreferencesPanel(), extras.getName());
        
        tabs.setSelectedTab(lastTab);
        boolean done = false;

        while (!done) {
            PanelDialog dlg = new PanelDialog(parent, Translate.text("prefsTitle"), tabs);
            lastTab = tabs.getSelectedTab();
            if (!dlg.clickedOk()) {
                return;
            }
            done = true;
            if (interactiveTolField.getValue() < 0.01) {
                String[] options = MessageDialog.getOptions();
                int choice = new BStandardDialog("", Translate.text("lowSurfErrorWarning"), BStandardDialog.WARNING).showOptionDialog(parent, options, options[0]);
                if (choice == 1) {
                    done = false;
                }
            }
        }
        ApplicationPreferences preferences = ArtOfIllusion.getPreferences();
        
        List<Renderer> renderers = PluginRegistry.getPlugins(Renderer.class);
        if (!renderers.isEmpty()) {
            preferences.setDefaultRenderer(renderers.get(defaultRendChoice.getSelectedIndex()));
            preferences.setObjectPreviewRenderer(renderers.get(objectRendChoice.getSelectedIndex()));
            preferences.setTexturePreviewRenderer(renderers.get(texRendChoice.getSelectedIndex()));
        }
        preferences.setInteractiveSurfaceError(interactiveTolField.getValue());
        preferences.setUndoLevels((int) undoField.getValue());

        if (preferences.getUseOpenGL() != glBox.getState()) {
            MessageDialog.create().withOwner(parent.getComponent()).info(Translate.text("glChangedWarning"));
        }

        preferences.setUseOpenGL(glBox.getState());
        preferences.setKeepBackupFiles(backupBox.getState());
        preferences.setReverseZooming(reverseZoomBox.getState());
        preferences.setUseViewAnimations(useViewAnimationsBox.getState());
        preferences.setMaxAnimationDuration(animationDurationField.getValue());
        preferences.setAnimationFrameRate(animationFrameRateField.getValue());
        preferences.setDrawActiveFrustum(drawActiveFrustumBox.getState());
        preferences.setDrawCameraFrustum(cameraFrustumState);
        preferences.setShowTravelCuesOnIdle(showTravelCuesOnIdleBox.getState());
        preferences.setShowTravelCuesScrolling(travelCuesState);
        preferences.setShowTiltDial(showTiltDialBox.getState());

        preferences.setUseCompoundMeshTool(toolChoice.getSelectedIndex() == 1);

        preferences.savePreferences();
        
        keystrokePanel.savePreferences();        
        appearance.savePreferences();
        extras.savePreferences();
    }

    /**
     * Create a Choice for selecting a renderer.
     */
    private BComboBox getRendererChoice(Renderer selected) {
        List<Renderer> renderers = PluginRegistry.getPlugins(Renderer.class);
        BComboBox c = new BComboBox();

        for (Renderer r : renderers) {
            c.add(r.getName());
        }
        if (selected != null) {
            c.setSelectedValue(selected.getName());
        }
        return c;
    }

    /**
     * Create the general settings panel.
     */
    private Widget createGeneralPanel() {
        // Create the Widgets.

        ApplicationPreferences prefs = ArtOfIllusion.getPreferences();
        defaultRendChoice = getRendererChoice(prefs.getDefaultRenderer());
        objectRendChoice = getRendererChoice(prefs.getObjectPreviewRenderer());
        texRendChoice = getRendererChoice(prefs.getTexturePreviewRenderer());
        interactiveTolField = new ValueField(prefs.getInteractiveSurfaceError(), ValueField.POSITIVE);
        undoField = new ValueField(prefs.getUndoLevels(), ValueField.POSITIVE + ValueField.INTEGER);
        glBox = new BCheckBox(Translate.text("useOpenGL"), prefs.getUseOpenGL());
        glBox.setEnabled(ViewerCanvas.isOpenGLAvailable());
        backupBox = new BCheckBox(Translate.text("keepBackupFiles"), prefs.getKeepBackupFiles());
        reverseZoomBox = new BCheckBox(Translate.text("reverseScrollWheelZooming"), prefs.getReverseZooming());

        useViewAnimationsBox = new BCheckBox(Translate.text("useViewAnimations"), prefs.getUseViewAnimations());
        animationDurationField = new ValueField(prefs.getMaxAnimationDuration(), ValueField.POSITIVE);
        animationFrameRateField = new ValueField(prefs.getAnimationFrameRate(), ValueField.POSITIVE);
        drawActiveFrustumBox = new BCheckBox(Translate.text("anyActiveView"), prefs.getDrawActiveFrustum());
        cameraFrustumState = prefs.getDrawCameraFrustum();
        drawCameraFrustumBox = new BCheckBox(Translate.text("cameraView"), cameraFrustumState);
        showTravelCuesOnIdleBox = new BCheckBox(Translate.text("onIdle"), prefs.getShowTravelCuesOnIdle());
        travelCuesState = prefs.getShowTravelCuesScrolling();
        showTravelCuesScrollingBox = new BCheckBox(Translate.text("duringScrolling"), travelCuesState);
        showTiltDialBox = new BCheckBox(Translate.text("ShowTiltDial"), prefs.getShowTiltDial());

        useViewAnimationsBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                animationDurationField.setEnabled(useViewAnimationsBox.getState());
                animationFrameRateField.setEnabled(useViewAnimationsBox.getState());
            }
        }
        );

        // Interaction between frustum checkboxes
        drawActiveFrustumBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                drawCameraFrustumBox.setEnabled(!drawActiveFrustumBox.getState());
                if (drawActiveFrustumBox.getState()) {
                    drawCameraFrustumBox.setState(drawActiveFrustumBox.getState());
                } else {
                    drawCameraFrustumBox.setState(cameraFrustumState);
                }
            }
        }
        );
        drawCameraFrustumBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                cameraFrustumState = drawCameraFrustumBox.getState();
            }
        }
        );
        if (drawActiveFrustumBox.getState()) {
            drawCameraFrustumBox.setEnabled(!drawActiveFrustumBox.getState());
            drawCameraFrustumBox.setState(drawActiveFrustumBox.getState());
        }

        // Interaction between scroll cue checkboxes
        showTravelCuesOnIdleBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                showTravelCuesScrollingBox.setEnabled(!showTravelCuesOnIdleBox.getState());
                if (showTravelCuesOnIdleBox.getState()) {
                    showTravelCuesScrollingBox.setState(showTravelCuesOnIdleBox.getState());
                } else {
                    showTravelCuesScrollingBox.setState(travelCuesState);
                }
            }
        }
        );
        showTravelCuesScrollingBox.addEventLink(ValueChangedEvent.class,
                new Object() {
            void processEvent() {
                travelCuesState = showTravelCuesScrollingBox.getState();
            }
        }
        );
        if (showTravelCuesOnIdleBox.getState()) {
            showTravelCuesScrollingBox.setEnabled(!showTravelCuesOnIdleBox.getState());
            showTravelCuesScrollingBox.setState(showTravelCuesOnIdleBox.getState());
        }
        
        toolChoice = new BComboBox(new String[]{
            Translate.text("Move"),
            Translate.text("compoundMoveScaleRotate")
        });
        toolChoice.setSelectedIndex(prefs.getUseCompoundMeshTool() ? 1 : 0);


        // Layout the panel.
        FormContainer panel = new FormContainer(3, 20);
        LayoutInfo labelLayout = new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE, new Insets(2, 5, 2, 5), null);
        LayoutInfo widgetLayout = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.BOTH, new Insets(2, 0, 2, 0), null);

        panel.setColumnWeight(0, 0.0);
        panel.setColumnWeight(1, 0.0);
        panel.setColumnWeight(2, 3.0);


        panel.add(Translate.label("defaultRenderer"), 0, 1, labelLayout);
        panel.add(Translate.label("objPreviewRenderer"), 0, 2, labelLayout);
        panel.add(Translate.label("texPreviewRenderer"), 0, 3, labelLayout);

        panel.add(Translate.label("defaultMeshEditingTool"), 0, 6, labelLayout);
        panel.add(Translate.label("maxUndoLevels"), 0, 7, labelLayout);
        panel.add(Translate.label("interactiveSurfError"), 0, 11, labelLayout);


        panel.add(defaultRendChoice, 1, 1, widgetLayout);
        panel.add(objectRendChoice, 1, 2, widgetLayout);
        panel.add(texRendChoice, 1, 3, widgetLayout);

        panel.add(toolChoice, 1, 6, widgetLayout);
        panel.add(undoField, 1, 7, widgetLayout);
        panel.add(backupBox, 1, 8, 2, 1, widgetLayout);
        panel.add(reverseZoomBox, 1, 9, 2, 1, widgetLayout);
        panel.add(glBox, 1, 10, 2, 1, widgetLayout);
        panel.add(interactiveTolField, 1, 11, widgetLayout);
        panel.add(useViewAnimationsBox, 1, 12, 2, 1, widgetLayout);

        panel.add(Translate.label("maxAnimationDuration"), 0, 13, labelLayout);
        panel.add(Translate.label("animationFrameRate"), 0, 14, labelLayout);
        panel.add(animationDurationField, 1, 13, widgetLayout);
        panel.add(animationFrameRateField, 1, 14, widgetLayout);

        panel.add(Translate.label("DisplayViewFrustumOf"), 0, 15, labelLayout);
        panel.add(drawActiveFrustumBox, 1, 15, 2, 1, widgetLayout);
        panel.add(drawCameraFrustumBox, 1, 16, 2, 1, widgetLayout);

        panel.add(Translate.label("ShowTravelScrollCues"), 0, 17, labelLayout);
        panel.add(showTravelCuesOnIdleBox, 1, 17, 2, 1, widgetLayout);
        panel.add(showTravelCuesScrollingBox, 1, 18, 2, 1, widgetLayout);
        panel.add(showTiltDialBox, 1, 19, 2, 1, widgetLayout);

        return panel;
    }

}
