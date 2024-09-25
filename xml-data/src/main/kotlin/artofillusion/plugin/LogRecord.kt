package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Data;


@Data
@XStreamAlias("log")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"text"})
public class LogRecord {
    @XStreamAsAttribute private String version;
    @XStreamAsAttribute private String date;
    @XStreamAsAttribute private String author;
    private String text;
}
