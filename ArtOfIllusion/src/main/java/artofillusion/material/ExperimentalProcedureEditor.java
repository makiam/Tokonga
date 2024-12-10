package artofillusion.material;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.procedural.Procedure;
import buoy.widget.BFrame;


import javax.swing.*;
import java.awt.*;

public class ExperimentalProcedureEditor extends BFrame {

    public ExperimentalProcedureEditor(Procedure proc, ProceduralMaterial3D owner, Scene scene) {
        super();
        this.getComponent().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getComponent().setTitle(owner.getWindowTitle());
        this.getComponent().setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.getComponent().getContentPane().setLayout(new BorderLayout());
        this.getComponent().getContentPane().add(new JSplitPane(), java.awt.BorderLayout.CENTER);
        this.setVisible(true);

    }

    @Override
    protected JFrame createComponent() {
        return new ExperimentalProcedureEditorWindow();
    }
}
