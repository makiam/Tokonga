/* Copyright 2024 by Maksim Khramov

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion

import artofillusion.animation.*
import artofillusion.animation.distortion.*
import artofillusion.ui.Translate
import buoy.widget.BMenu
import org.greenrobot.eventbus.Subscribe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.swing.SwingUtilities

class LayoutAnimationMenu (private val layout: LayoutWindow) : BMenu(Translate.text("menu.animation")) {

    private val scene by lazy { layout.theScene }

    init {
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
    }

    @Subscribe
    fun subscribeStub(event: org.greenrobot.eventbus.NoSubscriberEvent) {
    }

    private fun edt(action: () -> Unit) = SwingUtilities.invokeLater() { action() }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LayoutAnimationMenu::class.java)

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