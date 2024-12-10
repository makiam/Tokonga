package artofillusion.material;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.procedural.Procedure;
import artofillusion.procedural.ProcedureOwner;
import lombok.Getter;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class ExperimentalProcedureEditorWindow extends JFrame {

    public ExperimentalProcedureEditorWindow() {
        super();
    }

    public ExperimentalProcedureEditorWindow(Procedure proc, ProcedureOwner owner, Scene scene) {
        this();
        this.procedure = proc;
    }

    @Getter
    private Procedure procedure;

    @Override
    protected void frameInit() {
        super.frameInit();

        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.setSize(1280,1024);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
        this.setJMenuBar(new JMenuBar());

    }
}
