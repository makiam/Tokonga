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

public class TestKeystroke {
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



    }

    @Test
    void testXMLValue() {
        String xml = "<keystrokes><keystroke name=\"Delete Selection\" code=\"8\" modifiers=\"0\"/></keystrokes>";
        var result = (KeystrokesList)xstream.fromXML(xml);
        Assertions.assertNotNull(result);
        List<KeystrokeRecord> records = result.getRecords();
        Assertions.assertEquals(1, records.size());
        KeystrokeRecord record = records.get(0);
        Assertions.assertEquals("Delete Selection", record.getName());
        Assertions.assertEquals(8, record.getKeyCode());
        Assertions.assertEquals(0, record.getModifiers());
    }

    @Test
    void testOmitXMLModifierValue() {
        String xml = "<keystrokes><keystroke name=\"Delete Selection\" code=\"127\"/></keystrokes>";
        var result = (KeystrokesList)xstream.fromXML(xml);
        Assertions.assertNotNull(result);
        List<KeystrokeRecord> records = result.getRecords();
        Assertions.assertEquals(1, records.size());
        KeystrokeRecord record = records.get(0);
        Assertions.assertEquals("Delete Selection", record.getName());
        Assertions.assertEquals(127, record.getKeyCode());
        Assertions.assertEquals(0, record.getModifiers());
    }


    @Test
    void matchKeyEvents() {
        Component cc = new JFrame();
        KeyEvent event0 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        KeyEvent event1 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');

        Assertions.assertFalse(event0.equals(event1));
    }

    @Test
    void matchKeyEvents2() {
        Component cc = new JFrame();
        KeyEvent event0 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        KeyEvent event1 = new KeyEvent(cc, 0, 0, 0, KeyEvent.VK_U, 'u');
        var ec1 = new KeyEventContainer(event0);
        var ec2 = new KeyEventContainer(event1);
        Assertions.assertTrue(ec1.equals(ec2));
    }
}
