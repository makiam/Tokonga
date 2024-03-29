/* Copyright (C) 2017 by Petri Ihalainen
   Some methods copyright (C) by Peter Eastman
   Changes copyright 2019-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.image;

import artofillusion.*;
import artofillusion.texture.Texture;
import artofillusion.ui.*;
import buoy.event.*;
import buoy.widget.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageDetailsDialog extends BDialog {

    private final WindowWidget parent;
    private final Scene scene;
    private ImageMap im;
    private final BLabel imageField;
    private BufferedImage canvasImage;
    private final BButton refreshButton;
    private final BButton reconnectButton;
    private final BButton convertButton;
    private final BButton exportButton;
    private final BLabel[] title;
    private final BLabel[] data;
    private final Color defaultTextColor;
    private final Color errorTextColor;
    private final Color hilightTextColor;
    private Color currentTextColor;

    public ImageDetailsDialog(WindowWidget parent, Scene scene, ImageMap image) {
        super(parent, "Image data", true);
        this.parent = parent;
        this.scene = scene;
        this.im = image;
        LayoutInfo left = new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE, new Insets(0, 0, 0, 10), null);

        List<String> texturesUsing = scene.getTextures().stream().
                filter(texture -> texture.usesImage(im)).
                map(Texture::getName).
                collect(Collectors.toList());

        ColumnContainer fields;
        setContent(fields = new ColumnContainer());
        fields.add(imageField = new BLabel());
        FormContainer infoTable;
        fields.add(infoTable = new FormContainer(2, 7 + texturesUsing.size()));
        RowContainer buttonField;
        fields.add(buttonField = new RowContainer());

        Font boldFont = new BLabel().getFont().deriveFont(Font.BOLD);

        title = new BLabel[7];
        data = new BLabel[7 + texturesUsing.size()];

        infoTable.add(title[0] = Translate.label("imageName", ":"), 0, 0, left);
        infoTable.add(title[1] = Translate.label("imageType", ":"), 0, 1, left);
        infoTable.add(title[2] = Translate.label("imageSize", ":"), 0, 2, left);
        infoTable.add(title[3] = Translate.label("imageLink", ":"), 0, 3, left);
        infoTable.add(title[4] = Translate.label("imageCreated", ":"), 0, 4, left);
        infoTable.add(title[5] = Translate.label("imageEdited", ":"), 0, 5, left);
        infoTable.add(title[6] = Translate.label("imageUsedIn", ":"), 0, 6, left);
        for (int j = 0; j < 7; j++) {
            infoTable.add(data[j] = new BLabel(), 1, j, left);
            title[j].setFont(boldFont);
        }
        for (int q = 0; q < texturesUsing.size(); q++) {
            infoTable.add(data[q + 7] = new BLabel(texturesUsing.get(q)), 1, 6 + q, left);
        }

        imageField.getComponent().setPreferredSize(new Dimension(600, 600));
        createBackground();
        paintImage();

        buttonField.add(refreshButton = Translate.button("refreshImage", this, "refreshImage"));
        buttonField.add(reconnectButton = Translate.button("reconnectImage", "...", this, "reconnectImage"));
        buttonField.add(convertButton = Translate.button("convertImage", this, "convertToLocal"));
        buttonField.add(exportButton = Translate.button("exportImage", "...", this, "exportImage"));
        buttonField.add(Translate.button("ok", this, "closeDetailsDialog"));

        if (im instanceof ExternalImage) {
            exportButton.setEnabled(false);
        } else {
            refreshButton.setEnabled(false);
            reconnectButton.setEnabled(false);
            convertButton.setEnabled(false);
        }

        defaultTextColor = currentTextColor = title[0].getComponent().getForeground();
        hilightTextColor = new Color(0, 191, 191);
        errorTextColor = new Color(143, 0, 0);

        data[0].addEventLink(MouseClickedEvent.class, this, "nameClicked");
        data[0].addEventLink(MouseEnteredEvent.class, this, "nameEntered");
        data[0].addEventLink(MouseExitedEvent.class, this, "nameExited");
        title[0].addEventLink(MouseClickedEvent.class, this, "nameClicked");
        title[0].addEventLink(MouseEnteredEvent.class, this, "nameEntered");
        title[0].addEventLink(MouseExitedEvent.class, this, "nameExited");

        addAsListener(this);
        addEventLink(WindowClosingEvent.class, this, "closeDetailsDialog");
        setDataTexts();
        pack();
        setResizable(false);

        UIUtilities.centerDialog(this, parent);
        setVisible(true);
    }

    private void setDataTexts() {

        for (int d = 0; d < 7; d++) {
            data[d].setText(new String());
            if (im instanceof ExternalImage && !((ExternalImage) im).isConnected()) {
                currentTextColor = errorTextColor;
            } else {
                currentTextColor = defaultTextColor;
            }
            data[d].getComponent().setForeground(currentTextColor);
        }

        data[0].setText(im.getName());
        data[1].setText(im.getType());
        data[2].setText(im.getWidth() + " x " + im.getHeight());
        if (im instanceof ExternalImage) {
            data[3].setText(((ExternalImage) im).getPath());
        }
        if (!im.getUserCreated().isEmpty()) {
            data[4].setText(im.getUserCreated() + " - " + im.getDateCreated() + " - " + im.getZoneCreated());
        }
        if (!im.getUserEdited().isEmpty()) {
            data[5].setText(im.getUserEdited() + " - " + im.getDateEdited() + " - " + im.getZoneEdited());
        }
    }

    private void createBackground() {
        int midShade = 127 + 32 + 32 + 16;
        int difference = 12;
        Color bgColor1 = new Color(midShade - difference, midShade - difference, midShade - difference);
        Color bgColor2 = new Color(midShade + difference, midShade + difference, midShade + difference);
        int rgb1 = bgColor1.getRGB();
        int rgb2 = bgColor2.getRGB();

        canvasImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < 600; x++) {
            for (int y = 0; y < 600; y++) {
                if ((x % 20 < 10 && y % 20 < 10) || (x % 20 >= 10 && y % 20 >= 10)) // checkers
                {
                    canvasImage.setRGB(x, y, rgb1);
                } else {
                    canvasImage.setRGB(x, y, rgb2);
                }
            }
        }
    }

    private void paintImage() {
        try {
            Graphics2D g = canvasImage.createGraphics();
            Image image = im.getPreview(600);
            if (image == null) {
                return;
            }
            int xOffset = (600 - image.getWidth(null)) / 2;
            int yOffset = (600 - image.getHeight(null)) / 2;
            g.drawImage(image, xOffset, yOffset, null);
            imageField.setIcon(new ImageIcon(canvasImage));
        } catch (Exception e) {
            // "What could possibly go wrong?" :)
        }
    }

    private void refreshImage() {
        if (!refreshButton.isEnabled()) {
            return;
        }

        ((ExternalImage) im).refreshImage();
        createBackground();
        paintImage();
        setDataTexts();
        // The path to the referenced image does not change, so the parent is not set modified
        // whether refresh fails or not.
    }

    private void reconnectImage() {
        if (!reconnectButton.isEnabled()) {
            return;
        }

        var fc = new ImageFileChooser(Translate.text("selectImageToLink"));
        fc.setMultipleSelectionEnabled(false);
        if (!fc.showDialog(this)) {
            return;
        }
        File file = fc.getSelectedFile();

        try {
            Scene sc = null;
            if (parent instanceof EditingWindow) {
                sc = ((EditingWindow) parent).getScene();
            }
            ((ExternalImage) im).reconnectImage(file, sc);
            createBackground();
            paintImage();
            setDataTexts();
            if (parent instanceof EditingWindow) {
                ((EditingWindow) parent).setModified();
            }
        } catch (Exception e) {
            new BStandardDialog("", Translate.text("errorLoadingImage " + file.getName()), BStandardDialog.ERROR).showMessageDialog(this);
        }
    }

    private void exportImage() {
        if (!exportButton.isEnabled()) {
            return;
        }

        String ext = ".png";
        if (im instanceof SVGImage) {
            ext = ".svg";
        }
        if (im instanceof HDRImage) {
            ext = ".hdr";
        }

        var chooser = new JFileChooser();
        chooser.setName(Translate.text("exportImage"));
        
        String imageName = im.getName();
        if (imageName.isEmpty()) {
            imageName = Translate.text("unTitled");
        }
        chooser.setSelectedFile(new File(imageName + ext));
        if (chooser.showSaveDialog(this.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Make sure the file extension is correct
        String fileName = chooser.getSelectedFile().getName();
        if (!fileName.toLowerCase().endsWith(ext)) {
            fileName = fileName + ext;
        }
        File imageFile = new File(chooser.getCurrentDirectory(), fileName);

        // Check if the file already exist and the user wants to overwrite it.
        if (imageFile.isFile()) {
            String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};
            int choice = new BStandardDialog("", Translate.text("overwriteFile", fileName), BStandardDialog.QUESTION).showOptionDialog(this, options, options[1]);
            if (choice == 1) {
                return;
            }
        }

        // Write the file
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(imageFile))) {
            if (im instanceof HDRImage) {
                HDREncoder.writeImage((HDRImage) im, out);
            } else if (im instanceof SVGImage) {
                out.write(((SVGImage) im).getXML());
            } else { // MIPMappedImage
                ImageIO.write(((MIPMappedImage) im).getImage(), "png", out); // getImage returns BufferedImage
            }
        } catch (IOException ex) {
            setCursor(Cursor.getDefaultCursor());
            new BStandardDialog("", Translate.text("errorExportingImage", im.getName()), BStandardDialog.ERROR).showMessageDialog(this);
            log.atError().setCause(ex).log("Unable to export image: {} due {}", im.getName(), ex.getMessage());
        }
    }

    private void convertToLocal() {
        if (!convertButton.isEnabled()) {
            return;
        }

        String name = im.getName();
        if (name.isEmpty()) {
            name = Translate.text("unNamed");
        }

        if (confirmConvert(name)) {
            int s;
            for (s = 0; s < scene.getNumImages() && scene.getImage(s) != im; s++);

            ((ExternalImage) im).getImageMap().setName(im.getName());
            im = ((ExternalImage) im).getImageMap();
            scene.replaceImage(s, im);

            exportButton.setEnabled(true);
            refreshButton.setEnabled(false);
            reconnectButton.setEnabled(false);
            convertButton.setEnabled(false);
            setDataTexts();
        }
    }

    private boolean confirmConvert(String name) {
        String question = Translate.text("convertQuestion", name);

        BStandardDialog confirm = new BStandardDialog(Translate.text("confirmTitle"), question, BStandardDialog.QUESTION);
        String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};
        return (confirm.showOptionDialog(this, options, options[1]) == 0);
    }

    private void closeDetailsDialog() {
        dispose();
        removeAsListener(this);
    }

    /**
     * Pressing Return and Escape are equivalent to clicking OK and Cancel.
     */
    private void keyPressed(KeyPressedEvent ev) {
        int code = ev.getKeyCode();
        if (code == KeyPressedEvent.VK_ESCAPE) {
            closeDetailsDialog();
        }
    }

    /**
     * Add this as a listener to every Widget.
     */
    private void addAsListener(Widget w) {
        w.addEventLink(KeyPressedEvent.class, this, "keyPressed");
        if (w instanceof WidgetContainer) {
            ((WidgetContainer) w).getChildren().forEach(this::addAsListener);
        }
    }

    /**
     * Remove this as a listener before returning.
     */
    private void removeAsListener(Widget w) {
        w.removeEventLink(KeyPressedEvent.class, this);
        if (w instanceof WidgetContainer) {
            ((WidgetContainer) w).getChildren().forEach(this::removeAsListener);
        }
    }

    private void nameEntered() {
        title[0].getComponent().setForeground(hilightTextColor);
        data[0].getComponent().setForeground(hilightTextColor);
    }

    private void nameExited() {
        title[0].getComponent().setForeground(defaultTextColor);
        data[0].getComponent().setForeground(currentTextColor);
    }

    private void nameClicked() {
        new ImageNameEditor(im, this);
    }

    /**
     * Dialog for setting the name of the image
     */
    private class ImageNameEditor extends BDialog {

        private final BTextField nameField;
        private BCheckBox autoBox;
        private String autoText, userText;
        private boolean automatic = false;

        private ImageNameEditor(ImageMap im, WindowWidget parent) {
            setTitle(Translate.text("nameDialogTitle"));
            ColumnContainer content = new ColumnContainer();
            RowContainer buttons = new RowContainer();
            setContent(content);
            content.add(nameField = new BTextField(im.getName()));
            autoText = userText = im.getName();

            if (im instanceof ExternalImage) {
                try {
                    String fileName = im.getFile().getName();
                    autoText = fileName.substring(0, fileName.lastIndexOf('.'));
                } catch (Exception e) {
                    // Just display the saved name
                }
                automatic = ((ExternalImage) im).isNameAutomatic();
                content.add(autoBox = new BCheckBox(Translate.text("Automatic"), automatic));
                autoBox.addEventLink(ValueChangedEvent.class, this, "autoChanged");
                autoChanged();
            }
            nameField.setColumns(50);
            nameField.addEventLink(ValueChangedEvent.class, this, "textChanged");
            content.add(buttons);

            buttons.add(Translate.button("ok", this, "okNameEditor"));

            buttons.add(Translate.button("cancel", this, "cancelNameEditor"));
            addEventLink(WindowClosingEvent.class, this, "cancelNameEditor");
            addAsListener(this);
            layoutChildren();
            pack();
            setResizable(false);
            setModal(true); // I wonder, why this dialog requires setModal() and the other don't.

            Rectangle pb = parent.getBounds();
            Rectangle tb = getBounds();
            getComponent().setLocation(pb.x + (pb.width - tb.width) / 2, pb.y + (625 - tb.height));

            setVisible(true);
        }

        private void textChanged() {
            if (!automatic) {
                userText = nameField.getText();
            }
        }

        private void autoChanged() {
            automatic = autoBox.getState();
            nameField.setEnabled(!automatic);
            if (automatic) {
                nameField.setText(autoText);
            } else {
                nameField.setText(userText);
            }
        }

        private void cancelNameEditor() {
            dispose();
            removeAsListener(this);
        }

        private void okNameEditor() {
            if (automatic) {
                im.setName(autoText);
            } else {
                im.setName(userText);
            }
            if (im instanceof ExternalImage) {
                ((ExternalImage) im).setNameAutomatic(automatic);
            }
            im.setDataEdited();
            setDataTexts();
            dispose();
            removeAsListener(this);
        }

        /**
         * Pressing Return and Escape are equivalent to clicking OK and Cancel.
         */
        private void keyPressed(KeyPressedEvent ev) {
            int code = ev.getKeyCode();
            if (code == KeyPressedEvent.VK_ESCAPE) {
                cancelNameEditor();
            }
            if (code == KeyPressedEvent.VK_ENTER) {
                okNameEditor();
            }
        }

        /**
         * Add this as a listener to every Widget.
         */
        private void addAsListener(Widget w) {
            w.addEventLink(KeyPressedEvent.class, this, "keyPressed");
            if (w instanceof WidgetContainer) {
                ((WidgetContainer) w).getChildren().forEach(this::addAsListener);
            }
        }

        /**
         * Remove this as a listener before returning.
         */
        private void removeAsListener(Widget w) {
            w.removeEventLink(KeyPressedEvent.class, this);
            if (w instanceof WidgetContainer) {
                ((WidgetContainer) w).getChildren().forEach(this::removeAsListener);
            }
        }
    }
}
