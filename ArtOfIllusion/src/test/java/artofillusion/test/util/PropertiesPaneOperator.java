package artofillusion.test.util;

import org.netbeans.jemmy.operators.JComboBoxOperator;

import java.awt.*;

public class PropertiesPaneOperator extends org.netbeans.jemmy.operators.ContainerOperator {

    /**
     * Constructor.
     *
     * @param b Container component.
     */
    public PropertiesPaneOperator(Container b) {
        super(b);
    }

    public JComboBoxOperator getTexturesComboBoxOperator() {
        return new JComboBoxOperator(this, 0);
    }

    public JComboBoxOperator getMaterialsComboBoxOperator() {
        return new JComboBoxOperator(this, 1);
    }


}
