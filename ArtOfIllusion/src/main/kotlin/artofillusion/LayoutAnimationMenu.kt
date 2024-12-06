package artofillusion

import artofillusion.animation.*
import artofillusion.animation.distortion.*
import artofillusion.ui.Translate
import buoy.widget.BMenu
import javax.swing.SwingUtilities

class LayoutAnimationMenu (private val layout: LayoutWindow) : BMenu(Translate.text("menu.animation")) {

    private val scene by lazy { layout.theScene }

    init {

    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }

    companion object {
        private val commandToTrack: MutableMap<String, Class<out Track?>> = HashMap()

        init {
            commandToTrack["poseTrack"] = PoseTrack::class.java
            commandToTrack["constraintTrack"] = ConstraintTrack::class.java
            commandToTrack["visibilityTrack"] = VisibilityTrack::class.java
            commandToTrack["textureTrack"] = TextureTrack::class.java
            commandToTrack["bendDistortion"] = BendTrack::class.java
            commandToTrack["customDistortion"] = CustomDistortionTrack::class.java
            commandToTrack["scaleDistortion"] = ScaleTrack::class.java
            commandToTrack["shatterDistortion"] = ShatterTrack::class.java
            commandToTrack["twistDistortion"] = TwistTrack::class.java
            commandToTrack["IKTrack"] = IKTrack::class.java
            commandToTrack["skeletonShapeTrack"] = SkeletonShapeTrack::class.java
        }

        @JvmStatic
        fun getCommandToTrack(name: String): Class<out Track?> {
            return commandToTrack[name]!!
        }
    }
}