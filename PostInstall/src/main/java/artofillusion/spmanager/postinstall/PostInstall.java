/*
 * PostInstall: perform post-install cleanup
 *
 * Copyright (C) 2006 Nik Trevallyn-Jones, Sydney Australia
 * Changes copyright 2023 by Maksim Khramov
 *
 * Author: Nik Trevallyn-Jones, nik777@users.sourceforge.net
 * $Id: Exp $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with this program. If not, the license is available from the
 * GNU project, at http://www.gnu.org.
 */

package artofillusion.spmanager.postinstall;

import artofillusion.*;
import artofillusion.ui.*;

import java.io.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;

/**
 * AOI plugin to clean up after plugin installation/upgrade
 */
@Slf4j
public class PostInstall implements Plugin {

    protected List<String> ok, err;
    protected static File tempDir = null;

    @Override
    public void processMessage(int msg, Object... args) {
        int i, k;

        switch (msg) {
            case Plugin.APPLICATION_STARTING:
	    try {

                // find the SPManager temp dir
                if (tempDir == null) {
                    tempDir = new File(System.getProperty("java.io.tmpdir"));
                }

                if (!tempDir.exists()) {
                    tempDir = new File(ArtOfIllusion.APP_DIRECTORY, "temp");
                }

                if (!tempDir.exists()) {
                    tempDir = new File(System.getProperty("user.dir"), "SPMtemp");
                }

                if (!tempDir.exists()) {
                    log.info("PostInstall: No TEMP dir found");

                    tempDir = null;
                    return;
                }

                // get the correct subtree
                String prefix = "spmanager-temp-" + System.getProperty("user.name") + "-";

                for (String sub : tempDir.list()) {
                    if (sub.startsWith(prefix) && (!sub.endsWith(".lck"))) {
                        File lockfile = new File(tempDir, sub + ".lck");

                        // no lock-file means not active
                        if (!lockfile.exists()) {
                            tempDir = new File(tempDir, sub);
                            break;
                        }
                    }
                }

                // if no subtree found, exit now
                if (!tempDir.getName().startsWith("spmanager-temp-")) {
                    log.info("PostInstall: no TEMP sub-tree found");

                    tempDir = null;
                    return;
                }

                log.info("PostInstall: tempDir is {}", tempDir.getAbsolutePath());

                ok = new ArrayList<>(128);
                err = new ArrayList<>(128);

                cleanup(ArtOfIllusion.PLUGIN_DIRECTORY, ok, err);
                cleanup(ArtOfIllusion.TOOL_SCRIPT_DIRECTORY, ok, err);
                cleanup(ArtOfIllusion.OBJECT_SCRIPT_DIRECTORY, ok, err);
                cleanup(ArtOfIllusion.STARTUP_SCRIPT_DIRECTORY, ok, err);
            } catch (Exception e) {
                log.atError().setCause(e).log("PostInstall: exception raised - aborting: {}", e.getMessage());
                err.add("Exception raised - aborting: " + e);
            } finally {

                if (tempDir == null) {
                    return;
                }

                // delete the temp tree
                try {
                    File tmp;
                    String[] sub = tempDir.list();
                    List<String> list = new ArrayList<>(sub.length);
                    for (i = 0; i < sub.length; i++) {
                        list.add(tempDir + File.separator + sub[i]);
                    }

                    for (i = 0; i < list.size(); i++) {
                        tmp = new File(list.get(i));

                        // make sure we empty all sub-directories first
                        if (tmp.isDirectory()) {
                            sub = tmp.list();
                            if (sub.length > 0) {
                                log.info("PostInstall: descending into {}", tmp.getAbsolutePath());

                                for (k = 0; k < sub.length; k++) {
                                    list.add(i, tmp.getAbsolutePath() + File.separator + sub[k]);
                                }

                                // continue processing from this element
                                i--;
                                continue;
                            }
                        }
                        log.info("PostInstall: deleting {}", tmp.getAbsolutePath());

                        tmp.delete();
                    }
                } catch (Exception e) {
                    log.atError().setCause(e).log("PostInstall error: {}", e.getMessage());
                }

                log.info("PostInstall: deleting {}", tempDir.getAbsolutePath());

                tempDir.delete();
            }
            break;

            case Plugin.SCENE_WINDOW_CREATED:
	    try {
                if (err != null && err.size() > 0) {
                    JTextArea txt = new JTextArea();
                    txt.setColumns(45);
                    txt.setEditable(false);
                    txt.setRows(5);
                    txt.setText(String.join("\n", err));

                    JScrollPane detail = new JScrollPane(txt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                    JLabel message = new JLabel(Translate.text("postinstall:errMsg"));

                    MessageDialog.create().withTitle("PostInstall").info(new JComponent[]{ message, detail });

                }

                if (ok != null && ok.size() > 0) {

                    JTextArea txt = new JTextArea();
                    txt.setColumns(45);
                    txt.setEditable(false);
                    txt.setRows(5);
                    txt.setText(String.join("\n", ok));

                    JScrollPane detail = new JScrollPane(txt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                    JLabel message = new JLabel(Translate.text("postinstall:okMsg"));
                    JLabel restart = new JLabel(Translate.text("postinstall:restartMsg"));

                    MessageDialog.create().withTitle("PostInstall").info(new JComponent[]{ message, restart, detail });
                }
            } finally {
                ok = null;
                err = null;
            }
        }
    }

    /**
     * cleanup any incomplete file downloads in the specified directory
     *
     * @param path - the String representation of the pathname
     */
    public static void cleanup(String path, List<String> ok, List<String> err) {
        File from, to;
        File update;
        String fname;

        to = new File(path);
        from = new File(tempDir, to.getName());

        if (!from.exists()) {
            log.atError().log("PostInstall: FROM path does not exist: {}", from.getAbsolutePath());
            return;
        }

        if (!to.exists()) {
            log.atError().log("PostInstall: TO path does not exist: {}", to.getAbsolutePath());
            return;
        }

        String[] files = from.list();
        if (files == null || files.length == 0) {
            return;
        }

        // iterate all filenames in the directory
        for (int i = 0; i < files.length; i++) {

            // only process filename that look like an update file
            if (files[i].endsWith(".upd")) {

                update = new File(from, files[i]);

                // if the file is zero-length, jut try to delete it
                if (update.length() == 0) {
                    if (update.delete()) {
                        log.info("PostInstall: deleted zero-length file: {}", update.getAbsolutePath());
                    } else {
                        log.error("PostInstall.cleanup: Could not delete: {}", update.getAbsolutePath());

                        err.add("could not delete " + update.getAbsolutePath());
                    }

                    continue;	// skip to next file
                }

                fname = files[i].substring(0, files[i].length() - ".upd".length());

                File plugin = new File(to, fname);

                // if the corresponding plugin also exists then fix it
                if (plugin.exists()) {
                    plugin.delete();
                }

                if (update.renameTo(plugin)) {
                    log.info("PostInstall.cleanup: Updated {}", fname);
                    ok.add("Updated " + fname);
                } else {
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(update));
                        os = new BufferedOutputStream(new FileOutputStream(plugin));

                        int b;
                        while ((b = is.read()) >= 0) {
                            os.write((byte) b);
                        }

                        log.info("PostInstall.cleanup: Updated {}", fname);

                        ok.add("Updated (copied) " + fname);
                    } catch (IOException e) {
                        log.atError().setCause(e).log("PostInstall.cleanup: **Error updating ", fname);
                        err.add("couldn't rename or copy " + fname);
                    } finally {
                        try {
                            os.flush();
                            os.close();
                            is.close();
                        } catch (IOException e) {
                        }

                        if (!update.delete()) {
                            log.atError().log("PostInstall.cleanup: **Error: Could not delete: {}", update.getAbsolutePath());

                            err.add("couldn't delete " + update.getAbsolutePath());

                            // set file to zero-length
                            try (RandomAccessFile raf = new RandomAccessFile(update, "rw")) {
                                raf.setLength(0);
                            } catch (IOException e) {
                                log.atError().setCause(e).log("PostInstall: {}", Translate.text("postinstall:truncateMsg", update.getAbsolutePath()));
                            }
                        }
                    }
                }
            }
        }
    }

}
