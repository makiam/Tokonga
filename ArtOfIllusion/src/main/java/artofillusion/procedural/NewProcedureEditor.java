package artofillusion.procedural;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;

import artofillusion.ui.Translate;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;

@Slf4j
public final class NewProcedureEditor extends JFrame {

    private JMenuBar bar;

    public NewProcedureEditor(Procedure proc, ProcedureOwner owner, Scene sc) {
        super();
        this.setTitle(owner.getWindowTitle());
    }

    @Override
    protected void frameInit() {
        super.frameInit();
        this.setIconImage(ArtOfIllusion.APP_ICON.getImage());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(1280, 1024);
        this.setJMenuBar(bar = new JMenuBar());
        bar.add(new JMenu(Translate.text("menu.edit")));

        this.setVisible(true);
        var split = new JSplitPane();
        this.getContentPane().add(split);
        split.setRightComponent(new ProcedureView());
    }
}
