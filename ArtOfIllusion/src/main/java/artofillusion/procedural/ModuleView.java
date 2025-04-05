package artofillusion.procedural;

import javax.swing.*;
import java.awt.*;

public class ModuleView extends JFrame {
    @Override
    protected void frameInit() {
        super.frameInit();
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(1280, 1024);
        this.getContentPane().setBackground(Color.yellow);

    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> new ModuleView().setVisible(true));
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponents(g);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawModule(null, (Graphics2D)g, false);

    }

    private static void drawModule(Module module, Graphics2D g, boolean selected) {
        Rectangle bounds = new Rectangle(50, 50, 50, 50);

        Stroke currentStroke = g.getStroke();
        g.setColor(Color.green);
        g.fillRoundRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2, 3, 3);
        g.setColor(selected ? selectedColor : outlineColor);
        g.setStroke(contourStroke);
        g.drawRoundRect(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2, 4, 4);
        g.setStroke(currentStroke);

    }

    private static final Color outlineColor = new Color(110, 110, 160);
    private static final Color selectedColor = new Color(255, 60, 60);
    protected static final Stroke contourStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    /**
     * https://github.com/kirill-grouchnikov/radiance/issues/39#issuecomment-1137649208
     *
     */
}
