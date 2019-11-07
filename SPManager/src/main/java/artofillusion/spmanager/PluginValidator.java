/* 
   Copyright (C) 2019 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. 
 */

package artofillusion.spmanager;

import artofillusion.ui.Messages;
import com.google.common.flogger.FluentLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 *
 * @author maksim.khramov
 */
public final class PluginValidator implements Runnable {
    
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    
    private Path path;
    
    private PluginValidator() {        
    }
    
    public PluginValidator(Path path) {
        this.path = path;
    }
    
    
    @Override
    public void run() {
        final List<String> errors = new ArrayList<>();
        
        Consumer<Path> validatePath = new Consumer<Path>() {
            @Override
            public void accept(Path path) {
                System.out.println("Look at: " + path);
                
            }
            
        };
        
        try(Stream<Path> walk = Files.list(path)){
            walk.forEach(validatePath);
        } catch(IOException ioex) {
            logger.atSevere().withCause(ioex).log("Error list plugins");
        }
        
        if(errors.isEmpty()) return;
        String result = String.join("\n", errors);
        Messages.error(result, null);
    }
    
    private void validatePlugin(Path path) {
        SPMObjectInfo soi = new SPMObjectInfo();
        
    }
    
}
