/* Copyright (C) 2022 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.test.util;

import artofillusion.math.RGBColor;
import artofillusion.ui.Translate;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JSliderOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 *
 * @author MaksK
 */
public class ColorDialogOperator extends JDialogOperator {

    private final JButtonOperator okButton;
    private final JButtonOperator cancelButton;
    
    public ColorDialogOperator(WindowOperator owner, String title) {
        super(owner, title);
        okButton = new JButtonOperator(this, Translate.text("ok"));
        cancelButton = new JButtonOperator(this, Translate.text("cancel"));
    }

    public ColorDialogOperator(String title) {
        super(title);
        okButton = new JButtonOperator(this, Translate.text("ok"));
        cancelButton = new JButtonOperator(this, Translate.text("cancel"));
    }
    
    public void commit() {
        okButton.clickMouse();
    }
    
    public void cancel() {
        cancelButton.clickMouse();
    }

    public void setColor(RGBColor rgbColor) {  
        
        new JSliderOperator(this, 0).scrollToValue(Float.valueOf(rgbColor.getRed()*100).intValue());
        new JSliderOperator(this, 1).scrollToValue(Float.valueOf(rgbColor.getGreen()*100).intValue());
        new JSliderOperator(this, 2).scrollToValue(Float.valueOf(rgbColor.getBlue()*100).intValue());
    }
}
