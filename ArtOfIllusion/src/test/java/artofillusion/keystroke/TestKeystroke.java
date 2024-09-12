package artofillusion.keystroke;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestKeystroke {

    @Test
    void testXMLToObject() {

        XStream xstream = new XStream(new StaxDriver());
        xstream.allowTypes(new Class[]{KeystrokesList.class, KeystrokeRecord.class});
        xstream.processAnnotations(new Class[]{KeystrokesList.class, KeystrokeRecord.class});


        var result = (KeystrokesList)xstream.fromXML(KeystrokeManager.class.getResourceAsStream("keystrokes.xml"));

        List<KeystrokeRecord> records = result.getRecords();

        Assertions.assertNotNull(result);



    }
}
