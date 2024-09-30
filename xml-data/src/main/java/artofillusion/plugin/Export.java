package artofillusion.plugin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Data;

@XStreamAlias("export")
@Data
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"description"})
public class Export {
    @XStreamAlias("id") private String id;
    @XStreamAlias("method") private String method;

    public String getDescription() {
        return description.strip();
    }

    private String description;
}
