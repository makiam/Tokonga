/* Copyright (C) 2018 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
package artofillusion.ui;

import artofillusion.test.util.RegisterTestResources;
import buoy.widget.BDialog;
import buoy.widget.BStandardDialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.junit.Assert;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author maksim.khramov
 */
public class MessagesTest {

    private static final Logger logger = Logger.getLogger(MessagesTest.class.getName());

    @ClassRule
    public static final RegisterTestResources res = new RegisterTestResources();
    
    @BeforeClass
    public static void setupClass() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.INFO, "Exception at test setup", ex);
        }
    }

    @Test
    public void testError() {
        SwingUtilities.invokeLater(() -> {
            Messages.error("Hello Errors");
        });
        JDialogOperator dialog = new JDialogOperator();
        JButtonOperator ok = new JButtonOperator(dialog);
        ok.clickMouse();
    }

    @Test
    public void testWarning() {
        SwingUtilities.invokeLater(() -> {
            Messages.warning("This is warning");
        });
        
        JDialogOperator dialog = new JDialogOperator();
        JButtonOperator ok = new JButtonOperator(dialog);
        ok.clickMouse();
    }

    @Test
    public void testInformation() {
        SwingUtilities.invokeLater(() -> {
            Messages.information("Information message");
        });
        
        JDialogOperator dialog = new JDialogOperator();
        JButtonOperator ok = new JButtonOperator(dialog);
        ok.clickMouse();
    }

    @Test
    public void testPlain() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Plain message", "", JOptionPane.PLAIN_MESSAGE);
        });
        
        JDialogOperator dialog = new JDialogOperator();
        JButtonOperator ok = new JButtonOperator(dialog);
        ok.clickMouse();
    }

    @Test
    public void testPlainStyleDialog() {
        SwingUtilities.invokeLater(() -> {
            JDialogOperator dialog = new JDialogOperator();
            JButtonOperator ok = new JButtonOperator(dialog, 1);
            ok.clickMouse();
        });
        
        BStandardDialog dlg = new BStandardDialog("Art Of Illusion", Translate.text("savePoseAsGesture"), BStandardDialog.PLAIN);
        String name = dlg.showInputDialog(new BDialog(), null, "New Gesture");
        Assert.assertNull(name);
    }

    @Test
    public void testMessageString() {
        String name = "namme";
        String question = Translate.text("deleteSelectedImage", name);
        System.out.println(question);
    }
}
