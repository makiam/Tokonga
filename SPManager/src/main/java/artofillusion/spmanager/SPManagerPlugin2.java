/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package artofillusion.spmanager;

import artofillusion.DefaultPluginImplementation;
import artofillusion.LayoutWindow;
import buoy.event.CommandEvent;
import buoy.widget.BMenu;
import buoy.widget.BMenuItem;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;

/**
 *
 * @author maksim.khramov
 */
public class SPManagerPlugin2 extends DefaultPluginImplementation {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
            
    @Override
    protected void onSceneWindowCreated(LayoutWindow view) {
        BMenu toolsMenu = view.getToolsMenu();
        toolsMenu.addSeparator();
        BMenuItem menuItem = SPMTranslate.bMenuItem("SPManager", CommandEvent.class, this, "showPluginsDialog");
        menuItem.getComponent().putClientProperty("layout", view);
        toolsMenu.add(menuItem);
    }

    @Override
    protected void onApplicationStopping() {
        
    }

    @Override
    protected void onApplicationStarting() {
        Path pluginsPath = Paths.get(artofillusion.ArtOfIllusion.PLUGIN_DIRECTORY);
        
        if(Files.notExists(pluginsPath)) {
            logger.atInfo().log("Plugins folder is missing");
            return;
        }
        
        final List<String> errors = new ArrayList<>();
        
        Consumer<Path> validatePath = new Consumer<Path>() {
            @Override
            public void accept(Path path) {
                System.out.println("Look at: " + path);
            }
            
        };
        
        try(Stream<Path> walk = Files.list(pluginsPath)){
            walk.forEach(validatePath);
        } catch(IOException ioex) {
            logger.atSevere().withCause(ioex).log("Error list plugins");
        }

        
    }

    private void validatePlugin(Path path) {
        SPMObjectInfo soi = new SPMObjectInfo();
        
    }
    
    
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void showPluginsDialog(CommandEvent event) {
        BMenuItem mi = (BMenuItem) event.getWidget();
        final LayoutWindow view = (LayoutWindow) mi.getComponent().getClientProperty("layout");
        final SPManagerPlugin2 plugin = this;
        
        SwingUtilities.invokeLater(() -> {
            new SPManagerDialog(view.getComponent(), plugin).setVisible(true);
        });
    }
}
