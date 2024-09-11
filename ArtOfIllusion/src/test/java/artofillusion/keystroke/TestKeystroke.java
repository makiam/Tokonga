package artofillusion.keystroke;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.jupiter.api.Test;

public class TestKeystroke {

    @Test
    void testXMLToObject() {
        XStream xstream = new XStream(new StaxDriver());

    }
}
