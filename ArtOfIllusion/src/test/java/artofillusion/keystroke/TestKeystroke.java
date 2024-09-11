package artofillusion.keystroke;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestKeystroke {

    @Test
    void testXMLToObject() {
        XStream xstream = new XStream(new StaxDriver());
        xstream.alias("keystrokes", KeystrokesList.class);
        xstream.alias("keystroke", KeystrokeRecord.class);

        var result = xstream.fromXML(KeystrokeManager.class.getResourceAsStream("keystrokes.xml"));

        Assertions.assertNotNull(result);

    }
}
