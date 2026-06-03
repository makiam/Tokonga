package artofillusion.translators;

import artofillusion.ArtOfIllusion;
import artofillusion.Scene;
import artofillusion.ui.Translate;
import buoy.widget.BFrame;
import buoy.widget.BStandardDialog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Optional;

@Slf4j
public class PLYImporter {


    public static void importFile(@NotNull BFrame parent)  {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogTitle(Translate.text("Translators:importOBJ"));
        Optional.ofNullable(ArtOfIllusion.getCurrentDirectory()).ifPresent(dir -> jfc.setCurrentDirectory(new File(dir)));

        FileNameExtensionFilter objFilter = new FileNameExtensionFilter(Translate.text("Translators:fileFilter.ply"), "ply");
        jfc.addChoosableFileFilter(objFilter);
        jfc.setAcceptAllFileFilterUsed(true);
        jfc.setFileFilter(objFilter);
        if (jfc.showOpenDialog(parent.getComponent()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        ArtOfIllusion.setCurrentDirectory(jfc.getCurrentDirectory().getAbsolutePath());
        try {
            Scene scene = importFile(jfc.getSelectedFile());
            scene.setName(jfc.getSelectedFile().getName());
            ArtOfIllusion.newWindow(scene);
        } catch (Exception ex) {
            new BStandardDialog("", new String[]{Translate.text("errorLoadingFile"), ex.getMessage() == null ? "" : ex.getMessage()}, BStandardDialog.ERROR).showMessageDialog(parent);
        }
    }

    private static Scene importFile(File f) throws Exception {
        return new Scene();
    }
}
