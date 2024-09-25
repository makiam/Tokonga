package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;


import java.util.ArrayList;
import java.util.List;

@XStreamAlias("history")
public class History {

    @XStreamImplicit
    private List<LogRecord> records = new ArrayList<>();

    public List<LogRecord> getRecords() {
        return records == null ? List.of() : records;
    }

}
