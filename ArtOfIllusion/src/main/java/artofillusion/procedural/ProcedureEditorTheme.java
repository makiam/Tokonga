package artofillusion.procedural;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcedureEditorTheme {

    static final Color darkLinkColor = Color.darkGray;
    static final Color blueLinkColor = new Color(40, 40, 255);
    static final Color selectedLinkColor = new Color(255, 50, 50);
    static final Color outputBackgroundColor = new Color(210, 210, 240);
    static final Color outlineColor = new Color(110, 110, 160);
    static final Color selectedColor = new Color(255, 60, 60);

    static final float BEZIER_HARDNESS = 0.5f; //increase hardness to a have a more pronounced shape

    static final Stroke normal = new BasicStroke();
    static final Stroke bold = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    static final Stroke contourStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
}
