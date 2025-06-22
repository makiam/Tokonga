/*
 *  Copyright (C) 2006-2007 by Francois Guillet
 *  Changes copyright (C) 2022-2025 by Maksim Khramov
 *
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package artofillusion.polymesh;

import artofillusion.*;
import artofillusion.animation.Joint;
import artofillusion.animation.Skeleton;
import artofillusion.animation.SkeletonTool;
import artofillusion.keystroke.KeystrokeManager;
import artofillusion.keystroke.KeystrokePreferencesPanel;
import artofillusion.keystroke.KeystrokeRecord;
import artofillusion.math.CoordinateSystem;
import artofillusion.math.Vec3;
import artofillusion.object.Curve;
import artofillusion.object.Mesh;
import artofillusion.object.MeshVertex;
import artofillusion.object.Object3D;
import artofillusion.object.ObjectInfo;
import artofillusion.object.TriangleMesh;
import artofillusion.polymesh.PolyMesh.Wedge;
import artofillusion.polymesh.PolyMesh.Wface;
import artofillusion.polymesh.PolyMesh.Wvertex;
import artofillusion.polymesh.PolyMeshValueWidget.ValueWidgetOwner;
import artofillusion.texture.FaceParameterValue;
import artofillusion.texture.ParameterValue;
import artofillusion.texture.VertexParameterValue;
import artofillusion.ui.*;
import buoy.event.CommandEvent;
import buoy.event.EventSource;
import buoy.event.KeyPressedEvent;
import buoy.event.ValueChangedEvent;
import buoy.event.WidgetEvent;
import buoy.event.WidgetMouseEvent;
import buoy.widget.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.ChangeEvent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The PolyMeshEditorWindow class represents the window for editing PolyMesh
 * objects.
 *
 * @author Francois Guillet
 */
@Slf4j
public class PolyMeshEditorWindow extends MeshEditorWindow implements EditingWindow, PopupMenuManager, ValueWidgetOwner {

    private final ToolPalette modes;

    private final Runnable onClose;

    private BMenuItem[] editMenuItem;

    private BMenuItem[] meshMenuItem;

    private BCheckBoxMenuItem[] smoothItem;

    private BMenuItem[] mirrorItem;

    private final BMenu vertexMenu = Translate.menu("polymesh:vertex");

    private final BPopupMenu vertexPopupMenu = new BPopupMenu();

    private MenuWidget[] vertexMenuItem;

    private MenuWidget[] vertexPopupMenuItem;

    private final BMenu edgeMenu = Translate.menu("polymesh:edge");

    private final BPopupMenu edgePopupMenu = new BPopupMenu();

    private MenuWidget[] edgeMenuItem;

    private MenuWidget[] edgePopupMenuItem;

    private final BMenu faceMenu = Translate.menu("polymesh:face");

    private final BPopupMenu facePopupMenu = new BPopupMenu();

    private BMenuItem faceFindSimilarMenuItem;

    private BMenuItem faceFindSimilarPopupMenuItem;

    private MenuWidget[] skeletonMenuItem;

    private BMenuItem pasteItem;

    private final RowContainer levelContainer;

    private final RowContainer vertexContainer;

    private final RowContainer edgeContainer;

    private final RowContainer faceContainer;

    private final OverlayContainer overlayVertexEdgeFace;

    private final BSpinner tensionSpin;
    private final BSpinner ispin;

    private final ValueSlider edgeSlider;

    private final BCheckBox cornerCB;

    private final EditingTool reshapeMeshTool = new MeshStandardTool(this, this);

    public static final int RESHAPE_TOOL = 0;

    private final EditingTool skewMeshTool = new SkewMeshTool(this, this);

    public static final int SKEW_TOOL = 1;

    private final EditingTool taperMeshTool = new TaperMeshTool(this, this);

    public static final int TAPER_TOOL = 2;

    private final EditingTool bevelTool = new AdvancedBevelExtrudeTool(this, this);

    public static final int BEVEL_TOOL = 3;

    private final EditingTool thickenMeshTool = new ThickenMeshTool(this, this);

    public static final int THICKEN_TOOL = 4;

    private final EditingTool extrudeTool = new AdvancedExtrudeTool(this, this);

    public static final int EXTRUDE_TOOL = 5;

    private final EditingTool knifeTool = new PMKnifeTool(this, this);

    public static final int KNIFE_TOOL = 6;

    private final EditingTool createFaceTool = new PMCreateFaceTool(this, this);

    public static final int CREATE_FACE_TOOL = 7;

    private final EditingTool extrudeCurveTool = new PMExtrudeCurveTool(this, this);

    public static final int EXTRUDE_CURVE_TOOL = 8;

    private final EditingTool sewTool = new PMSewTool(this, this);

    public static final int SEW_TOOL = 9;

    private final EditingTool skeletonTool = new SkeletonTool(this, true);

    public static final int SKELETON_TOOL = 10;

    private boolean realView;

    private boolean realMirror;

    private int smoothingMethod;

    private PolyMesh priorValueMesh;

    private boolean[] valueSelection;

    private short moveDirection;

    private final PolyMeshValueWidget valueWidget;

    private final BDialog valueWidgetDialog;

    private final BMenuItem extrudeItem = Translate.menuItem("polymesh:extrudeNormal", this::doExtrudeNormal);
    private final BMenuItem extrudeEdgeItem = Translate.menuItem("polymesh:extrudeNormal", this::doExtrudeEdgeNormal);

    private final BMenuItem extrudeRegionItem = Translate.menuItem("polymesh:extrudeRegionNormal", this::doExtrudeRegionNormal);
    private final BMenuItem extrudeEdgeRegionItem = Translate.menuItem("polymesh:extrudeRegionNormal", this::doExtrudeEdgeRegionNormal);

    private Vec3 direction;

    private int[] selectionDistance;
    private int maxDistance;
    private int selectMode;

    private int[] projectedEdge;

    private boolean[] selected;

    private Vec3[] vertDisplacements;

    private static EventSource eventSource;

    private static PolyMesh clipboardMesh;

    protected boolean tolerant;

    private TextureParameter faceIndexParam, jointWeightParam;

    protected boolean[] hideFace;

    protected boolean[] hideVert;

    protected boolean[] selPoints;

    protected Vec3 selCenter;

    protected double meanSelDistance;

    private boolean projectOntoSurface;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private static double normalTol = 0.01;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private static double looseShapeTol = 0.01;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private static double strictShapeTol = 0.01;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private static double edgeTol = 0.01;

    protected static boolean lastFreehand, lastProjectOntoSurface,
            lastTolerant;

    private RenderingMesh lastPreview;

    private final GenericTool pointTool;
    private final GenericTool edgeTool;
    private final GenericTool faceTool;

    private short mirror;

    private boolean thickenFaces;

    private Shortcut singleNormalShortcut, groupNormalShortcut;

    private static boolean selectVisible = false;

    private static boolean looseSelect = false;

    private static int looseSelectValue = 20;

    private final BCheckBox frontSelectCB;

    private final BCheckBox looseSelectCB;

    private final BSpinner looseSelectSpinner;

    private boolean unseenValueWidgetDialog;

    private final Map<Integer, EditingTool> toolsMap = new HashMap<>();

    {
        toolsMap.put(RESHAPE_TOOL, reshapeMeshTool);
        toolsMap.put(SKEW_TOOL, skewMeshTool);
        toolsMap.put(TAPER_TOOL, taperMeshTool);
        toolsMap.put(BEVEL_TOOL, bevelTool);
        toolsMap.put(EXTRUDE_TOOL, extrudeTool);
        toolsMap.put(EXTRUDE_CURVE_TOOL, extrudeCurveTool);
        toolsMap.put(THICKEN_TOOL, thickenMeshTool);
        toolsMap.put(CREATE_FACE_TOOL, createFaceTool);
        toolsMap.put(KNIFE_TOOL, knifeTool);
        toolsMap.put(SEW_TOOL, sewTool);
        toolsMap.put(SKELETON_TOOL, skeletonTool);
    }

    /**
     * Constructor for the PolyMeshEditorWindow object
     *
     * @param parent
     * the window from which this command is being invoked
     * @param title
     * Window title
     * @param obj
     * the ObjectInfo corresponding to this object
     * @param onClose
     * a callback which will be executed when editing is over
     */
    public PolyMeshEditorWindow(EditingWindow parent, String title, ObjectInfo obj, Runnable onClose) {
        super(parent, title, obj);
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (eventSource == null) {
            eventSource = new EventSource();
        }
        eventSource.addEventLink(CopyEvent.class, this, "doCopyEvent");
        hideVert = new boolean[mesh.getVertices().length];
        this.onClose = onClose;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        FormContainer content = new FormContainer(new double[]{0, 1, 0}, new double[]{1, 0, 0, 0});
        setContent(content);
        valueWidget = new PolyMeshValueWidget(this);
        valueWidgetDialog = new BDialog(this, "choose value", true);
        valueWidgetDialog.setContent(valueWidget);
        valueWidgetDialog.pack();
        unseenValueWidgetDialog = true;

        content.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        BorderContainer widgets = new BorderContainer();
        RowContainer meshContainer = new RowContainer();
        levelContainer = new RowContainer();
        vertexContainer = new RowContainer();
        edgeContainer = new RowContainer();
        faceContainer = new RowContainer();
        meshContainer.add(looseSelectCB = new BCheckBox(Translate.text("polymesh:looseSelect"), looseSelect));
        looseSelectCB.getComponent().addActionListener(e -> doLooseSelectionChanged());
        meshContainer.add(looseSelectSpinner = new BSpinner(looseSelectValue, 1, 100, 1));
        looseSelectSpinner.getComponent().addChangeListener(this::doLooseSelectionValueChanged);
        meshContainer.add(frontSelectCB = new BCheckBox(Translate.text("polymesh:frontSelect"), selectVisible));
        frontSelectCB.getComponent().addActionListener(e -> doFrontSelectionChanged());
        meshContainer.add(new BLabel(Translate.text("polymesh:meshTension") + ": "));
        tensionSpin = new BSpinner(tensionDistance, 0, 999, 1);
        setSpinnerColumns(tensionSpin, 3);
        tensionSpin.getComponent().addChangeListener(this::doTensionChanged);
        meshContainer.add(tensionSpin);

        levelContainer.add(Translate.label("polymesh:interactiveSubdiv"));
        ispin = new BSpinner(1, 1, 6, 1);
        levelContainer.add(ispin);
        var mis = mesh.getInteractiveSmoothLevel();
        log.info("Mesh interactive smooth level: {}", mis);
        ispin.setValue(mis);
        ispin.getComponent().addChangeListener(this::onInteractiveLevelValueChange);

        meshContainer.add(levelContainer);
        cornerCB = new BCheckBox(Translate.text("polymesh:corner"), false);
        cornerCB.getComponent().addItemListener(this::onCornerCheckboxValueChange);

        vertexContainer.add(cornerCB);
        edgeSlider = new ValueSlider(0.0, 1.0, 1000, 0.0);
        edgeSlider.addEventLink(ValueChangedEvent.class, this, "doEdgeSliderChanged");

        edgeContainer.add(new BLabel(Translate.text("polymesh:smoothness")));
        edgeContainer.add(edgeSlider);
        overlayVertexEdgeFace = new OverlayContainer();
        overlayVertexEdgeFace.add(faceContainer);
        overlayVertexEdgeFace.add(edgeContainer);
        overlayVertexEdgeFace.add(vertexContainer);
        widgets.add(overlayVertexEdgeFace, BorderContainer.WEST);
        widgets.add(meshContainer, BorderContainer.EAST);
        content.add(widgets, 0, 1, 3, 1);
        content.add(helpText = new BLabel(), 0, 2, 3, 1);
        content.add(viewsContainer, 1, 0);
        RowContainer buttons = new RowContainer();
        buttons.add(Translate.button("ok", event -> doOk()));
        buttons.add(Translate.button("cancel", event -> doCancel()));
        content.add(buttons, 0, 3, 2, 1, new LayoutInfo());
        FormContainer toolsContainer = new FormContainer(new double[]{1}, new double[]{1, 0});
        toolsContainer.setDefaultLayout(new LayoutInfo(LayoutInfo.NORTH, LayoutInfo.BOTH));
        content.add(toolsContainer, 0, 0);
        toolsContainer.add(tools = new ToolPalette(1, 12), 0, 0);
        tools.addTool(defaultTool = reshapeMeshTool);
        tools.addTool(skewMeshTool);
        tools.addTool(taperMeshTool);
        tools.addTool(bevelTool);
        tools.addTool(extrudeTool);
        tools.addTool(thickenMeshTool);
        tools.addTool(knifeTool);
        tools.addTool(createFaceTool);
        tools.addTool(extrudeCurveTool);
        tools.addTool(sewTool);
        tools.addTool(skeletonTool);
        EditingTool metaTool;
        tools.addTool(metaTool = new MoveViewTool(this));
        EditingTool altTool;
        tools.addTool(altTool = new RotateViewTool(this));
        tools.selectTool(defaultTool);
        loadPreferences();
        for (int i = 0; i < theView.length; i++) {
            MeshViewer view = (MeshViewer) theView[i];
            view.setMetaTool(metaTool);
            view.setAltTool(altTool);
            view.setScene(parent.getScene(), obj);
            view.setFreehandSelection(lastFreehand);
            view.setPopupMenuManager(this);
        }
        tolerant = lastTolerant;
        projectOntoSurface = lastProjectOntoSurface;
        toolsContainer.add(modes = new ToolPalette(1, 3), 0, 1);
        modes.addTool(pointTool = new GenericTool(this, "polymesh:pmpoint", Translate.text("pointSelectionModeTool.tipText")));
        modes.addTool(edgeTool = new GenericTool(this, "polymesh:pmedge", Translate.text("edgeSelectionModeTool.tipText")));
        modes.addTool(faceTool = new GenericTool(this, "polymesh:pmface", Translate.text("faceSelectionModeTool.tipText")));
        setSelectionMode(modes.getSelection());
        UIUtilities.applyDefaultFont(content);
        UIUtilities.applyDefaultBackground(content);
        createPreferencesMenu();
        createEditMenu();
        createMeshMenu((PolyMesh) objInfo.object);
        createVertexMenu();
        createEdgeMenu();
        createFaceMenu();
        createSkeletonMenu();
        createTextureMenu();
        createViewMenu();
        recursivelyAddListeners(this);
        Dimension d1 = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension d2;
        d2 = new Dimension((d1.width * 3) / 4, (d1.height * 3) / 4);
        setBounds(new Rectangle((d1.width - d2.width) / 2, (d1.height - d2.height) / 2, d2.width, d2.height));
        tools.requestFocus();
        updateMenus();
        realView = false;
        realMirror = false;
        addExtraParameters();
        doLevelContainerEnable();
        selected = new boolean[((Mesh) objInfo.object).getVertices().length];
        // addEventLink( WindowClosingEvent.class, this, "doCancel" );

        overlayVertexEdgeFace.setVisibleChild(vertexContainer);
    }

    /**
     * Builds the edit menu
     *
     */
    private void createEditMenu() {
        BMenu editMenu = Translate.menu("edit");
        menubar.add(editMenu);
        editMenuItem = new BMenuItem[12];
        editMenu.add(undoItem = Translate.menuItem("undo", e -> undoCommand()));
        editMenu.add(redoItem = Translate.menuItem("redo", e -> redoCommand()));
        editMenu.addSeparator();
        editMenu.add(Translate.menuItem("polymesh:copy", e -> doCopy()));
        editMenu.add(pasteItem = Translate.menuItem("polymesh:paste", e -> doPaste()));
        if (clipboardMesh == null) {
            pasteItem.setEnabled(false);
        }
        editMenu.addSeparator();
        editMenu.add(editMenuItem[0] = Translate.menuItem("clear", event -> deleteCommand()));
        editMenu.add(editMenuItem[1] = Translate.menuItem("selectAll", this::selectAllCommand));
        editMenu.add(editMenuItem[2] = Translate.menuItem("polymesh:showNormal", this::bringNormal));
        editMenu.add(editMenuItem[3] = Translate.menuItem("extendSelection", this::extendSelectionCommand));
        editMenu.add(Translate.menuItem("invertSelection", this::invertSelectionCommand));
        editMenu.add(editMenuItem[4] = Translate.menuItem("polymesh:scaleSelection", this::scaleSelectionCommand));
        editMenu.add(editMenuItem[5] = Translate.menuItem("polymesh:scaleNormal", this::scaleNormalSelectionCommand));
        editMenu.add(editMenuItem[6] = Translate.checkboxMenuItem("tolerantSelection", this, "tolerantModeChanged", lastTolerant));
        editMenu.add(editMenuItem[7] = Translate.checkboxMenuItem("freehandSelection", this, "freehandModeChanged", lastFreehand));
        editMenu.add(editMenuItem[8] = Translate.checkboxMenuItem("projectOntoSurface", this, "projectModeChanged", lastProjectOntoSurface));
        editMenu.addSeparator();
        editMenu.add(editMenuItem[9] = Translate.menuItem("hideSelection", this::doHideSelection));
        editMenu.add(editMenuItem[10] = Translate.menuItem("showAll", this::doShowAll));
        editMenu.addSeparator();
        editMenu.add(Translate.menuItem("polymesh:editDisplayProperties", this::doEditProperties));
    }

    /**
     * Builds the mesh menu
     *
     * @param obj
     * The winged mesh being edited
     */
    void createMeshMenu(PolyMesh obj) {
        PolyMesh mesh = (PolyMesh) objInfo.object;

        BMenu meshMenu = Translate.menu("mesh");
        menubar.add(meshMenu);
        meshMenuItem = new BMenuItem[5];
        BMenu smoothMenu;
        meshMenu.add(Translate.menuItem("polymesh:centerMesh", this::doCenterMesh));
        meshMenu.add(smoothMenu = Translate.menu("smoothingMethod"));
        smoothItem = new BCheckBoxMenuItem[2];
        smoothMenu.add(smoothItem[0] = Translate.checkboxMenuItem("none", this, "smoothingChanged", obj.getSmoothingMethod() == Mesh.NO_SMOOTHING));
        smoothMenu.add(smoothItem[1] = Translate.checkboxMenuItem("approximating", this, "smoothingChanged", obj.getSmoothingMethod() == Mesh.APPROXIMATING));

        meshMenu.add(meshMenuItem[0] = Translate.menuItem("polymesh:controlledSmoothing", this::doControlledSmoothing));
        meshMenu.add(meshMenuItem[1] = Translate.menuItem("polymesh:smoothMesh", this::doSmoothMesh));
        meshMenu.add(meshMenuItem[2] = Translate.menuItem("polymesh:subdivideMesh", this::doSubdivideMesh));

        meshMenu.add(meshMenuItem[3] = Translate.menuItem("polymesh:thickenMeshFaceNormal", this, "doThickenMesh"));
        meshMenu.add(meshMenuItem[4] = Translate.menuItem("polymesh:thickenMeshVertexNormal", this, "doThickenMesh"));
        var mirrorMenu = Translate.menu("polymesh:mirrorMesh");
        meshMenu.add(mirrorMenu);
        mirrorItem = new BMenuItem[4];
        mirrorMenu.add(mirrorItem[0] = Translate.menuItem("polymesh:mirrorOff", this::doTurnMirrorOff));
        mirrorMenu.add(mirrorItem[1] = Translate.checkboxMenuItem("polymesh:mirrorOnXY", this, "doMirrorOn", false));
        mirrorMenu.add(mirrorItem[2] = Translate.checkboxMenuItem("polymesh:mirrorOnXZ", this, "doMirrorOn", false));
        mirrorMenu.add(mirrorItem[3] = Translate.checkboxMenuItem("polymesh:mirrorOnYZ", this, "doMirrorOn", false));
        if ((mesh.getMirrorState() & PolyMesh.MIRROR_ON_XY) != 0) {
            ((BCheckBoxMenuItem) mirrorItem[1]).setState(true);
        }
        if ((mesh.getMirrorState() & PolyMesh.MIRROR_ON_XZ) != 0) {
            ((BCheckBoxMenuItem) mirrorItem[2]).setState(true);
        }
        if ((mesh.getMirrorState() & PolyMesh.MIRROR_ON_YZ) != 0) {
            ((BCheckBoxMenuItem) mirrorItem[3]).setState(true);
        }
        BMenu mirrorWholeMesh;
        meshMenu.add(mirrorWholeMesh = Translate.menu("polymesh:mirrorWholeMesh"));
        mirrorWholeMesh.add(Translate.menuItem("polymesh:mirrorOnXY", this::doMirrorWholeXY));
        mirrorWholeMesh.add(Translate.menuItem("polymesh:mirrorOnYZ", this::doMirrorWholeYZ));
        mirrorWholeMesh.add(Translate.menuItem("polymesh:mirrorOnXZ", this::doMirrorWholeXZ));
        meshMenu.add(Translate.menuItem("invertNormals", this::doInvertNormals));
        meshMenu.add(Translate.menuItem("meshTension", e -> setTensionCommand()));
        meshMenu.add(Translate.menuItem("polymesh:checkMesh", this::doCheckMesh));
        meshMenu.addSeparator();
        meshMenu.add(Translate.menuItem("polymesh:saveAsTemplate", this::doSaveAsTemplate));
    }

    /**
     * Builds the vertex menu
     */
    void createVertexMenu() {

        menubar.add(vertexMenu);
        vertexMenuItem = new MenuWidget[16];
        vertexMenu.add(vertexMenuItem[0] = Translate.menuItem("polymesh:connect", this::doConnectVertices));
        BMenu local = Translate.menu("polymesh:moveAlong");
        local.add(Translate.menuItem("polymesh:normal", this::doMoveVerticesNormal));
        local.add(Translate.menuItem("polymesh:x", this::doMoveVerticesX));
        local.add(Translate.menuItem("polymesh:y", this::doMoveVerticesY));
        local.add(Translate.menuItem("polymesh:z", this::doMoveVerticesZ));
        vertexMenu.add(vertexMenuItem[1] = local);

        vertexMenu.add(vertexMenuItem[2] = Translate.menuItem("polymesh:collapse", this::doCollapseVertices));
        vertexMenu.add(vertexMenuItem[3] = Translate.menuItem("polymesh:facet", this::doFacetVertices));
        vertexMenu.add(vertexMenuItem[4] = Translate.menuItem("polymesh:bevel", this::doBevelVertices));
        vertexMenu.addSeparator();
        vertexMenu.add(vertexMenuItem[5] = Translate.menuItem("polymesh:meanSphere", this::doMeanSphere));
        vertexMenu.add(vertexMenuItem[6] = Translate.menuItem("polymesh:closestSphere", this::doClosestSphere));
        vertexMenu.add(vertexMenuItem[7] = Translate.menuItem("polymesh:plane", this::doPlane));
        vertexMenu.addSeparator();
        vertexMenu.add(vertexMenuItem[8] = Translate.menuItem("polymesh:selectBoundary", this::doSelectBoundary));
        vertexMenu.add(vertexMenuItem[9] = Translate.menuItem("polymesh:closeBoundary", this::doCloseBoundary));
        vertexMenu.add(vertexMenuItem[10] = Translate.menuItem("polymesh:joinBoundaries", this::doJoinBoundaries));
        vertexMenu.addSeparator();
        vertexMenu.add(vertexMenuItem[11] = Translate.menuItem("editPoints", e -> setPointsCommand()));
        vertexMenu.add(vertexMenuItem[12] = Translate.menuItem("transformPoints", e -> transformPointsCommand()));
        vertexMenu.add(vertexMenuItem[13] = Translate.menuItem("randomize", e -> randomizeCommand()));
        vertexMenu.addSeparator();
        vertexMenu.add(vertexMenuItem[14] = Translate.menuItem("parameters", e -> setParametersCommand()));
        vertexMenu.addSeparator();
        vertexMenu.add(vertexMenuItem[15] = Translate.menuItem("polymesh:selectCorners", this::doSelectCorners));

        vertexPopupMenuItem = new MenuWidget[16];
        vertexPopupMenu.add(vertexPopupMenuItem[0] = Translate.menuItem("polymesh:connect", this::doConnectVertices));
        local = Translate.menu("polymesh:moveAlong");
        local.add(Translate.menuItem("polymesh:normal", this::doMoveVerticesNormal));
        local.add(Translate.menuItem("polymesh:x", this::doMoveVerticesX));
        local.add(Translate.menuItem("polymesh:y", this::doMoveVerticesY));
        local.add(Translate.menuItem("polymesh:z", this::doMoveVerticesZ));
        vertexPopupMenu.add(vertexPopupMenuItem[1] = local);

        vertexPopupMenu.add(vertexPopupMenuItem[2] = Translate.menuItem("polymesh:collapse", this::doCollapseVertices));
        vertexPopupMenu.add(vertexPopupMenuItem[3] = Translate.menuItem("polymesh:facet", this::doFacetVertices));
        vertexPopupMenu.add(vertexPopupMenuItem[4] = Translate.menuItem("polymesh:bevel", this::doBevelVertices));
        vertexPopupMenu.addSeparator();
        vertexPopupMenu.add(vertexPopupMenuItem[5] = Translate.menuItem("polymesh:meanSphere", this::doMeanSphere));
        vertexPopupMenu.add(vertexPopupMenuItem[6] = Translate.menuItem("polymesh:closestSphere", this::doClosestSphere));
        vertexPopupMenu.add(vertexPopupMenuItem[7] = Translate.menuItem("polymesh:plane", this::doPlane));
        vertexPopupMenu.addSeparator();
        vertexPopupMenu.add(vertexPopupMenuItem[8] = Translate.menuItem("polymesh:selectBoundary", this::doSelectBoundary));
        vertexPopupMenu.add(vertexPopupMenuItem[9] = Translate.menuItem("polymesh:closeBoundary", this::doCloseBoundary));
        vertexPopupMenu.add(vertexPopupMenuItem[10] = Translate.menuItem("polymesh:joinBoundaries", this::doJoinBoundaries));
        vertexPopupMenu.addSeparator();
        vertexPopupMenu.add(vertexPopupMenuItem[11] = Translate.menuItem("editPoints", e -> setPointsCommand()));
        vertexPopupMenu.add(vertexPopupMenuItem[12] = Translate.menuItem("transformPoints", e -> transformPointsCommand()));
        vertexPopupMenu.add(vertexPopupMenuItem[13] = Translate.menuItem("randomize", e -> randomizeCommand()));
        vertexPopupMenu.addSeparator();
        vertexPopupMenu.add(vertexPopupMenuItem[14] = Translate.menuItem("parameters", e -> setParametersCommand()));
        vertexPopupMenu.addSeparator();
        vertexPopupMenu.add(vertexPopupMenuItem[15] = Translate.menuItem("polymesh:selectCorners", this::doSelectCorners));

    }

    /**
     * Builds the edge menu
     *
     */
    void createEdgeMenu() {

        menubar.add(edgeMenu);
        edgeMenuItem = new MenuWidget[22];

        BMenu local = Translate.menu("polymesh:divide");
        local.add(Translate.menuItem("polymesh:two", this::doDivideEdgesTwo));
        local.add(Translate.menuItem("polymesh:three", this::doDivideEdgesThree));
        local.add(Translate.menuItem("polymesh:four", this::doDivideEdgesFour));
        local.add(Translate.menuItem("polymesh:five", this::doDivideEdgesFive));
        local.add(Translate.menuItem("polymesh:specify", this::doDivideEdgesInteractive));
        edgeMenu.add(edgeMenuItem[0] = local);

        local = Translate.menu("polymesh:moveAlong");
        local.add(Translate.menuItem("polymesh:normal", this::doMoveEdgesNormal));
        local.add(Translate.menuItem("polymesh:x", this::doMoveEdgesX));
        local.add(Translate.menuItem("polymesh:y", this::doMoveEdgesY));
        local.add(Translate.menuItem("polymesh:z", this::doMoveEdgesZ));

        edgeMenu.add(edgeMenuItem[1] = local);
        edgeMenu.addSeparator();

        local = Translate.menu("polymesh:extrude");
        local.add(extrudeEdgeItem);
        local.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeEdgeX));
        local.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeEdgeY));
        local.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeEdgeZ));
        edgeMenu.add(edgeMenuItem[2] = local);

        singleNormalShortcut = extrudeEdgeItem.getShortcut();

        local = Translate.menu("polymesh:extrudeRegion");
        local.add(extrudeEdgeRegionItem);
        local.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeEdgeRegionX));
        local.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeEdgeRegionY));
        local.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeEdgeRegionZ));
        edgeMenu.add(edgeMenuItem[3] = local);

        groupNormalShortcut = extrudeEdgeRegionItem.getShortcut();
        edgeMenu.addSeparator();

        edgeMenu.add(edgeMenuItem[4] = Translate.menuItem("polymesh:collapse", this::doCollapseEdges));
        edgeMenu.add(edgeMenuItem[5] = Translate.menuItem("polymesh:merge", this::doMergeEdges));
        edgeMenu.add(edgeMenuItem[6] = Translate.menuItem("polymesh:bevel", this::doBevelEdges));
        edgeMenu.addSeparator();
        edgeMenu.add(edgeMenuItem[7] = Translate.menuItem("polymesh:selectLoop", this::doSelectLoop));

        local = Translate.menu("polymesh:selectRing");
        local.add(Translate.menuItem("polymesh:all", this::doSelectRingAll));
        local.add(Translate.menuItem("polymesh:two", this::doSelectRingTwo));
        local.add(Translate.menuItem("polymesh:three", this::doSelectRingThree));
        local.add(Translate.menuItem("polymesh:four", this::doSelectRingFour));
        local.add(Translate.menuItem("polymesh:five", this::doSelectRingFive));
        local.add(Translate.menuItem("polymesh:specify", this::doSelectRingInteractive));
        edgeMenu.add(edgeMenuItem[8] = local);

        edgeMenu.add(edgeMenuItem[9] = Translate.menuItem("polymesh:insertLoops", this::doInsertLoops));
        edgeMenu.add(edgeMenuItem[10] = Translate.menuItem("polymesh:selectBoundary", this::doSelectBoundary));
        edgeMenu.add(edgeMenuItem[11] = Translate.menuItem("polymesh:closeBoundary", this::doCloseBoundary));
        edgeMenu.add(edgeMenuItem[12] = Translate.menuItem("polymesh:findSimilar", this::doFindSimilarEdges));
        edgeMenu.add(edgeMenuItem[13] = Translate.menuItem("polymesh:extractToCurve", this::doExtractToCurve));
        edgeMenu.addSeparator();
        edgeMenu.add(edgeMenuItem[14] = Translate.menuItem("polymesh:markSelAsSeams", this::doMarkSelAsSeams));
        edgeMenu.add(edgeMenuItem[15] = Translate.menuItem("polymesh:seamsToSel", this::doSeamsToSel));
        edgeMenu.add(edgeMenuItem[16] = Translate.menuItem("polymesh:addSelToSeams", this::doAddSelToSeams));
        edgeMenu.add(edgeMenuItem[17] = Translate.menuItem("polymesh:removeSelFromSeams", this::doRemoveSelFromSeams));
        edgeMenu.add(edgeMenuItem[18] = Translate.menuItem("polymesh:openSeams", this::doOpenSeams));
        edgeMenu.add(edgeMenuItem[19] = Translate.menuItem("polymesh:clearSeams", this::doClearSeams));
        edgeMenu.addSeparator();
        edgeMenu.add(edgeMenuItem[20] = Translate.menuItem("polymesh:selectSmoothnessRange", this::doSelectEdgeSmoothnessRange));
        edgeMenu.addSeparator();
        edgeMenu.add(edgeMenuItem[21] = Translate.menuItem("polymesh:bevelProperties", this::doBevelProperties));

        edgePopupMenuItem = new MenuWidget[22];
        local = Translate.menu("polymesh:divide");
        local.add(Translate.menuItem("polymesh:two", this::doDivideEdgesTwo));
        local.add(Translate.menuItem("polymesh:three", this::doDivideEdgesThree));
        local.add(Translate.menuItem("polymesh:four", this::doDivideEdgesFour));
        local.add(Translate.menuItem("polymesh:five", this::doDivideEdgesFive));
        local.add(Translate.menuItem("polymesh:specify", this::doDivideEdgesInteractive));
        edgePopupMenu.add(edgePopupMenuItem[0] = local);

        local = Translate.menu("polymesh:moveAlong");
        local.add(Translate.menuItem("polymesh:normal", this::doMoveEdgesNormal));
        local.add(Translate.menuItem("polymesh:x", this::doMoveEdgesX));
        local.add(Translate.menuItem("polymesh:y", this::doMoveEdgesY));
        local.add(Translate.menuItem("polymesh:z", this::doMoveEdgesZ));
        edgePopupMenu.add(edgePopupMenuItem[1] = local);

        edgePopupMenu.addSeparator();
        local = Translate.menu("polymesh:extrude");
        local.add(Translate.menuItem("polymesh:extrudeNormal", this::doExtrudeEdgeNormal));
        local.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeEdgeX));
        local.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeEdgeY));
        local.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeEdgeZ));
        edgePopupMenu.add(edgePopupMenuItem[2] = local);

        local = Translate.menu("polymesh:extrudeRegion");
        local.add(Translate.menuItem("polymesh:extrudeRegionNormal", this::doExtrudeEdgeRegionNormal));
        local.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeEdgeRegionX));
        local.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeEdgeRegionY));
        local.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeEdgeRegionZ));
        edgePopupMenu.add(edgePopupMenuItem[3] = local);

        edgePopupMenu.addSeparator();
        edgePopupMenu.add(edgePopupMenuItem[4] = Translate.menuItem("polymesh:collapse", this::doCollapseEdges));
        edgePopupMenu.add(edgePopupMenuItem[5] = Translate.menuItem("polymesh:merge", this::doMergeEdges));
        edgePopupMenu.add(edgePopupMenuItem[6] = Translate.menuItem("polymesh:bevel", this::doBevelEdges));

        edgePopupMenu.addSeparator();
        edgePopupMenu.add(edgePopupMenuItem[7] = Translate.menuItem("polymesh:selectLoop", this::doSelectLoop));

        local = Translate.menu("polymesh:selectRing");
        local.add(Translate.menuItem("polymesh:all", this::doSelectRingAll));
        local.add(Translate.menuItem("polymesh:two", this::doSelectRingTwo));
        local.add(Translate.menuItem("polymesh:three", this::doSelectRingThree));
        local.add(Translate.menuItem("polymesh:four", this::doSelectRingFour));
        local.add(Translate.menuItem("polymesh:five", this::doSelectRingFive));
        local.add(Translate.menuItem("polymesh:specify", this::doSelectRingInteractive));

        edgePopupMenu.add(edgePopupMenuItem[8] = local);

        edgePopupMenu.add(edgePopupMenuItem[9] = Translate.menuItem("polymesh:insertLoops", this::doInsertLoops));
        edgePopupMenu.add(edgePopupMenuItem[10] = Translate.menuItem("polymesh:selectBoundary", this::doSelectBoundary));
        edgePopupMenu.add(edgePopupMenuItem[11] = Translate.menuItem("polymesh:closeBoundary", this::doCloseBoundary));
        edgePopupMenu.add(edgePopupMenuItem[12] = Translate.menuItem("polymesh:findSimilar", this::doFindSimilarEdges));
        edgePopupMenu.add(edgePopupMenuItem[13] = Translate.menuItem("polymesh:extractToCurve", this::doExtractToCurve));
        edgePopupMenu.addSeparator();
        edgePopupMenu.add(edgePopupMenuItem[14] = Translate.menuItem("polymesh:markSelAsSeams", this::doMarkSelAsSeams));
        edgePopupMenu.add(edgePopupMenuItem[15] = Translate.menuItem("polymesh:seamsToSel", this::doSeamsToSel));
        edgePopupMenu.add(edgePopupMenuItem[16] = Translate.menuItem("polymesh:addSelToSeams", this::doAddSelToSeams));
        edgePopupMenu.add(edgePopupMenuItem[17] = Translate.menuItem("polymesh:removeSelFromSeams", this::doRemoveSelFromSeams));
        edgePopupMenu.add(edgePopupMenuItem[18] = Translate.menuItem("polymesh:openSeams", this::doOpenSeams));
        edgePopupMenu.add(edgePopupMenuItem[19] = Translate.menuItem("polymesh:clearSeams", this::doClearSeams));
        edgePopupMenu.addSeparator();
        edgePopupMenu.add(edgePopupMenuItem[20] = Translate.menuItem("polymesh:selectSmoothnessRange", this::doSelectEdgeSmoothnessRange));
        edgePopupMenu.addSeparator();
        edgePopupMenu.add(edgePopupMenuItem[21] = Translate.menuItem("polymesh:bevelProperties", this::doBevelProperties));
    }

    /**
     * Builds the face menu
     *
     */
    void createFaceMenu() {

        BMenu moveAlong = Translate.menu("polymesh:moveAlong");
        moveAlong.add(Translate.menuItem("polymesh:normal", this::doMoveFacesNormal));
        moveAlong.add(Translate.menuItem("polymesh:x", this::doMoveFacesX));
        moveAlong.add(Translate.menuItem("polymesh:y", this::doMoveFacesY));
        moveAlong.add(Translate.menuItem("polymesh:z", this::doMoveFacesZ));
        faceMenu.add(moveAlong);

        BMenu extrude = Translate.menu("polymesh:extrude");
        extrude.add(extrudeItem);
        extrude.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeX));
        extrude.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeY));
        extrude.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeZ));
        faceMenu.add(extrude);

        BMenu extrudeRegion = Translate.menu("polymesh:extrudeRegion");
        extrudeRegion.add(extrudeRegionItem);
        extrudeRegion.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeRegionX));
        extrudeRegion.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeRegionY));
        extrudeRegion.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeRegionZ));

        faceMenu.add(extrudeRegion);
        faceMenu.addSeparator();

        faceMenu.add(Translate.menuItem("polymesh:smoothFaces", this::doSmoothFaces));
        faceMenu.add(Translate.menuItem("polymesh:subdivideFaces", this::doSubdivideFaces));
        faceMenu.add(Translate.menuItem("polymesh:collapse", this::doCollapseFaces));
        faceMenu.add(Translate.menuItem("polymesh:merge", this::doMergeFaces));
        faceMenu.add(Translate.menuItem("polymesh:triangulate", this::doTriangulateFaces));
        faceMenu.add(Translate.menuItem("polymesh:outlineFaces", this::doOutlineFaces));
        faceMenu.addSeparator();
        faceMenu.add(Translate.menuItem("parameters", event -> setParametersCommand()));
        faceMenu.add(faceFindSimilarMenuItem = Translate.menuItem("polymesh:findSimilar", event -> doFindSimilarFaces()));
        menubar.add(faceMenu);

        moveAlong = Translate.menu("polymesh:moveAlong");
        moveAlong.add(Translate.menuItem("polymesh:normal", this::doMoveFacesNormal));
        moveAlong.add(Translate.menuItem("polymesh:x", this::doMoveFacesX));
        moveAlong.add(Translate.menuItem("polymesh:y", this::doMoveFacesY));
        moveAlong.add(Translate.menuItem("polymesh:z", this::doMoveFacesZ));
        facePopupMenu.add(moveAlong);

        extrude = Translate.menu("polymesh:extrude");
        extrude.add(extrudeItem);
        extrude.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeX));
        extrude.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeY));
        extrude.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeZ));
        facePopupMenu.add(extrude);

        extrudeRegion = Translate.menu("polymesh:extrudeRegion");
        extrudeRegion.add(extrudeRegionItem);
        extrudeRegion.add(Translate.menuItem("polymesh:xExtrude", this::doExtrudeRegionX));
        extrudeRegion.add(Translate.menuItem("polymesh:yExtrude", this::doExtrudeRegionY));
        extrudeRegion.add(Translate.menuItem("polymesh:zExtrude", this::doExtrudeRegionZ));

        facePopupMenu.add(extrudeRegion);

        facePopupMenu.addSeparator();
        facePopupMenu.add(Translate.menuItem("polymesh:smoothFaces", this::doSmoothFaces));
        facePopupMenu.add(Translate.menuItem("polymesh:subdivideFaces", this::doSubdivideFaces));
        facePopupMenu.add(Translate.menuItem("polymesh:collapse", this::doCollapseFaces));
        facePopupMenu.add(Translate.menuItem("polymesh:merge", this::doMergeFaces));
        facePopupMenu.add(Translate.menuItem("polymesh:triangulate", this::doTriangulateFaces));
        facePopupMenu.add(Translate.menuItem("polymesh:outlineFaces", this::doOutlineFaces));
        facePopupMenu.addSeparator();
        facePopupMenu.add(Translate.menuItem("parameters", event -> setParametersCommand()));
        facePopupMenu.add(faceFindSimilarPopupMenuItem = Translate.menuItem("polymesh:findSimilar", event -> doFindSimilarFaces()));
    }

    /**
     * Builds the skeleton menu
     *
     */
    private void createSkeletonMenu() {

        BMenu skeletonMenu = Translate.menu("skeleton");
        menubar.add(skeletonMenu);
        skeletonMenuItem = new BMenuItem[6];
        skeletonMenu.add(skeletonMenuItem[0] = Translate.menuItem("editBone", event -> editJointCommand()));
        skeletonMenu.add(skeletonMenuItem[1] = Translate.menuItem("deleteBone", event -> deleteJointCommand()));
        skeletonMenu.add(skeletonMenuItem[2] = Translate.menuItem("setParentBone", event -> setJointParentCommand()));
        skeletonMenu.add(skeletonMenuItem[3] = Translate.menuItem("importSkeleton", event -> importSkeletonCommand()));
        skeletonMenu.addSeparator();
        skeletonMenu.add(skeletonMenuItem[4] = Translate.menuItem("bindSkeleton", event -> bindSkeletonCommand()));
        skeletonMenu.add(skeletonMenuItem[5] = Translate.checkboxMenuItem("detachSkeleton", this, "skeletonDetachedChanged", false));
    }

    private final BMenuItem unfoldMeshAction = Translate.menuItem("polymesh:unfoldMesh", this::doUnfoldMesh);
    private final BMenuItem editMappingAction = Translate.menuItem("polymesh:editMapping", this::doEditMapping);

    /**
     * Builds the texture menu
     */
    void createTextureMenu() {
        BMenu textureMenu = Translate.menu("polymesh:texture");
        textureMenu.add(unfoldMeshAction);
        textureMenu.add(editMappingAction);
        menubar.add(textureMenu);
    }

    private void createPreferencesMenu() {
        var preferencesMenu = Translate.menu("polymesh:prefs");
        menubar.add(preferencesMenu);
        preferencesMenu.add(Translate.menuItem("polymesh:reloadKeystrokes", this::reloadKeystrokes));
        preferencesMenu.add(Translate.menuItem("polymesh:editKeystrokes", this::editKeystrokes));
        preferencesMenu.addSeparator();
        preferencesMenu.add(Translate.menuItem("polymesh:loadDefaults", this::doLoadDefaultProperties));
        preferencesMenu.add(Translate.menuItem("polymesh:storeDefaults", this::doStoreDefaultProperties));
        preferencesMenu.add(Translate.menuItem("polymesh:resetDefaults", this::doResetDefaultProperties));
    }

    private void doSelectCorners(ActionEvent event) {
        if (selectMode != POINT_MODE) {
            return;
        }
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wvertex[] vertices = (Wvertex[]) mesh.getVertices();
        for (int i = 0; i < selected.length; i++) {
            selected[i] |= (vertices[i].type == Wvertex.CORNER);
        }
        setSelection(selected);
    }

    private void doSelectEdgeSmoothnessRange(ActionEvent event) {
        new EdgeSmoothnessRangeDialog(this).setVisible(true);
    }

    private void doLoadDefaultProperties(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.loadFromDisplayPropertiesPreferences();
        updateImage();
    }

    private void doStoreDefaultProperties(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.storeDisplayPropertiesAsReferences();
    }

    private void doResetDefaultProperties(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.resetDisplayPropertiesPreferences();
        updateImage();
    }

    /**
     * Edits the mesh display properties like colors and handle size
     *
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void doEditProperties(ActionEvent event) {
        new MeshPropertiesDialogAction(this);
    }

    public void doCenterMesh(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        MeshVertex[] v = mesh.getVertices();
        Vec3 center = new Vec3();
        for (var meshVertex : v) {
            center.add(meshVertex.r);
        }
        center.scale(1.0 / v.length);
        for (var meshVertex : v) {
            meshVertex.r.subtract(center);
        }
        mesh.resetMesh();
        objectChanged();
        updateImage();
    }

    /**
     * Delete the selected points, edges, or faces from the mesh.
     */
    @Override
    public void deleteCommand() {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();

        int count = 0;
        for (boolean b : selected) {
            if (b) {
                ++count;
            }
        }
        int[] indices = new int[count];
        count = 0;
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                indices[count++] = i;
            }
        }

        if (selectMode == POINT_MODE) {
            if (mesh.getVertices().length - indices.length < 3) {
                new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")), BStandardDialog.ERROR).showMessageDialog(null);
                return;
            }
            mesh.deleteVertices(indices);
        } else if (selectMode == EDGE_MODE) {
            if (mesh.getEdges().length - indices.length < 3) {
                new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")), BStandardDialog.ERROR).showMessageDialog(null);
                return;
            }
            mesh.deleteEdges(indices);
        } else {
            if (mesh.getFaces().length - indices.length < 1) {
                new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")), BStandardDialog.ERROR).showMessageDialog(null);
                return;
            }
            mesh.deleteFaces(indices);
        }
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();

    }

    /**
     * Select the entire mesh.
     */
    void selectAllCommand(ActionEvent event) {
        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_MESH_SELECTION, this, selectMode, selected.clone()));
        Arrays.fill(selected, true);
        setSelection(selected);
    }

    /**
     * Extend the selection outward by one edge.
     */
    public void extendSelectionCommand(ActionEvent event) {
        PolyMesh theMesh = (PolyMesh) objInfo.object;
        int[] dist = getSelectionDistance();
        boolean[] selectedVert = new boolean[dist.length];
        Wedge[] edges = theMesh.getEdges();

        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_MESH_SELECTION, this, selectMode, selected.clone()));
        for (int i = 0; i < edges.length; i++) {
            if ((dist[edges[i].vertex] == 0 || dist[edges[edges[i].hedge].vertex] == 0)) {
                selectedVert[edges[i].vertex] = selectedVert[edges[edges[i].hedge].vertex] = true;
            }
        }
        if (selectMode == MeshEditController.POINT_MODE) {
            setSelection(selectedVert);
        } else if (selectMode == MeshEditController.EDGE_MODE) {
            for (int i = 0; i < edges.length / 2; i++) {
                selected[i] = (selectedVert[edges[i].vertex] && selectedVert[edges[edges[i].hedge].vertex]);
            }
            setSelection(selected);
        } else {
            Wface[] faces = theMesh.getFaces();
            for (int i = 0; i < faces.length; i++) {
                selected[i] = true;
                int[] fv = theMesh.getFaceVertices(faces[i]);
                for (int k = 0; k < fv.length; ++k) {
                    selected[i] &= selectedVert[fv[k]];
                }
            }
            setSelection(selected);
        }
    }

    /**
     * Selects edge loops from current selection
     */
    public void doSelectLoop(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] loop = mesh.findEdgeLoops(selected);
        if (loop != null) {
            setSelection(loop);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingAll(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] ring = mesh.findEdgeStrips(selected, 1);
        if (ring != null) {
            setSelection(ring);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingTwo(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] ring = mesh.findEdgeStrips(selected, 2);
        if (ring != null) {
            setSelection(ring);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingThree(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] ring = mesh.findEdgeStrips(selected, 3);
        if (ring != null) {
            setSelection(ring);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingFour(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] ring = mesh.findEdgeStrips(selected, 4);
        if (ring != null) {
            setSelection(ring);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingFive(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] ring = mesh.findEdgeStrips(selected, 5);
        if (ring != null) {
            setSelection(ring);
        }
    }

    /**
     * Selects edge rings from current selection
     */
    public void doSelectRingInteractive(ActionEvent event) {

        SwingUtilities.invokeLater(() -> new SelectEdgesDialog(this, value -> {
            PolyMesh mesh = (PolyMesh) objInfo.object;
            boolean[] ring = mesh.findEdgeStrips(selected, value);
            if(ring != null) setSelection(ring);
        }).setVisible(true));

    }

    /**
     * Description of the Method
     */
    private void freehandModeChanged() {
        lastFreehand = ((BCheckBoxMenuItem) editMenuItem[7]).getState();
        for (int i = 0; i < theView.length; i++) {
            ((PolyMeshViewer) theView[i]).setFreehandSelection(((BCheckBoxMenuItem) editMenuItem[7]).getState());
        }
        savePreferences();
    }

    private void projectModeChanged() {
        setProjectOntoSurface(((BCheckBoxMenuItem) editMenuItem[8]).getState());
        updateImage();
    }

    /**
     * Get whether the control mesh is displayed projected onto the surface.
     */
    public boolean getProjectOntoSurface() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        return projectOntoSurface && (mesh.getSmoothingMethod() == Mesh.APPROXIMATING || mesh.getSmoothingMethod() == Mesh.INTERPOLATING);
    }

    /**
     * Set whether the control mesh is displayed projected onto the surface.
     */
    public void setProjectOntoSurface(boolean project) {
        lastProjectOntoSurface = projectOntoSurface = project;
        savePreferences();
    }

    /**
     * Determine which edge of the control mesh corresponds to each edge of
     * the subdivided mesh. If the control mesh is not being projected onto
     * the surface, this returns null.
     */
    int[] findProjectedEdges() {
        // See if we actually want to project the control mesh.
        if (!getProjectOntoSurface()) {
            lastPreview = null;
            return null;
        }

        // See whether we need to rebuild to list of projected edges.
        PolyMesh mesh = (PolyMesh) objInfo.object;
        RenderingMesh preview = getObject().getPreviewMesh();
        if (preview == lastPreview) {
            return projectedEdge; // The mesh hasn't changed.
        }
        lastPreview = preview;

        mesh = (PolyMesh) objInfo.object;
        QuadMesh subdividedMesh = mesh.getSubdividedMesh();
        projectedEdge = subdividedMesh.getProjectedEdges();
        if (projectedEdge == null) {
            log.info("null projected Edge");
        }
        return projectedEdge;
    }

    /**
     * Add an extra texture parameter to a triangle mesh.
     */
    private void addTriangleMeshExtraParameter(TriangleMesh mesh) {
        TextureParameter hideFaceParam = new TextureParameter(this, "Hide Face", 0.0, 1.0, 0.0);
        TextureParameter[] params = mesh.getParameters();
        TextureParameter[] newparams = new TextureParameter[params.length + 1];
        ParameterValue[] values = mesh.getParameterValues();
        ParameterValue[] newvalues = new ParameterValue[values.length + 1];
        for (int i = 0; i < params.length; i++) {
            newparams[i] = params[i];
            newvalues[i] = values[i];
        }
        newparams[params.length] = hideFaceParam;
        newvalues[values.length] = new FaceParameterValue(mesh, hideFaceParam);
        double[] index = new double[mesh.getFaces().length];
        for (int i = 0; i < index.length; i++) {
            index[i] = i;
        }
        ((FaceParameterValue) newvalues[values.length]).setValue(index);
        mesh.setParameters(newparams);
        mesh.setParameterValues(newvalues);
    }

    /**
     * Get the subdivided mesh which represents the surface. If the
     * mesh is not subdivided, this returns null.
     */
    QuadMesh getSubdividedPolyMesh() {
        return ((PolyMesh) objInfo.object).getSubdividedMesh();
    }

    /**
     * Cancel button selected
     */
    @Override
    protected void doCancel() {
        oldMesh = null;
        eventSource.removeEventLink(CopyEvent.class, this);
        dispose();
    }

    /**
     * OK button selection
     */
    @Override
    protected void doOk() {
        PolyMesh theMesh = (PolyMesh) objInfo.object;
        if (realView) {
            theMesh.setSmoothingMethod(smoothingMethod);
        }
        if (realMirror) {
            theMesh.setMirrorState(mirror);
        }
        if (((PolyMesh) oldMesh).getMaterial() != null) {
            if (!theMesh.isClosed()) {
                String[] options = new String[]{Translate.text("button.ok"),
                    Translate.text("button.cancel")};
                BStandardDialog dlg = new BStandardDialog(Translate
                        .text("polymesh:errorTitle"), UIUtilities.breakString(Translate
                        .text("surfaceNoLongerClosed")),
                        BStandardDialog.WARNING);
                int choice = dlg.showOptionDialog(this, options, options[0]);
                if (choice == 1) {
                    return;
                }
                theMesh.setMaterial(null, null);
            } else {
                theMesh.setMaterial(((PolyMesh) oldMesh).getMaterial(),
                        ((PolyMesh) oldMesh).getMaterialMapping());
            }
        }
        removeExtraParameters();
        if (oldMesh != theMesh) {
            oldMesh.copyObject(theMesh);
        }
        oldMesh = null;
        eventSource.removeEventLink(CopyEvent.class, this);
        dispose();
        onClose.run();
        parentWindow.updateImage();
        parentWindow.updateMenus();
    }

    /*
	 * EditingWindow methods.
     */
    /**
     * Sets the currently selected tool
     *
     * @param tool
     * The new tool
     */
    @Override
    public void setTool(EditingTool tool) {
        if (tool instanceof GenericTool) {
            if (selectMode == modes.getSelection()) {
                return;
            }
            if (undoItem != null) {
                setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_MESH_SELECTION, this, selectMode, selected));
            }
            setSelectionMode(modes.getSelection());
            theView[currentView].getCurrentTool().activate();
        } else {
            for (ViewerCanvas viewerCanvas : theView) {
                viewerCanvas.setTool(tool);
            }
            currentTool = tool;

        }
    }

    /**
     * Given a list of deltas which will be added to the selected vertices,
     * calculate the corresponding deltas for the unselected vertices
     * according to the mesh tension.
     *
     * @param delta
     * Description of the Parameter
     */
    @Override
    public void adjustDeltas(Vec3[] delta) {
        int[] dist = getSelectionDistance();
        int[] count = new int[delta.length];
        PolyMesh theMesh = (PolyMesh) objInfo.object;
        PolyMesh.Wedge[] edge = theMesh.getEdges();
        int maxDistance = getTensionDistance();
        double tension = getMeshTension();
        double[] scale = new double[maxDistance + 1];

        for (int i = 0; i < delta.length; i++) {
            if (dist[i] != 0) {
                delta[i].set(0.0, 0.0, 0.0);
            }
        }
        for (int i = 0; i < maxDistance; i++) {
            Arrays.fill(count, 0);
            for (int j = 0; j < edge.length; j++) {
                if (dist[edge[j].vertex] == i && dist[edge[edge[j].hedge].vertex] == i + 1) {
                    count[edge[edge[j].hedge].vertex]++;
                    delta[edge[edge[j].hedge].vertex].add(delta[edge[j].vertex]);
                } else if (dist[edge[edge[j].hedge].vertex] == i && dist[edge[j].vertex] == i + 1) {
                    count[edge[j].vertex]++;
                    delta[edge[j].vertex].add(delta[edge[edge[j].hedge].vertex]);
                }
            }
            for (int j = 0; j < count.length; j++) {
                if (count[j] > 1) {
                    delta[j].scale(1.0 / count[j]);
                }
            }
        }
        for (int i = 0; i < scale.length; i++) {
            scale[i] = Math.pow((maxDistance - i + 1.0) / (maxDistance + 1.0), tension);
        }
        for (int i = 0; i < delta.length; i++) {
            if (dist[i] > 0) {
                delta[i].scale(scale[dist[i]]);
            }
        }
    }

    /**
     * Updates window menus
     */
    @Override
    public void updateMenus() {
        super.updateMenus();
        switch (selectMode) {
            default:
            case POINT_MODE:
                vertexMenu.setEnabled(true);
                edgeMenu.setEnabled(false);
                faceMenu.setEnabled(false);
                break;
            case EDGE_MODE:
                vertexMenu.setEnabled(false);
                edgeMenu.setEnabled(true);
                extrudeEdgeItem.setShortcut(singleNormalShortcut);
                extrudeEdgeRegionItem.setShortcut(groupNormalShortcut);
                extrudeItem.setShortcut(null);
                extrudeRegionItem.setShortcut(null);
                faceMenu.setEnabled(false);
                break;
            case FACE_MODE:
                vertexMenu.setEnabled(false);
                edgeMenu.setEnabled(false);
                faceMenu.setEnabled(true);
                extrudeItem.setShortcut(singleNormalShortcut);
                extrudeRegionItem.setShortcut(groupNormalShortcut);
                extrudeEdgeItem.setShortcut(null);
                extrudeEdgeRegionItem.setShortcut(null);
                break;
        }
        PolyMesh mesh = (PolyMesh) objInfo.object;
        MeshViewer view = (MeshViewer) theView[currentView];
        boolean any = false;
        int i;
        int selCount = 0;

        if (selected != null) {
            for (i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    ++selCount;
                }
            }
        }
        if (selCount > 0) {
            //((RotateViewTool) altTool).setUseSelectionCenter(true);
            any = true;
            vertexMenu.getChildren().forEach(widget -> widget.setEnabled(true));
            vertexPopupMenu.getChildren().forEach(widget -> widget.setEnabled(true));
            edgeMenu.getChildren().forEach(widget -> widget.setEnabled(true));
            edgePopupMenu.getChildren().forEach(widget -> widget.setEnabled(true));
            faceMenu.getChildren().forEach(widget -> widget.setEnabled(true));
            facePopupMenu.getChildren().forEach(widget -> widget.setEnabled(true));

            editMenuItem[0].setEnabled(true);
            editMenuItem[2].setEnabled(true);
            editMenuItem[3].setEnabled(true);
            editMenuItem[4].setEnabled(true);
            editMenuItem[5].setEnabled(true);
            if (selCount < 4) {
                ((Widget) vertexMenuItem[6]).setEnabled(false);
                ((Widget) vertexMenuItem[7]).setEnabled(false);
                ((Widget) vertexMenuItem[8]).setEnabled(false);
                ((Widget) vertexPopupMenuItem[6]).setEnabled(false);
                ((Widget) vertexPopupMenuItem[7]).setEnabled(false);
                ((Widget) vertexPopupMenuItem[8]).setEnabled(false);
            }
            if (selCount != 2) {
                ((Widget) vertexMenuItem[10]).setEnabled(false);
                ((Widget) vertexPopupMenuItem[10]).setEnabled(false);
            }
            switch (selectMode) {
                default:
                case POINT_MODE:
                    if (mesh.getSmoothingMethod() == Mesh.APPROXIMATING) {
                        cornerCB.setEnabled(true);
                        boolean corner = true;
                        Wvertex[] vertices = (Wvertex[]) mesh.getVertices();
                        if (selected != null && selected.length == vertices.length) {
                            for (i = 0; i < selected.length; i++) {
                                if (selected[i]) {
                                    corner &= (vertices[i].type == Wvertex.CORNER);
                                }
                            }
                        }
                        cornerCB.setState(corner);
                    } else {
                        cornerCB.setState(false);
                        cornerCB.setEnabled(false);
                    }
                    break;
                case EDGE_MODE:
                    if (mesh.getSmoothingMethod() == Mesh.APPROXIMATING) {
                        edgeSlider.setEnabled(true);
                        float s = 1.0f;
                        Wedge[] ed = mesh.getEdges();
                        if (selected != null) {
                            for (i = 0; i < selected.length; i++) {
                                if (selected[i] && ed[i].smoothness < s) {
                                    s = ed[i].smoothness;
                                }
                            }
                        }
                        edgeSlider.setValue(s);
                    } else {
                        edgeSlider.setEnabled(false);
                    }
                    break;
                case FACE_MODE:
                    break;
            }
        } else {

            vertexMenu.getChildren().forEach(widget -> widget.setEnabled(false));
            vertexPopupMenu.getChildren().forEach(widget -> widget.setEnabled(false));
            edgeMenu.getChildren().forEach(widget -> widget.setEnabled(false));
            edgePopupMenu.getChildren().forEach(widget -> widget.setEnabled(false));
            faceMenu.getChildren().forEach(widget -> widget.setEnabled(false));
            facePopupMenu.getChildren().forEach(widget -> widget.setEnabled(false));

            editMenuItem[0].setEnabled(false);
            editMenuItem[2].setEnabled(false);
            editMenuItem[3].setEnabled(false);
            editMenuItem[4].setEnabled(false);
            editMenuItem[5].setEnabled(false);
            switch (selectMode) {
                default:
                case POINT_MODE:
                    cornerCB.setState(false);
                    cornerCB.setEnabled(false);
                    ((Widget) vertexMenuItem[15]).setEnabled(true);
                    ((Widget) vertexPopupMenuItem[15]).setEnabled(true);
                    break;
                case EDGE_MODE:
                    ((Widget) edgeMenuItem[20]).setEnabled(true);
                    ((Widget) edgeMenuItem[21]).setEnabled(true);
                    ((Widget) edgePopupMenuItem[20]).setEnabled(true);
                    ((Widget) edgePopupMenuItem[21]).setEnabled(true);
                    edgeSlider.setEnabled(false);
                    break;
                case FACE_MODE:
                    break;
            }
        }
        if (selected != null) {
            if (selCount == selected.length) {
                editMenuItem[1].setEnabled(false);
                editMenuItem[2].setEnabled(false);
                faceFindSimilarMenuItem.setEnabled(false);
                ((BMenuItem) edgeMenuItem[12]).setEnabled(false);
                faceFindSimilarPopupMenuItem.setEnabled(false);
                ((BMenuItem) edgePopupMenuItem[12]).setEnabled(false);
            } else {
                editMenuItem[1].setEnabled(true);
            }
        }

        editMappingAction.setEnabled(mesh.getMappingData() != null);

        if (mesh.isClosed()) {
            ((BMenuItem) vertexMenuItem[8]).setEnabled(false);
            ((BMenuItem) vertexMenuItem[9]).setEnabled(false);
            ((BMenuItem) vertexPopupMenuItem[8]).setEnabled(false);
            ((BMenuItem) vertexPopupMenuItem[9]).setEnabled(false);
            ((BMenuItem) edgeMenuItem[10]).setEnabled(false);
            ((BMenuItem) edgeMenuItem[11]).setEnabled(false);
            ((BMenuItem) edgePopupMenuItem[10]).setEnabled(false);
            ((BMenuItem) edgePopupMenuItem[11]).setEnabled(false);
            (meshMenuItem[3]).setEnabled(false);
            (meshMenuItem[4]).setEnabled(false);

            unfoldMeshAction.setEnabled(mesh.getSeams() != null);

        } else {
            ((BMenuItem) vertexMenuItem[8]).setEnabled(true);
            ((BMenuItem) vertexMenuItem[9]).setEnabled(true);
            ((BMenuItem) vertexPopupMenuItem[8]).setEnabled(true);
            ((BMenuItem) vertexPopupMenuItem[9]).setEnabled(true);
            ((BMenuItem) edgeMenuItem[10]).setEnabled(true);
            ((BMenuItem) edgePopupMenuItem[10]).setEnabled(true);
            ((BMenuItem) edgeMenuItem[11]).setEnabled(true);
            ((BMenuItem) edgePopupMenuItem[11]).setEnabled(true);
            (meshMenuItem[3]).setEnabled(true);
            (meshMenuItem[4]).setEnabled(true);
            unfoldMeshAction.setEnabled(true);
        }

        for (int j = 15; j <= 19; j++) {
            ((BMenuItem) edgeMenuItem[j]).setEnabled(mesh.getSeams() != null);
            ((BMenuItem) edgePopupMenuItem[j]).setEnabled(mesh.getSeams() != null);
        }

        // ( (BMenuItem) edgeMenuItem[4] ).setEnabled( false );
        templateItem.setEnabled(theView[currentView].getTemplateImage() != null);
        Skeleton s = mesh.getSkeleton();
        Joint selJoint = s.getJoint(view.getSelectedJoint());
        ((BMenuItem) skeletonMenuItem[0]).setEnabled(selJoint != null);
        ((BMenuItem) skeletonMenuItem[1]).setEnabled(selJoint != null && selJoint.children.length == 0);
        ((BMenuItem) skeletonMenuItem[2]).setEnabled(selJoint != null);
        ((BMenuItem) skeletonMenuItem[4]).setEnabled(any);
        ((BMenuItem) edgeMenuItem[14]).setEnabled(true);
        ((BMenuItem) edgePopupMenuItem[14]).setEnabled(true);
    }

    /**
     * Gets the action direction currently selected
     *
     * @return The actionDirection value
     */
    public int getActionDirection() {
        return PolyMesh.NORMAL;
    }

    /**
     * Changes the interactive smooth level, if appropriate
     *
     * @param amount
     * The quantity by which the level should be changed
     */
    @KeystrokeManager.UsedWithScriptBinding("PMKeystrokes.xml")
    public void changeInteractiveSmoothLevel(int amount) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        int level;
        if (mesh.getSmoothingMethod() != Mesh.APPROXIMATING) {
            return;
        }
        mesh = (PolyMesh) objInfo.object;
        level = mesh.getInteractiveSmoothLevel();
        if (level + amount > 0) {
            mesh.setInteractiveSmoothLevel(level + amount);
            ispin.setValue(level + amount);
            objectChanged();
            updateImage();
        }
    }

    /**
     * Toggles live smoothing on/off
     *
     */
    // NB. Method accessed via KeyStroke records. Do not remove!!!
    @KeystrokeManager.UsedWithScriptBinding("PMKeystrokes.xml")
    public void toggleSmoothing() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (realView) {
            mesh.setSmoothingMethod(smoothingMethod);
            realView = false;
        } else {
            smoothingMethod = mesh.getSmoothingMethod();
            mesh.setSmoothingMethod(Mesh.NO_SMOOTHING);
            realView = true;
        }
        objectChanged();
        updateImage();
    }

    /**
     * Toggles live mirror on/off
     */
    public void toggleMirror() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (realMirror) {
            mesh.setMirrorState(mirror);
            mesh.getMirroredMesh();
            realMirror = false;
        } else {
            mirror = mesh.getMirrorState();
            mesh.setMirrorState(PolyMesh.NO_MIRROR);
            realMirror = true;
        }
        objectChanged();
        updateImage();

    }

    /*
         * Method runs from Polymesh keystrokes script
     */
    @KeystrokeManager.UsedWithScriptBinding
    public void selectTool(int tool) {
        tools.selectTool(toolsMap.get(tool));
    }

    /**
     * Toggles manipulators between 2D and 3D (to be removed presumably)
     */
    // NB. Method accessed via KeyStroke records.
    @KeystrokeManager.UsedWithScriptBinding
    public void toggleManipulator() {
        if (currentTool instanceof AdvancedEditingTool) {
            PolyMeshViewer view = (PolyMeshViewer) getView();
            ((AdvancedEditingTool) currentTool).toggleManipulator(view);
            view.repaint();
        }
    }

    /**
     * Toggles manipulator view mode (i.e. X,Y,Z U,V and N, P, Q)
     */
    // NB. Method accessed via KeyStroke records. Do not remove!!!
    @KeystrokeManager.UsedWithScriptBinding
    public void toggleManipulatorViewMode() {
        PolyMeshViewer view = (PolyMeshViewer) getView();
        view.getManipulators().forEach(Manipulator::toggleViewMode);
        view.repaint();
    }

    /**
     * Edits AoI keystrokes
     */
    public void editKeystrokes(ActionEvent event) {
        BorderContainer bc = new BorderContainer();
        KeystrokePreferencesPanel keystrokePanel = new KeystrokePreferencesPanel();
        bc.add(keystrokePanel, BorderContainer.CENTER);
        PanelDialog dlg = new PanelDialog(this, Translate.text("keystrokes"), bc);
        if (!dlg.clickedOk()) {
            return;
        }
        keystrokePanel.saveChanges();
    }

    /**
     * Deletes any keystroke script associated to the PolyMesh plugin
     */
    private void cleanKeystrokes() {

        for (KeystrokeRecord key : KeystrokeManager.getRecords()) {
            if (key.getName().endsWith("(PolyMesh)")) {
                KeystrokeManager.removeRecord(key);
            }
        }
        try {
            KeystrokeManager.saveRecords();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Error saving keystrokes: {}", ex.getMessage());
        }
    }

    /**
     * Reloads keystroke scripts shipped with the PolyMesh plugin
     */
    public void reloadKeystrokes(ActionEvent event) {
        cleanKeystrokes();
        try (InputStream in = getClass().getResourceAsStream("/PMkeystrokes.xml")) {
            KeystrokeManager.addRecordsFromXML(in);
            KeystrokeManager.saveRecords();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Error reload Keystrokes due {}", ex.getLocalizedMessage());
        }
    }

    /**
     * Toggles help mode on/off
     */
    // NB. Method accessed via KeyStroke records.
    @KeystrokeManager.UsedWithScriptBinding
    public void toggleHelpMode() {
        Manipulator.toggleHelpMode();
    }

    /**
     * Called when a key has been pressed
     *
     * @param e
     * The KeyPressedEvent
     */
    @Override
    protected void keyPressed(KeyPressedEvent e) {
        ((PolyMeshViewer) getView()).keyPressed(e);
        valueWidget.keyPressed(e);
        KeystrokeManager.executeKeystrokes(e, this);
    }

    private void deactivateTools() {
        pointTool.deactivate();
        edgeTool.deactivate();
        faceTool.deactivate();
    }

    private void activateTools() {
        pointTool.activate();
        edgeTool.activate();
        faceTool.activate();
    }

    /**
     * Connects selected vertices
     */
    private void doConnectVertices(ActionEvent event) {
        PolyMesh theMesh = (PolyMesh) objInfo.getGeometry();

        PolyMesh prevMesh = theMesh.duplicate();
        if (selectMode == POINT_MODE) {
            int[] indices = getIndicesFromSelection(selected);
            theMesh.connectVertices(indices);
            setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, theMesh, prevMesh));
            objectChanged();
            updateImage();

        }
    }

    /**
     * Gets the selected points, edges or faces as an integer array given
     * the boolean selection array
     *
     * @param selected
     * The boolean
     * @return The indicesFromSelection value
     */
    private int[] getIndicesFromSelection(boolean[] selected) {
        int count = 0;
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                ++count;
            }
        }
        int[] indices = new int[count];
        count = 0;
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                indices[count++] = i;
            }
        }
        return indices;
    }

    /**
     * Divides selected edges into segments
     *
     */
    private void doDivideEdges(int counter) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] sel = mesh.divideEdges(selected, counter);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        if (sel != null) {
            modes.selectTool(pointTool);
            setSelectionMode(POINT_MODE);
            setSelection(sel);
            updateMenus();
        }
        updateImage();
    }

    private void doDivideEdgesTwo(ActionEvent event) {
        doDivideEdges(2);
    }

    private void doDivideEdgesThree(ActionEvent event) {
        doDivideEdges(3);
    }

    private void doDivideEdgesFour(ActionEvent event) {
        doDivideEdges(4);
    }

    private void doDivideEdgesFive(ActionEvent event) {
        doDivideEdges(5);
    }

    private void doDivideEdgesInteractive(ActionEvent event) {
        SwingUtilities.invokeLater(() -> new DivideDialog(this, this::doDivideEdges).setVisible(true));
    }

    /**
     * Called when a smoothing method command is selected
     *
     * @param ev
     * The command event
     */
    private void smoothingChanged(CommandEvent ev) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, mesh.duplicate()));
        Object source = ev.getWidget();
        for (var item : smoothItem) {
            item.setState(false);
        }
        /*
		 * for ( int i = 0; i < smoothItem.length; i++ ) if ( source ==
		 * smoothItem[i] ) { mesh.setSmoothingMethod( i );
		 * smoothItem[i].setState( true ); }
         */
        if (source == smoothItem[1]) {
            mesh.setSmoothingMethod(Mesh.APPROXIMATING);
            smoothItem[1].setState(true);
//		} else if (source == smoothItem[1]) {
//			mesh.setSmoothingMethod(Mesh.SMOOTH_SHADING);
//			smoothItem[1].setState(true);
        } else {
            mesh.setSmoothingMethod(Mesh.NO_SMOOTHING);
            smoothItem[0].setState(true);
        }
        realView = false;
        doLevelContainerEnable();
        objectChanged();
        updateImage();

    }

    private void doLevelContainerEnable() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean enable = mesh.getSmoothingMethod() == Mesh.APPROXIMATING;
        levelContainer.getChildren().forEach(widget -> widget.setEnabled(enable));
    }

    /**
     * Smoothes the mesh
     */
    private void doSmoothMesh(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        mesh.smoothWholeMesh(-1, false, 1, true);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();

    }

    /**
     * Subdivides the mesh
     */
    private void doSubdivideMesh(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] selected = new boolean[mesh.getFaces().length];
        Arrays.fill(selected, true);
        mesh.smooth(selected, true);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();

    }

    /**
     * Smoothes the mesh according to face selection
     */
    private void doSmoothFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        mesh.smooth(selected, false);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();

    }

    /**
     * Subdivides the mesh according to face selection
     */
    private void doSubdivideFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        mesh.smooth(selected, true);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();

    }

    /**
     * Selects edges surrounding face selection
     */
    private void doOutlineFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        Wedge[] edges = mesh.getEdges();
        Wface[] faces = mesh.getFaces();
        boolean[] edgeSel = new boolean[edges.length / 2];
        for (int i = 0; i < faces.length; i++) {
            if (selected[i]) {
                int[] fe = mesh.getFaceEdges(faces[i]);
                for (int j = 0; j < fe.length; j++) {
                    if (edges[fe[j]].face == -1 || edges[edges[fe[j]].hedge].face == -1) {
                        continue;
                    }
                    if (selected[edges[fe[j]].face] && !selected[edges[edges[fe[j]].hedge].face]) {
                        if (fe[j] < edges.length / 2) {
                            edgeSel[fe[j]] = true;
                        } else {
                            edgeSel[edges[fe[j]].hedge] = true;
                        }
                    } else if (!selected[edges[fe[j]].face] && selected[edges[edges[fe[j]].hedge].face]) {
                        if (fe[j] < edges.length / 2) {
                            edgeSel[fe[j]] = true;
                        } else {
                            edgeSel[edges[fe[j]].hedge] = true;
                        }
                    }
                }
            }
        }
        setSelectionMode(EDGE_MODE);
        setSelection(edgeSel);
        updateImage();
    }

    /**
     * Move vertices menu command (normal)
     */
    private void doMoveVerticesNormal(ActionEvent event) {
        move(selectMode, PolyMesh.NORMAL);
    }

    /**
     * Move vertices menu command (x)
     */
    private void doMoveVerticesX(ActionEvent event) {
        move(selectMode, PolyMesh.X);
    }

    /**
     * Move vertices menu command (y)
     */
    private void doMoveVerticesY(ActionEvent event) {
        move(selectMode, PolyMesh.Y);
    }

    /**
     * Move vertices menu command (z)
     */
    private void doMoveVerticesZ(ActionEvent event) {
        move(selectMode, PolyMesh.Z);
    }

    /**
     * Move edges menu command (normal)
     */
    private void doMoveEdgesNormal(ActionEvent event) {
        move(selectMode, PolyMesh.NORMAL);
    }

    /**
     * Move edges menu command (x)
     */
    private void doMoveEdgesX(ActionEvent event) {
        move(selectMode, PolyMesh.X);
    }

    /**
     * Move edges menu command (y)
     */
    private void doMoveEdgesY(ActionEvent event) {
        move(selectMode, PolyMesh.Y);
    }

    /**
     * Move edges menu command (z)
     */
    private void doMoveEdgesZ(ActionEvent event) {
        move(selectMode, PolyMesh.Z);
    }

    /**
     * Move faces menu command (normal)
     */
    private void doMoveFacesNormal(ActionEvent event) {
        move(selectMode, PolyMesh.NORMAL);
    }

    /**
     * Move faces menu command (x)
     */
    private void doMoveFacesX(ActionEvent event) {
        move(selectMode, PolyMesh.X);
    }

    /**
     * Move faces menu command (y)
     */
    private void doMoveFacesY(ActionEvent event) {
        move(selectMode, PolyMesh.Y);
    }

    /**
     * Move faces menu command (z)
     */
    private void doMoveFacesZ(ActionEvent event) {
        move(selectMode, PolyMesh.Z);
    }

    /**
     * Generic move command
     *
     * @param kind
     * Description of the Parameter
     * @param direction
     * Description of the Parameter
     */
    private void move(int kind, short direction) {
        if (valueWidget.isActivated()) {
            return;
        }
        moveDirection = direction;
        valueWidget.activate(this::doMoveCallback);
    }

    /**
     * Callback called when the value has changed in the value dialog (move)
     */
    private void doMoveCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        mesh.copyObject(priorValueMesh);
        switch (selectMode) {
            default:
            case POINT_MODE:
                mesh.moveVertices(valueSelection, valueWidget.getValue(), moveDirection);
                break;
            case EDGE_MODE:
                mesh.moveEdges(valueSelection, valueWidget.getValue(), moveDirection);
                break;
            case FACE_MODE:
                mesh.moveFaces(valueSelection, valueWidget.getValue(), moveDirection);
                break;
        }
        objectChanged();
        setSelection(valueSelection);
    }

    /**
     * Bevel edges command
     */
    private void doBevelEdges(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        valueWidget.activate(this::doBevelEdgesCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doBevelEdgesCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        boolean[] sel = mesh.bevelEdges(valueSelection, valueWidget.getValue());
        objectChanged();
        setSelection(sel);
    }

    /**
     * Bevel edges command
     */
    private void doBevelVertices(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        valueWidget.activate(this::doBevelVerticesCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doBevelVerticesCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        boolean[] sel = mesh.bevelVertices(valueSelection, valueWidget.getValue());
        objectChanged();
        setSelection(sel);
    }

    /**
     * Validate button selected
     */
    @Override
    public void doValueWidgetValidate() {
        valueWidgetDialog.setVisible(false);
        PolyMesh mesh = (PolyMesh) objInfo.object;
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, priorValueMesh));
    }

    /**
     * Cancel button selected
     */
    @Override
    public void doValueWidgetAbort() {
        valueWidgetDialog.setVisible(false);
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        objectChanged();
        PolyMesh valueMesh = null;
        priorValueMesh = null;
        setSelection(valueSelection);
        updateImage();
    }

    @Override
    public void prepareToShowValueWidget() {
        priorValueMesh = ((PolyMesh) objInfo.getGeometry()).duplicate();
        valueSelection = selected;
    }

    @Override
    public void showValueWidget() {
        if (unseenValueWidgetDialog) {
            Window main = (Window) this.getComponent();
            Window dlg = (Window) valueWidgetDialog.getComponent();
            Point mp = main.getLocation();
            Rectangle mb = main.getBounds();
            Point dp = new Point();
            dp.x = mp.x + mb.width + 5;
            dp.y = mp.y;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            if (dp.x + valueWidgetDialog.getBounds().width > screenSize.width) {
                dp.x = screenSize.width - valueWidget.getBounds().width;
            }
            dlg.setLocation(dp);
            unseenValueWidgetDialog = false;
        }
        valueWidgetDialog.setVisible(true);
    }

    public void hideValueWidget() {
        valueWidgetDialog.setVisible(false);
    }

    /**
     * Face extrusion along normal
     */
    private void doExtrudeNormal(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = null;
        valueWidget.activate(this::doExtrudeCallback);
    }

    /**
     * Face extrusion along X axis
     */
    private void doExtrudeX(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vx();
        valueWidget.activate(this::doExtrudeCallback);
    }

    /**
     * Face extrusion along Y axis
     */
    private void doExtrudeY(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vy();
        valueWidget.activate(this::doExtrudeCallback);
    }

    /**
     * Face extrusion along Z axis
     */
    private void doExtrudeZ(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vz();
        valueWidget.activate(this::doExtrudeCallback);
    }

    /**
     * Edge extrusion along normal
     */
    private void doExtrudeEdgeNormal(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = null;
        valueWidget.activate(this::doExtrudeEdgeCallback);
    }

    /**
     * Edge extrusion along X axis
     */
    private void doExtrudeEdgeX(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vx();
        valueWidget.activate(this::doExtrudeEdgeCallback);
    }

    /**
     * Edge extrusion along Y axis
     */
    private void doExtrudeEdgeY(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vy();
        valueWidget.activate(this::doExtrudeEdgeCallback);
    }

    /**
     * Edge extrusion along Z axis
     */
    private void doExtrudeEdgeZ(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vz();
        valueWidget.activate(this::doExtrudeEdgeCallback);
    }

    /**
     * Region extrusion along normal
     */
    private void doExtrudeRegionNormal(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = null;
        valueWidget.activate(this::doExtrudeRegionCallback);
    }

    /**
     * Region extrusion along X axis
     */
    private void doExtrudeRegionX(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vx();
        valueWidget.activate(this::doExtrudeRegionCallback);
    }

    /**
     * Region extrusion along Y axis
     */
    private void doExtrudeRegionY(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vy();
        valueWidget.activate(this::doExtrudeRegionCallback);
    }

    /**
     * Region extrusion along Z axis
     */
    private void doExtrudeRegionZ(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vz();
        valueWidget.activate(this::doExtrudeRegionCallback);
    }

    /**
     * Edge Region extrusion along normal
     */
    private void doExtrudeEdgeRegionNormal(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = null;
        valueWidget.activate(this::doExtrudeEdgeRegionCallback);
    }

    /**
     * Edge Region extrusion along X axis
     */
    private void doExtrudeEdgeRegionX(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vx();
        valueWidget.activate(this::doExtrudeEdgeRegionCallback);
    }

    /**
     * Edge Region extrusion along Y axis
     */
    private void doExtrudeEdgeRegionY(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vy();
        valueWidget.activate(this::doExtrudeEdgeRegionCallback);
    }

    /**
     * Edge Region extrusion along Z axis
     */
    private void doExtrudeEdgeRegionZ(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        direction = Vec3.vz();
        valueWidget.activate(this::doExtrudeEdgeRegionCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doExtrudeCallback() {
        PolyMesh valueMesh = (PolyMesh) objInfo.object;
        valueMesh.copyObject(priorValueMesh);
        valueMesh.extrudeFaces(valueSelection, valueWidget.getValue(), direction);
        boolean[] sel = new boolean[valueMesh.getFaces().length];
        System.arraycopy(valueSelection, 0, sel, 0, valueSelection.length);
        objectChanged();
        setSelection(sel);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doExtrudeEdgeCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        mesh.extrudeEdges(valueSelection, valueWidget.getValue(), direction);

        boolean[] sel = new boolean[mesh.getEdges().length / 2];
        System.arraycopy(valueSelection, 0, sel, 0, valueSelection.length);
        objectChanged();
        setSelection(sel);
    }

    /**
     * Mesh thickening
     */
    private void doThickenMesh(CommandEvent ev) {
        if (valueWidget.isActivated()) {
            return;
        }
        thickenFaces = ev.getWidget() == meshMenuItem[3];
        valueWidget.activate(this::doThickenMeshCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doThickenMeshCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        mesh.thickenMesh(valueWidget.getValue(), thickenFaces);
        boolean[] sel = new boolean[mesh.getFaces().length];
        objectChanged();
        updateImage();
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doExtrudeRegionCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        mesh.copyObject(priorValueMesh);
        mesh.extrudeRegion(valueSelection, valueWidget.getValue(),
                direction);
        boolean[] sel = new boolean[mesh.getFaces().length];
        System.arraycopy(valueSelection, 0, sel, 0, valueSelection.length);
        objectChanged();
        setSelection(sel);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doExtrudeEdgeRegionCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        mesh.extrudeEdgeRegion(valueSelection, valueWidget.getValue(),
                direction);
        boolean[] sel = new boolean[mesh.getEdges().length / 2];
        System.arraycopy(valueSelection, 0, sel, 0, valueSelection.length);
        objectChanged();
        setSelection(sel);
    }

    /**
     * Loops insertion
     */
    private void doInsertLoops(ActionEvent event) {

        if (valueWidget.isActivated()) {
            return;
        }

        valueWidget.setTempValueRange(0, 1.0);
        valueWidget.activate(0.5, this::doInsertLoopsCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doInsertLoopsCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        boolean[] sel = mesh.divideEdges(valueSelection, valueWidget
                .getValue());
        mesh.connectVertices(sel);
        objectChanged();
        setSelectionMode(POINT_MODE);
        setSelection(sel);
        updateImage();
    }

    /**
     * Brings selected vertices to the mean sphere calculated from these
     * vertices
     */
    private void doMeanSphere(ActionEvent event) {

        if (valueWidget.isActivated()) {
            return;
        }
        Vec3 origin;
        double radius;
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        priorValueMesh = mesh.duplicate();
        MeshVertex[] vert = priorValueMesh.getVertices();
        Vec3[] normals = priorValueMesh.getNormals();
        int count = 0;
        origin = new Vec3();
        radius = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                ++count;
                origin.add(vert[i].r);
            }
        }
        vertDisplacements = new Vec3[count];
        origin.scale(1.0 / count);
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                radius += vert[i].r.minus(origin).length();
            }
        }
        radius /= count;
        count = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                vertDisplacements[count] = vert[i].r.minus(origin);
                if (vertDisplacements[count].length() < 1e-6) {
                    vertDisplacements[count] = new Vec3(normals[i]);
                }
                vertDisplacements[count].scale(radius
                        / vertDisplacements[count].length());
                vertDisplacements[count].add(origin);
                vertDisplacements[count].subtract(vert[i].r);
                ++count;
            }
        }
        if (checkForNullMovement(vertDisplacements)) {
            return;
        }

        valueWidget.activate(this::doBringCallback);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog
     */
    private void doBringCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        MeshVertex[] vert = mesh.getVertices();
        int count = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                vert[i].r.add(vertDisplacements[count].times(valueWidget
                        .getValue()));
                ++count;
            }
        }
        mesh.resetMesh();
        objectChanged();
        setSelection(selected);
    }

    /**
     * Brings vertices onto the closest sphere portion
     */
    private void doClosestSphere(ActionEvent event) {

        double a;
        double b;
        double c;
        double l;
        double la;
        double lb;
        double lc;
        double t;
        Vec3 origin;
        double radius;

        if (valueWidget.isActivated()) {
            return;
        }
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        priorValueMesh = mesh.duplicate();
        MeshVertex[] vert = priorValueMesh.getVertices();
        Vec3[] normals = priorValueMesh.getNormals();
        int count = 0;
        origin = new Vec3();
        radius = 0;
        l = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                ++count;
                origin.add(vert[i].r);
            }
        }
        origin.scale(1.0 / count);
        a = origin.x;
        b = origin.y;
        c = origin.z;
        radius = 1;
        double delta = 1;
        double newa;
        double newb;
        double newc;
        int dummy = 0;
        while (delta > 0.01 && dummy < 10000) {
            l = 0;
            for (int i = 0; i < vert.length; ++i) {
                if (selected[i]) {
                    l += Math.sqrt((vert[i].r.x - a) * (vert[i].r.x - a) + (vert[i].r.y - b) * (vert[i].r.y - b) + (vert[i].r.z - c) * (vert[i].r.z - c));
                }
            }
            l /= count;
            la = lb = lc = 0;
            for (int i = 0; i < vert.length; ++i) {
                if (selected[i]) {

                    t = Math.sqrt((vert[i].r.x - a) * (vert[i].r.x - a) + (vert[i].r.y - b) * (vert[i].r.y - b) + (vert[i].r.z - c) * (vert[i].r.z - c));
                    if (t < 1e-6) {
                        continue;
                    }
                    la += (a - vert[i].r.x) / t;
                    lb += (b - vert[i].r.y) / t;
                    lc += (c - vert[i].r.z) / t;
                }
            }
            la /= count;
            lb /= count;
            lc /= count;
            newa = origin.x + l * la;
            newb = origin.y + l * lb;
            newc = origin.z + l * lc;
            delta = 0;
            if (Math.max(Math.abs(newa), Math.abs(a)) > 1e-6) {
                delta += Math.abs(newa - a)
                        / Math.max(Math.abs(newa), Math.abs(a));
            }
            if (Math.max(Math.abs(newb), Math.abs(b)) > 1e-6) {
                delta += Math.abs(newb - b)
                        / Math.max(Math.abs(newb), Math.abs(b));
            }
            if (Math.max(Math.abs(newc), Math.abs(c)) > 1e-6) {
                delta += Math.abs(newc - c)
                        / Math.max(Math.abs(newc), Math.abs(c));
            }
            if (Math.max(Math.abs(radius), Math.abs(l)) > 1e-6) {
                delta += Math.abs(l - radius)
                        / Math.max(Math.abs(radius), Math.abs(l));
            }
            a = newa;
            b = newb;
            c = newc;
            radius = l;
            // System.out.println( delta + " : " + newa + " " + newb + " " +
            // newc + " " + l );
            ++dummy;
        }
        origin = new Vec3(a, b, c);
        // System.out.println( dummy );
        if (dummy >= 10000) {
            log.info("Warning: Too many iterations");
        }
        vertDisplacements = new Vec3[count];
        count = 0;
        // System.out.println( a + " " + b + " " + c + " " + radius );
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                vertDisplacements[count] = vert[i].r.minus(origin);
                if (vertDisplacements[count].length() < 1e-6) {
                    vertDisplacements[count] = new Vec3(normals[i]);
                }
                vertDisplacements[count].scale(radius
                        / vertDisplacements[count].length());
                vertDisplacements[count].add(origin);
                vertDisplacements[count].subtract(vert[i].r);
                ++count;
            }
        }
        if (checkForNullMovement(vertDisplacements)) {
            return;
        }

        valueWidget.activate(this::doBringCallback);
    }

    /**
     * Checks if vertices movements will actually result in a displacement
     *
     * @param movement
     * Vertices movements
     * @return True if a displacement will occur
     */
    private boolean checkForNullMovement(Vec3[] movement) {
        double sum = 0;
        for (int i = 0; i < movement.length; ++i) {
            sum += movement[i].length();
        }
        if (sum / movement.length < 1e-6) {
            new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities
                    .breakString(Translate.text("polymesh:nullMovement")),
                    BStandardDialog.ERROR).showMessageDialog(null);
            return true;
        }
        return false;
    }

    /**
     * Brings selected vertices to a plane calculated from these vertices
     */
    private void doPlane(ActionEvent event) {

        if (valueWidget.isActivated()) {
            return;
        }
        Vec3 origin;
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        priorValueMesh = mesh.duplicate();
        MeshVertex[] vert = priorValueMesh.getVertices();
        Vec3[] normals = priorValueMesh.getNormals();
        Vec3 norm = new Vec3();
        int count = 0;
        origin = new Vec3();
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                ++count;
                origin.add(vert[i].r);
                norm.add(normals[i]);
            }
        }
        vertDisplacements = new Vec3[count];
        origin.scale(1.0 / count);
        /*
		 * if ( norm.length() < 1e-6 ) { new BStandardDialog( "",
		 * UIUtilities.breakString( Translate.text( "cantFlatten" ) ),
		 * BStandardDialog.ERROR ).showMessageDialog( null ); return; }
         */
        norm.normalize();
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
            }
        }
        count = 0;
        for (int i = 0; i < vert.length; ++i) {
            if (selected[i]) {
                vertDisplacements[count++] = norm.times(-norm.dot(vert[i].r.minus(origin)));
            }
        }
        if (checkForNullMovement(vertDisplacements)) {
            return;
        }

        valueWidget.activate(this::doBringCallback);
    }

    /**
     * Brings normal to current selection
     */
    private void bringNormal(ActionEvent event) {
        Camera theCamera = theView[currentView].getCamera();
        Vec3 orig = new Vec3(0, 0, 0);
        Vec3 zdir;
        Vec3 updir;
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Vec3[] norm = mesh.getNormals();
        Wedge[] ed = mesh.getEdges();
        Wface[] f = mesh.getFaces();
        if (selectMode == POINT_MODE) {
            for (int i = 0; i < selected.length; ++i) {
                if (selected[i]) {
                    orig.add(norm[i]);
                }
            }
        } else if (selectMode == EDGE_MODE) {
            for (int i = 0; i < selected.length; ++i) {
                if (selected[i]) {
                    orig.add(norm[ed[i].vertex]);
                    orig.add(norm[ed[ed[i].hedge].vertex]);
                }
            }
        } else {
            for (int i = 0; i < selected.length; ++i) {
                if (selected[i]) {
                    int[] fv = mesh.getFaceVertices(f[i]);
                    Vec3 v = new Vec3();
                    for (int j = 0; j < fv.length; ++j) {
                        v.add(norm[fv[j]]);
                    }
                    v.normalize();
                    orig.add(v);
                }
            }
        }
        if (orig.length() < 1e-6) {
            return;
        }
        orig.normalize();
        orig.scale(theCamera.getCameraCoordinates().getOrigin().length());
        zdir = orig.times(-1.0);
        updir = new Vec3(zdir.y, -zdir.x, 0.0);
        if (updir.length() < 1e-6) {
            updir = new Vec3(0.0, zdir.z, -zdir.y);
        }
        theCamera.setCameraCoordinates(new CoordinateSystem(orig, zdir, updir));
        theView[currentView].setOrientation(ViewerCanvas.VIEW_OTHER);
        updateImage();

    }

    private final Map<Integer, BPopupMenu> modeToMenu = new HashMap<>();

    {
        modeToMenu.put(POINT_MODE, vertexPopupMenu);
        modeToMenu.put(EDGE_MODE, edgePopupMenu);
        modeToMenu.put(FACE_MODE, facePopupMenu);
    }

    public void triggerPopupEvent(WidgetMouseEvent event) {
        modeToMenu.get(selectMode).show(event);
    }

    @Override
    public void showPopupMenu(Widget widget, int x, int y) {
        modeToMenu.get(selectMode).show(widget, x, y);
    }

    /**
     * Sets the number of columns displayed by a spinner
     *
     * @param spinner
     * The concerned BSpinner
     * @param numCol
     * The new number of columns to show
     */
    public static void setSpinnerColumns(BSpinner spinner, int numCol) {
        NumberEditor ed = (NumberEditor) spinner.getComponent().getEditor();
        JFormattedTextField field = ed.getTextField();
        field.setColumns(numCol);
        spinner.getComponent().setEditor(ed);
    }

    /**
     * Sets the smoothness of selected vertices or edges
     */
    void setSmoothnessCommand() {
        final PolyMesh theMesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = theMesh.duplicate();
        final Wvertex[] vt = (Wvertex[]) theMesh.getVertices();
        final Wedge[] ed = theMesh.getEdges();
        final boolean pointmode = (selectMode == POINT_MODE);
        final ActionProcessor processor = new ActionProcessor();
        float value;
        final ValueSlider smoothness;
        int i;

        for (i = 0; i < selected.length && !selected[i]; i++)
			;
        if (i == selected.length) {
            return;
        }
        /*
		 * if ( pointmode ) valueWidget.getValue() = vt[i].smoothness; else
         */
        value = ed[i].smoothness;
        value = 0.001f * (Math.round(valueWidget.getValue() * 1000.0f));
        smoothness = new ValueSlider(0.0, 1.0, 1000, valueWidget.getValue());
        smoothness.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                processor.addEvent(new Runnable() {
                    @Override
                    public void run() {
                        float s = (float) smoothness.getValue();
                        if (s < 0) {
                            s = 0;
                        }
                        if (s > 1) {
                            s = 1;
                        }
                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                /*
								 * if ( pointmode ) vt[i].smoothness = s; else {
                                 */
                                ed[i].smoothness = s;
                                ed[ed[i].hedge].smoothness = s;
                                // }
                            }
                        }
                        theMesh
                                .setSmoothingMethod(theMesh
                                        .getSmoothingMethod());
                        objectChanged();
                        updateImage();

                    }
                });
            }
        });
        ComponentsDialog dlg = new ComponentsDialog(this, Translate
                .text(pointmode ? "setPointSmoothness" : "setEdgeSmoothness"),
                new Widget[]{smoothness}, new String[]{Translate
                            .text("Smoothness")});
        processor.stopProcessing();
        if (dlg.clickedOk()) {
            setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, theMesh, prevMesh));
        } else {
            theMesh.copyObject(prevMesh);
            objectChanged();
            updateImage();

        }
    }

    public void doEdgeSliderChanged() {
        PolyMesh theMesh = (PolyMesh) objInfo.object;
        final Wedge[] ed = theMesh.getEdges();
        float s = (float) edgeSlider.getValue();
        if (s < 0) {
            s = 0;
        }
        if (s > 1) {
            s = 1;
        }
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                ed[i].smoothness = s;
                ed[ed[i].hedge].smoothness = s;
            }
        }
        objectChanged();
        updateImage();
    }

    /**
     * Bevel properties settings
     */
    private void doBevelProperties(ActionEvent event) {
        SwingUtilities.invokeLater(() -> new BevelProperties(this.getComponent()).setVisible(true));
    }

    /**
     * Gets the selectionDistance attribute of the PolyMeshEditorWindow
     * object
     *
     * @return The selectionDistance valueWidget.getValue()
     */
    @Override
    public int[] getSelectionDistance() {
        if (maxDistance != getTensionDistance() || selectionDistance == null) {
            findSelectionDistance();
        }
        return selectionDistance;
    }

    /**
     * Calculate the distance (in edges) between each vertex and the nearest
     * selected vertex.
     */
    void findSelectionDistance() {
        int i;
        int j;
        int[] dist = new int[((PolyMesh) objInfo.object).getVertices().length];
        Wedge[] e = ((PolyMesh) objInfo.object).getEdges();
        Wface[] f = ((PolyMesh) objInfo.object).getFaces();

        maxDistance = getTensionDistance();

        // First, set each distance to 0 or -1, depending on whether that vertex
        // is part of the
        // current selection.
        if (selectMode == POINT_MODE) {
            for (i = 0; i < dist.length; i++) {
                dist[i] = selected[i] ? 0 : -1;
            }
        } else if (selectMode == EDGE_MODE) {
            for (i = 0; i < dist.length; i++) {
                dist[i] = -1;
            }
            for (i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    dist[e[i].vertex] = dist[e[e[i].hedge].vertex] = 0;
                }
            }
        } else {
            for (i = 0; i < dist.length; i++) {
                dist[i] = -1;
            }
            for (i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    int[] vf = ((PolyMesh) objInfo.object)
                            .getFaceVertices(f[i]);
                    for (j = 0; j < vf.length; ++j) {
                        dist[vf[j]] = 0;
                    }
                }
            }
        }

        // Now extend this outward up to maxDistance.
        for (i = 0; i < maxDistance; i++) {
            for (j = 0; j < e.length / 2; j++) {
                if (dist[e[j].vertex] == -1 && dist[e[e[j].hedge].vertex] == i) {
                    dist[e[j].vertex] = i + 1;
                } else if (dist[e[e[j].hedge].vertex] == -1
                        && dist[e[j].vertex] == i) {
                    dist[e[e[j].hedge].vertex] = i + 1;
                }
            }
        }
        selectionDistance = dist;
    }

    /**
     * Determine whether we are in tolerant selection mode.
     *
     * @return The tolerant valueWidget.getValue()
     */
    public boolean isTolerant() {
        return tolerant;
    }

    /**
     * Set whether to use tolerant selection mode.
     *
     * @param tol
     * The new tolerant valueWidget.getValue()
     */
    public void setTolerant(boolean tol) {
        tolerant = tol;
    }

    /**
     * Get the extra texture parameter which was added the mesh to keep
     * track of which faces are hidden.
     *
     * @return The extraParameter valueWidget.getValue()
     */
    //public TextureParameter getExtraParameter() {
    //	return hideFaceParam;
    //}
    /**
     * Get which faces are hidden. This may be null, which means that all
     * faces are visible.
     *
     * @return The hiddenFaces valueWidget.getValue()
     */
    public boolean[] getHiddenFaces() {
        return hideFace;
    }

    /**
     * Gets the selectionMode attribute of the PolyMeshViewer object
     *
     * @return The selectionMode valueWidget.getValue()
     */
    @Override
    public int getSelectionMode() {
        return selectMode;
    }

    /**
     * Get an array of flags telling which parts of the mesh are currently
     * selected. Depending on the current selection mode, these flags may
     * correspond to vertices, edges or faces.
     *
     * @return The selection valueWidget.getValue()
     */
    @Override
    public boolean[] getSelection() {
        return selected;
    }

    public AdvancedEditingTool.SelectionProperties getSelectionProperties() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        MeshVertex[] v = mesh.getVertices();
        Wedge[] e = mesh.getEdges();
        Wface[] f = mesh.getFaces();
        Vec3[] normals = null;
        Vec3[] features = null;
        switch (selectMode) {
            case POINT_MODE:
                normals = mesh.getNormals();
                features = new Vec3[v.length + 2];
                break;
            case EDGE_MODE:
                normals = mesh.getEdgeNormals();
                features = new Vec3[e.length / 2 + 2];
                break;
            case FACE_MODE:
                normals = mesh.getFaceNormals();
                features = new Vec3[f.length + 2];
                break;
        }
        Vec3 normal = new Vec3();
        /*
		 * int count = 0; for (int i =0; i < selected.length; i++) if
		 * (selected[i]) ++count; Vec3[] features = new Vec3[count+1];
		 * features[0] = new Vec3(); count = 1; Vec3 middle; for (int i =0; i <
		 * selected.length; i++) { if (selected[i]) { switch(selectMode) { case
		 * POINT_MODE : features[0].add(v[i].r); features[count] = new
		 * Vec3(v[i].r); break; case EDGE_MODE : middle =
		 * v[e[i].vertex].r.plus(v[e[e[i].hedge].vertex].r); middle.scale(0.5);
		 * features[0].add(middle); features[count] = middle; break; case
		 * FACE_MODE : int[] fv = mesh.getFaceVertices(f[i]); middle = new
		 * Vec3(); for (int j = 0; j < fv.length; j++) middle.add(v[fv[j]].r);
		 * middle.scale(1.0/(double)fv.length); features[0].add(middle);
		 * features[count] = middle; } normal.add(normals[i]); count++; } }
         */
        int count = 0;
        features[0] = new Vec3();
        features[1] = new Vec3();
        count = 0;
        Vec3 middle;
        for (int i = 0; i < selected.length; i++) {
            switch (selectMode) {
                case POINT_MODE:
                    if (selected[i]) {
                        features[0].add(v[i].r);
                    }
                    features[i + 2] = v[i].r;
                    break;
                case EDGE_MODE:
                    middle = v[e[i].vertex].r.plus(v[e[e[i].hedge].vertex].r);
                    middle.scale(0.5);
                    if (selected[i]) {
                        features[0].add(middle);
                    }
                    features[i + 2] = middle;
                    break;
                case FACE_MODE:
                    int[] fv = mesh.getFaceVertices(f[i]);
                    middle = new Vec3();
                    for (int k : fv) {
                        middle.add(v[k].r);
                    }
                    middle.scale(1.0 / (double) fv.length);
                    if (selected[i]) {
                        features[0].add(middle);
                    }
                    features[i + 2] = middle;
            }
            if (selected[i]) {
                normal.add(normals[i]);
                count++;
            }
        }
        double coef = 1.0 / (double) (count);
        features[0].scale(coef);
        CoordinateSystem coords = null;
        if (normal.length() > 0) {
            normal.normalize();
            Vec3 updir = Vec3.vx();
            if (updir.dot(normal) < 0.9) {
                updir = normal.cross(updir);
                updir.normalize();
            } else {
                updir = normal.cross(Vec3.vy());
                updir.normalize();
            }
            coords = new CoordinateSystem(new Vec3(0, 0, 0), normal, updir);
        }
        AdvancedEditingTool.SelectionProperties props = new AdvancedEditingTool.SelectionProperties();
        props.featurePoints = features;
        props.specificCoordinateSystem = coords;
        return props;
    }

    /**
     * When the selection mode changes, do our best to convert the old
     * selection to the new mode.
     *
     * @param mode
     * The new selectionMode valueWidget.getValue()
     */
    @Override
    public void setSelectionMode(int mode) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        MeshVertex[] v = mesh.getVertices();
        Wedge[] e = mesh.getEdges();
        Wface[] f = mesh.getFaces();
        boolean[] newSel;
        int i;

        if (mode == selectMode) {
            return;
        }
        if (mode == POINT_MODE) {
            overlayVertexEdgeFace.setVisibleChild(vertexContainer);
            newSel = new boolean[v.length];
            if (selectMode == FACE_MODE) {
                for (i = 0; i < f.length; i++) {
                    if (selected[i]) {
                        int[] vf = ((PolyMesh) objInfo.object)
                                .getFaceVertices(f[i]);
                        for (int j = 0; j < vf.length; ++j) {
                            newSel[vf[j]] = true;
                        }
                    }
                }
            } else {
                for (i = 0; i < e.length / 2; i++) {
                    if (selected[i]) {
                        newSel[e[i].vertex] = newSel[e[e[i].hedge].vertex] = true;
                    }
                }
            }
        } else if (mode == EDGE_MODE) {
            overlayVertexEdgeFace.setVisibleChild(edgeContainer);
            newSel = new boolean[e.length / 2];
            if (selectMode == POINT_MODE) {
                if (tolerant) {
                    for (i = 0; i < e.length / 2; i++) {
                        newSel[i] = selected[e[i].vertex] | selected[e[e[i].hedge].vertex];
                    }
                } else {
                    for (i = 0; i < e.length / 2; i++) {
                        newSel[i] = selected[e[i].vertex] & selected[e[e[i].hedge].vertex];
                    }
                }
            } else {
                for (i = 0; i < f.length; i++) {
                    if (selected[i]) {
                        int[] fe = ((PolyMesh) objInfo.object).getFaceEdges(f[i]);
                        for (int j = 0; j < fe.length; ++j) {
                            if (fe[j] >= e.length / 2) {
                                newSel[e[fe[j]].hedge] = true;
                            } else {
                                newSel[fe[j]] = true;
                            }
                        }
                    }
                }
            }
        } else {
            overlayVertexEdgeFace.setVisibleChild(faceContainer);
            newSel = new boolean[f.length];
            if (selectMode == POINT_MODE) {
                if (tolerant) {
                    for (i = 0; i < f.length; i++) {
                        int[] vf = ((PolyMesh) objInfo.object)
                                .getFaceVertices(f[i]);
                        for (int j = 0; j < vf.length; ++j) {
                            newSel[i] |= selected[vf[j]];
                        }
                    }
                } else {
                    for (i = 0; i < f.length; i++) {
                        newSel[i] = true;
                        int[] vf = ((PolyMesh) objInfo.object)
                                .getFaceVertices(f[i]);
                        for (int j = 0; j < vf.length; ++j) {
                            newSel[i] &= selected[vf[j]];
                        }
                    }
                }
            } else {
                int k;
                for (i = 0; i < f.length; i++) {
                    if (!tolerant) {
                        newSel[i] = true;
                    }
                    int[] fe = ((PolyMesh) objInfo.object).getFaceEdges(f[i]);
                    for (int j = 0; j < fe.length; j++) {
                        if (fe[j] >= e.length / 2) {
                            k = e[fe[j]].hedge;
                        } else {
                            k = fe[j];
                        }
                        if (tolerant) {
                            newSel[i] |= selected[k];
                        } else {
                            newSel[i] &= selected[k];
                        }
                    }
                }
            }
        }
        selectMode = mode;
        setSelection(newSel);
        if (modes.getSelection() != mode) {
            modes.selectTool(modes.getTool(mode));
        }
        layoutChildren();
        if (currentTool instanceof AdvancedEditingTool) {
            ((AdvancedEditingTool) currentTool).selectionModeChanged(mode);
        }
        repaint();
    }

    /**
     * Set which faces are hidden. Pass null to show all faces.
     *
     * @param hidden
     * The new hiddenFaces valueWidget.getValue()
     */
    public void setHiddenFaces(boolean[] hidden) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        hideFace = hidden;
        hideVert = new boolean[mesh.getVertices().length];
        if (hideFace == null) {
            for (int i = 0; i < hideVert.length; i++) {
                hideVert[i] = false;
            }
        } else {
            Arrays.fill(hideVert, true);
            Wface[] face = mesh.getFaces();
            for (int i = 0; i < face.length; i++) {
                if (!hideFace[i]) {
                    int[] vf = mesh.getFaceVertices(face[i]);
                    for (int j = 0; j < vf.length; ++j) {
                        hideVert[vf[j]] = false;
                    }
                }
            }
        }
        FaceParameterValue val = (FaceParameterValue) objInfo.object.getParameterValue(faceIndexParam);
        double[] param = val.getValue();
        for (int i = 0; i < param.length; i++) {
            param[i] = i;
        }
        val.setValue(param);
        objInfo.object.setParameterValue(faceIndexParam, val);
        objInfo.clearCachedMeshes();
        updateImage();
    }

    public boolean selectOnlyVisible() {
        return frontSelectCB.getState();
    }

    /**
     * Sets point or edge selection
     *
     * @param sel
     * The new selection array
     */
    @Override
    public void setSelection(boolean[] sel) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wvertex[] verts = (Wvertex[]) mesh.getVertices();
        Wedge[] edges = mesh.getEdges();
        if (selectMode == POINT_MODE && sel.length == verts.length) {
            selected = sel;
        } else if (selectMode == EDGE_MODE && sel.length == mesh.getEdges().length / 2) {
            selected = sel;
        } else if (selectMode == FACE_MODE && sel.length == mesh.getFaces().length) {
            selected = sel;
        }
        findSelectionDistance();
        currentTool.getWindow().updateMenus();
        for (ViewerCanvas view : theView) {
            view.repaint();
        }
        repaint();
    }

    /**
     * Set the object being edited in this window.
     *
     * @param obj
     * The new object valueWidget.getValue()
     */
    public void setObject(Object3D obj) {
        objInfo.object = obj;
        objInfo.clearCachedMeshes();
    }

    /**
     * Sets a new mesh
     *
     * @param mesh
     * The new mesh valueWidget.getValue()
     */
    @Override
    public void setMesh(Mesh mesh) {
        PolyMesh obj = (PolyMesh) mesh;
        setObject(obj);
        hideVert = new boolean[mesh.getVertices().length];
        for (int i = 0; i < theView.length; i++) {
            if (getSelectionMode() == PolyMeshEditorWindow.POINT_MODE && selected.length != obj.getVertices().length) {
                ((PolyMeshViewer) theView[i]).visible = new boolean[obj.getVertices().length];
            }
            if (getSelectionMode() == PolyMeshEditorWindow.EDGE_MODE && selected.length != obj.getEdges().length / 2) {
                ((PolyMeshViewer) theView[i]).visible = new boolean[obj.getEdges().length];
            }
            if (getSelectionMode() == PolyMeshEditorWindow.FACE_MODE && selected.length != obj.getFaces().length) {
                ((PolyMeshViewer) theView[i]).visible = new boolean[obj.getFaces().length];
            }
        }
        if (getSelectionMode() == PolyMeshEditorWindow.POINT_MODE && selected.length != obj.getVertices().length) {
            selected = new boolean[obj.getVertices().length];
        }
        if (getSelectionMode() == PolyMeshEditorWindow.EDGE_MODE && selected.length != obj.getEdges().length / 2) {
            selected = new boolean[obj.getEdges().length / 2];
        }
        if (getSelectionMode() == PolyMeshEditorWindow.FACE_MODE && selected.length != obj.getFaces().length) {
            selected = new boolean[obj.getFaces().length];
        }
        if (hideFace != null) {
            boolean[] oldHideFace = hideFace;
            FaceParameterValue val = (FaceParameterValue) getObject().getGeometry().getParameterValue(faceIndexParam);
            double[] param = val.getValue();
            hideFace = new boolean[obj.getFaces().length];
            for (int i = 0; i < param.length; i++) {
                int index = (int) param[i];
                if (index < oldHideFace.length) {
                    hideFace[i] = oldHideFace[index];
                }
            }
        }

        setHiddenFaces(hideFace);
        updateJointWeightParam();
        findSelectionDistance();
        currentTool.getWindow().updateMenus();
        updateImage();
    }

    /**
     * When the object changes, we need to rebuild the display.
     */
    @Override
    public void objectChanged() {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        mesh.resetMesh();
        setMesh(mesh);
        super.objectChanged();
    }

    /**
     * Update the parameter which records weights for the currently selected joint.
     */
    private void updateJointWeightParam() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        MeshVertex[] vert = mesh.getVertices();
        double[] jointWeight = new double[vert.length];
        int selJointId = ((MeshViewer) theView[currentView]).getSelectedJoint();
        Joint selJoint = getObject().getSkeleton().getJoint(selJointId);
        for (int i = 0; i < jointWeight.length; i++) {
            Joint vertJoint = getObject().getSkeleton().getJoint(vert[i].ikJoint);
            if (selJoint == null) {
                jointWeight[i] = 0.0;
            } else if (vert[i].ikJoint == selJointId) {
                jointWeight[i] = (selJoint.parent == null ? 1.0 : vert[i].ikWeight);
            } else if (vertJoint != null && vertJoint.parent == selJoint) {
                jointWeight[i] = 1.0 - vert[i].ikWeight;
            } else {
                jointWeight[i] = 0.0;
            }
        }
        VertexParameterValue value = (VertexParameterValue) getObject().getGeometry().getParameterValue(jointWeightParam);
        value.setValue(jointWeight);
        getObject().getGeometry().setParameterValues(getObject().getGeometry().getParameterValues());

        objInfo.clearCachedMeshes();
    }

    /**
     * Get the extra texture parameter which was added to the mesh to keep track of
     * face indices in the editor.
     */
    @Override
    public TextureParameter getFaceIndexParameter() {
        return faceIndexParam;
    }

    /**
     * Get the extra texture parameter which was added to the mesh to keep track of
     * joint weighting.
     */
    @Override
    public TextureParameter getJointWeightParam() {
        return jointWeightParam;
    }

    /**
     * Add extra texture parameters to the mesh which will be used for keeping track of face
     * and vertex indices.
     */
    private void addExtraParameters() {
        if (faceIndexParam != null) {
            return;
        }
        faceIndexParam = new TextureParameter(this, "Face Index", 0.0, Double.MAX_VALUE, 0.0);
        jointWeightParam = new TextureParameter(this, "Joint Weight", 0.0, 1.0, 0.0);
        PolyMesh mesh = (PolyMesh) getObject().getGeometry();
        TextureParameter[] params = mesh.getParameters();
        TextureParameter[] newparams = new TextureParameter[params.length + 2];
        ParameterValue[] values = mesh.getParameterValues();
        ParameterValue[] newvalues = new ParameterValue[values.length + 2];
        for (int i = 0; i < params.length; i++) {
            newparams[i] = params[i];
            newvalues[i] = values[i];
        }
        newparams[params.length] = faceIndexParam;
        newvalues[values.length] = new FaceParameterValue(mesh, faceIndexParam);
        double[] faceIndex = new double[mesh.getFaces().length];
        for (int i = 0; i < faceIndex.length; i++) {
            faceIndex[i] = i;
        }
        ((FaceParameterValue) newvalues[values.length]).setValue(faceIndex);
        newparams[params.length + 1] = jointWeightParam;
        newvalues[values.length + 1] = new VertexParameterValue(mesh, jointWeightParam);
        mesh.setParameters(newparams);
        mesh.setParameterValues(newvalues);
        getObject().clearCachedMeshes();
        updateJointWeightParam();
    }

    /**
     * Remove the extra texture parameters from the mesh which were used for keeping track of
     * face and vertex indices.
     */
    public void removeExtraParameters() {
        if (faceIndexParam == null) {
            return;
        }
        faceIndexParam = null;
        jointWeightParam = null;
        PolyMesh mesh = (PolyMesh) getObject().getGeometry();
        TextureParameter[] params = mesh.getParameters();
        TextureParameter[] newparams = new TextureParameter[params.length - 2];
        ParameterValue[] values = mesh.getParameterValues();
        ParameterValue[] newvalues = new ParameterValue[values.length - 2];
        for (int i = 0; i < newparams.length; i++) {
            newparams[i] = params[i];
            newvalues[i] = values[i];
        }
        mesh.setParameters(newparams);
        mesh.setParameterValues(newvalues);
        getObject().clearCachedMeshes();
    }

    /**
     * Selects complete boundaries based on vertex or edge selection. If
     * selection is empty, then all boundaries are selected.
     */
    private void doSelectBoundary(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wedge[] edges = mesh.getEdges();
        Wvertex[] vertices = (Wvertex[]) mesh.getVertices();

        boolean emptySel = true;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                emptySel = false;
            }
        }

        if (selectMode == POINT_MODE) {
            boolean[] edgeSel = new boolean[edges.length / 2];
            for (int i = 0; i < selected.length; ++i) {
                if (emptySel || selected[i]) {
                    int[] ve = mesh.getVertexEdges(vertices[i]);
                    for (int j = 0; j < ve.length; ++j) {
                        if (edges[ve[j]].face == -1) {
                            int sel = ve[j];
                            if (sel >= edges.length / 2) {
                                sel = edges[sel].hedge;
                            }
                            edgeSel[sel] = true;
                        }
                    }
                }
            }
            edgeSel = mesh.getBoundarySelection(edgeSel);
            for (int i = 0; i < selected.length; ++i) {
                selected[i] = false;
            }
            for (int i = 0; i < edgeSel.length; ++i) {
                if (edgeSel[i]) {
                    selected[edges[i].vertex] = true;
                    selected[edges[edges[i].hedge].vertex] = true;
                }
            }
        } else {
            if (emptySel) {
                for (int i = 0; i < edges.length; ++i) {
                    if (edges[i].face == -1) {
                        if (i >= edges.length / 2) {
                            selected[edges[i].hedge] = true;
                        } else {
                            selected[i] = true;
                        }
                    }
                }
            } else {
                selected = mesh.getBoundarySelection(selected);
            }
        }
        setSelection(selected);
    }

    /**
     * Closes selected boundaries
     */
    private void doCloseBoundary(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        Wedge[] edges = mesh.getEdges();
        Wvertex[] vertices = (Wvertex[]) mesh.getVertices();
        boolean[] newFaceSel;
        if (selectMode == POINT_MODE) {
            boolean[] edgeSel = new boolean[edges.length / 2];
            for (int i = 0; i < selected.length; ++i) {
                if (selected[i]) {
                    int[] ve = mesh.getVertexEdges(vertices[i]);
                    for (int j = 0; j < ve.length; ++j) {
                        if (edges[ve[j]].face == -1) {
                            int sel = ve[j];
                            if (sel >= edges.length / 2) {
                                sel = edges[sel].hedge;
                            }
                            edgeSel[sel] = true;
                        }
                    }
                }
            }
            newFaceSel = mesh.closeBoundary(edgeSel);

        } else {
            newFaceSel = mesh.closeBoundary(selected);
        }
        objectChanged();
        setSelectionMode(FACE_MODE);
        updateMenus();
        setSelection(newFaceSel);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
    }

    /**
     * Joins boundaries, each one of the two being identified by a selected
     * vertex
     */
    private void doJoinBoundaries(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] newFaceSel;
        int one = -1;
        int two = -1;
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                if (one == -1) {
                    one = i;
                } else {
                    two = i;
                }
            }
        }
        if (!mesh.joinBoundaries(one, two)) {
            return;
        }
        objectChanged();
        setSelectionMode(FACE_MODE);
        updateMenus();
        newFaceSel = new boolean[mesh.getFaces().length];
        for (int i = prevMesh.getFaces().length; i < newFaceSel.length; ++i) {
            newFaceSel[i] = true;
        }
        setSelection(newFaceSel);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
    }

    /**
     * Hide the selected part of the mesh.
     */
    private void doHideSelection(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] hide = new boolean[mesh.getFaces().length];
        if (selectMode == FACE_MODE) {
            System.arraycopy(selected, 0, hide, 0, selected.length);
        } else if (selectMode == EDGE_MODE) {
            Wedge[] edges = mesh.getEdges();
            for (int i = 0; i < selected.length; i++) {
                if (selected[i]) {
                    if (edges[i].face >= 0) {
                        hide[edges[i].face] = true;
                    }
                    if (edges[edges[i].hedge].face >= 0) {
                        hide[edges[edges[i].hedge].face] = true;
                    }
                }
            }
        } else {
            Wface[] faces = mesh.getFaces();
            for (int i = 0; i < faces.length; i++) {
                hide[i] = false;
                int[] vf = mesh.getFaceVertices(faces[i]);
                for (int j = 0; j < vf.length; ++j) {
                    hide[i] = (hide[i] || selected[vf[j]]);
                }
            }
        }
        boolean[] wasHidden = hideFace;
        if (wasHidden != null) {
            for (int i = 0; i < wasHidden.length; i++) {
                if (wasHidden[i]) {
                    hide[i] = true;
                }
            }
        }
        setHiddenFaces(hide);
        for (int i = 0; i < selected.length; i++) {
            selected[i] = false;
        }
        setSelection(selected);
    }

    /**
     * Show all faces of the mesh.
     */
    private void doShowAll(ActionEvent event) {
        setHiddenFaces(null);
    }

    /**
     * Collapse faces command
     */
    private void doCollapseFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (mesh.getFaces().length == 1) {
            new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")),
                    BStandardDialog.ERROR).showMessageDialog(null);
            return;
        }
        PolyMesh prevMesh = mesh.duplicate();
        mesh.collapseFaces(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();
    }

    /**
     * Collapse edges command
     */
    private void doCollapseEdges(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (mesh.getFaces().length == 1) {
            new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")),
                    BStandardDialog.ERROR).showMessageDialog(null);
            return;
        }
        PolyMesh prevMesh = mesh.duplicate();
        mesh.collapseEdges(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();
    }

    /**
     * Collapse vertices command
     */
    private void doCollapseVertices(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wvertex[] verts = (Wvertex[]) mesh.getVertices();
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                int[] fv = mesh.getVertexEdges(verts[i]);
                if (fv.length == selected.length) {
                    new BStandardDialog(Translate.text("polymesh:errorTitle"), UIUtilities.breakString(Translate.text("illegalDelete")),
                            BStandardDialog.ERROR).showMessageDialog(null);
                    return;
                }
            }
        }
        PolyMesh prevMesh = mesh.duplicate();
        mesh.collapseVertices(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();
    }

    /**
     * Facet vertices command
     */
    private void doFacetVertices(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wvertex[] verts = (Wvertex[]) mesh.getVertices();
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                int[] fv = mesh.getVertexEdges(verts[i]);
                if (fv.length == selected.length) {
                    new BStandardDialog(Translate.text("polymesh:errorTitle"),
                            UIUtilities.breakString(Translate
                                    .text("illegalDelete")),
                            BStandardDialog.ERROR).showMessageDialog(null);
                    return;
                }
            }
        }
        PolyMesh prevMesh = mesh.duplicate();
        mesh.facetVertices(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        updateImage();
    }

    /**
     * Merge edges command
     */
    private void doMergeEdges(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] sel = mesh.mergeEdges(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        setSelection(sel);
        updateImage();
    }

    /**
     * Merge faces command
     */
    private void doMergeFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] sel = mesh.mergeFaces(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        setSelection(sel);
        updateImage();
    }

    /**
     * Triangulate faces command
     */
    private void doTriangulateFaces(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        boolean[] sel = mesh.triangulateFaces(selected);
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
        objectChanged();
        setSelection(sel);
        updateImage();
    }

    /**
     * Invert the current selection.
     */
    public void invertSelectionCommand(ActionEvent event) {
        boolean[] newSel = new boolean[selected.length];
        for (int i = 0; i < newSel.length; i++) {
            newSel[i] = !selected[i];
        }
        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_MESH_SELECTION, this, selectMode, selected));
        setSelection(newSel);
    }

    /**
     * Scales current selection using the slider
     */
    void scaleSelectionCommand(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        initSelPoints();
        valueWidget.activate(1.0, this::doScaleSelectionCallback);
    }

    /**
     * Push/pulls current selection using the slider
     */
    void scaleNormalSelectionCommand(ActionEvent event) {
        if (valueWidget.isActivated()) {
            return;
        }
        initSelPoints();

        valueWidget.activate(this::doScaleNormalSelectionCallback);
    }

    private void initSelPoints() {
        prepareToShowValueWidget();
        MeshVertex[] orVerts = priorValueMesh.getVertices();

        if (selectMode == POINT_MODE) {
            selPoints = selected;
        } else if (selectMode == EDGE_MODE) {
            selPoints = new boolean[orVerts.length];
            Wedge[] edges = priorValueMesh.getEdges();
            for (int i = 0; i < valueSelection.length; ++i) {
                if (valueSelection[i]) {
                    selPoints[edges[i].vertex] = true;
                    selPoints[edges[edges[i].hedge].vertex] = true;
                }
            }
        } else {
            selPoints = new boolean[orVerts.length];
            Wface[] faces = priorValueMesh.getFaces();
            for (int i = 0; i < valueSelection.length; ++i) {
                if (valueSelection[i]) {
                    int[] fv = priorValueMesh.getFaceVertices(faces[i]);
                    for (int j = 0; j < fv.length; j++) {
                        selPoints[fv[j]] = true;
                    }
                }
            }
        }
        int count = 0;
        selCenter = new Vec3();
        for (int i = 0; i < selPoints.length; i++) {
            if (selPoints[i]) {
                selCenter.add(orVerts[i].r);
                ++count;
            }
        }
        if (count > 0) {
            selCenter.scale(1.0 / count);
            for (int i = 0; i < selPoints.length; i++) {
                if (selPoints[i]) {
                    meanSelDistance += orVerts[i].r.distance(selCenter);
                }
            }
            meanSelDistance /= count;
        }
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog (scaleSelection)
     */
    private void doScaleSelectionCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        MeshVertex[] vertices = mesh.getVertices();
        MeshVertex[] orVerts = priorValueMesh.getVertices();

        for (int i = 0; i < selPoints.length; i++) {
            if (selPoints[i]) {
                vertices[i].r = selCenter.plus(orVerts[i].r.minus(selCenter)
                        .times(valueWidget.getValue()));
            }
        }
        objectChanged();
        setSelection(valueSelection);
    }

    /**
     * Callback called when the valueWidget.getValue() has changed in the
     * valueWidget.getValue() dialog (push/pull selection)
     */
    private void doScaleNormalSelectionCallback() {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        mesh.copyObject(priorValueMesh);
        Vec3[] normals = priorValueMesh.getNormals();
        MeshVertex[] vertices = mesh.getVertices();
        MeshVertex[] orVerts = priorValueMesh.getVertices();
        for (int i = 0; i < selPoints.length; i++) {
            if (selPoints[i]) {
                vertices[i].r = orVerts[i].r.plus(normals[i].times(valueWidget
                        .getValue()));
            }
        }
        objectChanged();
        setSelection(valueSelection);
    }

    /**
     * mirrors the mesh about XY plane
     */
    private void doMirrorWholeXY(ActionEvent event) {
        doMirrorWhole(PolyMesh.MIRROR_ON_XY);
    }

    /**
     * mirrors the mesh about YZ plane
     */
    private void doMirrorWholeYZ(ActionEvent event) {
        doMirrorWhole(PolyMesh.MIRROR_ON_YZ);
    }

    /**
     * mirrors the mesh about XZ plane
     */
    private void doMirrorWholeXZ(ActionEvent event) {
        doMirrorWhole(PolyMesh.MIRROR_ON_XZ);
    }

    /**
     * mirrors the mesh about a plane
     */
    private void doMirrorWhole(short mirrorOrientation) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        mesh.mirrorWholeMesh(mirrorOrientation);
        objectChanged();
        updateMenus();
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
    }

    /**
     * inverts normals
     */
    private void doInvertNormals(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh prevMesh = mesh.duplicate();
        mesh.invertNormals();
        objectChanged();
        updateMenus();
        updateImage();
        setUndoRecord(new UndoRecord(this, false, UndoRecord.COPY_OBJECT, mesh, prevMesh));
    }

    /**
     * Sets off a previously set mirror
     */
    //TODO: Extract this dialog
    private void doTurnMirrorOff(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        if (mesh.getMirrorState() == PolyMesh.NO_MIRROR) {
            return;
        }
        BStandardDialog dlg = new BStandardDialog(Translate.text("polymesh:removeMeshMirror"), Translate.text("polymesh:keepMirroredMesh"), BStandardDialog.QUESTION);
        int r = dlg.showOptionDialog(this, new String[]{
            Translate.text("polymesh:keep"), Translate.text("polymesh:discard"),
            Translate.text("button.cancel")}, "cancel");
        if (r == 0) {
            PolyMesh newMesh = mesh.getMirroredMesh();
            mesh.copyObject(newMesh);
            ((BCheckBoxMenuItem) mirrorItem[1]).setState(false);
            ((BCheckBoxMenuItem) mirrorItem[2]).setState(false);
            ((BCheckBoxMenuItem) mirrorItem[3]).setState(false);
            objectChanged();
            updateImage();
        } else if (r == 1) {
            ((BCheckBoxMenuItem) mirrorItem[1]).setState(false);
            ((BCheckBoxMenuItem) mirrorItem[2]).setState(false);
            ((BCheckBoxMenuItem) mirrorItem[3]).setState(false);
            mesh.setMirrorState(PolyMesh.NO_MIRROR);
            objectChanged();
            setSelection(selected);
        }
    }

    /**
     * Sets a mirror on XY, YZ or XZ plane
     *
     * @param ev
     * CommandEvent
     */
    @SuppressWarnings("java:S1172")
    private void doMirrorOn(CommandEvent ev) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        short mirrorState = 0;
        if (((BCheckBoxMenuItem) mirrorItem[1]).getState()) {
            mirrorState |= PolyMesh.MIRROR_ON_XY;
        }
        if (((BCheckBoxMenuItem) mirrorItem[2]).getState()) {
            mirrorState |= PolyMesh.MIRROR_ON_XZ;
        }
        if (((BCheckBoxMenuItem) mirrorItem[3]).getState()) {
            mirrorState |= PolyMesh.MIRROR_ON_YZ;
        }
        mesh.setMirrorState(mirrorState);
        realMirror = false;
        objectChanged();
        setSelection(selected);
    }

    private void doCopyEvent() {
        pasteItem.setEnabled(true);
    }

    private void doCopy() {
        clipboardMesh = ((PolyMesh) objInfo.getGeometry()).duplicate();
        int selCount = 0;

        if (selected != null) {
            for (int i = 0; i < selected.length; i++) {
                if (!selected[i]) {
                    ++selCount;
                }
            }
        }
        if (selCount > 0) {
            if (selectMode == POINT_MODE) {
                int[] indices = new int[selCount];
                int count = 0;
                for (int i = 0; i < selected.length; ++i) {
                    if (!selected[i]) {
                        indices[count++] = i;
                    }
                }
                if (clipboardMesh.getVertices().length - indices.length < 3) {
                    //back to original mesh
                    clipboardMesh = ((PolyMesh) objInfo.getGeometry()).duplicate();
                } else {
                    clipboardMesh.deleteVertices(indices);
                }
            } else if (selectMode == EDGE_MODE) {
                int count = 0;
                int[] indices = new int[selCount];
                for (int i = 0; i < selected.length; ++i) {
                    if (!selected[i]) {
                        indices[count++] = i;
                    }
                }
                if (clipboardMesh.getEdges().length - indices.length < 3) {
                    //back to original mesh
                    clipboardMesh =  ((PolyMesh) objInfo.getGeometry()).duplicate();
                } else {
                    clipboardMesh.deleteEdges(indices);
                }
            } else {
                int count = 0;
                int[] indices = new int[selCount];
                for (int i = 0; i < selected.length; ++i) {
                    if (!selected[i]) {
                        indices[count++] = i;
                    }
                }
                if (clipboardMesh.getFaces().length - indices.length < 1) {
                    //back to original mesh
                    clipboardMesh = ((PolyMesh) objInfo.getGeometry()).duplicate();
                } else {
                    clipboardMesh.deleteFaces(indices);
                }
            }
        }
        eventSource.dispatchEvent(new CopyEvent(this));
    }

    private void doPaste() {
        if (clipboardMesh == null) {
            return;
        }
        setSelectionMode(POINT_MODE);
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] sel = mesh.addMesh(clipboardMesh);
        objectChanged();
        setSelection(sel);
    }

    private void doSaveAsTemplate(ActionEvent event) {

        File templateDir = new File(ArtOfIllusion.PLUGIN_DIRECTORY + File.separator + "PolyMeshTemplates");
        if (!templateDir.exists() && !templateDir.mkdir()) {
            new BStandardDialog(Translate.text("polymesh:errorTemplateDir"), UIUtilities.breakString(Translate.text("illegalDelete")),
                    BStandardDialog.ERROR).showMessageDialog(null);
            return;
        }
        var chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(Translate.text("polymesh:saveTemplate"));
        chooser.setCurrentDirectory(templateDir);
        if(chooser.showSaveDialog(this.getComponent()) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                ((PolyMesh) objInfo.object).writeToFile(dos, null);
            } catch (IOException ex) {
                log.atError().setCause(ex).log("Error writing template: {}", ex.getMessage());
            }
        }
    }

    /**
     * Get the object being edited in this window.
     *
     * @return The object
     */
    @Override
    public ObjectInfo getObject() {
        return objInfo;
    }

    private void skeletonDetachedChanged() {
        for (int i = 0; i < theView.length; i++) {
            ((PolyMeshViewer) theView[i])
                    .setSkeletonDetached(((BCheckBoxMenuItem) skeletonMenuItem[5])
                            .getState());
        }
    }

    private void doLooseSelectionChanged() {
        looseSelect = looseSelectCB.getState();
    }

    private void doLooseSelectionValueChanged(ChangeEvent event) {
        log.info("Loose Value Event: {}", event);
        looseSelectValue = ((Integer) looseSelectSpinner.getValue());
    }

    private void doFrontSelectionChanged() {
        if (selectVisible = frontSelectCB.getState()) {
            setSelection(selected);
        }
    }

    public boolean isFrontSelectionOn() {
        return frontSelectCB.getState();
    }

    public int getLooseSelectionRange() {
        return looseSelectCB.getState() ? (Integer) looseSelectSpinner.getValue() : 0;
    }

    /**
     * Checks mesh for validity
     */
    private void doCheckMesh(ActionEvent event) {
        SwingUtilities.invokeLater(() -> new CheckMeshDialog(this).setVisible(true));
    }

    private void tolerantModeChanged() {
        tolerant = lastTolerant = ((BCheckBoxMenuItem) editMenuItem[6]).getState();
        setTolerant(((BCheckBoxMenuItem) editMenuItem[6]).getState());
        savePreferences();
    }

    private void doControlledSmoothing(ActionEvent event) {
        new ControlledSmoothingDialog(this).setVisible(true);
    }

    /**
     * Finds appropriate seams in the mesh
     *
     */
    private void doFindSeams() {
        ((PolyMesh) objInfo.object).findSeams();
        objectChanged();
        updateImage();
    }

    @SuppressWarnings("unused")
    private void doMarkSelAsSeams(ActionEvent event) {
        if (selectMode == EDGE_MODE) {
            boolean[] seams = new boolean[selected.length];
            for (int i = 0; i < selected.length; i++) {
                seams[i] = selected[i];
            }
            ((PolyMesh) objInfo.object).setSeams(seams);
            objectChanged();
            updateImage();
            updateMenus();
        }
    }

    private void doOpenSeams(ActionEvent event) {
        ((PolyMesh) objInfo.object).openSeams();
        objectChanged();
        updateImage();
        updateMenus();
    }

    private void doClearSeams(ActionEvent event) {
        ((PolyMesh) objInfo.object).setSeams(null);
        objectChanged();
        updateImage();
        updateMenus();
    }

    private void doUnfoldMesh(ActionEvent event) {
        UnfoldStatusDialog dlg = new UnfoldStatusDialog(this);
        if (!dlg.cancelled) {
            doEditMapping(null);
        }
    }

    @SuppressWarnings("unused")
    void doUnfold(UnfoldStatusDialog dlg) {
        PolyMesh theMesh = (PolyMesh) objInfo.getGeometry();
        PolyMesh mesh =  ((PolyMesh) objInfo.getGeometry()).duplicate();
        ObjectInfo info = objInfo.duplicate();
        info.coords = new CoordinateSystem();
        int[] vertTable;
        vertTable = mesh.openSeams();
        int vertCount = mesh.getVertices().length;
        try {
            mesh.setSmoothingMethod(Mesh.NO_SMOOTHING);
            TriangleMesh triMesh = mesh.convertToTriangleMesh(0);
            int newVertCount = triMesh.getVertices().length;
            if (vertTable == null && newVertCount != vertCount) {
                vertTable = new int[newVertCount];
                for (int i = 0; i < vertCount; i++) {
                    vertTable[i] = i;
                }
                for (int i = vertCount; i < newVertCount; i++) {
                    vertTable[i] = -1;
                }
            } else if (vertTable != null && newVertCount != vertCount) {
                int[] nVertTable = new int[newVertCount];
                for (int i = 0; i < vertCount; i++) {
                    nVertTable[i] = vertTable[i];
                }
                for (int i = vertCount; i < newVertCount; i++) {
                    nVertTable[i] = -1;
                }
                vertTable = nVertTable;
            }
            int[] faceTable = mesh.getTriangleFaceIndex();
            MeshUnfolder unfolder = new MeshUnfolder(mesh, triMesh, vertTable, faceTable);
            if (unfolder.unfold(dlg.textArea, dlg.residual)) {
                UVMappingData data = new UVMappingData(unfolder.getUnfoldedMeshes());
                theMesh.setMappingData(data);
                dlg.unfoldFinished(true);
            } else {
                dlg.unfoldFinished(false);
            }
        } catch (Exception ex) {
            dlg.unfoldFinished(false);
            log.atError().setCause(ex).log("Error unfolding: {}", ex.getMessage());
        }
        updateMenus();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void doEditMapping(ActionEvent event) {
        PolyMesh theMesh = (PolyMesh) objInfo.object;
        ObjectInfo info = objInfo.duplicate();
        info.coords = new CoordinateSystem();
        UVMappingData data = theMesh.getMappingData();
        if (data == null) {
            return;
        }
        new UVMappingEditorDialog(info, true, this);
    }

    private void doFindSimilarFaces() {
        //SwingUtilities.invokeLater(() -> new FindSimilarFacesDialogNew(this).setVisible(true));
        new FindSimilarFacesDialog(this).setVisible(true);
        
    }

    private void doFindSimilarEdges(ActionEvent event) {
        new FindSimilarEdgesDialog(this).setVisible(true);
    }

    private void onEdgeSliderValueChange(ChangeEvent event) {
        log.debug("Value changed for {}", event.getSource());
    }
    private void onCornerCheckboxValueChange(ItemEvent event) {

        PolyMesh mesh = (PolyMesh) objInfo.object;
        Wvertex[] vertices = (Wvertex[]) mesh.getVertices();
        short type = Wvertex.NONE;
        if (cornerCB.getState()) {
            type = Wvertex.CORNER;
        }
        for (int i = 0; i < selected.length; ++i) {
            if (selected[i]) {
                vertices[i].type = type;
            }
        }
        objectChanged();
        updateImage();
    }

    private void onInteractiveLevelValueChange(ChangeEvent event) {

        var model = (SpinnerNumberModel)ispin.getModel();
        ((PolyMesh) objInfo.object).setInteractiveSmoothLevel(model.getNumber().intValue());
        objectChanged();
        updateImage();
    }


    public PolyMeshValueWidget getValueWidget() {
        return valueWidget;
    }

    /**
     * Load all the preferences into memory.
     */
    @Override
    protected void loadPreferences() {
        super.loadPreferences();
        lastFreehand = preferences.getBoolean("freehandSelection", lastFreehand);
        lastTolerant = preferences.getBoolean("tolerantSelection", lastTolerant);
        lastProjectOntoSurface = preferences.getBoolean("projectOntoSurface", lastProjectOntoSurface);
    }

    /**
     * Save user settings that should be persistent between sessions.
     */
    @Override
    protected void savePreferences() {
        super.savePreferences();
        preferences.putBoolean("freehandSelection", lastFreehand);
        preferences.putBoolean("tolerantSelection", lastTolerant);
        preferences.putBoolean("projectOntoSurface", lastProjectOntoSurface);
    }

    @Override
    public void setTensionCommand() {
        super.setTensionCommand();
        tensionSpin.setValue(tensionDistance);
    }

    public void doTensionChanged(ChangeEvent event) {
        lastTensionDistance = tensionDistance = ((Integer) tensionSpin.getValue());
        savePreferences();
    }

    /**
     * This method extracts the current selection to AoI curves
     */
    private void doExtractToCurve(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        ArrayList curves = mesh.extractCurveFromSelection(selected);
        ArrayList closed = (ArrayList) curves.get(curves.size() - 1);
        for (int i = 0; i < curves.size() - 1; i++) {
            ArrayList curve = (ArrayList) curves.get(i);
            Vec3[] v = new Vec3[curve.size()];
            float[] s = new float[v.length];
            for (int j = 0; j < v.length; j++) {
                v[j] = (Vec3) curve.get(j);
                s[j] = 1.0f;
            }
            boolean b = ((Boolean) closed.get(i));
            Curve c = new Curve(v, s, mesh.getSmoothingMethod(), b);
            ((LayoutWindow) parentWindow).addObject(c, objInfo.coords, ("PMCurve " + i), null);
        }
        ((LayoutWindow) parentWindow).repaint();
    }

    /**
     * This method adds the mesh seams to edge selection
     */
    public void doSeamsToSel(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] seams = mesh.getSeams();
        if (seams == null) {
            return;
        }
        for (int i = 0; i < seams.length; i++) {
            selected[i] |= seams[i];
        }
        objectChanged();
        updateImage();
    }

    /**
     * This method removes the selection off the mesh
     */
    public void doRemoveSelFromSeams(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] seams = mesh.getSeams();
        if (seams == null) {
            return;
        }
        for (int i = 0; i < seams.length; i++) {
            seams[i] &= !selected[i];
        }
        mesh.setSeams(seams);
        objectChanged();
        updateImage();
    }

    /**
     * This method removes the selection off the mesh
     */
    public void doAddSelToSeams(ActionEvent event) {
        PolyMesh mesh = (PolyMesh) objInfo.object;
        boolean[] seams = mesh.getSeams();
        if (seams == null) {
            return;
        }
        for (int i = 0; i < seams.length; i++) {
            seams[i] |= selected[i];
        }
        mesh.setSeams(seams);
        objectChanged();
        updateImage();
    }

    @AllArgsConstructor
    private class CopyEvent implements WidgetEvent {

        @Getter
        private final Widget widget;
    }

}
