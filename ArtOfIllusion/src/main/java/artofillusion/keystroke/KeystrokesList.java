package artofillusion.keystroke;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("keystrokes")
public class KeystrokesList {
    public List<KeystrokeRecord> getRecords() {
        return records;
    }

    public void setRecords(List<KeystrokeRecord> records) {
        this.records = records;
    }
    @XStreamImplicit(itemFieldName = "keystroke")
    private List<KeystrokeRecord> records = new ArrayList<>();
}
