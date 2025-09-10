package artofillusion.keystroke;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

class TestKeystroke {
    static XStream xstream;

    @BeforeAll
    static void init() {
        xstream = new XStream(new StaxDriver());
        xstream.allowTypes(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
        xstream.processAnnotations(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
    }

    @Test
    void testXMLToObject() {

        var result = (KeystrokesList)xstream.fromXML(KeystrokeManager.class.getResourceAsStream("keystrokes.xml"));

        List<KeystrokeRecord> records = result.getRecords();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(records);


    }

    @Test
    void testXMLValue() {
        String xml = "<keystrokes><keystroke name=\"Delete Selection\" code=\"8\" modifiers=\"0\"/></keystrokes>";
        var result = (KeystrokesList)xstream.fromXML(xml);
        Assertions.assertNotNull(result);
        List<KeystrokeRecord> records = result.getRecords();
        Assertions.assertEquals(1, records.size());
        var rec = records.get(0);
        Assertions.assertEquals("Delete Selection", rec.getName());
        Assertions.assertEquals(8, rec.getKeyCode());
        Assertions.assertEquals(0, rec.getModifiers());
    }

    @Test
    void testXMLScriptValue() {
        String xml = "<keystrokes><keystroke name=\"Delete Selection\" code=\"8\">println(\"Delete Selection\")</keystroke></keystrokes>";
        var result = (KeystrokesList)xstream.fromXML(xml);
        Assertions.assertNotNull(result);
        List<KeystrokeRecord> records = result.getRecords();
        Assertions.assertEquals(1, records.size());
        var rec = records.get(0);
        Assertions.assertEquals("println(\"Delete Selection\")", rec.getScript());

    }

    @Test
    void testXMLGroupValue() {
        var result = (KeystrokesList)xstream.fromXML(KeystrokeManager.class.getResourceAsStream("keystrokes.xml"));
        List<KeystrokeRecord> records = result.getRecords();
        var ks = records.get(records.size()-1);
        Assertions.assertEquals("Polymesh", ks.getGroup());
        ks = records.get(0);
        Assertions.assertEquals("", ks.getGroup());
    }


    @Test
    void testOmitXMLModifierValue() {
        String xml = "<keystrokes><keystroke name=\"Delete Selection\" code=\"127\"/></keystrokes>";
        var result = (KeystrokesList)xstream.fromXML(xml);
        Assertions.assertNotNull(result);
        List<KeystrokeRecord> records = result.getRecords();
        Assertions.assertEquals(1, records.size());
        var rec = records.get(0);
        Assertions.assertEquals("Delete Selection", rec.getName());
        Assertions.assertEquals(127, rec.getKeyCode());
        Assertions.assertEquals(0, rec.getModifiers());
    }


    @Test
    void matchKeyEvents() {
        Component cc = new JFrame();
        KeyEvent event0 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        KeyEvent event1 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');

        Assertions.assertNotEquals(event0, event1);
    }

    @Test
    void matchKeyEvents2() {
        Component cc = new JFrame();
        KeyEvent event0 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        KeyEvent event1 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        var ec1 = new KeyEventContainer(event0);
        var ec2 = new KeyEventContainer(event1);
        Assertions.assertEquals(ec1, ec2);
    }

    @Test
    void createNewKSNoGroup() {
        var rec = new KeystrokeRecord(0, 0, "", "println()");
        Assertions.assertEquals("", rec.getGroup());
        Assertions.assertEquals("println()", rec.getScript());
    }

    @Test
    void createNewKSWithGroup() {
        var rec = new KeystrokeRecord(0, 0, "", "Polymesh", "println()");
        Assertions.assertEquals("Polymesh", rec.getGroup());
        Assertions.assertEquals("println()", rec.getScript());
    }


}
