package artofillusion.procedural;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;

@Getter
@XStreamAlias("node")
public class NodeDef {
    private String name;
}
