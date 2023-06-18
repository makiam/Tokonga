/* Copyright (C) 2006-2009 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import buoy.widget.*;
import buoy.event.*;
import artofillusion.*;
import artofillusion.math.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class presents a user interface for editing the value of a {@link Property}.
 * Given a Property object, it constructs an appropriate Widget based on the type of value
 * it represents and the allowed values. The Widget dispatches a ValueChangedEvent
 * whenever its value changes.
 */
public class PropertyEditor {

    private final Property property;
    private final Widget widget;

    /**
     * Create a PropertyEditor to edit the value of a Property.
     *
     * @param property the Property to edit
     * @param value the initial value of the Property
     */
    public PropertyEditor(Property property, Object value) {
        this.property = property;
        if (property.getType() == Property.DOUBLE) {
            widget = new ValueSelector(0.0, property.getMinimum(), property.getMaximum(), 0.005);
        } else if (property.getType() == Property.INTEGER) {
            widget = new ValueField(0.0, ValueField.INTEGER);
            final Property prop = property;
            ((ValueField) widget).setValueChecker(new ValueChecker() {
                @Override
                public boolean isValid(double val) {
                    return (val >= prop.getMinimum() && val <= prop.getMaximum());
                }
            });
        } else if (property.getType() == Property.BOOLEAN) {
            widget = new BCheckBox(property.getName(), false);
        } else if (property.getType() == Property.STRING) {
            widget = new BTextField(15);
        } else if (property.getType() == Property.COLOR) {
            widget = new ColorSelector(property.getName());
        } else if (property.getType() == Property.ENUMERATION) {
            widget = new BComboBox(property.getAllowedValues());
        } else {
            throw new IllegalArgumentException("Unknown Property type");
        }
        if (widget instanceof BTextField) {
            widget.addEventLink(FocusLostEvent.class, this, "stringValueChanged");
            widget.addEventLink(KeyPressedEvent.class, this, "stringValueChanged");
        }
        if (value != null) {
            setValue(value);
        }
    }

    /**
     * Get the Property this editor is for.
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Get the Widget representing the user interface for editing the Property.
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * Get the label to display with this component. This may be null, in which case
     * no label should be displayed.
     */
    public String getLabel() {
        if (property.getType() == Property.BOOLEAN) {
            return null;
        }
        return property.getName();
    }

    /**
     * Get the value of the Property.
     */
    public Object getValue() {
        if (widget instanceof ValueSelector) {
            return ((ValueSelector) widget).getValue();
        } else if (widget instanceof ValueField) {
            return (int) ((ValueField) widget).getValue();
        } else if (widget instanceof BCheckBox) {
            return ((BCheckBox) widget).getState();
        } else if (widget instanceof BTextField) {
            return ((BTextField) widget).getText();
        } else if (widget instanceof ColorSelector) {
            return ((ColorSelector) widget).getColor();
        } else if (widget instanceof BComboBox) {
            return ((BComboBox) widget).getSelectedValue();
        }
        throw new AssertionError("Unexpected Widget type");
    }

    /**
     * Set the value of the Property.
     */
    public void setValue(Object value) {
        if (widget instanceof ValueSelector) {
            ((ValueSelector) widget).setValue(value == null ? Double.NaN : ((Double) value));
        } else if (widget instanceof ValueField) {
            ((ValueField) widget).setValue(value == null ? Double.NaN : ((Integer) value));
        } else if (widget instanceof BCheckBox) {
            ((BCheckBox) widget).setState(value == null ? false : ((Boolean) value));
        } else if (widget instanceof BTextField) {
            ((BTextField) widget).setText((String) value);
        } else if (widget instanceof ColorSelector) {
            ((ColorSelector) widget).setColor((RGBColor) value);
        } else if (widget instanceof BComboBox) {
            ((BComboBox) widget).setSelectedValue(value);
        }
    }

    /**
     * This is used for events generated by BTextFields for editing String properties. These
     * need to be handled differently, since the property value should only change when editing
     * is complete.
     */
    private void stringValueChanged(WidgetEvent ev) {
        if (ev instanceof FocusLostEvent || (ev instanceof KeyPressedEvent && ((KeyPressedEvent) ev).getKeyCode() == KeyPressedEvent.VK_ENTER)) {
            widget.dispatchEvent(new ValueChangedEvent(ev.getWidget(), false));
        }
    }

    /**
     * A selector Widget for setting a color.
     */
    private class ColorSelector extends CustomWidget {

        private final String title;
        private RGBColor color;

        ColorSelector(String title) {
            this.title = title;
            color = new RGBColor();
            setPreferredSize(new Dimension(30, 15));
            setMaximumSize(new Dimension(30, 15));
            setBackground(color.getColor());
            ((JComponent) getComponent()).setBorder(BorderFactory.createLoweredBevelBorder());
            addEventLink(MouseClickedEvent.class, this);
        }

        public RGBColor getColor() {
            return color.duplicate();
        }

        public void setColor(RGBColor color) {
            if (color == null) {
                this.color = null;
                setBackground(Color.GRAY);
            } else {
                this.color = color.duplicate();
                setBackground(color.getColor());
            }
        }

        private void processEvent() {
            if (color != null) {
                RGBColor oldColor = color.duplicate();
                new ColorChooser(this, title, color);
                if (!color.equals(oldColor)) {
                    setBackground(color.getColor());
                    dispatchEvent(new ValueChangedEvent(this));
                }
            }
        }
    }
}
