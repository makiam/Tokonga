package artofillusion.keystroke;

import com.thoughtworks.xstream.annotations.XStreamAlias;

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

    private List<KeystrokeRecord> records = new ArrayList<>();
}
