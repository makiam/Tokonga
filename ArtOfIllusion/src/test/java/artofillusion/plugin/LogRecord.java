package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;


@XStreamAlias("log")
@Data
public class LogRecord {
    @XStreamAsAttribute private String version;
    @XStreamAsAttribute private String date;
    @XStreamAsAttribute private String author;
}
