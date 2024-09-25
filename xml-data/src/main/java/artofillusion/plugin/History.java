package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("history")
@Data
public class History {
    @XStreamImplicit
    private List<LogRecord> records = new ArrayList<>();
}
