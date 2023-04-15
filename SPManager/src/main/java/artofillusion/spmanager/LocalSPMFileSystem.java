/* Copyright 2004 Francois Guillet
 *  Changes copyright 2022 by Maksim Khramov
   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. 
*/

package artofillusion.spmanager;

import java.io.*;
import java.util.*;

public class LocalSPMFileSystem extends SPMFileSystem
{   
    public LocalSPMFileSystem()
    {
        super();
    }
    
    @Override
    public void initialize()
    {
        super.initialize();
        scanPlugins();
        scanToolScripts();
        scanObjectScripts();
        scanStartupScripts();
        initialized = true;
    }
    
    
    private void scanPlugins()
    {
        scanFiles(SPManagerPlugin.PLUGIN_DIRECTORY, pluginsInfo, ".jar");
    }
    
    private void scanToolScripts()
    {
        scanFiles(SPManagerPlugin.TOOL_SCRIPT_DIRECTORY, toolInfo, ".bsh");
    }
    
    private void scanObjectScripts()
    {
        scanFiles(SPManagerPlugin.OBJECT_SCRIPT_DIRECTORY, objectInfo, ".bsh");
    }
    
    private void scanStartupScripts()
    {
        scanFiles(SPManagerPlugin.STARTUP_SCRIPT_DIRECTORY, startupInfo, ".bsh");
    }
    
    private void scanFiles(String directory, List<SPMObjectInfo> infoVector, String suffix)
    {
        
        File dir = new File(directory);
        if (dir.exists())
        {
            String[] files = dir.list();
            if (files.length > 0) Arrays.sort(files);
            for (String file : files)
            {
              if (file.endsWith(suffix))
              {
                infoVector.add(new SPMObjectInfo(directory + File.separatorChar + file));
              }
            }
        }
    }

}
