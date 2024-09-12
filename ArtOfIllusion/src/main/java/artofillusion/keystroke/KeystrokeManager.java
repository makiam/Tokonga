/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2020-2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke;

import artofillusion.*;
import artofillusion.ui.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import groovy.lang.Script;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

import org.codehaus.groovy.control.CompilationFailedException;

/**
 * This class maintains the list of keystrokes, and executes them in response to KeyEvents.
 */
public class KeystrokeManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeystrokeManager.class);

    private KeystrokeManager() {}

    private static final XStream xstream = new XStream(new StaxDriver());
    private static final Path path = ApplicationPreferences.getPreferencesFolderPath();


    static {
        xstream.allowTypes(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
        xstream.processAnnotations(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
    }



    private static final List<KeystrokeRecord> records = new ArrayList<>();
    private static Map<Integer, List<KeystrokeRecord>> keyIndex = new HashMap<>();

    private static final String KEYSTROKE_FILENAME = "keystrokes.xml";

    public static List<KeystrokeRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public static void setRecords(List<KeystrokeRecord> newRecords) {
        records.clear();
        records.addAll(newRecords);
        recordModified();
    }

    /**
     * Add a new KeystrokeRecord.
     */
    public static void addRecord(KeystrokeRecord keystrokeRecord) {
        records.add(keystrokeRecord);
        recordModified();
    }

    /**
     * Remove a KeystrokeRecord.
     */
    public static void removeRecord(KeystrokeRecord keystrokeRecord) {
        records.remove(keystrokeRecord);
        recordModified();
    }

    /**
     * This should be called whenever a KeystrokeRecord has been modified.
     */
    public static void recordModified() {
        keyIndex.clear();
    }

    /**
     * Given a key event, find any matching KeystrokeRecords and execute them.
     *
     * @param event the KeyEvent which has occurred
     * @param window the EditingWindow in which the event occurred
     */
    public static void executeKeystrokes(KeyEvent event, EditingWindow window) {
        if (keyIndex.isEmpty()) {
            // We need to build an index for quickly looking up KeystrokeRecords.
            keyIndex = new HashMap<>(records.size());
            records.forEach(keystrokeRecord -> {
                List<KeystrokeRecord> list = keyIndex.computeIfAbsent(keystrokeRecord.getKeyCode(), code -> new ArrayList<>());
                list.add(keystrokeRecord);
            });
        }

        // Get the list of all records with the correct ID.
        List<KeystrokeRecord> list = keyIndex.get(event.getKeyCode());
        if (list == null) {
            return;
        }

        for (KeystrokeRecord keystrokeRecord : list) {
            if (keystrokeRecord.getModifiers() == event.getModifiers()) {
                try {
                    Script script = ArtOfIllusion.getShell().parse(keystrokeRecord.getScript());
                    script.setProperty("window", window);
                    script.run();
                } catch (CompilationFailedException ee) {
                    log.atError().setCause(ee).log("Keystroke exception: {}", ee.getMessage());
                }
                event.consume();
            }
        }
    }

    /**
     * Locate the file containing keystroke definitions and load them.
     */
    public static void loadRecords() {
        try {

            File inputFile = new File(path.toFile(), KEYSTROKE_FILENAME);
            InputStream in;
            if (inputFile.exists()) {
                in = new BufferedInputStream(new FileInputStream(inputFile));
            } else {
                in = KeystrokeManager.class.getResourceAsStream("/" + KEYSTROKE_FILENAME);
            }
            addRecordsFromXML(in);
            in.close();
        } catch (Exception ex) {
            log.atError().setCause(ex).log("Unable to load keystroke records: {}", ex.getMessage());
        }
    }

    /**
     * Read an XML file from an InputStream and add all the keystrokes it contains. For each one,
     * it checks whether there was already an existing keystroke with the same name. If so, the
     * new keystroke replaces the old one. If not, the new keystroke is simply added.
     */
    public static void addRecordsFromXML(InputStream in) {
        // Build a table of existing records.

        Map<String, KeystrokeRecord> existing = new HashMap<>();
        records.forEach(keystrokeRecord -> existing.put(keystrokeRecord.getName(), keystrokeRecord));

        // Parse the XML and load the records.
        var result = (KeystrokesList)xstream.fromXML(in);
        List<KeystrokeRecord> loadedRecords = result.getRecords();

        loadedRecords.forEach(rec -> {
            if (existing.containsKey(rec.getName())) {
                records.remove(existing.get(rec.getName()));
            }
            addRecord(rec);
        });

    }

    /**
     * Save the list of keystrokes to an XML file.
     */
    public static void saveRecords() throws Exception {
        // Save it to disk.
        File outFile = new File(path.toFile(), KEYSTROKE_FILENAME);
        try (OutputStream out = new BufferedOutputStream(new SafeFileOutputStream(outFile, SafeFileOutputStream.OVERWRITE))) {
            xstream.toXML(new KeystrokesList(records), out);
        }
    }
}
