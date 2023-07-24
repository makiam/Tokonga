/* Copyright (C) 2006-2013 by Peter Eastman
   Changes copyright (C) 2020-2023 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.keystroke;

import artofillusion.*;
import artofillusion.ui.*;
import groovy.lang.Script;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.w3c.dom.*;

/**
 * This class maintains the list of keystrokes, and executes them in response to KeyEvents.
 */
@Slf4j
public class KeystrokeManager {

    private static final List<KeystrokeRecord> records = new ArrayList<>();
    private static Map<Integer, List<KeystrokeRecord>> keyIndex = new HashMap<>();

    private static final String KEYSTROKE_FILENAME = "keystrokes.xml";

    /**
     * Get a list of all defined KeystrokeRecords.
     */
    public static KeystrokeRecord[] getAllRecords() {
        return records.toArray(KeystrokeRecord[]::new);
    }

    /**
     * Set the list of all defined KeystrokeRecords, completely replacing the existing ones.
     */
    public static void setAllRecords(KeystrokeRecord[] allRecords) {
        records.clear();
        Collections.addAll(records, allRecords);
        recordModified();
    }

    /**
     * Add a new KeystrokeRecord.
     */
    public static void addRecord(KeystrokeRecord record) {
        records.add(record);
        recordModified();
    }

    /**
     * Remove a KeystrokeRecord.
     */
    public static void removeRecord(KeystrokeRecord record) {
        records.remove(record);
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
            records.forEach(record -> {
                List<KeystrokeRecord> list = keyIndex.computeIfAbsent(record.getKeyCode(), code -> new ArrayList<>());
                list.add(record);
            });
        }

        // Get the list of all records with the correct ID.
        List<KeystrokeRecord> list = keyIndex.get(event.getKeyCode());
        if (list == null) {
            return;
        }

        for (KeystrokeRecord record : list) {
            if (record.getModifiers() == event.getModifiers()) {
                try {
                    Script script = ArtOfIllusion.getShell().parse(record.getScript());
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
            Path path = ApplicationPreferences.getPreferencesFolderPath();
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
    public static void addRecordsFromXML(InputStream in) throws Exception {
        // Build a table of existing records.

        HashMap<String, KeystrokeRecord> existing = new HashMap<>();
        records.forEach(record -> existing.put(record.getName(), record));

        // Parse the XML and load the records.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(in);
        NodeList keystrokes = doc.getElementsByTagName("keystroke");
        for (int i = 0; i < keystrokes.getLength(); i++) {
            Node keystroke = keystrokes.item(i);
            String name = keystroke.getAttributes().getNamedItem("name").getNodeValue();
            
            int code = Integer.parseInt(keystroke.getAttributes().getNamedItem("code").getNodeValue());
            int modifiers = Integer.parseInt(keystroke.getAttributes().getNamedItem("modifiers").getNodeValue());
            String script = keystroke.getFirstChild().getNodeValue();
            if (existing.containsKey(name)) {
                records.remove(existing.get(name));
            }
            addRecord(new KeystrokeRecord(code, modifiers, name, script));
        }
    }

    /**
     * Save the list of keystrokes to an XML file.
     */
    public static void saveRecords() throws Exception {
        // Construct the XML.

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement("keystrokes");
        doc.appendChild(root);
        for (KeystrokeRecord record : records) {
            Element recordElement = doc.createElement("keystroke");
            recordElement.setAttribute("name", record.getName());
            
            recordElement.setAttribute("code", Integer.toString(record.getKeyCode()));
            recordElement.setAttribute("modifiers", Integer.toString(record.getModifiers()));
            Text scriptElement = doc.createTextNode(record.getScript());
            recordElement.appendChild(scriptElement);
            root.appendChild(recordElement);
        }

        // Save it to disk.
        Path path = ApplicationPreferences.getPreferencesFolderPath();
        File outFile = new File(path.toFile(), KEYSTROKE_FILENAME);
        try (OutputStream out = new BufferedOutputStream(new SafeFileOutputStream(outFile, SafeFileOutputStream.OVERWRITE))) {
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(out);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        }
    }
}
