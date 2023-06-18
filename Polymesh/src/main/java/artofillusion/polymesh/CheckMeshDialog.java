/* Copyright (C) 2001-2004 by Peter Eastman, 2005 by Francois Guillet
   Changes copyright (C) 2023 Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.polymesh;

import artofillusion.ui.Translate;
import artofillusion.ui.UIUtilities;
import buoy.event.CommandEvent;
import buoy.widget.BButton;
import buoy.widget.BDialog;
import buoy.widget.BTextArea;
import buoy.widget.BorderContainer;
import buoy.xml.WidgetDecoder;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * A dialog which show the result of check/repair operation
 *
 * @author Francois Guillet
 */
@Slf4j
public class CheckMeshDialog extends BDialog {

    /**
     * Constructor for the CheckMeshDialog object
     */
    public CheckMeshDialog(final PolyMeshEditorWindow owner) {
        super(owner, Translate.text("polymesh:checkRepair"), true);
        BorderContainer borderContainer = null;
        BButton dismiss = null;
        BTextArea textArea = null;
        try (InputStream is = getClass().getResource("interfaces/check.xml").openStream()) {
            WidgetDecoder decoder = new WidgetDecoder(is);
            borderContainer = (BorderContainer) decoder.getRootObject();
            textArea = ((BTextArea) decoder.getObject("TextArea"));
            dismiss = ((BButton) decoder.getObject("dismiss"));
            dismiss.setText(Translate.text("polymesh:dismiss"));
        } catch (IOException ex) {
            log.atError().setCause(ex).log("Error creating CheckMeshDialog due {}", ex.getLocalizedMessage());
        }
        setContent(borderContainer);
        dismiss.addEventLink(CommandEvent.class, this, "doDismiss");
        pack();
        UIUtilities.centerWindow(this);
        PolyMesh mesh = (PolyMesh) owner.getObject().getObject();
        textArea.append(mesh.checkMesh());
    }

    /**
     * Description of the Method
     */
    private void doDismiss() {
        dispose();
    }

}
