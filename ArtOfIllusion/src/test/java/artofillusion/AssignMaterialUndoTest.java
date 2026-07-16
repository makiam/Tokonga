package artofillusion;

import artofillusion.material.UniformMaterial;
import artofillusion.math.CoordinateSystem;
import artofillusion.object.Cube;
import artofillusion.object.ObjectInfo;
import artofillusion.test.util.PropertiesPaneOperator;
import buoy.widget.BScrollPane;
import buoy.widget.BTabbedPane;
import buoyx.docking.DockableWidget;
import buoyx.docking.DockingContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.Bundle;
import org.netbeans.jemmy.ClassReference;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class AssignMaterialUndoTest {

    //private static final Bundle bundle = new Bundle();

    private JFrameOperator appFrame;
    private JMenuBarOperator appMainMenu;

    private LayoutWindow layout;

    @BeforeAll
    static void setupClass() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        Locale.setDefault(Locale.ENGLISH);
        new ClassReference("artofillusion.ArtOfIllusion").startApplication();
        //bundle.load(ArtOfIllusion.class.getClassLoader().getResourceAsStream("artofillusion.properties"));
        JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
    }

    @BeforeEach
    void setUp() {
        appFrame = new JFrameOperator("Untitled");
        appMainMenu = new JMenuBarOperator(appFrame);
        appMainMenu.closeSubmenus();
        layout = (LayoutWindow) ArtOfIllusion.getWindows()[0];
        layout.updateImage();
        layout.updateMenus();
    }

    @Test
    void testAssignMaterial1() {

        var cube = new ObjectInfo(new Cube(1,1,1), new CoordinateSystem(), "I am Cube");


        layout.addObject(cube, null);

        layout.setSelection(2);
        layout.updateImage();

        EventQueue.invokeLater(() -> layout.dispatchEvent( new SceneChangedEvent(layout)));

        Assertions.assertNull(cube.getGeometry().getMaterial());
        Assertions.assertNull(cube.getGeometry().getMaterialMapping());

        var ppg = getPropertiesPane(layout);


        var newMaterial = new UniformMaterial();
        newMaterial.setName("Assign Me");


    }

    PropertiesPaneOperator getPropertiesPane(LayoutWindow layout) {
        DockingContainer rightDock = layout.getDockingContainer(BTabbedPane.RIGHT);
        DockableWidget propsWidget = rightDock.getChild(0, 1);
        BScrollPane scroller = (BScrollPane) propsWidget.getContent();


        return new PropertiesPaneOperator((Container)scroller.getContent().getComponent());
    }


}
