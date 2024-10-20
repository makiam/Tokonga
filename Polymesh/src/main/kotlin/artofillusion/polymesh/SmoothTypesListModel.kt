package artofillusion.polymesh

import artofillusion.ui.Translate
import javax.swing.DefaultComboBoxModel

class SmoothTypesListModel  : DefaultComboBoxModel<String?>() {
    init {
        addElement(Translate.text("menu.none"))
        addElement(Translate.text("menu.shading"))
        addElement(Translate.text("menu.approximating"))
        addElement(Translate.text("menu.interpolating"))
    }
}