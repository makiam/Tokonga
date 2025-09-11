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
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.awt.event.*;
import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class maintains the list of keystrokes, and executes them in response to KeyEvents.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeystrokeManager {

    private static final XStream xstream = new XStream(new StaxDriver());
    private static final Path path = ApplicationPreferences.getPreferencesFolderPath();


    static {
        xstream.allowTypes(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
        xstream.processAnnotations(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
    }



    private static final List<KeystrokeRecord> records = new ArrayList<>();
    private static Map<KeyEventContainer, List<Script>> scripts = new HashMap<>();

    private static final String KEYSTROKE_FILENAME = "keystrokes.xml";

    public static List<KeystrokeRecord> getRecords() {
        return new ArrayList<>(records);
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
        scripts.clear();

    }
    /**
     * Given a key event, find any matching KeystrokeRecords and execute them.
     *
     * @param event the KeyEvent which has occurred
     * @param window the EditingWindow in which the event occurred
     */
    public static void executeKeystrokes(KeyEvent event, EditingWindow window) {
        log.debug("KeyEvent: code {} m:{} vs e:{}", event.getKeyCode(), event.getModifiers(), event.getModifiersEx());

        if(scripts.isEmpty()) {
            mapRecordsToScripts();
        }

        var shell = ArtOfIllusion.getShell();
        List<Script> rec = scripts.getOrDefault(new KeyEventContainer(event), Collections.emptyList());
        rec.forEach(script -> {
            script.setProperty("window", window);
            script.run();
            event.consume();
        });

    }

    private static void mapRecordsToScripts() {
        var tmp = records.stream().collect(Collectors.groupingBy(KeystrokeRecord::getKeyEventKey, Collectors.toList()));
        var shell = ArtOfIllusion.getShell();
        Function<KeystrokeRecord, Script> mapper = r -> getCompiledScript(shell, r);
        tmp.forEach((k, v) -> scripts.put(k, v.stream().map(mapper).collect(Collectors.toList())));

    }

    private static final Script stub = new Script() {
        @Override
        public Object run() {
            return null;
        }
    };

    private static Script getCompiledScript(GroovyShell shell, KeystrokeRecord rec) {
        Script script = stub;
        try {
            script = shell.parse(rec.getScript(), rec.getName());
        } catch (org.codehaus.groovy.control.CompilationFailedException ex) {
            log.atError().log("Unable to compile keystroke script: {} due {}", rec.getName(), ex.getMessage());
        }
        return script;
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

    @Retention(RetentionPolicy.SOURCE)
    public @interface UsedWithScriptBinding {
        String value() default "";
    }
}
