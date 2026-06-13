package artofillusion.translators;

import artofillusion.Scene;
import artofillusion.Translator;
import buoy.widget.BFrame;

public class PLYTranslator implements Translator {
    /**
     * Get the name of the file format which this translator imports or exports.
     */
    @Override
    public String getName() {
        return "Stanford PLY";
    }

    /**
     * Specify whether this translator can import files.
     */
    @Override
    public boolean canImport() {
        return true;
    }

    /**
     * Prompt the user to select a file, read it, and create a new LayoutWindow containing
     * the imported scene. parent is the Frame which should be used as the parent for
     * dialog boxes. If canImport() returns false, this method will never be called.
     *
     * @param parent
     */
    @Override
    public void importFile(BFrame parent) {
        PLYImporter.importFile(parent);
    }

    /**
     * Prompt the user for a filename and any other necessary information, and export the
     * scene. parent is the Frame which should be used as the parent for dialog boxes.
     * The user should be given the option of only exporting the objects which are
     * currently selected. If canExport() returns false, this method will never be called.
     *
     * @param parent
     * @param theScene
     */
    @Override
    public void exportFile(BFrame parent, Scene theScene) {
        //Operation not yet supported
    }

    /**
     * Specify whether this translator can export files.
     */
    @Override
    public boolean canExport() {
        return false;
    }
}
