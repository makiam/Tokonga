/* Copyright (C) 2017 by Petri Ihalainen
   Some methods copyright (C) by Peter Eastman
   Changes copyright 2019-2024 by Maksim Khramov

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
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.*;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.greenrobot.eventbus.Subscribe;

@Slf4j
public class ImageDetailsDialog extends BDialog {

    private static final Color errorTextColor = new Color(143, 0, 0);
    private static final Color hilightTextColor = new Color(0, 191, 191);

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

    private Color currentTextColor;

    public ImageDetailsDialog(WindowWidget parent, Scene scene, ImageMap image) {
        super(parent, "Image data", true);
        org.greenrobot.eventbus.EventBus.getDefault().register(this);
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

        buttonField.add(refreshButton = Translate.button("refreshImage", event -> refreshImage()));
        buttonField.add(reconnectButton = Translate.button("reconnectImage", "...", event -> reconnectImage()));
        buttonField.add(convertButton = Translate.button("convertImage", event -> convertToLocal()));
        buttonField.add(exportButton = Translate.button("exportImage", "...", event -> exportImage()));

        buttonField.add(Translate.button("ok", event -> closeDetailsDialog()));

        if (im instanceof ExternalImage) {
            exportButton.setEnabled(false);
        } else {
            refreshButton.setEnabled(false);
            reconnectButton.setEnabled(false);
            convertButton.setEnabled(false);
        }

        defaultTextColor = currentTextColor = title[0].getComponent().getForeground();

        data[0].addEventLink(MouseClickedEvent.class, this, "nameClicked");
        data[0].addEventLink(MouseEnteredEvent.class, this, "nameEntered");
        data[0].addEventLink(MouseExitedEvent.class, this, "nameExited");

        title[0].addEventLink(MouseClickedEvent.class, this, "nameClicked");
        title[0].addEventLink(MouseEnteredEvent.class, this, "nameEntered");
        title[0].addEventLink(MouseExitedEvent.class, this, "nameExited");

        this.getComponent().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDetailsDialog();
            }
        });

        // Close the dialog when Esc is pressed
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        ActionListener action = e -> closeDetailsDialog();
        this.getComponent().getRootPane().registerKeyboardAction(action, escape, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setDataTexts();
        pack();
        setResizable(false);

        UIUtilities.centerDialog(this, parent);
        setVisible(true);
    }

    private void setDataTexts() {

        for (int d = 0; d < 7; d++) {
            data[d].setText("");
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
            MessageDialog.create().withOwner(this.getComponent()).error(Translate.text("errorLoadingImage", file.getName()));
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
            String caption = "";
            String prompt = Translate.text("overwriteFile", fileName);
            String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};
            var result = JOptionPane.showOptionDialog(this.getComponent(), prompt, caption, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (result == 1) {
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
            MessageDialog.create().withOwner(this.getComponent()).error(Translate.text("errorExportingImage", im.getName()));
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

        if (confirm(name)) {

            int imageIndex = scene.indexOf(im);

            ((ExternalImage) im).getImageMap().setName(im.getName());
            im = ((ExternalImage) im).getImageMap();
            scene.replaceImage(imageIndex, im);

            exportButton.setEnabled(true);
            refreshButton.setEnabled(false);
            reconnectButton.setEnabled(false);
            convertButton.setEnabled(false);
            setDataTexts();
        }
    }

    private boolean confirm(String name) {
        String caption = Translate.text("confirmTitle");
        String prompt = Translate.text("convertQuestion", name);
        String[] options = new String[]{Translate.text("Yes"), Translate.text("No")};

        var result = JOptionPane.showOptionDialog(this.getComponent(), prompt, caption, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        return result == 0;
    }

    private void closeDetailsDialog() {
        dispose();
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
        SwingUtilities.invokeLater(() -> new  artofillusion.image.ui.ImageNameEditor(this, im).setVisible(true));
    }

    @Subscribe
    public void onImageUpdated(ImageNameChangeEvent event) {
        im.setDataEdited();
        setDataTexts();
    }

    public static class ImageNameChangeEvent {
    }
}
