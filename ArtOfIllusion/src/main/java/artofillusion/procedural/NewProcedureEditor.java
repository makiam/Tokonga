package artofillusion.procedural;

import artofillusion.ArtOfIllusion;
import artofillusion.MaterialPreviewer;
import artofillusion.Scene;

import artofillusion.ui.Translate;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

import static artofillusion.procedural.ProcedureEditor.createPreview;

@Slf4j
public final class NewProcedureEditor extends JFrame {

    private MaterialPreviewer preview;
    public NewProcedureEditor(Procedure proc, ProcedureOwner owner, Scene sc) {
        super();
        this.setTitle(owner.getWindowTitle());
        preview = owner.getPreview();
        
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(1280, 1024);
        this.setJMenuBar(new JMenuBar());
        JMenu edit;
        this.getJMenuBar().add(edit = new JMenu(Translate.text("menu.edit")));
        edit.add(new JMenuItem(Translate.text("menu.undo"))).addActionListener(this::undoAction);
        edit.add(new JMenuItem(Translate.text("menu.redo"))).addActionListener(this::redoAction);
        this.setVisible(true);
        var split = new JSplitPane();
        this.getContentPane().add(split);
        split.setLeftComponent(new ModulesMenu());
        split.setRightComponent(new ProcedureView());
    }

    private void redoAction(ActionEvent actionEvent) {
    }

    private void undoAction(ActionEvent actionEvent) {
    }
}
