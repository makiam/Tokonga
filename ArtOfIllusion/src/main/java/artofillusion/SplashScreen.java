/* Copyright (C) 2021 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author MaksK
 */
@Slf4j
public class SplashScreen extends JDialog implements PropertyChangeListener {

    private final Runtime runtime = Runtime.getRuntime();
    private final ResourceBundle bundle = ResourceBundle.getBundle("artofillusion.splash.Bundle");

    private final String vendor = String.format(bundle.getString("splash.java"), System.getProperty("java.version"), System.getProperty("java.vendor"));
    private final String os = String.format(bundle.getString("splash.system"), System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));

    private long used = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
    private long allocated = runtime.totalMemory() / 1048576L;
    private long max = runtime.maxMemory() / 1048576L;

    private String memory = String.format(bundle.getString("splash.memory"), used, allocated, max);
    private String cpu = String.format(bundle.getString("splash.cpu"), runtime.availableProcessors());

    private static final Border border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 1), BorderFactory.createEmptyBorder(0, 0, 5, 0));
    private JLabel label;

    public SplashScreen() {
        super();
        String text = "<html><div align=\"center\">"
                + "Art of Illusion v" + ArtOfIllusion.getVersion()
                + "<br>Build: " + ArtOfIllusion.getBuildInfo()
                + "<br>Copyright 1999-2020 by Peter Eastman and others"
                + "<br>(See the README file for details.)"
                + "<br>This program may be freely distributed under"
                + "<br>the terms of the accompanying license.</div></html>";
        label.setText(text);
        this.pack();
        this.setLocationRelativeTo(null);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", this);
    }

    public SplashScreen(Frame owner) {
        super(owner, true);
        String text = "<div align=\"center\">"
                + "Art of Illusion v" + ArtOfIllusion.getVersion()
                + "<br>Build: " + ArtOfIllusion.getBuildInfo()
                + "<br>Copyright 1999-2020 by Peter Eastman and others"
                + "<br>(See the README file for details.)"
                + "<br>This program may be freely distributed under"
                + "<br>the terms of the accompanying license.</div>";

        String glav = ViewerCanvas.isOpenGLAvailable() ? bundle.getString("available") : bundle.getString("unavailable");
        String glen = ArtOfIllusion.getPreferences().getUseOpenGL() ? bundle.getString("enabled") : bundle.getString("disabled");

        String opengl = String.format(bundle.getString("splash.opengl"), glav, glen);
        text = text + vendor + os + memory + cpu + opengl;
        text = "<html>" + text + "</html>";
        label.setText(text);

        setDisposable();

        this.pack();
        this.setLocationRelativeTo(owner);

    }


    @Override
    protected void dialogInit() {
        super.dialogInit(); //To change body of generated methods, choose Tools | Templates.
        this.setModalityType(ModalityType.DOCUMENT_MODAL);
        this.setUndecorated(true);

        int imageNumber = new Random(System.currentTimeMillis()).nextInt(8);
        ImageIcon image = new ImageIcon(getClass().getResource("/artofillusion/titleImages/titleImage" + imageNumber + ".jpg"));

        label = new JLabel("", image, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setFont(UIManager.getFont("TextField.font"));
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setBorder(border);

        Color background = Color.white;
        if (imageNumber == 4) {
            background = new Color(204, 204, 255);
        } else if (imageNumber == 6) {
            background = new Color(232, 255, 232);
        }

        label.setBackground(background);

        this.getContentPane().add(label);

        this.pack();
    }

    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("activeWindow", this);
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Object value = event.getNewValue();
        log.info("Active window changed from: {} to {}", event.getOldValue(), value);
        if (value instanceof Dialog && value != SplashScreen.this) {
            setVisible(false);
        } else if (!isVisible()) {
            setVisible(true);
        }
    }

    public final void setDisposable() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                SplashScreen.this.dispose();
            }

        };
        this.addMouseListener(ma);
    }

}
