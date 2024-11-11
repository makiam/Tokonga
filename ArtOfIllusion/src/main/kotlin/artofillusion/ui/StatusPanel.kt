package artofillusion.ui

import buoy.widget.BLabel

class StatusPanel {
    private val component: BLabel = BLabel();

    fun setText(text: String) {
        component.text = text;
    }

    fun getComponent() = component;
}