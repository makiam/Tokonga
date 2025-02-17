/*
 *  Copyright 2004 Francois Guillet
 *  Changes copyright 2022-2024 by Maksim Khramov
 *  This program is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */
package artofillusion.spmanager;

import artofillusion.*;
import artofillusion.ui.*;
import artofillusion.util.SearchlistClassLoader;
import buoy.event.*;
import buoy.widget.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * The Plugin corresponding to the SPManager
 *
 * @author Francois Guillet
 */
@Slf4j
public class SPManagerPlugin implements Plugin {

    public static final String AOI_VERSION = ArtOfIllusion.getMajorVersion();
    public static String TEMP_DIR;
    public static String APP_DIRECTORY;
    public static String PLUGIN_DIRECTORY;
    public static String TOOL_SCRIPT_DIRECTORY;
    public static String OBJECT_SCRIPT_DIRECTORY;
    public static String STARTUP_SCRIPT_DIRECTORY;

    /**
     * Gets the frame attribute of the SPManagerPlugin class
     *
     * @return The frame value
     */
    @Getter
    private static SPManagerFrame spmFrame;

    @Override
    public void onSceneWindowCreated(LayoutWindow view) {
        BMenu toolsMenu = view.getToolsMenu();
        toolsMenu.addSeparator();
        BMenuItem menuItem = Translate.menuItem("spmanager:SPManager", event -> doMenu());

        toolsMenu.add(menuItem);
    }

    @Override
    public void onApplicationStarting() {
        SPMTranslate.setLocale(ArtOfIllusion.getPreferences().getLocale());

        APP_DIRECTORY = ArtOfIllusion.APP_DIRECTORY;
        PLUGIN_DIRECTORY = ArtOfIllusion.PLUGIN_DIRECTORY;
        TOOL_SCRIPT_DIRECTORY = ArtOfIllusion.TOOL_SCRIPT_DIRECTORY;
        OBJECT_SCRIPT_DIRECTORY = ArtOfIllusion.OBJECT_SCRIPT_DIRECTORY;
        STARTUP_SCRIPT_DIRECTORY = ArtOfIllusion.STARTUP_SCRIPT_DIRECTORY;
    }

    /**
     * Description of the Method
     *
     * @param message Description of the Parameter
     * @param args Description of the Parameter
     */
    @Override
    public void processMessage(int message, Object... args) {

        switch (message) {
            case Plugin.APPLICATION_STARTING:
                log.atInfo().log("SPManager starting...");
                onApplicationStarting();

                // get details of plugin classloaders
                URL[] urlList;

                ClassLoader ldr = null;
                URLClassLoader urlldr = null;
                SearchlistClassLoader searchldr = null;
                Object obj;

                Map<URL, ClassLoader> loaders = new HashMap<>();
                for (ClassLoader loader : PluginRegistry.getPluginClassLoaders()) {
                    obj = loader;
                    if (obj instanceof URLClassLoader) {
                        urlList = ((URLClassLoader) obj).getURLs();
                    } else {
                        urlList = ((SearchlistClassLoader) obj).getURLs();
                    }

                    if (urlList.length > 0) {
                        loaders.put(urlList[0], (ClassLoader) obj);
                    }
                }

                Method addUrl = null;

                try {
                    addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addUrl.setAccessible(true);
                } catch (NoSuchMethodException | SecurityException e) {
                    log.atError().setCause(e).log("Error getting addURL method: {}", e.getMessage());
                }

                // get details of all local plugins
                StringBuffer errs = null;

                File urlfile;
                URL url;

                File plugdir = new File(PLUGIN_DIRECTORY);
                if (plugdir.exists()) {

                    for (File file : plugdir.listFiles()) {
                        SPMObjectInfo info = new SPMObjectInfo(file.getAbsolutePath());

                        if (info.invalid) {
                            if (errs == null) {
                                errs = new StringBuffer(1024);
                            }
                            if (errs.length() > 0) {
                                errs.append('\n');
                            }
                            errs.append(SPMTranslate.text("pluginFailure", info.getName()));
                        }

                        if (info.actions != null && info.actions.size() > 0) {

                            try {
                                url = file.toURI().toURL();
                            } catch (MalformedURLException e) {
                                continue;
                            }

                            // get the classloader for the current plugin
                            obj = loaders.get(url);

                            if (obj == null) {
                                log.atInfo().log("SPManager: could not find classloader: {}", file.getPath());
                                continue;
                            }

                            // cast or convert it to a SearchlistClassLoader
                            if (obj instanceof SearchlistClassLoader) {
                                searchldr = (SearchlistClassLoader) obj;
                            } else {
                                urlldr = (URLClassLoader) obj;

                            }

                            // ok, now perform the actions
                            for (Map.Entry<String, String> entry : info.actions.entrySet()) {
                                String value = entry.getValue();
                                String[] key = entry.getKey().split(":");

                                try {
                                    if (key[0].startsWith("/")) {
                                        urlfile = new File(APP_DIRECTORY, key[0].substring(1));
                                    } else {
                                        urlfile = new File(plugdir, key[0]);
                                    }

                                    url = urlfile.toURI().toURL();
                                } catch (MalformedURLException e) {
                                    log.atError().setCause(e).log("Error making url: {}", e.getMessage());
                                    continue;
                                }

                                log.atInfo().log("SPM: adding path: {}", url);

                                if ("classpath".equalsIgnoreCase(value)) {
                                    if (searchldr != null) {
                                        searchldr.add(url);
                                    } else if (addUrl != null) {
                                        try {
                                            addUrl.invoke(urlldr, url);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            log.atError().setCause(e).log("Error invoking: {}", e.getMessage());
                                        }
                                    } else {
                                        log.error("Could not add path {}", url);
                                    }
                                } else if ("import".equalsIgnoreCase(value)) {
                                    ldr = loaders.get(url);

                                    if (key.length == 1) {
                                        if (obj == null) {
                                            log.error("SPM: could not find loader for: {}", url);
                                        } else {
                                            searchldr.add(ldr);
                                        }
                                    }

                                }
                            }
                        }
                    }

                    if (errs != null) {
                        JTextArea txt = new JTextArea();
                        txt.setColumns(45);
                        txt.setEditable(true);
                        txt.setRows(5);
                        txt.setWrapStyleWord(true);
                        txt.setText(errs.toString());

                        JScrollPane detail = new JScrollPane(txt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                        var messageLabel = new JLabel(Translate.text("loadError"));

                        MessageDialog.create().withTitle("SPManager initialise").error(new JComponent[]{messageLabel, detail});
                    }
                } else {
                    log.error("SPManager: could not find plugin dir: {}", PLUGIN_DIRECTORY);
                }

                init();
                break;

            case Plugin.SCENE_WINDOW_CREATED: {
                onSceneWindowCreated((LayoutWindow) args[0]);
            }
            break;

            default:
            //Just ignore the message
        }
    }

    /**
     * initialise the plugin
     */
    public void init() {

        List<String> errors = new ArrayList<>();

        log.info("SPManager: java temp dir is {}", System.getProperty("java.io.tmpdir"));

        // try system TEMP directory
        File temp = new File(System.getProperty("java.io.tmpdir"));

        // try 'temp' in AOI installation directory
        if (!((temp.exists() && temp.isDirectory()) || temp.mkdir())) {
            log.atError().log("SPManager: could not open/create temp dir: {}", temp.getAbsolutePath());
            temp = new File(APP_DIRECTORY, "temp");
        }

        // try 'SPtemp' in user's home directory
        if (!((temp.exists() && temp.isDirectory()) || temp.mkdir())) {
            log.atError().log("SPManager: Cannot create temp folder: {}", temp.getAbsolutePath());
            temp = new File(System.getProperty("user.dir"), "SPMtemp");
        }

        if (!((temp.exists() && temp.isDirectory()) || temp.mkdir())) {
            errors.add("Cannot create temp folder: " + temp.getAbsolutePath());
        }

        if (!temp.canWrite()) {
            errors.add("Write permission denied to temp folder: " + temp.getAbsolutePath());
        }

        // create temporary private sub-tree
        String path;
        File t = null;
        try {
            t = File.createTempFile("spmanager-temp-" + System.getProperty("user.name") + "-", ".lck", temp);
            t.deleteOnExit();
            path = t.getName();
            path = path.substring(0, path.length() - ".lck".length());
        } catch (IOException e) {
            // failed to create temp file, use fallback naming algorithm
            log.atError().setCause(e).log("SPManager: could not create temp file: {} {}", t.getAbsolutePath(), e.getMessage());

            path = System.getProperty("user.name") + "-" + System.currentTimeMillis();
        }

        temp = new File(temp, path);

        if (!((temp.exists() && temp.isDirectory()) || temp.mkdir())) {
            errors.add("Cannot create temp folder: " + temp.getAbsolutePath());
        }

        if (!temp.canWrite()) {
            errors.add("Write permission denied to temp folder: " + temp.getAbsolutePath());
        }

        TEMP_DIR = temp.getAbsolutePath();

        log.info("SPManager: temp dir set to: {}", temp.getAbsolutePath());

        // make sure all temp directories are created
        File subfolder = new File(PLUGIN_DIRECTORY);
        temp = new File(TEMP_DIR, subfolder.getName());
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create temp plugin folder: " + temp.getAbsolutePath());
        }

        subfolder = new File(TOOL_SCRIPT_DIRECTORY);
        temp = new File(TEMP_DIR, subfolder.getName());
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create temp script folder: " + temp.getAbsolutePath());
        }

        subfolder = new File(OBJECT_SCRIPT_DIRECTORY);
        temp = new File(TEMP_DIR, subfolder.getName());
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create temp script folder: " + temp.getAbsolutePath());
        }

        subfolder = new File(STARTUP_SCRIPT_DIRECTORY);
        temp = new File(TEMP_DIR, subfolder.getName());
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create temp script folder: " + temp.getAbsolutePath());
        }

        // make sure all live directories exist
        temp = new File(PLUGIN_DIRECTORY);
        if (!temp.exists() && !temp.mkdir()) {
            errors.add("Cannot create missing plugin folder: " + temp.getAbsolutePath());
        }

        temp = new File(TOOL_SCRIPT_DIRECTORY);
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create missing script folder: " + temp.getAbsolutePath());
        }

        temp = new File(OBJECT_SCRIPT_DIRECTORY);
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create missing script folder: " + temp.getAbsolutePath());
        }

        temp = new File(STARTUP_SCRIPT_DIRECTORY);
        if (!temp.exists() && !temp.mkdirs()) {
            errors.add("Cannot create missing script folder: " + temp.getAbsolutePath());
        }

        if (errors.isEmpty()) {
            return;
        }

        BTextArea txt = new BTextArea(5, 45);
        txt.setEditable(false);
        txt.setText(String.join("\n", errors));

        BScrollPane detail = new BScrollPane(txt, BScrollPane.SCROLLBAR_NEVER, BScrollPane.SCROLLBAR_AS_NEEDED);
        BLabel messg = SPMTranslate.bLabel("errMsg");
        new BStandardDialog("SPManager initialise", new Widget[]{messg, detail}, BStandardDialog.WARNING).showMessageDialog(null);

    }

    public void registerResource(String type, String id, ClassLoader loader, String baseName, Locale locale) {
        String suffix = "";

        if (locale.getLanguage().length() > 0) {
            suffix += "_" + locale.getLanguage();
        }
        if (locale.getCountry().length() > 0) {
            suffix += "_" + locale.getCountry();
        }
        if (locale.getVariant().length() > 0) {
            suffix += "_" + locale.getVariant();
        }

        URL url;
        int cut;

        for (int i = 0; i < 3; i++) {
            try {
                url = loader.getResource(baseName + suffix + ".properties");

                if (url != null) {
                    PluginRegistry.registerResource(type, id, loader, url.getPath(), locale);
                    break;
                }
            } catch (IllegalArgumentException e) {
            }

            // can we remove part of the suffix?
            cut = suffix.lastIndexOf('_');
            if (cut > 0) {
                suffix = suffix.substring(0, cut);
            } else {
                break;
            }
        }
    }

    public void download(BFrame frame, URL from) {
        download(frame, from, null);
    }

    /**
     * download (and install if possible) the specified file(set).
     */
    public void download(BFrame frame, URL from, URL to) {
        final BFrame context = frame;
        final URL url = from;

        final StatusDialog status = new StatusDialog(context) {
            SPMObjectInfo info;
            BButton okbtn;
            String filename, name;
            ColumnContainer col;
            RowContainer buttons;
            BTextField savePath;
            Thread worker;

            @Override
            public void setVisible(boolean vis) {
                if (!vis) {
                    super.setVisible(vis);
                    return;
                }

                col = (ColumnContainer) getContent();

                filename = url.getFile();
                int cut = filename.lastIndexOf('/');
                if (cut > 0 && cut < filename.length()) {
                    filename = filename.substring(cut + 1);
                }

                cut = filename.lastIndexOf('?');
                if (cut > 0) {
                    filename = filename.substring(0, cut);
                }

                name = new String(filename);
                cut = name.lastIndexOf('.');
                if (cut > 0) {
                    name = name.substring(0, cut);
                }

                setText(name);
                setProgressText(SPMTranslate.text("clickStart"));

                okbtn = Translate.button("spmanager:start", this, "ok");

                buttons = new RowContainer();
                buttons.add(okbtn);
                buttons.add(Translate.button("cancel", event -> close()));

                okbtn.setActionCommand("ok");

                col.add(buttons);
                pack();
                super.setVisible(true);
            }

            public void close() {
                if (worker != null) {
                    worker.interrupt();
                }
                doClose();
            }

            public void ok(CommandEvent ev) {
                String cmd = ev.getActionCommand();
                okbtn.setEnabled(false);

                final BButton okbut = okbtn;

                final StatusDialog stat = this;

                if (cmd.equals("ok")) {

                    setProgressText(SPMTranslate.text("contacting"));
                    setIdle(true);

                    log.info("DOWNLOAD: creating ObjectInfo... {}", url.toString());
                    worker = new Thread() {
                        @Override
                        public void run() {
                            info = new SPMObjectInfo(url);
                            if (info.length == 0) {
                                log.info("DOWNLOAD: no info");
                                info.length = info.getRemoteFileSize(url.toString());
                            }

                            // get destination if needed
                            if (info.name == null || info.name.isEmpty()) {

                                log.info("need path...");

                                RowContainer row = new RowContainer();
                                row.add(SPMTranslate.bLabel("savePath"));
                                savePath = new BTextField("", 25);
                                savePath.addEventLink(ValueChangedEvent.class, this, "savePath");
                                row.add(savePath);
                                row.add(Translate.button("spmanager:browse", event -> browse()));
                                col.remove(buttons);
                                col.add(row);
                                col.add(buttons);
                                pack();
                            }

                            long total = info.getTotalLength();

                            String sz = (total > 1000000
                                    ? " "
                                    + (total / 1000000)
                                    + " MB"
                                    : total > 1000
                                            ? " " + (total / 1000)
                                            + " kB"
                                            : (total > 0)
                                                    ? " " + total
                                                    + " bytes"
                                                    : "");

                            setText(info.getName() + " " + sz);
                            setIdle(false);
                            setProgressText(SPMTranslate.text("ready"));

                            okbut.setActionCommand("install");
                            okbut.setText(SPMTranslate.text("install"));
                            okbut.setEnabled(true);
                            pack();
                        }

                        public void savePath() {
                            String val = savePath.getText();
                            okbtn.setEnabled(val != null && val.length() > 0);
                        }

                        public void browse() {
                            BFileChooser fc = new BFileChooser(BFileChooser.SAVE_FILE, Translate.text("savePath"));

                            File file = null;
                            String path = null;
                            String fname = savePath.getText();

                            if (fname == null || fname.isEmpty()) {
                                path = System.getProperty("user.home");
                                fname = filename;
                            }

                            if (fname != null) {
                                if (path != null) {
                                    file = new File(path, fname);
                                } else {
                                    file = new File(fname);
                                }

                                fc.setDirectory(file.getParentFile());
                                fc.setSelectedFile(file);
                            } else {
                                fc.setDirectory(new File(path));
                            }

                            if (fc.showDialog(context)) {
                                savePath.setText(fc.getSelectedFile().getAbsolutePath());
                            }
                        }

                    };

                    worker.start();

                } else if (cmd.equals("install")) {
                    log.atInfo().log("DOWNLOAD: downloading: {}", url);

                    setText(SPMTranslate.text("downloading", info.getName()));
                    pack();

                    final List<String> errs = new ArrayList<>();
                    worker = new Thread() {
                        @Override
                        public void run() {
                            long total = info.getTotalLength();

                            // full save path
                            String path = null;
                            if (savePath != null) {
                                path = savePath.getText();
                            } else if (info.name != null) {
                                path = ArtOfIllusion.PLUGIN_DIRECTORY + File.separatorChar + info.name + ".jar";
                            }

                            if (path == null || path.isEmpty()) {
                                log.atInfo().log("DOWNLOAD: no save location");
                                MessageDialog.create().withTitle("SPManager").error(SPMTranslate.text("noSaveLocation"));
                                doClose();
                            }

                            log.atInfo().log("DOWNLOAD: downloading file..");
                            if (total > 0) {
                                setBarValue(total > 0 ? 0 : -1);
                            }

                            long dl = HttpSPMFileSystem.downloadRemoteBinaryFile(url, path, info.length, stat, total, 0, errs);

                            for (int i = 0; info.files != null && i < info.files.length; i++) {
                                if (Thread.interrupted()) {
                                    doClose();
                                    return;
                                }

                                name = PLUGIN_DIRECTORY + File.separatorChar + info.destination.get(i) + info.files[i];

                                setText(SPMTranslate.text("downloading", info.files[i]));
                                pack();

                                dl += HttpSPMFileSystem.downloadRemoteBinaryFile(info.getAddFileURL(i), name, info.fileSizes[i], stat, total, dl, errs);
                            }

                            if (errs == null || errs.isEmpty()) {
                                MessageDialog.create().withTitle("SPManager").error(SPMTranslate.text("modified"));
                            } else {
                                InstallSplitPane.showErrors(errs);
                            }

                            log.atInfo().log("DOWNLOAD: done");
                            doClose();
                        }
                    };

                    worker.start();
                } else {
                    log.info("?? cmd={}", cmd);
                }
            }
        };
    }

    /**
     * Description of the Method
     */
    public void doMenu() {

        if (spmFrame == null) {
            spmFrame = new SPManagerFrame();
        }
        spmFrame.getComponent().toFront();
        spmFrame.getComponent().setVisible(true);
    }

    /**
     * Gets the frame attribute of the SPManagerPlugin class
     *
     * @return The frame value
     */
    public static SPManagerFrame getFrame() {
        return spmFrame;
    }

}
