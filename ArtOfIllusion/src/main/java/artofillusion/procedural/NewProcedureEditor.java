package artofillusion.procedural;

import artofillusion.ArtOfIllusion;
import artofillusion.MaterialPreviewer;
import artofillusion.Scene;

import artofillusion.ui.Translate;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Optional;


@Slf4j
public final class NewProcedureEditor extends JFrame {

    private MaterialPreviewer preview;

    public NewProcedureEditor(Procedure proc, ProcedureOwner owner, Scene sc) {
        super();
        this.setTitle(owner.getWindowTitle());
        preview = owner.getPreview();

        Optional.ofNullable(preview).ifPresent(materialPreviewer -> createPreview(this, materialPreviewer));
    }

    private void createPreview(final Frame owner, final MaterialPreviewer preview) {
        SwingUtilities.invokeLater(() -> new PreviewDialog(owner, preview));
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

    private static class PreviewDialog extends JDialog {


        public PreviewDialog(final Frame owner, final MaterialPreviewer preview) {
            super(owner, "Preview");
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.getContentPane().add(preview.getComponent());
            owner.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {onParentMoved(); }
                @Override
                public void componentMoved(ComponentEvent e) { onParentMoved();}
            });
            pack();
            this.onParentMoved();
            setVisible(true);
        }
        private void onParentMoved() {
            Rectangle parentBounds = this.getParent().getBounds();
            Rectangle location = this.getBounds();
            location.y = parentBounds.y;
            location.x = parentBounds.x + parentBounds.width;
            this.setBounds(location);
        }

        @Override
        protected void dialogInit() {
            super.dialogInit();
        }
    }
}
