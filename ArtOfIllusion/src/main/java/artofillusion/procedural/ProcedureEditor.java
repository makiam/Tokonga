/* Copyright (C) 2000-2012 by Peter Eastman
   Changes copyright (C) 2023-2025 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

import artofillusion.*;
import static artofillusion.procedural.IOPort.SIZE;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * This is the editor for editing procedures. It subclasses CustomWidget, but you should never
 * add it to any Container. Instead, it will automatically create a BFrame and add itself
 * to that.
 */
@Slf4j
public class ProcedureEditor extends CustomWidget {

    private final BFrame parent;
    private final Procedure proc;
    /**
     * -- GETTER --
     *  Get the editor's owner.
     */
    @Getter
    private final ProcedureOwner owner;
    /**
     * -- GETTER --
     *  Get the scene the procedure is part of.
     */
    @Getter
    private final Scene scene;
    private EditingWindow win;
    private final Dimension size;
    private BMenuItem undoItem;
    private BMenuItem redoItem;
    private BMenuItem cutItem;
    private BMenuItem copyItem;
    private BMenuItem pasteItem;
    private BMenuItem clearItem;
    private BTextField nameField;

    private boolean[] selectedModule;

    private boolean[] selectedLink;
    private boolean draggingLink;
    private boolean draggingModule;
    private boolean draggingBox;
    private boolean draggingMultiple;
    private Point clickPos;
    private Point lastPos;
    private final InfoBox inputInfo;
    private final InfoBox outputInfo;
    private IOPort dragFromPort;
    private IOPort dragToPort;
    private Optional<MaterialPreviewer> preview = Optional.empty();
    private final ByteArrayOutputStream cancelBuffer;
    private final ArrayList<ByteArrayOutputStream> undoStack;
    private final ArrayList<ByteArrayOutputStream> redoStack;

    private static final Color darkLinkColor = Color.darkGray;
    private static final Color blueLinkColor = new Color(40, 40, 255);
    private static final Color selectedLinkColor = new Color(255, 50, 50);
    private static final Color outputBackgroundColor = new Color(210, 210, 240);

    private static final Color outlineColor = new Color(110, 110, 160);
    private static final Color selectedColor = new Color(255, 60, 60);
    protected static final Stroke contourStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final float BEZIER_HARDNESS = 0.5f; //increase hardness to a have a more pronounced shape
    private static final Stroke normal = new BasicStroke();
    private static final Stroke bold = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static ClipboardSelection clipboard;

    public ProcedureEditor(Procedure proc, ProcedureOwner owner, Scene scene) {
        super();
        this.proc = proc;
        this.owner = owner;
        this.scene = scene;
        selectedModule = new boolean[proc.getModules().size()];
        selectedLink = new boolean[proc.getLinks().length];
        inputInfo = new InfoBox();
        outputInfo = new InfoBox();
        cancelBuffer = new ByteArrayOutputStream();
        undoStack = new ArrayList<>();
        redoStack = new ArrayList<>();
        parent = new BFrame(owner.getWindowTitle());
        parent.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        parent.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());
        parent.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelImpl();
            }
        });
        BorderContainer content = new BorderContainer();
        parent.setContent(content);
        BScrollPane scroll;
        content.add(scroll = new BScrollPane(this), BorderContainer.CENTER);
        scroll.setPreferredViewSize(new Dimension(600, 600));
        new AutoScroller(scroll, 5, 5);
        size = new Dimension(1000, 1000);
        setBackground(Color.white);
        KeyStroke deleteKS = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke backKS = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
        ActionListener ra = e -> deleteSelection();
        this.getComponent().getRootPane().registerKeyboardAction(ra, deleteKS, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.getComponent().getRootPane().registerKeyboardAction(ra, backKS, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addEventLink(MousePressedEvent.class, this, "mousePressed");
        addEventLink(MouseReleasedEvent.class, this, "mouseReleased");
        addEventLink(MouseClickedEvent.class, this, "mouseClicked");
        addEventLink(MouseDraggedEvent.class, this, "mouseDragged");
        addEventLink(RepaintEvent.class, this, "paint");

        // Save the current state of the procedure so that editing can be canceled.
        DataOutputStream out = new DataOutputStream(cancelBuffer);
        try {
            proc.writeToStream(out, this.scene);
            out.close();
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error save procedure state: {}", ex.getMessage());
        }

        // Create the buttons at the top of the window.
        FormContainer top = new FormContainer(new double[]{0.0, 0.0, 1.0, 0.0, 0.0}, new double[]{1.0});
        top.setDefaultLayout(new LayoutInfo(LayoutInfo.CENTER, LayoutInfo.HORIZONTAL, new Insets(0, 0, 0, 5), null));
        top.add(Translate.button("properties", event -> doProperties()), 0, 0);
        if (owner.canEditName()) {
            top.add(new BLabel(Translate.text("Name") + ':'), 1, 0);
            top.add(nameField = new BTextField(owner.getName()), 2, 0);
        }
        top.add(Translate.button("ok", event -> doOk()), 3, 0);
        top.add(Translate.button("cancel", event -> cancel()), 4, 0);

        content.add(top, BorderContainer.NORTH);

        // Create the module menu.
        ModuleMenu moduleMenu = new ModuleMenu(this);
        content.add(moduleMenu, BorderContainer.WEST);

        // Let each output module calculate its preferred width, then set all of them to be
        // as wide as the widest one.
        int widest = 0;
        for (OutputModule om : proc.getOutputModules()) {
            om.calcSize();
            if (om.getBounds().width > widest) {
                widest = om.getBounds().width;
            }
        }
        int x = size.width - widest;
        int y = 15;
        for (OutputModule om : proc.getOutputModules()) {
            om.setWidth(widest);
            om.setPosition(x - 15, y);
            y += om.getBounds().height + 15;
        }

        // Add the menu bar.
        BMenuBar mb = new BMenuBar();
        parent.setMenuBar(mb);
        mb.add(getEditMenu());
        updateMenus();

        // Display the window.
        parent.pack();
        scroll.getHorizontalScrollBar().setBlockIncrement(100);
        scroll.getVerticalScrollBar().setBlockIncrement(100);
        scroll.getHorizontalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        parent.setVisible(true);
        scroll.getHorizontalScrollBar().setValue(getBounds().width - scroll.getViewSize().width);
        preview = Optional.ofNullable(owner.getPreview());
        preview.ifPresent(view -> createPreview(getParentFrame(), view));
    }

    private void cancelImpl() {
        doOk();
    }

    private static void createPreview(BFrame frame, MaterialPreviewer view) {

        BDialog previewDialog = new BDialog(frame, "Preview", false);
        BorderContainer content = new BorderContainer();

        content.add(view, BorderContainer.CENTER);
        RowContainer row = new RowContainer();
        content.add(row, BorderContainer.SOUTH, new LayoutInfo());
        row.add(Translate.label("Time", ":"));
        final ValueSelector value = new ValueSelector(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.01);
        final ActionProcessor processor = new ActionProcessor();
        row.add(value);
        value.addEventLink(ValueChangedEvent.class, new Object() {
            void processEvent() {
                processor.addEvent(()
                        -> {
                    view.getScene().setTime(value.getValue());
                    view.render();
                });
            }
        });
        previewDialog.setContent(content);
        previewDialog.pack();
        frame.getComponent().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent event) {
                onParentMoved();
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                onParentMoved();
            }

            private void onParentMoved() {
                Rectangle parentBounds = frame.getBounds();
                Rectangle location = previewDialog.getBounds();
                location.y = parentBounds.y;
                location.x = parentBounds.x + parentBounds.width;
                previewDialog.setBounds(location);
            }
        });

        Rectangle parentBounds = frame.getBounds();
        Rectangle location = previewDialog.getBounds();
        location.y = parentBounds.y;
        location.x = parentBounds.x + parentBounds.width;
        previewDialog.setBounds(location);
        previewDialog.setVisible(true);
    }

    /**
     * Create the Edit menu.
     */
    private BMenu getEditMenu() {
        BMenu editMenu = Translate.menu("edit");
        editMenu.add(undoItem = Translate.menuItem("undo", event -> undo()));
        editMenu.add(redoItem = Translate.menuItem("redo", event -> redo()));
        undoItem.setEnabled(false);
        redoItem.setEnabled(false);
        editMenu.addSeparator();
        editMenu.add(cutItem = Translate.menuItem("cut", event -> doCut()));
        editMenu.add(copyItem = Translate.menuItem("copy", event -> doCopy()));
        editMenu.add(pasteItem = Translate.menuItem("paste", event -> doPaste()));
        editMenu.add(clearItem = Translate.menuItem("clear", event -> deleteSelection()));
        editMenu.addSeparator();
        editMenu.add(Translate.menuItem("properties", event -> doProperties()));
        return editMenu;
    }

    /**
     * Get the editor's parent Frame.
     */
    public BFrame getParentFrame() {
        return parent;
    }

    /**
     * Set the editing window which owns the scene the procedure is part of.
     */
    public void setEditingWindow(EditingWindow window) {
        win = window;
    }

    /**
     * Get the editing window which owns the scene the procedure is part of.
     */
    public EditingWindow getEditingWindow() {
        return win;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @SuppressWarnings("java:S1144")
    private void paint(RepaintEvent ev) {
        paint(ev.getGraphics());
    }

    private void paint(Graphics2D g) {
        OutputModule[] output = proc.getOutputModules();

        int divider = output[0].getBounds().x - 5;

        // Draw the line marking off the output modules.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(outputBackgroundColor);
        g.fillRoundRect(divider, 5, size.width - divider - 10, size.height - 10, 8, 8);

        // Draw the output modules.
        Arrays.stream(output).forEach(mod -> {
            drawModule(mod, g, false);
            Arrays.stream(mod.getInputPorts()).forEach(port -> drawPort(port, g));
            mod.drawContents(g);
        });

        // Draw the modules.
        var  modules = proc.getModules();
        IntStream.range(0, modules.size()).forEach(index -> {
            var  mod = modules.get(index);
            drawModule(mod, g, selectedModule[index]);
            Arrays.stream(mod.getInputPorts()).forEach(port -> drawPort(port, g));
            Arrays.stream(mod.output).forEach(port -> drawPort(port, g));
            mod.drawContents(g);
        });

        Link[] link = proc.getLinks();
        // Draw the unselected links.

        g.setStroke(bold);
        for (int i = 0; i < link.length; i++) {
            if (!selectedLink[i]) {
                g.setColor(link[i].from.getValueType() == IOPort.NUMBER ? darkLinkColor : blueLinkColor);
                g.draw(createBezierCurve(link[i]));
            }
        }

        // Draw the selected links.
        g.setColor(selectedLinkColor);
        for (int i = 0; i < link.length; i++) {
            if (selectedLink[i]) {
                g.draw(createBezierCurve(link[i]));
            }
        }
        g.setStroke(normal);

        // If we are in the middle of dragging something, draw the thing being dragged.
        if (draggingLink) {
            // Draw the info boxes on the input and output ports.

            boolean isInput = (dragFromPort.getType() == IOPort.INPUT);
            if (isInput || dragToPort != null) {
                inputInfo.draw(g);
            }
            if (!isInput || dragToPort != null) {
                outputInfo.draw(g);
            }
        }
        g.setColor(Color.BLACK);
        if (draggingBox && lastPos != null) {
            // Draw the selection box.

            Rectangle rect = getRectangle(clickPos, lastPos);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }
        if (draggingLink && lastPos != null) {
            if (dragToPort == null) {
                // Draw lines from the origin port(s) to the current mouse position.

                if (draggingMultiple) {
                    int dx = lastPos.x - clickPos.x;
                    int dy = lastPos.y - clickPos.y;
                    IOPort[] ports = (dragFromPort.getType() == IOPort.OUTPUT ? dragFromPort.getModule().getOutputPorts() : dragFromPort.getModule().getInputPorts());
                    for (IOPort port : ports) {
                        if (port.getType() == IOPort.OUTPUT || !port.getModule().inputConnected(port.getIndex())) {
                            g.drawLine(port.getPosition().x, port.getPosition().y, port.getPosition().x + dx, port.getPosition().y + dy);
                        }
                    }
                } else {
                    g.drawLine(clickPos.x, clickPos.y, lastPos.x, lastPos.y);
                }
            } else {
                // Draw lines between the origin and target port(s).

                if (draggingMultiple) {
                    IOPort outputPort = (dragFromPort.getType() == IOPort.OUTPUT ? dragFromPort : dragToPort);
                    IOPort inputPort = (dragFromPort.getType() == IOPort.INPUT ? dragFromPort : dragToPort);
                    IOPort[] outputs = outputPort.getModule().getOutputPorts();
                    IOPort[] inputs = inputPort.getModule().getInputPorts();
                    int outputIndex = outputPort.getIndex();
                    int inputIndex = inputPort.getIndex();
                    for (int i = 0; i < outputs.length; i++) {
                        int j = i - outputIndex + inputIndex;
                        if (j >= 0 && j < inputs.length && !inputPort.getModule().inputConnected(j)) {
                            g.drawLine(outputs[i].getPosition().x, outputs[i].getPosition().y, inputs[j].getPosition().x, inputs[j].getPosition().y);
                        }
                    }
                } else {
                    Point pos = dragToPort.getPosition();
                    g.drawLine(clickPos.x, clickPos.y, pos.x, pos.y);
                }
            }
        }
    }

    private static void drawPort(IOPort port, Graphics2D g) {
        g.setColor(Color.BLUE);
        if (port.getValueType() == IOPort.NUMBER) {
            g.setColor(Color.BLACK);
        }
        int x = port.x;
        int y = port.y;

        switch (port.getLocation()) {
            case IOPort.TOP:
                g.fillPolygon(new int[]{x + SIZE, x - SIZE, x}, new int[]{y, y, y + SIZE}, 3);
                break;
            case IOPort.BOTTOM:
                g.fillPolygon(new int[]{x + SIZE, x - SIZE, x}, new int[]{y, y, y - SIZE}, 3);
                break;
            case IOPort.LEFT:
                g.fillPolygon(new int[]{x, x, x + SIZE}, new int[]{y + SIZE, y - SIZE, y}, 3);
                break;
            case IOPort.RIGHT:
                g.fillPolygon(new int[]{x - SIZE, x - SIZE, x}, new int[]{y + SIZE, y - SIZE, y}, 3);
                break;
            default:
                break;
        }
    }

    private static void drawModule(Module<?> module, Graphics2D g, boolean selected) {
        Rectangle bounds = module.getBounds();

        Stroke currentStroke = g.getStroke();
        g.setColor(Color.lightGray);
        g.fillRoundRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2, 3, 3);
        g.setColor(selected ? selectedColor : outlineColor);
        g.setStroke(contourStroke);
        g.drawRoundRect(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2, 4, 4);
        g.setStroke(currentStroke);

    }

    private static Shape createBezierCurve(Link link) {
        int x1 = link.from.getPosition().x;
        int y1 = link.from.getPosition().y;
        int x2 = link.to.getPosition().x;
        int y2 = link.to.getPosition().y;
        float ctrlX1;
        float ctrlY1;
        float ctrlX2;
        float ctrlY2;
        if (link.from.getLocation() == IOPort.LEFT || link.from.getLocation() == IOPort.RIGHT) {
            ctrlX1 = (x2 - x1) * BEZIER_HARDNESS + x1;
            ctrlY1 = y1;
        } else {
            ctrlX1 = x1;
            ctrlY1 = (y2 - y1) * BEZIER_HARDNESS + y1;
        }
        if (link.to.getLocation() == IOPort.LEFT || link.to.getLocation() == IOPort.RIGHT) {
            ctrlX2 = (1 - BEZIER_HARDNESS) * (x2 - x1) + x1;
            ctrlY2 = y2;
        } else {
            ctrlX2 = x2;
            ctrlY2 = (1 - BEZIER_HARDNESS) * (y2 - y1) + y1;
        }
        return new CubicCurve2D.Float(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
    }

    /**
     * Update the items in the Edit menu whenever the selection changes.
     */
    private void updateMenus() {
        boolean anyModule = false;
        boolean anyLink = false;
        for (boolean selected : selectedModule) {
            if (selected) {
                anyModule = true;
            }
        }
        for (boolean selected : selectedLink) {
            if (selected) {
                anyLink = true;
            }
        }
        cutItem.setEnabled(anyModule);
        copyItem.setEnabled(anyModule);
        pasteItem.setEnabled(clipboard != null);
        clearItem.setEnabled(anyModule || anyLink);
    }


    public void cancel() {
        undoStack.add(cancelBuffer);
        undo();
        parent.getComponent().dispose();
    }

    public void doProperties() {
        owner.editProperties(this);
        updatePreview();
    }

    private void doPaste() {
        Optional.ofNullable(clipboard).ifPresent(sel -> {
            saveState(false);
            sel.paste(this);
            repaint();
        });
    }

    private void doCopy() {
        clipboard = new ClipboardSelection(proc, selectedModule, selectedLink);
        updateMenus();
    }

    private void doCut() {
        clipboard = new ClipboardSelection(proc, selectedModule, selectedLink);
        deleteSelection();
        updateMenus();
    }

    /**
     * Save changes and close the window.
     */
    private void doOk() {
        if (owner.canEditName()) {
            owner.setName(nameField.getText());
        }
        owner.acceptEdits(this);
        parent.dispose();
    }

    /**
     * Add a module to the procedure.
     */
    public void addModule(Module<?> mod) {
        selectedModule = new boolean[selectedModule.length + 1];
        selectedModule[selectedModule.length - 1] = true;
        Arrays.fill(selectedLink, false);
        proc.addModule(mod);
        updateMenus();
    }

    /**
     * Add a link between two ports in the procedure.
     */
    private void addLink(IOPort port1, IOPort port2) {
        IOPort from;
        IOPort to;

        selectedLink = new boolean[selectedLink.length + 1];
        selectedLink[selectedLink.length - 1] = true;
        Arrays.fill(selectedModule, false);
        if (port1.getType() == IOPort.OUTPUT) {
            from = port1;
            to = port2;
        } else {
            from = port2;
            to = port1;
        }
        proc.addLink(new Link(from, to));
        if (proc.checkFeedback()) {
            proc.deleteLink(proc.getLinks().length - 1);
            selectedLink = new boolean[proc.getLinks().length];
            MessageDialog.create().error("The link you have selected cannot be created, as it would result in a feedback loop.");
        }
        updateMenus();
    }

    /**
     * Record the current state of the procedure, so that it can be undone.
     */
    public void saveState(boolean redo) {
        ArrayList<ByteArrayOutputStream> stack = (redo ? redoStack : undoStack);
        if (stack.size() == ArtOfIllusion.getPreferences().getUndoLevels()) {
            stack.remove(0);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);
        try {
            proc.writeToStream(out, scene);
            out.close();
            stack.add(buffer);
            if (redo) {
                redoItem.setEnabled(true);
            } else {
                undoItem.setEnabled(true);
            }
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error save procedure state: {}", ex.getMessage());
        }
    }

    /**
     * Undo the last action.
     */
    private void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        saveState(true);
        ByteArrayOutputStream buffer = undoStack.get(undoStack.size() - 1);
        undoStack.remove(undoStack.size() - 1);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        try {
            proc.readFromStream(in, scene);
            in.close();
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error read procedure state: {}", ex.getMessage());
        }
        if (redoStack.size() == ArtOfIllusion.getPreferences().getUndoLevels()) {
            redoStack.remove(0);
        }
        undoItem.setEnabled(!undoStack.isEmpty());
        selectedModule = new boolean[proc.getModules().size()];
        selectedLink = new boolean[proc.getLinks().length];
        repaint();
        updatePreview();
        updateMenus();
    }

    /**
     * Redo the last action that was undone.
     */
    private void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        saveState(false);
        ByteArrayOutputStream buffer = redoStack.get(redoStack.size() - 1);
        redoStack.remove(redoStack.size() - 1);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        try {
            proc.readFromStream(in, scene);
            in.close();
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error read procedure state: {}", ex.getMessage());
        }
        if (undoStack.size() == ArtOfIllusion.getPreferences().getUndoLevels()) {
            undoStack.remove(0);
        }
        redoItem.setEnabled(!redoStack.isEmpty());
        selectedModule = new boolean[proc.getModules().size()];
        selectedLink = new boolean[proc.getLinks().length];
        repaint();
        updatePreview();
        updateMenus();
    }

    /**
     * Update the preview.
     */
    public void updatePreview() {
        preview.ifPresent(owner::updatePreview);
    }

    /**
     * Respond to mouse clicks.
     */
    @SuppressWarnings("java:S1144")
    private void mouseClicked(MouseClickedEvent e) {
        Point pos = e.getPoint();
        if (e.getClickCount() == 2) {
            // See if the click was on a module.  If so, call its edit() method.

            for (var       mod : proc.getModules()) {
                if (mod.getBounds().contains(pos)) {
                    saveState(false);
                    if (mod.edit(this, scene)) {
                        repaint();
                        updatePreview();
                    } else {
                        undo();
                    }
                    return;
                }
            }
        }
    }

    /**
     * Respond to mouse presses.
     */
    protected void mousePressed(MousePressedEvent e) {
        OutputModule[] output = proc.getOutputModules();
        var  modules = proc.getModules();
        Link[] link = proc.getLinks();
        IOPort port;
        int i;

        requestFocus();
        clickPos = e.getPoint();
        lastPos = null;
        draggingMultiple = false;

        // First see if the mouse was pressed on a port.
        for (var mod: modules) {
            port = mod.getClickedPort(clickPos);
            if (port != null) {
                startDragLink(port);
                draggingMultiple = e.isShiftDown();
                return;
            }
        }
        for (i = 0; i < output.length; i++) {
            port = output[i].getClickedPort(clickPos);
            if (port != null) {
                startDragLink(port);
                draggingMultiple = e.isShiftDown();
                return;
            }
        }

        // See if the mouse was pressed on a selected module.
        for (i = modules.size() - 1; i >= 0; i--) {
            if (selectedModule[i] && modules.get(i).getBounds().contains(clickPos)) {
                draggingModule = true;
                if (e.getWidget() == this) // Otherwise, it's a synthetic mouse event being sent from the ModuleMenu
                {
                    saveState(false);
                }
                lastPos = clickPos;
                repaint();
                return;
            }
        }

        // See if the mouse was pressed on an unselected module.
        for (i = modules.size() - 1; i >= 0; i--) {
            if (!selectedModule[i] && modules.get(i).getBounds().contains(clickPos)) {
                draggingModule = true;
                saveState(false);
                if (!e.isShiftDown()) {
                    Arrays.fill(selectedModule, false);
                    Arrays.fill(selectedLink, false);
                }
                selectedModule[i] = true;
                lastPos = clickPos;
                repaint();
                updateMenus();
                return;
            }
        }

        // See if the mouse was pressed on a link.
        for (i = 0; i < link.length; i++) {
            int tol = 2;
            if (!createBezierCurve(link[i]).intersects(new Rectangle(clickPos.x - tol, clickPos.y - tol, 2 * tol, 2 * tol))) {
                continue;
            }
            if (!e.isShiftDown()) {
                Arrays.fill(selectedModule, false);
                Arrays.fill(selectedLink, false);
            }
            selectedLink[i] = true;
            repaint();
            updateMenus();
            return;
        }

        // Erase the selection if the shift key is not held down.
        if (!e.isShiftDown()) {
            Arrays.fill(selectedModule, false);
            Arrays.fill(selectedLink, false);
        }
        draggingBox = true;
        repaint();
        updateMenus();
    }

    protected void mouseReleased() {
        if (draggingLink) {
            draggingLink = false;
            if (dragToPort != null) {
                saveState(false);
                if (draggingMultiple) {
                    // Connect up multiple input and output ports.

                    int numLinks = selectedLink.length;
                    IOPort outputPort = (dragFromPort.getType() == IOPort.OUTPUT ? dragFromPort : dragToPort);
                    IOPort inputPort = (dragFromPort.getType() == IOPort.INPUT ? dragFromPort : dragToPort);
                    IOPort[] outputs = outputPort.getModule().getOutputPorts();
                    IOPort[] inputs = inputPort.getModule().getInputPorts();
                    int outputIndex = outputPort.getIndex();
                    int inputIndex = inputPort.getIndex();
                    for (int i = 0; i < outputs.length; i++) {
                        int j = i - outputIndex + inputIndex;
                        if (j >= 0 && j < inputs.length && !inputPort.getModule().inputConnected(j)) {
                            addLink(outputs[i], inputs[j]);
                        }
                    }
                    for (int i = numLinks; i < selectedLink.length; i++) {
                        selectedLink[i] = true;
                    }
                } else {
                    addLink(dragFromPort, dragToPort);
                }
                updatePreview();
            }
            repaint();
            return;
        }
        if (draggingBox) {
            if (lastPos == null) {
                draggingBox = false;
                repaint();
                return;
            }
            Rectangle rect = getRectangle(clickPos, lastPos);
            var  modules = proc.getModules();
            for (int i = 0; i < selectedModule.length; i++) {
                if (modules.get(i).getBounds().intersects(rect)) {
                    selectedModule[i] = true;
                }
            }
            draggingBox = false;
            repaint();
            updateMenus();
            return;
        }
        if (draggingModule) {
            draggingModule = false;
        }
        repaint();
    }

    /**
     * Deal with mouse drags.
     */
    protected void mouseDragged(MouseDraggedEvent e) {
        Point pos = e.getPoint();

        if (draggingBox) {
            // A selection box is being dragged.

            Graphics g = getComponent().getGraphics();
            Rectangle rect;
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            if (lastPos != null) {
                rect = getRectangle(clickPos, lastPos);
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
            }
            lastPos = pos;
            rect = getRectangle(clickPos, lastPos);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
            g.dispose();
            return;
        }
        if (draggingLink) {
            // A link is being dragged.  If the mouse was previously dragged to another port,
            // and is still over it, simply return.

            if (dragToPort != null && dragToPort.contains(pos)) {
                return;
            }

            // See whether the mouse is now over a port.
            boolean isInput = (dragFromPort.getType() == IOPort.INPUT);
            if (dragToPort != null) {
                dragToPort = null;
            }

            for (var mod :  proc.getModules()) {
                IOPort[] port = isInput ? mod.getOutputPorts() : mod.getInputPorts();
                for (int j = 0; j < port.length; j++) {
                    if (isInput || !mod.inputConnected(j)) {
                        if (port[j].getValueType() == dragFromPort.getValueType() && port[j].contains(pos)) {
                            dragToPort = port[j];
                        }
                    }
                }
                if (dragToPort != null) {
                    break;
                }
            }
            if (!isInput) {
                for (var mod : proc.getOutputModules()) {
                    IOPort[] port = mod.getInputPorts();
                    for (int j = 0; j < port.length; j++) {
                        if (!mod.inputConnected(j)) {
                            if (port[j].getValueType() == dragFromPort.getValueType() && port[j].contains(pos)) {
                                dragToPort = port[j];
                            }
                        }
                    }
                    if (dragToPort != null) {
                        break;
                    }
                }
            }

            // If the mouse is now over a port, we need to show its info box.
            if (dragToPort != null) {
                InfoBox info = isInput ? outputInfo : inputInfo;
                Rectangle rect = info.getBounds();
                pos = dragToPort.getPosition();
                if (isInput) {
                    info.setPosition(pos.x - rect.width - 10, pos.y - rect.height / 2);
                } else {
                    info.setPosition(pos.x + 10, pos.y - rect.height / 2);
                }
                info.setText(dragToPort.getDescription());
            }
            lastPos = pos;
            repaint();
            return;
        }

        // Move any selected modules.
        if (!draggingModule) {
            return;
        }
        int dx = 0;
        int dy = 0;
        var  modules = proc.getModules();
        if (lastPos != null) {
            dx = pos.x - lastPos.x;
            dy = pos.y - lastPos.y;
        }
        for (int i = 0; i < selectedModule.length; i++) {
            if (selectedModule[i]) {
                Rectangle rect = modules.get(i).getBounds();
                modules.get(i).setPosition(rect.x + dx, rect.y + dy);
            }
        }
        repaint();
        lastPos = pos;
    }

    /**
     * Start dragging a link.
     */
    private void startDragLink(IOPort port) {
        boolean isInput = (port.getType() == IOPort.INPUT);
        InfoBox info = isInput ? inputInfo : outputInfo;
        Point pos = port.getPosition();

        draggingLink = true;
        draggingModule = false;
        dragFromPort = port;
        dragToPort = null;
        clickPos = port.getPosition();
        info.setText(port.getDescription());
        Rectangle rect = info.getBounds();
        if (isInput) {
            info.setPosition(pos.x + 10, pos.y - rect.height / 2);
        } else {
            info.setPosition(pos.x - rect.width - 10, pos.y - rect.height / 2);
        }
        Graphics g = getComponent().getGraphics();
        info.draw(g);
        g.dispose();

        // If this is an input port which is already connected to something, don't actually
        // drag a link.
        if (isInput) {
            var  module = port.getModule();
            IOPort[] inputs = module.getInputPorts();
            for (int i = 0; i < inputs.length; i++) {
                if (inputs[i] == port && module.inputConnected(i)) {
                    draggingLink = false;
                }
            }
        }
    }

    /**
     * Delete any selected modules and links.
     */
    private void deleteSelection() {
        saveState(false);

        // First select any links which are connected to selected modules, since they will also need to be deleted.
        var  modules = proc.getModules();
        Link[] link = proc.getLinks();
        for (int i = 0; i < selectedLink.length; i++) {
            for (int j = 0; j < selectedModule.length; j++) {
                var module = modules.get(j);
                boolean isSel = selectedModule[j];
                if ((module == link[i].from.getModule() && isSel) || (module == link[i].to.getModule() && isSel)) {
                    selectedLink[i] = true;
                }
            }
        }

        // Delete any selected links.
        for (int i = selectedLink.length - 1; i >= 0; i--) {
            if (selectedLink[i]) {
                proc.deleteLink(i);
            }
        }

        // Now delete any selected modules.
        for (int i = selectedModule.length - 1; i >= 0; i--) {
            if (selectedModule[i]) {
                proc.deleteModule(i);
            }
        }
        selectedModule = new boolean[proc.getModules().size()];
        selectedLink = new boolean[proc.getLinks().length];
        updatePreview();
        repaint();
        updateMenus();
    }

    /**
     * Utility function to create a Rectangle from two Points.
     */
    private static Rectangle getRectangle(Point p1, Point p2) {
        int x;
        int y;
        int width;
        int height;

        x = Math.min(p1.x, p2.x);
        y = Math.min(p1.y, p2.y);
        width = Math.abs(p1.x - p2.x);
        height = Math.abs(p1.y - p2.y);
        return new Rectangle(x, y, width, height);
    }

    /**
     * Inner class representing a set of Modules and Links on the clipboard.
     */
    private static class ClipboardSelection {

        final Module[] module;
        final Link[] link;

        /**
         * Create a new ClipboardSelection representing the current selection.
         */
        public ClipboardSelection(Procedure proc, boolean[] selectedModule, boolean[] selectedLink) {
            // Determine which modules and links to copy.

            List<Module<?>> mod = new ArrayList<>();
            List<Link> ln = new ArrayList<>();
            var  allModules = proc.getModules();
            Link[] allLinks = proc.getLinks();

            for (int i = 0; i < selectedModule.length; i++) {
                if (selectedModule[i]) {
                    mod.add(allModules.get(i));
                }
            }
            for (int i = 0; i < selectedLink.length; i++) {
                if (mod.contains(allLinks[i].from.getModule()) && mod.contains(allLinks[i].to.getModule())) {
                    ln.add(allLinks[i]);
                }
            }

            // Duplicate them and build the arrays.
            module = new Module[mod.size()];
            link = new Link[ln.size()];
            for (int i = 0; i < module.length; i++) {
                module[i] = mod.get(i).duplicate();
            }
            for (int i = 0; i < link.length; i++) {
                Link thisLink = ln.get(i);
                int from = mod.indexOf(thisLink.from.getModule());
                int to = mod.indexOf(thisLink.to.getModule());
                int fromPort = thisLink.getFromPortIndex();
                int toPort = thisLink.getToPortIndex();
                link[i] = new Link(module[from].getOutputPorts()[fromPort], module[to].getInputPorts()[toPort]);
            }
        }

        /**
         * Paste the clipboard selection into the procedure.
         */
        public void paste(ProcedureEditor editor) {
            int numModules = editor.selectedModule.length;
            int numLinks = editor.selectedLink.length;
            var realMod = new Module[module.length];

            // Add the modules.
            for (int i = 0; i < module.length; i++) {

                realMod[i] = module[i].duplicate();
                if (realMod[i] instanceof ImageModule && editor.scene.indexOf(((ImageModule) realMod[i]).getMap()) == -1) {
                    ((ImageModule) realMod[i]).setMap(null);
                }
                Rectangle bounds = realMod[i].getBounds();
                realMod[i].setPosition(bounds.x - 30, bounds.y + 30);
                editor.addModule(realMod[i]);
            }

            // Add the links.
            for (Link ln : link) {
                int from;
                int to;
                for (from = 0; module[from] != ln.from.getModule(); from++)
            ;
                for (to = 0; module[to] != ln.to.getModule(); to++)
            ;
                if (realMod[from] == null || realMod[to] == null) {
                    continue;
                }
                int fromPort = ln.getFromPortIndex();
                int toPort = ln.getToPortIndex();
                editor.addLink(realMod[from].getOutputPorts()[fromPort], realMod[to].getInputPorts()[toPort]);
            }

            // Select everything that was just pasted.
            for (int i = 0; i < editor.selectedModule.length; i++) {
                editor.selectedModule[i] = (i >= numModules);
            }
            for (int i = 0; i < editor.selectedLink.length; i++) {
                editor.selectedLink[i] = (i >= numLinks);
            }
            editor.updateMenus();
        }
    }
}
