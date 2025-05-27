/* Copyright (C) 1999-2015 by Peter Eastman
   Changes copyright (C) 2016-2025 by Maksim Khramov
   Changes copyright (C) 2017-2020 by Petri Ihalainen

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import artofillusion.animation.*;
import artofillusion.image.ImagesDialog;
import artofillusion.keystroke.KeystrokeManager;
import artofillusion.math.*;
import artofillusion.object.*;
import artofillusion.script.*;
import artofillusion.texture.*;
import artofillusion.tools.PrimitivesMenu;
import artofillusion.ui.*;
import artofillusion.view.ViewAnimation;
import buoy.event.*;
import buoy.widget.*;
import buoyx.docking.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * The LayoutWindow class represents the main window for creating and laying out
 * scenes.
 */
@Slf4j
public class LayoutWindow extends BFrame implements EditingWindow, PopupMenuManager {


    SceneViewer[] theView;
    BorderContainer[] viewPanel;
    FormContainer viewsContainer;

    private final DockingContainer[] dock;

    private final Score score;
    private final ToolPalette tools;

    private final StatusPanel helpText = new StatusPanel();
    private final SceneExplorer sceneExplorer;
    Scene theScene;

    /**
     * -- GETTER --
     * Determine whether the scene has been modified since it was last saved.
     */
    @Getter
    private boolean modified;
    /**
     * -- GETTER --
     * Get the File menu.
     */
    @Getter
    private final BMenu fileMenu = new LayoutFileMenu(this);
    /**
     * -- GETTER --
     * Get the Edit menu.
     */
    @Getter
    private final BMenu editMenu = new LayoutEditMenu(this);
    /**
     * -- GETTER --
     * Get the Scene menu.
     */
    @Getter
    private final BMenu sceneMenu = new LayoutSceneMenu(this);
    /**
     * -- GETTER --
     * Get the Object menu.
     */
    @Getter
    private final BMenu objectMenu = Translate.menu("object");
    /**
     * -- GETTER --
     * Get the Animation menu.
     */
    @Getter
    private final BMenu animationMenu = new LayoutAnimationMenu(this);
    /**
     * -- GETTER --
     * Get the Tools menu.
     */
    @Getter
    private final BMenu toolsMenu = new LayoutToolsMenu(this);
    /**
     * -- GETTER --
     * Get the View menu.
     */
    @Getter
    private final BMenu viewMenu = Translate.menu("view");

    BMenu newScriptMenu;
    @Getter
    BMenu recentFilesMenu;
    @Getter
    BMenu scriptMenu;

    BMenu addTrackMenu, positionTrackMenu, rotationTrackMenu, distortionMenu;

    private final BMenuItem fileMenuItem = Translate.menuItem("save", event -> saveCommand());

    private BMenuItem[] editMenuItem;
    private BMenuItem[] objectMenuItem;
    private BMenuItem[] viewMenuItem;

    BMenuItem[] animationMenuItem;
    BMenuItem[] popupMenuItem;
    BCheckBoxMenuItem[] displayItem;
    /**
     * -- GETTER --
     *  Get the popup menu.
     */
    @Getter
    BPopupMenu popupMenu;
    private final UndoStack undoStack = new UndoStack();
    int numViewsShown, currentView;
    private final ActionProcessor uiEventProcessor;

    private boolean sceneChangePending;
    private boolean objectListShown;
    private final KeyEventPostProcessor keyEventHandler;
    private final SceneChangedEvent sceneChangedEvent;

    protected Preferences preferences;
    private boolean hasNotifiedPlugins;
    private BMenu recentScriptMenu;


    /**
     * Create a new LayoutWindow for editing a Scene. Usually, you will not use
     * this constructor directly. Instead, call ArtOfIllusion.newWindow(Scene
     * s).
     */
    public LayoutWindow(Scene s) {
        super(s.getName() == null ? "Untitled" : s.getName());
        theScene = s;

        score = new Score(this);

        sceneChangedEvent = new SceneChangedEvent(this);
        uiEventProcessor = new ActionProcessor();

        objectListShown = true;

        // Create the four SceneViewer panels.
        theView = new SceneViewer[4];
        viewPanel = new BorderContainer[4];
        RowContainer row;
        Object listen = new Object() {
            void processEvent(MousePressedEvent ev) {
                setCurrentView((ViewerCanvas) ev.getWidget());
            }
        };
        Object keyListener = new Object() {
            public void processEvent(KeyPressedEvent ev) {
                handleKeyEvent(ev);
            }
        };
        for (int i = 0; i < 4; i++) {
            viewPanel[i] = new BorderContainer() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, 0);
                }

                @Override
                public Dimension getMinimumSize() {
                    return new Dimension(0, 0);
                }
            };
            viewPanel[i].add(row = new RowContainer(), BorderContainer.NORTH);
            viewPanel[i].add(theView[i] = new SceneViewer(theScene, row, this), BorderContainer.CENTER);
            theView[i].setGrid(theScene.getGridSpacing(), theScene.getGridSubdivisions(), theScene.getShowGrid(), theScene.getSnapToGrid());
            theView[i].addEventLink(MousePressedEvent.class, listen);
            theView[i].addEventLink(KeyPressedEvent.class, keyListener);
            theView[i].setPopupMenuManager(this);
            theView[i].setViewAnimation(new ViewAnimation(this, theView[i]));
            theView[i].lastSetNavigation = 1;
            theView[i].setNavigationMode(1, false);
        }
        theView[1].setOrientation(2);
        theView[2].setOrientation(4);
        theView[3].setOrientation(6);

        theView[currentView].setDrawFocus(true);
        viewsContainer = new FormContainer(new double[]{1, 1}, new double[]{1, 1});
        viewsContainer.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        viewsContainer.add(viewPanel[0], 0, 0);
        viewsContainer.add(viewPanel[1], 1, 0);
        viewsContainer.add(viewPanel[2], 0, 1);
        viewsContainer.add(viewPanel[3], 1, 1);
        FormContainer centerContainer = new FormContainer(new double[]{0.0, 1.0}, new double[]{0.0, 1.0, 0.0, 0.0});
        centerContainer.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.BOTH, null, null));
        centerContainer.add(viewsContainer, 1, 0, 1, 3);
        centerContainer.add(helpText.getComponent(), 0, 3, 2, 1);
        dock = new DockingContainer[4];
        dock[0] = new DockingContainer(centerContainer, BTabbedPane.LEFT);
        dock[1] = new DockingContainer(dock[0], BTabbedPane.RIGHT);
        dock[2] = new DockingContainer(dock[1], BTabbedPane.BOTTOM);
        dock[3] = new DockingContainer(dock[2], BTabbedPane.TOP);
        setContent(dock[3]);
        for (int i = 0; i < dock.length; i++) {
            dock[i].setHideSingleTab(true);
            dock[i].addEventLink(DockingEvent.class, this, "dockableWidgetMoved");
            BSplitPane split = dock[i].getSplitPane();
            split.setContinuousLayout(true);
            split.setOneTouchExpandable(true);
            BTabbedPane.TabPosition pos = dock[i].getTabPosition();
            split.setResizeWeight(pos == BTabbedPane.TOP || pos == BTabbedPane.LEFT ? 1.0 : 0.0);
            split.addEventLink(ValueChangedEvent.class, this, "updateMenus");
        }
        ObjectPropertiesPanel propertiesPanel = new ObjectPropertiesPanel(this);
        BScrollPane propertiesScroller = new BScrollPane(propertiesPanel, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_AS_NEEDED);
        propertiesScroller.getVerticalScrollBar().setUnitIncrement(10);
        propertiesScroller.setBackground(ThemeManager.getAppBackgroundColor());
        getDockingContainer(BTabbedPane.RIGHT).addDockableWidget(sceneExplorer = new SceneExplorer(this));
        getDockingContainer(BTabbedPane.RIGHT).addDockableWidget(new DefaultDockableWidget(propertiesScroller, Translate.text("Properties")), 0, 1);
        getDockingContainer(BTabbedPane.BOTTOM).addDockableWidget(new DefaultDockableWidget(score, Translate.text("Score")));

        // Build the tool palette.
        tools = new ToolPalette(2, 7, this);
        EditingTool metaTool, altTool, defaultTool, compoundTool;
        ScrollViewTool scrollTool;
        tools.addTool(defaultTool = new MoveObjectTool(this));
        tools.addTool(new RotateObjectTool(this));
        tools.addTool(new ScaleObjectTool(this));
        tools.addTool(compoundTool = new MoveScaleRotateObjectTool(this));
        tools.addTool(new CreateCubeTool(this));
        tools.addTool(new CreateSphereTool(this));
        tools.addTool(new CreateCylinderTool(this));
        tools.addTool(new CreateSplineMeshTool(this));
        tools.addTool(new CreatePolygonTool(this));
        tools.addTool(new CreateCurveTool(this));
        tools.addTool(new CreateCameraTool(this));
        tools.addTool(new CreateLightTool(this));
        tools.addTool(metaTool = new MoveViewTool(this));
        tools.addTool(altTool = new RotateViewTool(this));

        // Scroll tool does not go to the palette.
        scrollTool = new ScrollViewTool(this);

        if (ArtOfIllusion.getPreferences().getUseCompoundMeshTool()) {
            defaultTool = compoundTool;
        }
        tools.setDefaultTool(defaultTool);
        tools.selectTool(defaultTool);
        for (int i = 0; i < theView.length; i++) {
            theView[i].setMetaTool(metaTool);
            theView[i].setAltTool(altTool);
            theView[i].setScrollTool(scrollTool);
        }

        // Fill in the left hand panel.
        centerContainer.add(tools, 0, 0);

        // Build the menubar.
        setMenuBar(new BMenuBar());
        createFileMenu();
        createEditMenu();

        getMenuBar().add(sceneMenu);

        createObjectMenu();
        createAnimationMenu();
        createToolsMenu();
        createViewMenu();
        createPopupMenu();
        preferences = Preferences.userNodeForPackage(getClass()).node("LayoutWindow");
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
        loadPreferences();
        numViewsShown = (numViewsShown == 1 ? 4 : 1);
        toggleViewsCommand();
        keyEventHandler = new KeyEventPostProcessor() {
            @Override
            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED || e.isConsumed()) {
                    return false;
                }
                KeyPressedEvent press = new KeyPressedEvent(LayoutWindow.this, e.getWhen(), e.getModifiersEx(), e.getKeyCode());
                handleKeyEvent(press);
                return (press.isConsumed());
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyEventHandler);
        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ArtOfIllusion.closeWindow(LayoutWindow.this);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                log.info("Update menus on window activation {}", LayoutWindow.this.getName());
                updateMenus();
            }
        });


        UIUtilities.applyDefaultFont(getContent());
        UIUtilities.applyDefaultBackground(centerContainer);

        if (ArtOfIllusion.APP_ICON != null) {
            setIcon(ArtOfIllusion.APP_ICON);
        }
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        String os = System.getProperties().getProperty("os.name").toLowerCase();
        if (os.startsWith("mac os")) {
            screenBounds.height -= 11; // Workaround for bug in Java on Mac.
        }
        setBounds(screenBounds);
        tools.requestFocus();
        setTime(theScene.getTime());
    }

    /**
     * Load all the preferences into memory.
     */
    protected void loadPreferences() {
        boolean lastShowAxes = preferences.getBoolean("showAxes", false);
        numViewsShown = preferences.getInt("numViews", 4);
        byte[] lastRenderMode = preferences.getByteArray("displayMode", new byte[]{ViewerCanvas.RENDER_SMOOTH, ViewerCanvas.RENDER_SMOOTH, ViewerCanvas.RENDER_SMOOTH, ViewerCanvas.RENDER_SMOOTH});
        for (int i = 0; i < theView.length; i++) {
            theView[i].setShowAxes(lastShowAxes);
            theView[i].setRenderMode((int) lastRenderMode[i]);
        }
    }

    /**
     * Save user settings that should be persistent between sessions.
     */
    protected void savePreferences() {
        preferences.putBoolean("showAxes", theView[currentView].getShowAxes());
        preferences.putInt("numViews", numViewsShown);
        preferences.putByteArray("displayMode", new byte[]{(byte) theView[0].getRenderMode(), (byte) theView[1].getRenderMode(), (byte) theView[2].getRenderMode(), (byte) theView[3].getRenderMode()});
    }

    private void handleKeyEvent(KeyPressedEvent e) {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (manager.getFocusedWindow() != getComponent() || manager.getFocusOwner() instanceof JTextComponent) {
            return;
        }
        tools.getSelectedTool().keyPressed(e, theView[currentView]);
        if (!e.isConsumed()) {
            KeystrokeManager.executeKeystrokes(e, this);
        }
    }

    // This method calls only from Scene Explorer.
    @SuppressWarnings("java:S1144")
    private void rebuildList() {
        score.rebuildList();
    }

    /**
     * Rebuild the TreeList of objects, attempting as much as possible to
     * preserve its current state.
     */
    public void rebuildItemList() {
        sceneExplorer.rebuildList();
        score.rebuildList();
    }

    /**
     * This is called whenever the user moves a DockableWidget. It saves the
     * current configuration to the preferences.
     */
    private void dockableWidgetMoved() {
        StringBuilder config = new StringBuilder();
        for (int i = 0; i < dock.length; i++) {
            for (int j = 0; j < dock[i].getTabCount(); j++) {
                for (int k = 0; k < dock[i].getTabChildCount(j); k++) {
                    DockableWidget w = dock[i].getChild(j, k);
                    config.append(w.getContent().getClass().getName());
                    config.append('\t');
                    config.append(w.getLabel());
                    config.append('\n');
                }
                config.append('\n');
            }
            config.append("-\n");
        }
        Preferences prefs = Preferences.userNodeForPackage(getClass()).node("LayoutWindow");
        prefs.put("dockingConfiguration", config.toString());
    }

    /**
     * This is called when the window is first created. It attempts to arrange
     * the DockableWidgets however they were last arranged by the user.
     */
    void arrangeDockableWidgets() {
        // Look up how they were last arranged.

        Preferences prefs = Preferences.userNodeForPackage(getClass()).node("LayoutWindow");
        String config = prefs.get("dockingConfiguration", null);
        if (config == null) {
            return;
        }

        // Make a table of all DockableWidgets.
        HashMap<String, DockableWidget> widgets = new HashMap<>();
        for (int i = 0; i < dock.length; i++) {
            for (Widget next : dock[i].getChildren()) {
                if (next instanceof DockableWidget) {
                    DockableWidget w = (DockableWidget) next;
                    widgets.put(w.getContent().getClass().getName() + '\t' + w.getLabel(), w);
                }
            }
        }

        // Rearrange them.
        String[] lines = config.split("\n");
        int container = 0, tab = 0, index = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                tab++;
                index = 0;
            } else if ("-".equals(lines[i])) {
                container++;
                tab = 0;
                index = 0;
            } else {
                DockableWidget w = widgets.get(lines[i]);
                if (w != null) {
                    dock[container].addDockableWidget(w, tab, index++);
                    widgets.remove(lines[i]);
                }
            }
        }
        setScoreVisible(false);
    }

    private void createFileMenu() {

        BMenu importMenu, exportMenu;
        List<Translator> translators = PluginRegistry.getPlugins(Translator.class);

        getMenuBar().add(fileMenu);
        importMenu = Translate.menu("import");
        exportMenu = Translate.menu("export");


        fileMenu.add(recentFilesMenu = Translate.menu("openRecent"));
        RecentFiles.createMenu(recentFilesMenu);

        fileMenu.add(Translate.menuItem("close", event -> closeSceneAction()));
        fileMenu.addSeparator();
        translators.sort(Comparator.comparing(Translator::getName));
        for (Translator translator : translators) {
            if (translator.canImport()) {
                BMenuItem item = new BMenuItem(translator.getName());
                item.getComponent().putClientProperty("translator", translator);
                item.getComponent().addActionListener(this::importAction);
                importMenu.add(item);
            }
            if (translator.canExport()) {
                BMenuItem item = new BMenuItem(translator.getName());
                item.getComponent().putClientProperty("translator", translator);
                item.getComponent().addActionListener(this::exportAction);

                exportMenu.add(item);
            }
        }
        if (importMenu.getChildCount() > 0) {
            fileMenu.add(importMenu);
        }
        if (exportMenu.getChildCount() > 0) {
            fileMenu.add(exportMenu);
        }
        fileMenu.add(Translate.menuItem("linkExternal", event -> linkExternalCommand()));
        fileMenu.addSeparator();
        fileMenu.add(fileMenuItem);
        fileMenu.add(Translate.menuItem("saveas", event -> saveAsCommand()));
        fileMenu.addSeparator();
        fileMenu.add(Translate.menuItem("quit", event -> applicationQuitAction()));
    }

    private void createEditMenu() {

        getMenuBar().add(editMenu);
        editMenuItem = new BMenuItem[11];

        editMenu.add(editMenuItem[2] = Translate.menuItem("cut", event -> cutCommand()));
        editMenu.add(editMenuItem[3] = Translate.menuItem("copy", event -> copyCommand()));
        editMenu.add(editMenuItem[4] = Translate.menuItem("paste", event -> pasteCommand()));
        editMenu.add(editMenuItem[5] = Translate.menuItem("clear", event -> clearCommand()));
        editMenu.addSeparator();
        editMenu.add(editMenuItem[6] = Translate.menuItem("selectChildren", event -> selectChildrenAction()));
        editMenu.add(editMenuItem[7] = Translate.menuItem("selectAll", event -> selectAllCommand()));
        editMenu.add(editMenuItem[8] = Translate.menuItem("deselectAll", event -> clearSelection()));
        editMenu.addSeparator();
        editMenu.add(editMenuItem[9] = Translate.menuItem("duplicate", event -> duplicateCommand()));
        editMenu.add(editMenuItem[10] = Translate.menuItem("sever", event -> severCommand()));
        editMenu.addSeparator();
        editMenu.add(Translate.menuItem("preferences", event -> preferencesCommand()));

    }

    private void createObjectMenu() {

        getMenuBar().add(objectMenu);
        objectMenuItem = new BMenuItem[12];
        objectMenu.add(objectMenuItem[0] = Translate.menuItem("editObject", event -> editObjectCommand()));
        objectMenu.add(objectMenuItem[1] = Translate.menuItem("objectLayout", event -> objectLayoutCommand()));
        objectMenu.add(objectMenuItem[2] = Translate.menuItem("transformObject", event -> transformObjectCommand()));
        objectMenu.add(objectMenuItem[3] = Translate.menuItem("alignObjects", event -> alignObjectsCommand()));
        objectMenu.add(objectMenuItem[4] = Translate.menuItem("setTextureAndMaterial", event -> setTextureCommand()));
        objectMenu.add(objectMenuItem[5] = Translate.menuItem("renameObject", event -> renameObjectCommand()));
        objectMenu.add(objectMenuItem[6] = Translate.menuItem("convertToTriangle", event -> convertToTriangleCommand()));
        objectMenu.add(objectMenuItem[7] = Translate.menuItem("convertToActor", event -> convertToActorCommand()));
        objectMenu.addSeparator();
        objectMenu.add(objectMenuItem[8] = Translate.menuItem("hideSelection",  event -> setObjectVisibility(false, true)));
        objectMenu.add(objectMenuItem[9] = Translate.menuItem("showSelection", event -> setObjectVisibility(true, true)));
        objectMenu.add(Translate.menuItem("showAll", event -> setObjectVisibility(true, false)));
        objectMenu.addSeparator();
        objectMenu.add(objectMenuItem[10] = Translate.menuItem("lockSelection", event -> setObjectsLocked(true, true)));
        objectMenu.add(objectMenuItem[11] = Translate.menuItem("unlockSelection",  event -> setObjectsLocked(false, true)));
        objectMenu.add(Translate.menuItem("unlockAll", (ActionEvent e) -> setObjectsLocked(false, false)));
        objectMenu.addSeparator();
        objectMenu.add(new PrimitivesMenu(this));
    }

    private void createToolsMenu() {
        getMenuBar().add(toolsMenu);


        BMenu editScriptMenu = Translate.menu("editToolScript");
        editScriptMenu.add(newScriptMenu = Translate.menu("newScript"));
        for (String language : ScriptRunner.getLanguageNames()) {
            BMenuItem item = new BMenuItem(language);
            item.addEventLink(CommandEvent.class, this, "newScriptCommand");
            item.setActionCommand("newScript");
            item.getComponent().putClientProperty("language", language);
            newScriptMenu.add(item);
        }
        editScriptMenu.add(this.recentScriptMenu = Translate.menu("recentScript"));
        BMenuItem other;
        editScriptMenu.add(other = Translate.menuItem("editScript", this, "editScriptCommand"));
        other.getComponent().putClientProperty("filepath", ExecuteScriptWindow.NEW_SCRIPT_NAME);
        toolsMenu.add(editScriptMenu);
        toolsMenu.add(scriptMenu = Translate.menu("scripts"));
        rebuildScriptsMenu();
    }

    public void editScriptCommand(CommandEvent ev) {
        BMenuItem item = (BMenuItem) ev.getWidget();
        String scriptAbsolutePath = (String) item.getComponent().getClientProperty("filepath");
        // We don't test the language for the filepath because it should be ok
        try {
            new ExecuteScriptWindow(this, scriptAbsolutePath, ScriptRunner.getLanguageForFilename(scriptAbsolutePath));
        } catch (IOException ioe) {
            new BStandardDialog(null, new String[]{Translate.text("errorOpeningScript"),
                    scriptAbsolutePath + (ioe.getMessage() == null ? "" : ioe.getMessage())}, BStandardDialog.ERROR).showMessageDialog(this);
        }
    }

    public void newScriptCommand(CommandEvent ev) {
        BMenuItem item = (BMenuItem) ev.getWidget();
        String language = (String) item.getComponent().getClientProperty("language");
        try {
            new ExecuteScriptWindow(this, ExecuteScriptWindow.NEW_SCRIPT_NAME, language);
        } catch (IOException ioe) {
            new BStandardDialog(null, new String[]{Translate.text("errorCreatingScript"),
                    language + " : " + (ioe.getMessage() == null ? "" : ioe.getMessage())}, BStandardDialog.ERROR).showMessageDialog(this);
        }
    }

    /*
    Creating the View menu. All view manipulation related menu items and sub menus should be added here.
     */
    private void createViewMenu() {
        BMenu displayMenu;

        getMenuBar().add(viewMenu);
        viewMenuItem = new BMenuItem[8];

        viewMenu.add(displayMenu = Translate.menu("displayMode"));
        displayItem = new BCheckBoxMenuItem[6];
        int renderMode = theView[0].getRenderMode();

        displayMenu.add(displayItem[0] = Translate.checkboxMenuItem("wireframeDisplay", this, "setDisplayModeWireframe", renderMode == ViewerCanvas.RENDER_WIREFRAME));
        displayMenu.add(displayItem[1] = Translate.checkboxMenuItem("shadedDisplay", this, "setDisplayModeShaded", renderMode == ViewerCanvas.RENDER_FLAT));
        displayMenu.add(displayItem[2] = Translate.checkboxMenuItem("smoothDisplay", this, "setDisplayModeSmooth", renderMode == ViewerCanvas.RENDER_SMOOTH));
        displayMenu.add(displayItem[3] = Translate.checkboxMenuItem("texturedDisplay", this, "setDisplayModeTextured", renderMode == ViewerCanvas.RENDER_TEXTURED));
        displayMenu.add(displayItem[4] = Translate.checkboxMenuItem("transparentDisplay", this, "setDisplayModeTransparent", renderMode == ViewerCanvas.RENDER_TRANSPARENT));
        displayMenu.add(displayItem[5] = Translate.checkboxMenuItem("renderedDisplay", this, "setDisplayModeRendered", renderMode == ViewerCanvas.RENDER_RENDERED));

        viewMenu.add(viewMenuItem[0] = Translate.menuItem("fourViews", event -> toggleViewsCommand()));
        viewMenu.add(Translate.menuItem("grid", event -> setGridCommand()));
        viewMenu.add(viewMenuItem[2] = Translate.menuItem("showCoordinateAxes", event -> showCoordinateAxesAction()));
        viewMenu.add(viewMenuItem[3] = Translate.menuItem("showTemplate", event -> showTemplateAction()));
        viewMenu.add(Translate.menuItem("setTemplate", event -> setTemplateCommand()));
        viewMenu.addSeparator();
        viewMenu.add(viewMenuItem[4] = Translate.menuItem("fitToSelection", event -> fitToSelectionAction()));
        viewMenu.add(viewMenuItem[5] = Translate.menuItem("fitToAll",  event -> fitToAllAction()));
        viewMenu.add(viewMenuItem[6] = Translate.menuItem("alignWithClosestAxis", event -> alignWithClosestAxisAction()));
        viewMenu.addSeparator();
        viewMenu.add(viewMenuItem[1] = Translate.menuItem("hideObjectList", event -> setObjectListVisible(objectListShown = !objectListShown)));

    }

    /**
     * Rebuild the list of tool scripts in the Tools menu. This should be called
     * whenever a script has been added to or deleted from the Scripts/Tools
     * directory on disk.
     */
    public void rebuildScriptsMenu() {
        scriptMenu.removeAll();
        addScriptsToMenu(scriptMenu, new File(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY));
        rebuildRecentScriptsMenu();
    }

    public void rebuildRecentScriptsMenu() {
        recentScriptMenu.removeAll();
        for (String fileAbsolutePath : ExecuteScriptWindow.getRecentScripts()) {
            BMenuItem item = new BMenuItem(new File(fileAbsolutePath).getName());
            item.addEventLink(CommandEvent.class, this, "editScriptCommand");
            item.setActionCommand("editScript");
            item.getComponent().putClientProperty("filepath", fileAbsolutePath);
            recentScriptMenu.add(item);
        }
    }

    private void addScriptsToMenu(BMenu menu, File dir) {
        String[] files = dir.list();
        if (files == null) {
            return;
        }
        Arrays.sort(files, Collator.getInstance(Translate.getLocale()));
        for (String file : files) {
            File f = new File(dir, file);
            if (f.isDirectory()) {
                BMenu m = new BMenu(file);
                menu.add(m);
                addScriptsToMenu(m, f);
            } else {
                if (ScriptRunner.getLanguageForFilename(file) != ScriptRunner.UNKNOWN_LANGUAGE) {
                    BMenuItem item = new BMenuItem(file.substring(0, file.lastIndexOf('.')));
                    item.setActionCommand(f.getAbsolutePath());
                    item.addEventLink(CommandEvent.class, this, "executeScriptCommand");
                    menu.add(item);
                }
            }
        }
    }

    private void createAnimationMenu() {

        getMenuBar().add(animationMenu);

        animationMenuItem = new BMenuItem[13];
        animationMenu.add(addTrackMenu = Translate.menu("addTrack"));
        addTrackMenu.add(positionTrackMenu = Translate.menu("positionTrack"));
        positionTrackMenu.add(Translate.menuItem("xyzOneTrack", event -> addOnePositionTrackAction()));
        positionTrackMenu.add(Translate.menuItem("xyzThreeTracks", event -> addThreePositionTrackAction()));
        positionTrackMenu.add(Translate.menuItem("proceduralTrack", event -> addProceduralPositionTrackAction()));
        addTrackMenu.add(rotationTrackMenu = Translate.menu("rotationTrack"));
        rotationTrackMenu.add(Translate.menuItem("xyzOneTrack", event -> addOneRotationTrackAction()));
        rotationTrackMenu.add(Translate.menuItem("xyzThreeTracks", event -> addThreeRotationTrackAction()));
        rotationTrackMenu.add(Translate.menuItem("quaternionTrack", event -> addQuaternionTrackAction()));
        rotationTrackMenu.add(Translate.menuItem("proceduralTrack", event -> addProceduralRotationTrackAction()));
        addTrackMenu.add(Translate.menuItem("poseTrack", event -> addTrackAction(event)));
        addTrackMenu.add(distortionMenu = Translate.menu("distortionTrack"));
        distortionMenu.add(Translate.menuItem("bendDistortion", event -> addTrackAction(event)));
        distortionMenu.add(Translate.menuItem("customDistortion", event -> addTrackAction(event)));
        distortionMenu.add(Translate.menuItem("scaleDistortion", event -> addTrackAction(event)));
        distortionMenu.add(Translate.menuItem("shatterDistortion", event -> addTrackAction(event)));
        distortionMenu.add(Translate.menuItem("twistDistortion", event -> addTrackAction(event)));
        distortionMenu.addSeparator();
        distortionMenu.add(Translate.menuItem("IKTrack", event -> addTrackAction(event)));
        distortionMenu.add(Translate.menuItem("skeletonShapeTrack", event -> addTrackAction(event)));
        addTrackMenu.add(Translate.menuItem("constraintTrack", event -> addTrackAction(event)));
        addTrackMenu.add(Translate.menuItem("visibilityTrack", event -> addTrackAction(event)));
        addTrackMenu.add(Translate.menuItem("textureTrack", event -> addTrackAction(event)));
        animationMenu.add(animationMenuItem[0] = Translate.menuItem("editTrack", event -> score.editSelectedTrack()));
        animationMenu.add(animationMenuItem[1] = Translate.menuItem("duplicateTracks", event -> score.duplicateSelectedTracks()));
        animationMenu.add(animationMenuItem[2] = Translate.menuItem("deleteTracks", event -> score.deleteSelectedTracks()));
        animationMenu.add(animationMenuItem[3] = Translate.menuItem("selectAllTracks", event -> score.selectAllTracks()));
        animationMenu.add(animationMenuItem[4] = Translate.menuItem("enableTracks", event -> score.setTracksEnabled(true)));
        animationMenu.add(animationMenuItem[5] = Translate.menuItem("disableTracks", event -> score.setTracksEnabled(false)));
        animationMenu.addSeparator();
        animationMenu.add(animationMenuItem[6] = Translate.menuItem("keyframe", event -> score.keyframeSelectedTracks()));
        animationMenu.add(animationMenuItem[7] = Translate.menuItem("keyframeModified", event -> score.keyframeModifiedTracks()));
        animationMenu.add(animationMenuItem[8] = Translate.menuItem("editKeyframe", event -> score.editSelectedKeyframe()));
        animationMenu.add(animationMenuItem[9] = Translate.menuItem("deleteSelectedKeyframes", event -> score.deleteSelectedKeyframes()));

        BMenu editKeyframeMenu = Translate.menu("bulkEditKeyframes");
        animationMenu.add(editKeyframeMenu);
        editKeyframeMenu.add(Translate.menuItem("moveKeyframes", this, "bulkEditKeyframeAction"));
        editKeyframeMenu.add(Translate.menuItem("copyKeyframes", this, "bulkEditKeyframeAction"));
        editKeyframeMenu.add(Translate.menuItem("rescaleKeyframes", this, "bulkEditKeyframeAction"));
        editKeyframeMenu.add(Translate.menuItem("loopKeyframes", this, "bulkEditKeyframeAction"));
        editKeyframeMenu.add(Translate.menuItem("deleteKeyframes", this, "bulkEditKeyframeAction"));

        animationMenu.add(animationMenuItem[10] = Translate.menuItem("pathFromCurve", event -> pathFromCurveAction()));
        animationMenu.add(animationMenuItem[11] = Translate.menuItem("bindToParent", event -> bindToParentCommand()));
        animationMenu.addSeparator();
        animationMenu.add(Translate.menuItem("forwardFrame", event -> forwardFrameAction()));
        animationMenu.add(Translate.menuItem("backFrame", event -> backFrameAction()));
        animationMenu.add(Translate.menuItem("jumpToTime", event -> jumpToTimeCommand()));
        animationMenu.addSeparator();
        animationMenu.add(Translate.menuItem("previewAnimation", event -> previewAnimationAction()));
        animationMenu.add(animationMenuItem[12] = Translate.menuItem("showScore", event -> showScoreAction()));
    }

    /**
     * Create the popup menu.
     */
    private void createPopupMenu() {
        popupMenu = new BPopupMenu();
        popupMenuItem = new BMenuItem[15];
        popupMenu.add(popupMenuItem[0] = Translate.menuItem("editObject", event -> editObjectCommand()));
        popupMenu.add(popupMenuItem[1] = Translate.menuItem("objectLayout", event -> objectLayoutCommand()));
        popupMenu.add(popupMenuItem[2] = Translate.menuItem("setTextureAndMaterial", event -> setTextureCommand()));
        popupMenu.add(popupMenuItem[3] = Translate.menuItem("renameObject", event -> renameObjectCommand()));
        popupMenu.add(popupMenuItem[4] = Translate.menuItem("convertToTriangle", event -> convertToTriangleCommand()));
        popupMenu.addSeparator();
        popupMenu.add(popupMenuItem[5] = Translate.menuItem("selectChildren", event -> selectChildrenAction()));
        popupMenu.add(Translate.menuItem("selectAll", event -> selectAllCommand()));
        popupMenu.add(popupMenuItem[6] = Translate.menuItem("deselectAll", event -> clearSelection()));
        popupMenu.addSeparator();
        popupMenu.add(popupMenuItem[7] = Translate.menuItem("hideSelection", event -> setObjectVisibility(false, true)));
        popupMenu.add(popupMenuItem[8] = Translate.menuItem("showSelection", event -> setObjectVisibility(true, true)));
        popupMenu.add(popupMenuItem[9] = Translate.menuItem("lockSelection", event -> setObjectsLocked(true, true)));
        popupMenu.add(popupMenuItem[10] = Translate.menuItem("unlockSelection", event -> setObjectsLocked(false, true)));
        popupMenu.addSeparator();
        popupMenu.add(popupMenuItem[11] = Translate.menuItem("cut", event -> cutCommand()));
        popupMenu.add(popupMenuItem[12] = Translate.menuItem("copy", event -> copyCommand()));
        popupMenu.add(popupMenuItem[13] = Translate.menuItem("paste", event -> pasteCommand()));
        popupMenu.add(popupMenuItem[14] = Translate.menuItem("clear", event -> clearCommand()));
    }

    /**
     * Display the popup menu.
     */
    @Override
    public void showPopupMenu(Widget w, int x, int y) {
        Object[] sel = sceneExplorer.getSelectedObjects();
        boolean canConvert, canSetTexture, canHide, canShow, canLock, canUnlock, hasChildren;
        ObjectInfo info;
        Object3D obj;

        canConvert = canSetTexture = (sel.length > 0);
        canHide = canShow = canLock = canUnlock = hasChildren = false;
        for (int i = 0; i < sel.length; i++) {
            info = (ObjectInfo) sel[i];
            obj = info.getObject();
            if (obj.canConvertToTriangleMesh() == Object3D.CANT_CONVERT) {
                canConvert = false;
            }
            if (!obj.canSetTexture()) {
                canSetTexture = false;
            }
            if (info.getChildren().length > 0) {
                hasChildren = true;
            }
            if (info.isVisible()) {
                canHide = true;
            } else {
                canShow = true;
            }
            if (info.isLocked()) {
                canUnlock = true;
            } else {
                canLock = true;
            }
        }
        if (sel.length == 0) {
            for (BMenuItem item : popupMenuItem) {
                item.setEnabled(false);
            }
        } else {
            obj = ((ObjectInfo) sel[0]).getObject();
            popupMenuItem[0].setEnabled(sel.length == 1 && obj.isEditable()); // Edit Object
            popupMenuItem[1].setEnabled(true); // Object Layout
            popupMenuItem[2].setEnabled(canSetTexture); // Set Texture
            popupMenuItem[3].setEnabled(sel.length == 1); // Rename Object
            popupMenuItem[4].setEnabled(canConvert); // Convert to Triangle Mesh
            popupMenuItem[5].setEnabled(sel.length == 1 && hasChildren); // Select Children, works if only one item is selected
            popupMenuItem[6].setEnabled(sel.length > 0); // Deselect All
            popupMenuItem[7].setEnabled(canHide); // Hide Selection
            popupMenuItem[8].setEnabled(canShow); // Show Selection
            popupMenuItem[9].setEnabled(canLock); // Lock Selection
            popupMenuItem[10].setEnabled(canUnlock); // Unlock Selection
            popupMenuItem[11].setEnabled(sel.length > 0); // Cut
            popupMenuItem[12].setEnabled(sel.length > 0); // Copy
            popupMenuItem[14].setEnabled(sel.length > 0); // Clear
        }
        popupMenuItem[13].setEnabled(ArtOfIllusion.getClipboardSize() > 0); // Paste
        popupMenu.show(w, x, y);
    }

    @Override
    public void setVisible(boolean visible) {
        Map<String, Throwable> errors = null;
        if (visible && !hasNotifiedPlugins) {
            hasNotifiedPlugins = true;
            errors = PluginRegistry.notifyPlugins(Plugin.SCENE_WINDOW_CREATED, this);
        }
        super.setVisible(visible);
        if (errors != null && !errors.isEmpty()) {
            ArtOfIllusion.showErrors(errors);
        }
    }

    /**
     * Get the DockingContainer which holds DockableWidgets on one side of the
     * window.
     */
    public DockingContainer getDockingContainer(BTabbedPane.TabPosition position) {
        for (var dockingContainer : dock) {
            if (dockingContainer.getTabPosition() == position) {
                return dockingContainer;
            }
        }
        return null; // should be impossible
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 100);
    }

    /* EditingWindow methods. */

    /**
     * This method is called to close the window. If the Scene has been
     * modified, it first gives the user a chance to save the Scene, or to
     * cancel. If the user cancels it, the method returns false. Otherwise, it
     * closes the window and returns true.
     */
    @Override
    public boolean confirmClose() {
        if (modified) {
            String name = theScene.getName();
            if (name == null) {
                name = "Untitled";
            }
            BStandardDialog dlg = new BStandardDialog("", Translate.text("checkSaveChanges", name), BStandardDialog.QUESTION);
            String[] options = new String[]{Translate.text("button.save"), Translate.text("button.dontSave"), Translate.text("button.cancel")};
            int choice = dlg.showOptionDialog(this, options, options[0]);
            if (choice == 0) {
                saveCommand();
                if (modified) {
                    return false;
                }
            }
            if (choice == 2) {
                return false;
            }
        }
        PluginRegistry.notifyPlugins(Plugin.SCENE_WINDOW_CLOSING, this);
        dispose();
        return true;
    }

    /**
     * Set the selected EditingTool for this window.
     */
    @Override
    public void setTool(EditingTool tool) {
        for (SceneViewer view : theView) {
            view.setTool(tool);
        }
    }

    /**
     * When a tool gets selected in the tool palette, notify the UI. It may be
     * possible, that some options need to be disabled/changed etc.
     */
    public void toolChanged(EditingTool tool) {
        for (ViewerCanvas v : theView) {
            v.navigationTravelEnabled = !(tool instanceof MoveViewTool || tool instanceof RotateViewTool);
            v.viewChanged(false); // This should do nothing now...
        }
    }

    /**
     * Set the help text displayed at the bottom of the window.
     */
    @Override
    public void setHelpText(String text) {
        helpText.setText(text);
    }

    /**
     * Get the Frame corresponding to this window. (Because LayoutWindow is a
     * Frame, it simply returns itself.)
     */
    @Override
    public BFrame getFrame() {
        return this;
    }

    /**
     * Update the images displayed in all of the viewport.
     */
    @Override
    public void updateImage() {
        if (numViewsShown == 1) {
            theView[currentView].copyOrientationFromCamera();
            theView[currentView].repaint();
        } else {
            for (int i = 0; i < numViewsShown; i++) {
                theView[i].copyOrientationFromCamera();
                theView[i].repaint();
            }
        }
    }

    /**
     * Update the state of all menu items.
     */
    @Override
    public void updateMenus() {
        Object[] sel = sceneExplorer.getSelectedObjects();
        dumpSelection(sel);
        int numSelObjects = sel.length;
        Track[] selTrack = score.getSelectedTracks();
        int numSelTracks = selTrack.length;
        int numSelKeyframes = score.getSelectedKeyframes().length;
        ViewerCanvas view = theView[currentView];
        boolean canConvert, canSetTexture;
        boolean curve, noncurve, disable, hasChildren, hasParent;
        boolean enable;
        ObjectInfo info;
        Object3D obj;
        int i;

        canConvert = canSetTexture = (numSelObjects > 0);
        curve = noncurve = enable = disable = hasChildren = hasParent = false;
        for (i = 0; i < numSelObjects; i++) {
            info = (ObjectInfo) sel[i];
            obj = info.getObject();
            if (obj instanceof Curve && !(obj instanceof Tube)) {
                curve = true;
            } else {
                noncurve = true;
            }
            if (obj.canConvertToTriangleMesh() == Object3D.CANT_CONVERT) {
                canConvert = false;
            }
            if (!obj.canSetTexture()) {
                canSetTexture = false;
            }
            if (info.getChildren().length > 0) {
                hasChildren = true;
            }
            if (info.getParent() != null) {
                hasParent = true;
            }
        }
        for (i = 0; i < numSelTracks; i++) {
            if (selTrack[i].isEnabled()) {
                disable = true;
            } else {
                enable = true;
            }
        }

        fileMenuItem.setEnabled(modified);

        editMenuItem[2].setEnabled(numSelObjects > 0); // Cut
        editMenuItem[3].setEnabled(numSelObjects > 0); // Copy
        editMenuItem[4].setEnabled(ArtOfIllusion.getClipboardSize() > 0); // Paste
        editMenuItem[5].setEnabled(numSelObjects > 0); // Clear
        editMenuItem[6].setEnabled(hasChildren); // Select Children
        editMenuItem[8].setEnabled(numSelObjects > 0); // Deselect All
        editMenuItem[9].setEnabled(numSelObjects > 0); // Make Live Duplicates
        editMenuItem[10].setEnabled(numSelObjects > 0); // Sever Duplicates
        if (numSelObjects == 0) {
            for (i = 0; i < objectMenuItem.length; i++) {
                objectMenuItem[i].setEnabled(false);
            }
        } else {
            obj = ((ObjectInfo) sel[0]).getObject();
            objectMenuItem[0].setEnabled(numSelObjects == 1 && obj.isEditable()); // Edit Object
            objectMenuItem[1].setEnabled(true); // Object Layout
            objectMenuItem[2].setEnabled(true); // Transform Object
            objectMenuItem[3].setEnabled(numSelObjects > 0); // Align Objects
            objectMenuItem[4].setEnabled(canSetTexture); // Set Texture
            objectMenuItem[5].setEnabled(sel.length == 1); // Rename Object
            objectMenuItem[6].setEnabled(canConvert && sel.length == 1); // Convert to Triangle Mesh
            objectMenuItem[7].setEnabled(sel.length == 1 && ((ObjectInfo) sel[0]).getObject().canConvertToActor()); // Convert to Actor
            objectMenuItem[8].setEnabled(true); // Hide Selection
            objectMenuItem[9].setEnabled(true); // Show Selection
            objectMenuItem[10].setEnabled(true); // Lock Selection
            objectMenuItem[11].setEnabled(true); // Unlock Selection
        }
        animationMenuItem[0].setEnabled(numSelTracks == 1); // Edit Track
        animationMenuItem[1].setEnabled(numSelTracks > 0); // Duplicate Tracks
        animationMenuItem[2].setEnabled(numSelTracks > 0); // Delete Tracks
        animationMenuItem[3].setEnabled(numSelObjects > 0); // Select All Tracks
        animationMenuItem[4].setEnabled(enable); // Enable Tracks
        animationMenuItem[5].setEnabled(disable); // Disable Tracks
        animationMenuItem[6].setEnabled(numSelTracks > 0); // Keyframe Selected Tracks
        animationMenuItem[7].setEnabled(numSelObjects > 0); // Keyframe Modified Tracks
        animationMenuItem[8].setEnabled(numSelKeyframes == 1); // Edit Keyframe
        animationMenuItem[9].setEnabled(numSelKeyframes > 0); // Delete Selected Keyframes
        animationMenuItem[10].setEnabled(curve && noncurve); // Set Path From Curve
        animationMenuItem[11].setEnabled(hasParent); // Bind to Parent Skeleton
        animationMenuItem[12].setText(Translate.text(score.getBounds().height == 0 || score.getBounds().width == 0 ? "menu.showScore" : "menu.hideScore"));
        addTrackMenu.setEnabled(numSelObjects > 0);
        distortionMenu.setEnabled(sel.length > 0);

        viewMenuItem[1].setText(Translate.text(!objectListShown ? "menu.showObjectList" : "menu.hideObjectList"));
        viewMenuItem[2].setText(Translate.text(view.getShowAxes() ? "menu.hideCoordinateAxes" : "menu.showCoordinateAxes"));
        viewMenuItem[3].setEnabled(view.getTemplateImage() != null); // Show template
        viewMenuItem[3].setText(Translate.text(view.getTemplateShown() ? "menu.hideTemplate" : "menu.showTemplate"));
        viewMenuItem[4].setEnabled(sel.length > 0); // Frame Selection With Camera

        displayItem[0].setState(view.getRenderMode() == ViewerCanvas.RENDER_WIREFRAME);
        displayItem[1].setState(view.getRenderMode() == ViewerCanvas.RENDER_FLAT);
        displayItem[2].setState(view.getRenderMode() == ViewerCanvas.RENDER_SMOOTH);
        displayItem[3].setState(view.getRenderMode() == ViewerCanvas.RENDER_TEXTURED);
        displayItem[4].setState(view.getRenderMode() == ViewerCanvas.RENDER_TRANSPARENT);
        displayItem[5].setState(view.getRenderMode() == ViewerCanvas.RENDER_RENDERED);
    }

    private void dumpSelection(Object[] sel) {
        //log.info("Do dump Selection");
        var so = Set.of(sel);
        if(so.size() != sel.length) throw new RuntimeException("Some selection items doubled");
        for(Object o : sel) {
            if (o instanceof ObjectInfo) continue;
            throw new RuntimeException("Some selection items are not Scene Objects");
        }
    }

    /**
     * Set the UndoRecord which will be executed if the user chooses Undo from
     * the Edit menu.
     */
    @Override
    public void setUndoRecord(UndoRecord command) {
        undoStack.addRecord(command);
        boolean modified = false;
        for (int c : command.getCommands()) {
            if (c != UndoRecord.SET_SCENE_SELECTION) {
                modified = true;
            }
        }
        if (modified) {
            setModified();
        } else {
            dispatchSceneChangedEvent();
        }
        updateMenus();
    }

    /**
     * Set whether the scene has been modified since it was last saved.
     */
    @Override
    public void setModified() {
        modified = true;
        for (ViewerCanvas view : theView) {
            view.viewChanged(false);
        }
        dispatchSceneChangedEvent();
    }

    /**
     * Cause a SceneChangedEvent to be dispatched to this window's listeners.
     */
    private void dispatchSceneChangedEvent() {
        if (sceneChangePending) {
            return; // There's already a Runnable on the event queue waiting to dispatch a SceneChangedEvent.
        }
        sceneChangePending = true;
        EventQueue.invokeLater(() -> {
            sceneChangePending = false;
            dispatchEvent(sceneChangedEvent);
        });
    }

    /**
     * Add a new object to the scene. If undo is not null, appropriate commands
     * will be added to it to undo this operation.
     */
    public void addObject(Object3D obj, CoordinateSystem coords, String name, UndoRecord undo) {
        addObject(new ObjectInfo(obj, coords, name), undo);
    }

    /**
     * Add a new object to the scene. If undo is not null, appropriate commands
     * will be added to it to undo this operation.
     */
    public void addObject(ObjectInfo info, UndoRecord undo) {
        theScene.addObject(info, undo);
        sceneExplorer.setUpdateEnabled(false);
        sceneExplorer.add(info);
        uiEventProcessor.addEvent(new Runnable() {
            @Override
            public void run() {
                sceneExplorer.setUpdateEnabled(true);
                for (var viewer : theView) {
                    viewer.rebuildCameraList();
                }
                score.rebuildList();
            }
        });
    }

    /**
     * Add a new object to the scene. If undo is not null, appropriate commands
     * will be added to it to undo this operation.
     * <p>
     * <p>
     * NOTE! This method is only used by 'UndoRecord'. Using it in any other
     * context is not safe.
     */
    public void addObject(ObjectInfo info, int index, UndoRecord undo) {
        theScene.addObject(info, index, undo);
        sceneExplorer.setUpdateEnabled(false);
        sceneExplorer.add(info, index);
        uiEventProcessor.addEvent(new Runnable() {
            @Override
            public void run() {
                sceneExplorer.setUpdateEnabled(true);
                for (int i = 0; i < theView.length; i++) {
                    theView[i].rebuildCameraList();
                }
                score.rebuildList();
            }
        });
    }

    /**
     * Remove an object from the scene. If undo is not null, appropriate
     * commands will be added to it to undo this operation.
     */
    public void removeObject(int which, UndoRecord undo) {
        sceneExplorer.setUpdateEnabled(false);
        final ObjectInfo info = theScene.getObject(which);
        ObjectInfo parent = info.getParent();
        int childIndex = -1;
        if (parent != null) {
            for (int i = 0; i < parent.getChildren().length; i++) {
                if (parent.getChildren()[i] == info) {
                    childIndex = i;
                }
            }
        }
        sceneExplorer.remove(info);
        if (childIndex > -1 && info.getParent() == null) {
            undo.addCommandAtBeginning(UndoRecord.ADD_TO_GROUP, parent, info, childIndex);
        }
        theScene.removeObject(which, undo);
        uiEventProcessor.addEvent(new Runnable() {
            @Override
            public void run() {
                sceneExplorer.setUpdateEnabled(true);
                for (int i = 0; i < theView.length; i++) {
                    if (theView[i].getBoundCamera() == info) {
                        theView[i].setOrientation(ViewerCanvas.VIEW_OTHER);
                    }
                    theView[i].rebuildCameraList();
                }
                score.rebuildList();
            }
        });
    }

    /**
     * Set the name of an object in the scene.
     */
    public void setObjectName(int which, String name) {
        theScene.getObject(which).setName(name);
        sceneExplorer.repaint();
        for (int i = 0; i < theView.length; i++) {
            theView[i].rebuildCameraList();
        }
        score.rebuildList();
    }

    /**
     * Set the time which is currently being displayed.
     */
    public void setTime(double time) {
        theScene.setTime(time);
        score.setTime(time);
        score.repaint();
        sceneExplorer.repaint();
        for (SceneViewer view : theView) {
            view.viewChanged(false);
        }
        updateImage();
        dispatchSceneChangedEvent();
    }

    /**
     * Get the Scene associated with this window.
     */
    @Override
    public Scene getScene() {
        return theScene;
    }

    /**
     * Get the ViewerCanvas which currently has focus.
     */
    @Override
    public ViewerCanvas getView() {
        return theView[currentView];
    }

    /**
     * Get all ViewerCanvases contained in this window.
     */
    @Override
    public ViewerCanvas[] getAllViews() {
        return (ViewerCanvas[]) theView.clone();
    }

    /**
     * Set which ViewerCanvas has focus.
     *
     * @param view the ViewerCanvas which should become the currently focused
     *             view. If this is not one of the views belonging to this window, this
     *             method does nothing.
     */
    public void setCurrentView(ViewerCanvas view) {
        for (int i = 0; i < theView.length; i++) {
            if (currentView != i && view == theView[i]) {
                theView[currentView].setDrawFocus(false);
                theView[i].setDrawFocus(true);
                displayItem[0].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_WIREFRAME);
                displayItem[1].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_FLAT);
                displayItem[2].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_SMOOTH);
                displayItem[3].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_TEXTURED);
                displayItem[4].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_TRANSPARENT);
                displayItem[5].setState(theView[i].getRenderMode() == ViewerCanvas.RENDER_RENDERED);
                currentView = i;
                updateImage();
                updateMenus();
            }
        }
    }

    /**
     * Get the Score for this window.
     */
    public Score getScore() {
        return score;
    }

    /**
     * Get the ToolPalette for this window.
     */
    @Override
    public ToolPalette getToolPalette() {
        return tools;
    }

    /**
     * Set whether a DockableWidget contained in this window is visible.
     */
    private void setDockableWidgetVisible(DockableWidget widget, boolean visible) {
        DockingContainer parent = (DockingContainer) widget.getParent();
        BTabbedPane.TabPosition pos = parent.getTabPosition();
        BSplitPane split = parent.getSplitPane();
        if (visible) {
            split.resetToPreferredSizes();
        } else {
            split.setDividerLocation(pos == BTabbedPane.TOP || pos == BTabbedPane.LEFT ? 0.0 : 1.0);
        }
        updateMenus();
    }

    /**
     * Set whether the object list should be displayed.
     */
    public void setObjectListVisible(boolean visible) {
        objectListShown = visible; // in case this method is called from outside
        setDockableWidgetVisible(sceneExplorer, visible);
    }

    /**
     * Set whether the score should be displayed.
     */
    public void setScoreVisible(boolean visible) {
        setDockableWidgetVisible((DockableWidget) score.getParent(), visible);
    }

    /**
     * Set whether the window is split into four views.
     */
    public void setSplitView(boolean split) {
        if ((numViewsShown == 1) == split) {
            toggleViewsCommand();
        }
    }

    /**
     * Get whether the window is split into four views.
     */
    public boolean getSplitView() {
        return (numViewsShown > 1);
    }

    /**
     * This is called when the selection in the object tree changes.
     */
    //NB. Bound to sceneExplorer. Do not remove.
    @SuppressWarnings("unused")
    private void treeSelectionChanged() {
        log.info("Tree selection changed");
        Object[] sel = sceneExplorer.getSelectedObjects();
        int[] which = new int[sel.length];

        for (int i = 0; i < sel.length; i++) {
            which[i] = theScene.indexOf((ObjectInfo) sel[i]);
        }
        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_SCENE_SELECTION, getSelectedIndices()));
        setSelection(which);
        updateImage();
    }

    private void setViewMode(Widget source, int mode) {
        theView[currentView].setRenderMode(mode);
        for (BCheckBoxMenuItem item : displayItem) {
            item.setState(item == source);
        }
        savePreferences();
    }

    @SuppressWarnings("unused")
    private void setDisplayModeWireframe(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_WIREFRAME);
    }

    @SuppressWarnings("unused")
    private void setDisplayModeShaded(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_FLAT);
    }

    @SuppressWarnings("unused")
    private void setDisplayModeSmooth(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_SMOOTH);
    }

    @SuppressWarnings("unused")
    private void setDisplayModeTextured(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_TEXTURED);
    }

    @SuppressWarnings("unused")
    private void setDisplayModeTransparent(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_TRANSPARENT);
    }

    @SuppressWarnings("unused")
    private void setDisplayModeRendered(CommandEvent event) {
        setViewMode(event.getWidget(), ViewerCanvas.RENDER_RENDERED);
    }

    /**
     * Get a list of the indices of all selected objects.
     */
    public int[] getSelectedIndices() {
        return theScene.getSelection();
    }

    /**
     * Get a collection of all selected objects.
     */
    public Collection<ObjectInfo> getSelectedObjects() {
        ArrayList<ObjectInfo> objects = new ArrayList<>();
        for (int index : theScene.getSelection()) {
            objects.add(theScene.getObject(index));
        }
        return objects;
    }

    /**
     * Determine whether an object is selected.
     */
    public boolean isObjectSelected(ObjectInfo info) {
        return info.isSelected();
    }

    /**
     * Determine whether an object is selected.
     */
    public boolean isObjectSelected(int index) {
        return theScene.getObject(index).isSelected();
    }

    /**
     * Get the indices of all objects which are either selected, or are children
     * of selected objects.
     */
    public int[] getSelectionWithChildren() {
        return theScene.getSelectionWithChildren();
    }

    /**
     * Set the list of objects in the scene which should be selected.
     */
    public void setSelection(int... which) {
        if (which.length == 0) return;

        sceneExplorer.setUpdateEnabled(false);
        clearSelection();
        theScene.setSelection(which);
        for (int i = 0; i < which.length; i++) {
            sceneExplorer.setSelected(theScene.getObject(which[i]), true);
        }
        sceneExplorer.setUpdateEnabled(true);
        score.rebuildList();
        updateMenus();
    }

    public void setSelection(int which) {
        setSelection(new int[]{which});
    }

    /**
     * Set an object to be selected.
     */
    public void addToSelection(int which) {
        theScene.addToSelection(which);
        sceneExplorer.setSelected(theScene.getObject(which), true);
        score.rebuildList();
        updateMenus();
    }

    /**
     * Deselect all objects.
     */
    public void clearSelection() {
        theScene.clearSelection();
        sceneExplorer.deselectAll();
        score.rebuildList();
        updateImage();
        updateMenus();
    }

    /**
     * Deselect a single object.
     */
    public void removeFromSelection(int which) {
        theScene.removeFromSelection(which);
        sceneExplorer.setSelected(theScene.getObject(which), false);
        score.rebuildList();
        updateMenus();
    }


    private void addTrackAction(ActionEvent event) {
        var trackClass = LayoutAnimationMenu.getCommandToTrack(event.getActionCommand());
        score.addTrack(sceneExplorer.getSelectedObjects(), trackClass, null, true);
    }

    private void addOnePositionTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), PositionTrack.class, null, true);
    }

    private void addThreePositionTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), PositionTrack.class, new Object[]{"Z Position", Boolean.FALSE, Boolean.FALSE, Boolean.TRUE}, true);
        score.addTrack(sceneExplorer.getSelectedObjects(), PositionTrack.class, new Object[]{"Y Position", Boolean.FALSE, Boolean.TRUE, Boolean.FALSE});
        score.addTrack(sceneExplorer.getSelectedObjects(), PositionTrack.class, new Object[]{"X Position", Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});
    }

    private void addOneRotationTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), RotationTrack.class, new Object[]{"Rotation", Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE}, true);
    }

    private void addThreeRotationTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), RotationTrack.class, new Object[]{"Z Rotation", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE}, true);
        score.addTrack(sceneExplorer.getSelectedObjects(), RotationTrack.class, new Object[]{"Y Rotation", Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE});
        score.addTrack(sceneExplorer.getSelectedObjects(), RotationTrack.class, new Object[]{"X Rotation", Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE});
    }

    private void addQuaternionTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), RotationTrack.class, new Object[]{"Rotation", Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE}, true);
    }

    private void addProceduralPositionTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), ProceduralPositionTrack.class, null, true);
    }

    private void addProceduralRotationTrackAction() {
        score.addTrack(sceneExplorer.getSelectedObjects(), ProceduralRotationTrack.class, null, true);
    }

    private void closeSceneAction() {
        savePreferences();
        ArtOfIllusion.closeWindow(this);
    }

    private void applicationQuitAction() {
        savePreferences();
        ArtOfIllusion.quit();
    }

    private void importAction(ActionEvent event) {
        var source = (JMenuItem) event.getSource();
        Translator trans = (Translator) source.getClientProperty("translator");
        trans.importFile(this);
    }

    private void exportAction(ActionEvent event) {
        var source = (JMenuItem) event.getSource();
        Translator trans = (Translator) source.getClientProperty("translator");
        trans.exportFile(this, theScene);
    }

    private void selectChildrenAction() {
        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_SCENE_SELECTION, getSelectedIndices()));
        setSelection(getSelectionWithChildren());
        updateImage();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void imagesDialogAction() {
        SwingUtilities.invokeLater(() -> new ImagesDialog(this, theScene));
    }

    private void showCoordinateAxesAction() {
        boolean wasShown = theView[currentView].getShowAxes();
        for (SceneViewer view : theView) {
            view.setShowAxes(!wasShown);
        }
        savePreferences();
        updateImage();
        updateMenus();
    }

    private void showTemplateAction() {
        boolean wasShown = theView[currentView].getTemplateShown();
        theView[currentView].setShowTemplate(!wasShown);
        updateImage();
        updateMenus();
    }

    private void fitToSelectionAction() {
        getView().fitToObjects(getSelectedObjects());
    }

    private void fitToAllAction() {
        getView().fitToObjects(getScene().getObjects());
    }

    private void alignWithClosestAxisAction() {
        getView().alignWithClosestAxis();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void pathFromCurveAction() {
        new PathFromCurveDialog(this, sceneExplorer.getSelectedObjects());
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void previewAnimationAction() {
        new AnimationPreviewer(this);
    }

    private void showScoreAction() {
        setScoreVisible(score.getBounds().height == 0 || score.getBounds().width == 0);
    }

    private void forwardFrameAction() {
        setTime(theScene.getTime() + 1.0 / theScene.getFramesPerSecond());
    }

    private void backFrameAction() {
        setTime(theScene.getTime() - 1.0 / theScene.getFramesPerSecond());
    }

    private static final Map<String, Integer> bmap;

    static {
        bmap = new HashMap<>();
        bmap.put("moveKeyframes", EditKeyframesDialog.MOVE);
        bmap.put("copyKeyframes", EditKeyframesDialog.COPY);
        bmap.put("rescaleKeyframes", EditKeyframesDialog.RESCALE);
        bmap.put("loopKeyframes", EditKeyframesDialog.LOOP);
        bmap.put("deleteKeyframes", EditKeyframesDialog.DELETE);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void bulkEditKeyframeAction(CommandEvent event) {
        new EditKeyframesDialog(this, bmap.get(event.getActionCommand()));
    }

    public void linkExternalCommand() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("externalObject.selectScene"));
        if (chooser.showOpenDialog(this.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        ExternalObject obj = new ExternalObject(chooser.getSelectedFile(), "");
        ObjectInfo info = new ObjectInfo(obj, new CoordinateSystem(), "External Object");
        if (obj.getTexture() == null) {
            obj.setTexture(getScene().getDefaultTexture(), getScene().getDefaultTexture().getDefaultMapping(obj));
        }
        UndoRecord undo = new UndoRecord(this);
        int[] sel = getSelectedIndices();
        addObject(info, undo);
        undo.addCommand(UndoRecord.SET_SCENE_SELECTION, sel);
        setUndoRecord(undo);
        setSelection(theScene.getNumObjects() - 1);
        editObjectCommand();
    }

    public void saveCommand() {
        if (theScene.getName() == null) {
            saveAsCommand();
        } else {
            modified = !ArtOfIllusion.saveScene(theScene, this);
            updateMenus();
        }
    }

    public void saveAsCommand() {
        var chooser = new JFileChooser();
        chooser.setName(Translate.text("saveScene"));
        if (theScene.getName() == null) {
            chooser.setSelectedFile(new File("Untitled.aoi"));
        } else {
            chooser.setSelectedFile(new File(theScene.getName()));
        }
        if (theScene.getDirectory() != null) {
            chooser.setCurrentDirectory(new File(theScene.getDirectory()));
        } else if (ArtOfIllusion.getCurrentDirectory() != null) {
            chooser.setCurrentDirectory(new File(ArtOfIllusion.getCurrentDirectory()));
        }
        if (chooser.showSaveDialog(this.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String name = chooser.getSelectedFile().getName();
        if (!name.toLowerCase().endsWith(".aoi")) {
            name = name + ".aoi";
        }
        File file = new File(chooser.getCurrentDirectory(), name);
        if (file.isFile()) {
            String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};
            int choice = new BStandardDialog("", Translate.text("Translators:overwriteFile", name), BStandardDialog.QUESTION).showOptionDialog(this, options, options[1]);
            if (choice == 1) {
                return;
            }
        }
        theScene.setName(name);
        theScene.setDirectory(chooser.getCurrentDirectory().getAbsolutePath());
        setTitle(name);
        modified = !ArtOfIllusion.saveScene(theScene, this);

        // The UI seems to react to something somewhere in the saving process and
        // updateMenus() even without this, but that probably cannot be guaranteed.
        updateMenus();
    }

    public void undoCommand() {
        undoStack.executeUndo();
        for (ViewerCanvas view : theView) {
            view.viewChanged(false);
        }
        rebuildItemList();
        updateImage();
        updateMenus();
    }

    public void redoCommand() {
        undoStack.executeRedo();
        for (ViewerCanvas view : theView) {
            view.viewChanged(false);
        }
        rebuildItemList();
        updateImage();
        updateMenus();
    }

    public void cutCommand() {
        copyCommand();
        clearCommand();
    }

    public void copyCommand() {
        int[] sel = getSelectionWithChildren();
        if (sel.length == 0) {
            return;
        }
        ObjectInfo[] copy = new ObjectInfo[sel.length];
        for (int i = 0; i < sel.length; i++) {
            copy[i] = theScene.getObject(sel[i]);
        }
        copy = ObjectInfo.duplicateAll(copy);
        ArtOfIllusion.copyToClipboard(copy, theScene);
        updateMenus();
    }

    public void pasteCommand() {
        int[] which = new int[ArtOfIllusion.getClipboardSize()];
        int num = theScene.getNumObjects();
        for (int i = 0; i < which.length; i++) {
            which[i] = num + i;
        }
        ArtOfIllusion.pasteClipboard(this);
        setSelection(which);
        rebuildItemList();
        updateImage();
    }

    public void clearCommand() {
        Object[] sel = sceneExplorer.getSelectedObjects();
        int[] selIndex = getSelectedIndices();
        boolean any;
        int i;

        if (sel.length == 0) {
            return;
        }
        clearSelection();
        UndoRecord undo = new UndoRecord(this);

        // First, remove any selected objects.
        for (i = sel.length - 1; i >= 0; i--) {
            ObjectInfo info = (ObjectInfo) sel[i];
            int index = theScene.indexOf(info);
            removeObject(index, undo);
        }

        // Now remove any objects whose parents were just deleted.
        do {
            any = false;
            for (i = 0; i < theScene.getNumObjects(); i++) {
                ObjectInfo info = theScene.getObject(i);
                if (info.getParent() != null && theScene.indexOf(info.getParent()) == -1) {
                    removeObject(i, undo);
                    i--;
                    any = true;
                }
            }
        } while (any);
        undo.addCommand(UndoRecord.SET_SCENE_SELECTION, selIndex);
        setUndoRecord(undo);
        updateMenus();
        updateImage();
    }

    public void selectAllCommand() {
        int[] which = new int[theScene.getNumObjects()];

        for (int i = 0; i < which.length; i++) {
            which[i] = i;
        }
        setUndoRecord(new UndoRecord(this, false, UndoRecord.SET_SCENE_SELECTION, getSelectedIndices()));
        setSelection(which);
        updateImage();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void preferencesCommand() {
        new PreferencesWindow(this);
    }

    public void duplicateCommand() {
        Object[] sel = sceneExplorer.getSelectedObjects();
        int[] which = new int[sel.length];
        int num = theScene.getNumObjects();

        // Create the duplicates.
        Map<ObjectInfo, ObjectInfo> duplicateMap = new HashMap<>();
        for (Object selection : sel) {
            ObjectInfo original = (ObjectInfo) selection;
            duplicateMap.put(original, original.duplicate());
        }

        // Maintain relationships between parents and children.
        for (ObjectInfo original : duplicateMap.keySet()) {
            if (duplicateMap.containsKey(original.getParent())) {
                duplicateMap.get(original.getParent()).addChild(duplicateMap.get(original), 0);
            }
        }

        // Add the new objects to the scene.
        UndoRecord undo = new UndoRecord(this);
        int[] selected = getSelectedIndices();
        for (ObjectInfo duplicate : duplicateMap.values()) {
            addObject(duplicate, undo);
        }
        for (int i = 0; i < sel.length; i++) {
            which[i] = num + i;
        }
        undo.addCommand(UndoRecord.SET_SCENE_SELECTION, selected);
        setSelection(which);
        setUndoRecord(undo);
        rebuildItemList();
        updateImage();
    }

    public void severCommand() {
        Object[] sel = sceneExplorer.getSelectedObjects();


        UndoRecord undo = new UndoRecord(this);
        for (Object selected : sel) {
            var info = (ObjectInfo) selected;
            undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
            info.setObject(info.object.duplicate());
        }
        setUndoRecord(undo);
    }

    public void editObjectCommand() {
        int[] sel = getSelectedIndices();
        final Object3D obj;

        if (sel.length != 1) {
            return;
        }
        obj = theScene.getObject(sel[0]).getObject();
        if (obj.isEditable()) {
            final UndoRecord undo = new UndoRecord(this, false, UndoRecord.COPY_OBJECT, obj, obj.duplicate());
            obj.edit(this, theScene.getObject(sel[0]), new Runnable() {
                @Override
                public void run() {
                    setUndoRecord(undo);
                    theScene.objectModified(obj);
                    updateImage();
                    updateMenus();
                }
            });
        }
    }

    public void objectLayoutCommand() {
        int i;
        int[] sel = getSelectedIndices();
        TransformDialog dlg;
        ObjectInfo[] obj = new ObjectInfo[sel.length];
        Vec3 orig, size;
        double[] angles;
        double[] values;

        if (sel.length == 0) {
            return;
        }
        UndoRecord undo = new UndoRecord(this);
        for (i = 0; i < sel.length; i++) {
            obj[i] = theScene.getObject(sel[i]);
            undo.addCommand(UndoRecord.COPY_OBJECT, obj[i].getObject(), obj[i].getObject().duplicate());
            undo.addCommand(UndoRecord.COPY_COORDS, obj[i].getCoords(), obj[i].getCoords().duplicate());
        }
        if (sel.length == 1) {
            orig = obj[0].getCoords().getOrigin();
            angles = obj[0].getCoords().getRotationAngles();
            size = obj[0].getObject().getBounds().getSize();
            dlg = new TransformDialog(this, Translate.text("objectLayoutTitle", theScene.getObject(sel[0]).getName()),
                    new double[]{orig.x, orig.y, orig.z, angles[0], angles[1], angles[2],
                            size.x, size.y, size.z}, false, false);
            if (!dlg.clickedOk()) {
                return;
            }
            values = dlg.getValues();
            if (!Double.isNaN(values[0])) {
                orig.x = values[0];
            }
            if (!Double.isNaN(values[1])) {
                orig.y = values[1];
            }
            if (!Double.isNaN(values[2])) {
                orig.z = values[2];
            }
            if (!Double.isNaN(values[3])) {
                angles[0] = values[3];
            }
            if (!Double.isNaN(values[4])) {
                angles[1] = values[4];
            }
            if (!Double.isNaN(values[5])) {
                angles[2] = values[5];
            }
            if (!Double.isNaN(values[6])) {
                size.x = values[6];
            }
            if (!Double.isNaN(values[7])) {
                size.y = values[7];
            }
            if (!Double.isNaN(values[8])) {
                size.z = values[8];
            }
            obj[0].getCoords().setOrigin(orig);
            obj[0].getCoords().setOrientation(angles[0], angles[1], angles[2]);
            obj[0].getObject().setSize(size.x, size.y, size.z);
            theScene.objectModified(obj[0].getObject());
            obj[0].getObject().sceneChanged(obj[0], theScene);
            theScene.applyTracksAfterModification(Collections.singleton(obj[0]));
        } else {
            dlg = new TransformDialog(this, Translate.text("objectLayoutTitleMultiple"), false, false);
            if (!dlg.clickedOk()) {
                return;
            }
            values = dlg.getValues();
            for (i = 0; i < sel.length; i++) {
                orig = obj[i].getCoords().getOrigin();
                angles = obj[i].getCoords().getRotationAngles();
                size = obj[i].getObject().getBounds().getSize();
                if (!Double.isNaN(values[0])) {
                    orig.x = values[0];
                }
                if (!Double.isNaN(values[1])) {
                    orig.y = values[1];
                }
                if (!Double.isNaN(values[2])) {
                    orig.z = values[2];
                }
                if (!Double.isNaN(values[3])) {
                    angles[0] = values[3];
                }
                if (!Double.isNaN(values[4])) {
                    angles[1] = values[4];
                }
                if (!Double.isNaN(values[5])) {
                    angles[2] = values[5];
                }
                if (!Double.isNaN(values[6])) {
                    size.x = values[6];
                }
                if (!Double.isNaN(values[7])) {
                    size.y = values[7];
                }
                if (!Double.isNaN(values[8])) {
                    size.z = values[8];
                }
                obj[i].getCoords().setOrigin(orig);
                obj[i].getCoords().setOrientation(angles[0], angles[1], angles[2]);
                obj[i].getObject().setSize(size.x, size.y, size.z);
            }
            ArrayList<ObjectInfo> modified = new ArrayList<>();
            for (int index : sel) {
                modified.add(theScene.getObject(index));
            }
            theScene.applyTracksAfterModification(modified);
        }
        setUndoRecord(undo);
        updateImage();
    }

    public void transformObjectCommand() {
        int i;
        int[] sel = getSelectedIndices();
        TransformDialog dlg;
        ObjectInfo info;
        Object3D obj;
        CoordinateSystem coords;
        Vec3 orig, size, center;
        double[] values;
        Mat4 m;

        if (sel.length == 0) {
            return;
        }
        if (sel.length == 1) {
            dlg = new TransformDialog(this, Translate.text("transformObjectTitle", theScene.getObject(sel[0]).getName()),
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0}, true, true);
        } else {
            dlg = new TransformDialog(this, Translate.text("transformObjectTitleMultiple"),
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0}, true, true);
        }
        if (!dlg.clickedOk()) {
            return;
        }
        values = dlg.getValues();

        // Find the center of all selected objects.
        BoundingBox bounds = null;
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            if (bounds == null) {
                bounds = info.getBounds().transformAndOutset(info.getCoords().fromLocal());
            } else {
                bounds = bounds.merge(info.getBounds().transformAndOutset(info.getCoords().fromLocal()));
            }
        }
        center = bounds.getCenter();
        if (dlg.applyToChildren()) {
            sel = getSelectionWithChildren();
        }

        // Determine the rotation matrix.
        m = Mat4.identity();
        if (!Double.isNaN(values[3])) {
            m = m.times(Mat4.xrotation(values[3] * Math.PI / 180.0));
        }
        if (!Double.isNaN(values[4])) {
            m = m.times(Mat4.yrotation(values[4] * Math.PI / 180.0));
        }
        if (!Double.isNaN(values[5])) {
            m = m.times(Mat4.zrotation(values[5] * Math.PI / 180.0));
        }
        UndoRecord undo = new UndoRecord(this);
        HashSet<Object3D> scaledObjects = new HashSet<>();
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            obj = info.getObject();
            coords = info.getCoords();
            if (!scaledObjects.contains(obj)) {
                undo.addCommand(UndoRecord.COPY_OBJECT, obj, obj.duplicate());
            }
            undo.addCommand(UndoRecord.COPY_COORDS, coords, coords.duplicate());
            orig = coords.getOrigin();
            size = obj.getBounds().getSize();
            if (!Double.isNaN(values[0])) {
                orig.x += values[0];
            }
            if (!Double.isNaN(values[1])) {
                orig.y += values[1];
            }
            if (!Double.isNaN(values[2])) {
                orig.z += values[2];
            }
            if (!Double.isNaN(values[6])) {
                size.x *= values[6];
            }
            if (!Double.isNaN(values[7])) {
                size.y *= values[7];
            }
            if (!Double.isNaN(values[8])) {
                size.z *= values[8];
            }
            if (dlg.useSelectionCenter()) {
                Vec3 neworig = orig.minus(center);
                if (!Double.isNaN(values[6])) {
                    neworig.x *= values[6];
                }
                if (!Double.isNaN(values[7])) {
                    neworig.y *= values[7];
                }
                if (!Double.isNaN(values[8])) {
                    neworig.z *= values[8];
                }
                coords.setOrigin(neworig);
                coords.transformCoordinates(m);
                coords.setOrigin(coords.getOrigin().plus(center));
            } else {
                coords.setOrigin(orig);
                coords.transformAxes(m);
            }
            if (!scaledObjects.contains(obj)) {
                obj.setSize(size.x, size.y, size.z);
                scaledObjects.add(obj);
            }
        }
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            theScene.objectModified(info.getObject());
        }
        ArrayList<ObjectInfo> modified = new ArrayList<>();
        for (int index : sel) {
            modified.add(theScene.getObject(index));
        }
        theScene.applyTracksAfterModification(modified);
        setUndoRecord(undo);
        updateImage();
    }

    public void alignObjectsCommand() {
        int i;
        int[] sel = getSelectedIndices();
        ComponentsDialog dlg;
        ObjectInfo info;
        CoordinateSystem coords;
        Vec3 alignTo, orig, center;
        BComboBox xchoice, ychoice, zchoice;
        RowContainer px = new RowContainer(), py = new RowContainer(), pz = new RowContainer();
        ValueField vfx, vfy, vfz;
        BoundingBox bounds;

        if (sel.length == 0) {
            return;
        }
        px.add(xchoice = new BComboBox(new String[]{
                Translate.text("doNotAlign"),
                Translate.text("Right"),
                Translate.text("Center"),
                Translate.text("Left"),
                Translate.text("Origin")
        }));
        px.add(Translate.label("alignTo"));
        px.add(vfx = new ValueField(Double.NaN, ValueField.NONE, 5));
        py.add(ychoice = new BComboBox(new String[]{
                Translate.text("doNotAlign"),
                Translate.text("Top"),
                Translate.text("Center"),
                Translate.text("Bottom"),
                Translate.text("Origin")
        }));
        py.add(Translate.label("alignTo"));
        py.add(vfy = new ValueField(Double.NaN, ValueField.NONE, 5));
        pz.add(zchoice = new BComboBox(new String[]{
                Translate.text("doNotAlign"),
                Translate.text("Front"),
                Translate.text("Center"),
                Translate.text("Back"),
                Translate.text("Origin")
        }));
        pz.add(Translate.label("alignTo"));
        pz.add(vfz = new ValueField(Double.NaN, ValueField.NONE, 5));
        dlg = new ComponentsDialog(this, Translate.text("alignObjectsTitle"),
                new Widget[]{px, py, pz}, new String[]{"X", "Y", "Z"});
        if (!dlg.clickedOk()) {
            return;
        }
        UndoRecord undo = new UndoRecord(this);

        // Determine the position to align the objects to.
        alignTo = new Vec3();
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            coords = info.getCoords();
            bounds = info.getBounds();
            bounds = bounds.transformAndOutset(coords.fromLocal());
            center = bounds.getCenter();
            orig = coords.getOrigin();
            if (!Double.isNaN(vfx.getValue())) {
                alignTo.x += vfx.getValue();
            } else if (xchoice.getSelectedIndex() == 1) {
                alignTo.x += bounds.maxx;
            } else if (xchoice.getSelectedIndex() == 2) {
                alignTo.x += center.x;
            } else if (xchoice.getSelectedIndex() == 3) {
                alignTo.x += bounds.minx;
            } else if (xchoice.getSelectedIndex() == 4) {
                alignTo.x += orig.x;
            }
            if (!Double.isNaN(vfy.getValue())) {
                alignTo.y += vfy.getValue();
            } else if (ychoice.getSelectedIndex() == 1) {
                alignTo.y += bounds.maxy;
            } else if (ychoice.getSelectedIndex() == 2) {
                alignTo.y += center.y;
            } else if (ychoice.getSelectedIndex() == 3) {
                alignTo.y += bounds.miny;
            } else if (ychoice.getSelectedIndex() == 4) {
                alignTo.y += orig.y;
            }
            if (!Double.isNaN(vfz.getValue())) {
                alignTo.z += vfz.getValue();
            } else if (zchoice.getSelectedIndex() == 1) {
                alignTo.z += bounds.maxz;
            } else if (zchoice.getSelectedIndex() == 2) {
                alignTo.z += center.z;
            } else if (zchoice.getSelectedIndex() == 3) {
                alignTo.z += bounds.minz;
            } else if (zchoice.getSelectedIndex() == 4) {
                alignTo.z += orig.z;
            }
        }
        alignTo.scale(1.0 / sel.length);

        // Now transform all of the objects.
        for (i = 0; i < sel.length; i++) {
            info = theScene.getObject(sel[i]);
            coords = info.getCoords();
            bounds = info.getBounds();
            bounds = bounds.transformAndOutset(coords.fromLocal());
            center = bounds.getCenter();
            orig = coords.getOrigin();
            undo.addCommand(UndoRecord.COPY_COORDS, coords, coords.duplicate());
            if (xchoice.getSelectedIndex() == 1) {
                orig.x += alignTo.x - bounds.maxx;
            } else if (xchoice.getSelectedIndex() == 2) {
                orig.x += alignTo.x - center.x;
            } else if (xchoice.getSelectedIndex() == 3) {
                orig.x += alignTo.x - bounds.minx;
            } else if (xchoice.getSelectedIndex() == 4) {
                orig.x += alignTo.x - orig.x;
            }
            if (ychoice.getSelectedIndex() == 1) {
                orig.y += alignTo.y - bounds.maxy;
            } else if (ychoice.getSelectedIndex() == 2) {
                orig.y += alignTo.y - center.y;
            } else if (ychoice.getSelectedIndex() == 3) {
                orig.y += alignTo.y - bounds.miny;
            } else if (ychoice.getSelectedIndex() == 4) {
                orig.y += alignTo.y - orig.y;
            }
            if (zchoice.getSelectedIndex() == 1) {
                orig.z += alignTo.z - bounds.maxz;
            } else if (zchoice.getSelectedIndex() == 2) {
                orig.z += alignTo.z - center.z;
            } else if (zchoice.getSelectedIndex() == 3) {
                orig.z += alignTo.z - bounds.minz;
            } else if (zchoice.getSelectedIndex() == 4) {
                orig.z += alignTo.z - orig.z;
            }
            coords.setOrigin(orig);
        }
        ArrayList<ObjectInfo> modified = new ArrayList<>();
        for (int index : sel) {
            modified.add(theScene.getObject(index));
        }
        theScene.applyTracksAfterModification(modified);
        setUndoRecord(undo);
        updateImage();
    }

    public void setTextureCommand() {
        int[] sel = getSelectedIndices();
        int i;
        int count = 0;
        ObjectInfo[] obj;

        for (i = 0; i < sel.length; i++) {
            if (theScene.getObject(sel[i]).getObject().canSetTexture()) {
                count++;
            }
        }
        if (count == 0) {
            return;
        }
        obj = new ObjectInfo[count];
        for (i = 0; i < sel.length; i++) {
            if (theScene.getObject(sel[i]).getObject().canSetTexture()) {
                obj[i] = theScene.getObject(sel[i]);
            }
        }
        new ObjectTextureDialog(this, obj);
        for (i = 0; i < sel.length; i++) {
            theScene.objectModified(theScene.getObject(sel[i]).getObject());
        }
        modified = true;
        updateImage();
    }

    public void renameObjectCommand() {
        int[] sel = getSelectedIndices();

        if (sel.length != 1) {
            return;
        }
        String current = theScene.getObject(sel[0]).getName();
        BStandardDialog dlg = new BStandardDialog("", Translate.text("renameObjectTitle"), BStandardDialog.PLAIN);
        String newName = dlg.showInputDialog(this, null, current);
        if (newName == null) {
            return;
        }
        UndoableEdit edit = new ObjectRenameEdit(this, sel[0], newName).execute();
        setUndoRecord(new UndoRecord(this, false, edit));
    }

    public void convertToTriangleCommand() {
        int[] sel = getSelectedIndices();
        Object3D obj, mesh;
        ObjectInfo info;

        if (sel.length != 1) {
            return;
        }
        info = theScene.getObject(sel[0]);
        obj = info.getObject();
        if (obj.canConvertToTriangleMesh() == Object3D.CANT_CONVERT) {
            return;
        }

        // If the object has a Pose track, all Pose keyframes will need to be deleted.
        boolean confirmed = false, hasPose = false;
        for (int i = 0; i < info.getTracks().length; i++) {
            if (info.getTracks()[i] instanceof PoseTrack) {
                hasPose = true;
                if (!confirmed && !info.getTracks()[i].isNullTrack()) {
                    BStandardDialog dlg = new BStandardDialog("", Translate.text("convertLosesPosesWarning", info.getName()), BStandardDialog.QUESTION);
                    String[] options = new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
                    if (dlg.showOptionDialog(this, options, options[0]) == 1) {
                        return;
                    }
                    confirmed = true;
                }
                if (info.getTracks()[i].getTimecourse() != null) {
                    info.getTracks()[i].getTimecourse().removeAllTimepoints();
                }
                info.setPose(null);
            }
        }
        if (confirmed) {
            score.repaintAll();
        }
        UndoRecord undo = new UndoRecord(this, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
        if (obj.canConvertToTriangleMesh() == Object3D.EXACTLY) {
            if (!confirmed) {
                BStandardDialog dlg = new BStandardDialog("", Translate.text("confirmConvertToTriangle", info.getName()), BStandardDialog.QUESTION);
                String[] options = new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
                if (dlg.showOptionDialog(this, options, options[0]) == 1) {
                    return;
                }
            }
            mesh = obj.convertToTriangleMesh(0.0);
        } else {
            ValueField errorField = new ValueField(0.1, ValueField.POSITIVE);
            ComponentsDialog dlg = new ComponentsDialog(this, Translate.text("selectToleranceForMesh"),
                    new Widget[]{errorField}, new String[]{Translate.text("maxError")});
            if (!dlg.clickedOk()) {
                return;
            }
            mesh = obj.convertToTriangleMesh(errorField.getValue());
        }
        if (mesh == null) {
            new BStandardDialog("", Translate.text("cannotTriangulate"), BStandardDialog.ERROR).showMessageDialog(this);
            return;
        }
        if (hasPose) {
            mesh = mesh.getPosableObject();
        }
        if (mesh.getTexture() == null) {
            Texture tex = theScene.getDefaultTexture();
            mesh.setTexture(tex, tex.getDefaultMapping(mesh));
        }
        theScene.replaceObject(obj, mesh, undo);
        setUndoRecord(undo);
        updateImage();
        updateMenus();
    }

    public void convertToActorCommand() {
        int[] sel = getSelectedIndices();
        Object3D obj;
        ObjectInfo info;

        if (sel.length != 1) {
            return;
        }
        info = theScene.getObject(sel[0]);
        obj = info.getObject();
        Object3D posable = obj.getPosableObject();
        if (posable == null) {
            return;
        }
        BStandardDialog dlg = new BStandardDialog("", UIUtilities.breakString(Translate.text("confirmConvertToActor", info.getName())), BStandardDialog.QUESTION);
        String[] options = new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
        if (dlg.showOptionDialog(this, options, options[0]) == 1) {
            return;
        }
        UndoRecord undo = new UndoRecord(this, false, UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
        theScene.replaceObject(obj, posable, undo);
        setUndoRecord(undo);
        updateImage();
        updateMenus();
    }

    private void setObjectVisibility(boolean visible, boolean selectionOnly) {
        UndoRecord undo = new UndoRecord(this);
        if (selectionOnly) {
            int[] sel = getSelectedIndices();
            for (int i = 0; i < sel.length; i++) {
                ObjectInfo info = theScene.getObject(sel[i]);
                undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
                info.setVisible(visible);
            }
        } else {
            for (ObjectInfo info : theScene.getObjects()) {
                undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
                info.setVisible(visible);
            }
        }
        setUndoRecord(undo);
        updateImage();
        sceneExplorer.repaint();
    }

    private void setObjectsLocked(boolean locked, boolean selectionOnly) {
        UndoRecord undo = new UndoRecord(this);
        if (selectionOnly) {

            for (int j : getSelectedIndices()) {
                ObjectInfo info = theScene.getObject(j);
                undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
                info.setLocked(locked);
            }
        } else {
            for (ObjectInfo info : theScene.getObjects()) {
                undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
                info.setLocked(locked);
            }
        }
        setUndoRecord(undo);
        updateImage();
        sceneExplorer.repaint();
    }

    public void createScriptObjectCommand() {
        // Prompt the user to select a name and, optionally, a predefined script.

        BTextField nameField = new BTextField(Translate.text("Script"));
        BComboBox scriptChoice = new BComboBox();
        scriptChoice.add(Translate.text("newScript"));
        String[] files = new File(ArtOfIllusion.OBJECT_SCRIPT_DIRECTORY).list();
        ArrayList<String> scriptNames = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                if (ScriptRunner.getLanguageForFilename(file) != ScriptRunner.UNKNOWN_LANGUAGE) {
                    scriptChoice.add(file.substring(0, file.lastIndexOf(".")));
                    scriptNames.add(file);
                }
            }
        }
        ComponentsDialog dlg = new ComponentsDialog(this, Translate.text("newScriptedObject"),
                new Widget[]{nameField, scriptChoice}, new String[]{Translate.text("Name"), Translate.text("Script")});
        if (!dlg.clickedOk()) {
            return;
        }

        // If they are using a predefined script, load it.
        String scriptText = "";
        String language;
        if (scriptChoice.getSelectedIndex() > 0) {
            try {
                File f = new File(ArtOfIllusion.OBJECT_SCRIPT_DIRECTORY, scriptNames.get(scriptChoice.getSelectedIndex() - 1));
                scriptText = ArtOfIllusion.loadFile(f);
                language = ScriptRunner.getLanguageForFilename(f.getName());
                if (language == ScriptRunner.UNKNOWN_LANGUAGE) {
                    // Predefined scripts are supposed to have a correct extension,
                    // so it's ok to throw an exception here
                    throw new IOException("Unrecognized extension for " + f.getName());
                }
            } catch (IOException ex) {
                new BStandardDialog("", new String[]{Translate.text("errorReadingScript"), ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
                return;
            }
        } else {
            // Default language : Beanshell
            language = ScriptRunner.Language.BEANSHELL.name;
        }
        ScriptedObject obj = new ScriptedObject(scriptText, language);
        ObjectInfo info = new ObjectInfo(obj, new CoordinateSystem(), nameField.getText());
        UndoRecord undo = new UndoRecord(this);
        int[] sel = getSelectedIndices();
        addObject(info, undo);
        undo.addCommand(UndoRecord.SET_SCENE_SELECTION, sel);
        setSelection(theScene.getNumObjects() - 1);
        setUndoRecord(undo);
        updateImage();
        editObjectCommand();
    }

    public void jumpToTimeCommand() {
        ValueField timeField = new ValueField(theScene.getTime(), ValueField.NONE);
        ComponentsDialog dlg = new ComponentsDialog(this, Translate.text("jumpToTimeTitle"),
                new Widget[]{timeField}, new String[]{Translate.text("Time")});

        if (!dlg.clickedOk()) {
            return;
        }
        double t = timeField.getValue();
        double fps = theScene.getFramesPerSecond();
        t = Math.round(t * fps) / fps;
        setTime(t);
    }

    public void bindToParentCommand() {
        BStandardDialog dlg = new BStandardDialog("", UIUtilities.breakString(Translate.text("confirmBindParent")), BStandardDialog.QUESTION);
        String[] options = new String[]{Translate.text("button.ok"), Translate.text("button.cancel")};
        if (dlg.showOptionDialog(this, options, options[0]) == 1) {
            return;
        }
        int[] sel = getSelectedIndices();

        UndoRecord undo = new UndoRecord(this);
        for (int i = 0; i < sel.length; i++) {
            ObjectInfo info = theScene.getObject(sel[i]);
            if (info.getParent() == null) {
                continue;
            }
            Skeleton s = info.getParent().getSkeleton();
            ObjectRef relObj = new ObjectRef(info.getParent());
            if (s != null) {
                double nearest = Double.MAX_VALUE;
                Joint[] jt = s.getJoints();
                Vec3 pos = info.getCoords().getOrigin();
                for (int j = 0; j < jt.length; j++) {
                    ObjectRef r = new ObjectRef(info.getParent(), jt[j]);
                    double dist = r.getCoords().getOrigin().distance2(pos);
                    if (dist < nearest) {
                        relObj = r;
                        nearest = dist;
                    }
                }
            }
            undo.addCommand(UndoRecord.COPY_OBJECT_INFO, info, info.duplicate());
            PositionTrack pt = new PositionTrack(info);
            pt.setCoordsObject(relObj);
            info.addTrack(pt, 0);
            pt.setKeyframe(theScene.getTime());
            RotationTrack rt = new RotationTrack(info);
            rt.setCoordsObject(relObj);
            info.addTrack(rt, 1);
            rt.setKeyframe(theScene.getTime());
        }
        setUndoRecord(undo);
        score.rebuildList();
        score.repaint();
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void renderCommand() {
        SwingUtilities.invokeLater(() -> new RenderSetupDialog(this, theScene));
    }

    public void toggleViewsCommand() {
        if (numViewsShown == 4) {
            numViewsShown = 1;
            viewsContainer.setColumnWeight(0, (currentView == 0 || currentView == 2) ? 1 : 0);
            viewsContainer.setColumnWeight(1, (currentView == 1 || currentView == 3) ? 1 : 0);
            viewsContainer.setRowWeight(0, (currentView == 0 || currentView == 1) ? 1 : 0);
            viewsContainer.setRowWeight(1, (currentView == 2 || currentView == 3) ? 1 : 0);
            viewMenuItem[0].setText(Translate.text("menu.fourViews"));
        } else {
            numViewsShown = 4;
            viewsContainer.setColumnWeight(0, 1);
            viewsContainer.setColumnWeight(1, 1);
            viewsContainer.setRowWeight(0, 1);
            viewsContainer.setRowWeight(1, 1);
            viewMenuItem[0].setText(Translate.text("menu.oneView"));
        }
        viewsContainer.layoutChildren();
        savePreferences();
        updateImage();
        viewPanel[currentView].requestFocus();
    }

    public void setTemplateCommand() {
        var fc = new ImageFileChooser(Translate.text("selectTemplateImage"));
        if (!fc.showDialog(this)) {
            return;
        }
        File f = fc.getSelectedFile();
        try {
            theView[currentView].setTemplateImage(f);
        } catch (InterruptedException ex) {
            new BStandardDialog("", UIUtilities.breakString(Translate.text("errorLoadingImage", f.getName())), BStandardDialog.ERROR).showMessageDialog(this);
        }
        theView[currentView].setShowTemplate(true);
        updateImage();
        updateMenus();
    }

    public void setGridCommand() {
        ValueField spaceField = new ValueField(theScene.getGridSpacing(), ValueField.POSITIVE);
        ValueField divField = new ValueField(theScene.getGridSubdivisions(), ValueField.POSITIVE + ValueField.INTEGER);
        BCheckBox showBox = new BCheckBox(Translate.text("showGrid"), theScene.getShowGrid());
        BCheckBox snapBox = new BCheckBox(Translate.text("snapToGrid"), theScene.getSnapToGrid());
        ComponentsDialog dlg = new ComponentsDialog(this, Translate.text("gridTitle"),
                new Widget[]{spaceField, divField, showBox, snapBox},
                new String[]{Translate.text("gridSpacing"), Translate.text("snapToSubdivisions"), null, null});
        if (!dlg.clickedOk()) {
            return;
        }
        theScene.setGridSpacing(spaceField.getValue());
        theScene.setGridSubdivisions((int) divField.getValue());
        theScene.setShowGrid(showBox.getState());
        theScene.setSnapToGrid(snapBox.getState());
        for (int i = 0; i < theView.length; i++) {
            theView[i].setGrid(theScene.getGridSpacing(), theScene.getGridSubdivisions(), theScene.getShowGrid(), theScene.getSnapToGrid());
        }
        updateImage();
    }

    /**
     * @deprecated Use ViewerCanvas.fitToObjects() instead
     */
    @Deprecated
    public void frameWithCameraCommand(boolean selectionOnly) {
        int[] sel = getSelectionWithChildren();
        BoundingBox bb = null;

        if (selectionOnly) {
            for (int i = 0; i < sel.length; i++) {
                ObjectInfo info = theScene.getObject(sel[i]);
                BoundingBox bounds = info.getBounds().transformAndOutset(info.getCoords().fromLocal());
                if (bb == null) {
                    bb = bounds;
                } else {
                    bb = bb.merge(bounds);
                }
            }
        } else {
            for (ObjectInfo info : theScene.getObjects()) {
                BoundingBox bounds = info.getBounds().transformAndOutset(info.getCoords().fromLocal());
                if (bb == null) {
                    bb = bounds;
                } else {
                    bb = bb.merge(bounds);
                }
            }
        }
        if (bb == null) {
            return;
        }
        if (numViewsShown == 1) {
            theView[currentView].frameBox(bb);
        } else {
            for (int i = 0; i < theView.length; i++) {
                theView[i].frameBox(bb);
            }
        }
        updateImage();
    }

    public void texturesCommand() {
        showTexturesDialog(theScene);
    }

    /**
     * Show the dialog for editing textures and materials.
     */
    public void showTexturesDialog(Scene target) {
        new TexturesAndMaterialsDialog(this, target);
    }

    private void executeScriptCommand(CommandEvent ev) {
        executeScript(new File(ev.getActionCommand()));
    }

    /**
     * Execute the tool script contained in a file, passing a reference to this
     * window in its "window" variable.
     */
    public void executeScript(File f) {
        // Read the script from the file.

        String language = null;
        try {
            language = ScriptRunner.getLanguageForFilename(f.getName());
            if (language == ScriptRunner.UNKNOWN_LANGUAGE) // Predefined scripts are supposed to have a correct extension,
            // so it's ok to throw an exception here
            {
                throw new IOException("Unrecognized extension for " + f.getName());
            }
            String scriptText = ArtOfIllusion.loadFile(f);
            ToolScript script = ScriptRunner.parseToolScript(language, scriptText);
            script.execute(this);
        } catch (IOException ex) {
            new BStandardDialog("", new String[]{Translate.text("errorReadingScript"), ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(this);
            return;
        } catch (Exception e) {
            ScriptRunner.displayError(language, e);
        }
        updateImage();
        dispatchSceneChangedEvent(); // To be safe, since we can't rely on scripts to set undo records or call setModified().
    }

    @Subscribe
    public void onObjectPreviewRendererChange(ApplicationPreferences.ObjectPreviewRendererChangeEvent event) {
        log.info("Renderer changed to {}", event.getNewRenderer().getName());
        event.getOldValue().cancelRendering(theScene);
        for (ViewerCanvas view : theView) {
            view.viewChanged(false);
        }
        updateImage();
    }

    @Subscribe
    public void onInteractiveSurfaceErrorChange(ApplicationPreferences.InteractiveSurfaceErrorChangeEvent event) {
        log.info("Tolerance changed to {}", event.getTolerance());
        theScene.getObjects().forEach(ObjectInfo::clearCachedMeshes);
        updateImage();
    }

    @Override
    public void dispose() {
        super.dispose();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyEventHandler);
    }

}
