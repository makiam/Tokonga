package artofillusion

import javax.swing.ImageIcon

object AppIcon {
    val appIcon: ImageIcon? by lazy {
        var icon: ImageIcon? = ImageIcon(Thread.currentThread().getContextClassLoader().getResource("artofillusion/Icons/appIcon.png"))
        icon = when {
            icon?.getIconWidth() == -1 -> null
            else -> icon
        }
        icon
    }
}
